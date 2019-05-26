/*
 *    Copyright 2019 ChronosX88
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.chronosx88.influence.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.instacart.library.truetime.TrueTime;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.PresenceEventListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.models.GenericMessage;
import io.github.chronosx88.influence.models.GenericUser;
import io.github.chronosx88.influence.models.appEvents.LastMessageEvent;
import io.github.chronosx88.influence.models.appEvents.NewMessageEvent;
import io.github.chronosx88.influence.models.appEvents.UserPresenceChangedEvent;
import java9.util.concurrent.CompletableFuture;

public class NetworkHandler implements IncomingChatMessageListener, PresenceEventListener {
    private final static String LOG_TAG = "NetworkHandler";
    private final static String NOTIFICATION_CHANNEL_ID = "InfluenceNotificationsChannel";

    private NotificationManagerCompat notificationManager = NotificationManagerCompat.from(AppHelper.getContext());

    public NetworkHandler() {
        createNotificationChannel();
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        String chatID = chat.getXmppAddressOfChatPartner().asUnescapedString();
        if(LocalDBWrapper.getChatByChatID(from.asEntityBareJidString()) == null) {
            LocalDBWrapper.createChatEntry(chatID, chat.getXmppAddressOfChatPartner().asBareJid().asUnescapedString().split("@")[0], new ArrayList<>());
        }
        long messageID = LocalDBWrapper.createMessageEntry(chatID, message.getStanzaId(), from.asUnescapedString(), TrueTime.now().getTime(), message.getBody(), true, false);
        int newUnreadMessagesCount = LocalDBWrapper.getChatByChatID(chatID).unreadMessagesCount + 1;
        LocalDBWrapper.updateChatUnreadMessagesCount(chatID, newUnreadMessagesCount);

        EventBus.getDefault().post(new NewMessageEvent(chatID, messageID));
        EventBus.getDefault().post(new LastMessageEvent(chatID, new GenericMessage(LocalDBWrapper.getMessageByID(messageID))));
        if(!AppHelper.getCurrentChatActivity().equals(chatID)) {
            byte[] avatarBytes = new byte[0];
            try {
                CompletableFuture<byte[]> future = loadAvatar(chatID);
                if(future != null) {
                    avatarBytes = future.get();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Bitmap avatar = null;
            if(avatarBytes != null) {
                avatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
            }
            NotificationCompat.Builder notification = new NotificationCompat.Builder(AppHelper.getContext(), NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_message_white_24dp)
                    .setContentTitle(chatID)
                    .setContentText(message.getBody())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            if(avatar != null) {
                notification.setLargeIcon(avatar);
            } else {
                String firstLetter = Character.toString(Character.toUpperCase(chatID.charAt(0)));
                Drawable avatarText = TextDrawable.builder()
                        .beginConfig()
                        .width(64)
                        .height(64)
                        .endConfig()
                        .buildRound(firstLetter, ColorGenerator.MATERIAL.getColor(firstLetter));
                notification.setLargeIcon(drawableToBitmap(avatarText));
            }
            notificationManager.notify(new Random().nextInt(), notification.build());
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }



    @Override
    public void presenceAvailable(FullJid address, Presence availablePresence) {
        EventBus.getDefault().post(new UserPresenceChangedEvent(address.asBareJid().asUnescapedString(), availablePresence.isAvailable()));
    }

    @Override
    public void presenceUnavailable(FullJid address, Presence presence) {
        EventBus.getDefault().post(new UserPresenceChangedEvent(address.asBareJid().asUnescapedString(), presence.isAvailable()));
    }

    @Override
    public void presenceError(Jid address, Presence errorPresence) {
        EventBus.getDefault().post(new UserPresenceChangedEvent(address.asBareJid().asUnescapedString(), errorPresence.isAvailable()));
    }

    @Override
    public void presenceSubscribed(BareJid address, Presence subscribedPresence) {

    }

    @Override
    public void presenceUnsubscribed(BareJid address, Presence unsubscribedPresence) {

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = AppHelper.getContext().getString(R.string.notification_channel_name);
            String description = AppHelper.getContext().getString(R.string.notification_channel_desc);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            NotificationManager notificationManager = AppHelper.getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private CompletableFuture<byte[]> loadAvatar(String senderID) {
        if(senderID.length() != 0) {
            if(AppHelper.avatarsCache.containsKey(senderID)) {
                return CompletableFuture.completedFuture(AppHelper.avatarsCache.get(senderID));
            }
            CompletableFuture<byte[]> completableFuture = CompletableFuture.supplyAsync(() -> {
                while (AppHelper.getXmppConnection() == null);
                while (AppHelper.getXmppConnection().isConnectionAlive() != true);
                EntityBareJid jid = null;
                try {
                    jid = JidCreate.entityBareFrom(senderID);
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }
                return AppHelper.getXmppConnection().getAvatar(jid);
            }).thenApply((avatarBytes) -> {
                if(avatarBytes != null) {
                    AppHelper.avatarsCache.put(senderID, avatarBytes);
                }
                return avatarBytes;
            });
            return completableFuture;
        }
        return null;
    }
}