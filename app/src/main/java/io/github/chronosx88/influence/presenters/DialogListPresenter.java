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

package io.github.chronosx88.influence.presenters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.appcompat.app.AlertDialog;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.logic.DialogListLogic;
import io.github.chronosx88.influence.models.GenericDialog;
import io.github.chronosx88.influence.models.appEvents.AuthenticationStatusEvent;
import io.github.chronosx88.influence.models.appEvents.NewChatEvent;
import io.github.chronosx88.influence.models.appEvents.NewMessageEvent;
import io.github.chronosx88.influence.views.ChatActivity;
import java8.util.stream.StreamSupport;
import java9.util.concurrent.CompletableFuture;

public class DialogListPresenter implements CoreContracts.IDialogListPresenterContract {
    private ConcurrentHashMap<String, byte[]> avatarsMap = new ConcurrentHashMap<>();
    private CoreContracts.IChatListViewContract view;
    private CoreContracts.IDialogListLogicContract logic;
    private DialogsListAdapter<GenericDialog> dialogListAdapter = new DialogsListAdapter<>(R.layout.item_dialog_custom, (imageView, url, payload) -> {
        String firstLetter = Character.toString(Character.toUpperCase(url.charAt(0)));
        imageView.setImageDrawable(TextDrawable.builder()
                .beginConfig()
                .width(64)
                .height(64)
                .endConfig()
                .buildRound(firstLetter, ColorGenerator.MATERIAL.getColor(firstLetter)));
        CompletableFuture.supplyAsync(() -> {
            while (AppHelper.getXmppConnection() == null);
            while (AppHelper.getXmppConnection().isConnectionAlive() != true);
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(url);
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
            return AppHelper.getXmppConnection().getAvatar(jid);
        }).thenAccept((avatarBytes) -> {
            AppHelper.getMainUIThread().post(() -> {
                if(avatarBytes != null) {
                    Bitmap avatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                    imageView.setImageBitmap(avatar);
                    avatarsMap.put(url, avatarBytes);
                }
            });
        });
    });

    public DialogListPresenter(CoreContracts.IChatListViewContract view) {
        this.view = view;
        dialogListAdapter.setOnDialogClickListener(dialog -> openChat(dialog.getId()));
        dialogListAdapter.setOnDialogLongClickListener(dialog -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getActivityContext());
            builder.setPositiveButton(R.string.ok, (dialog1, id) -> {
                dialogListAdapter.deleteById(dialog.getId());
                AppHelper.getChatDB().chatDao().deleteChat(dialog.getId());
                AppHelper.getChatDB().messageDao().deleteMessagesByChatID(dialog.getId());
            });
            builder.setNegativeButton(R.string.cancel, (dialog2, which) -> {
                //
            });
            builder.setMessage("Remove chat?");
            builder.create().show();
        });
        this.logic = new DialogListLogic();
        this.view.setDialogAdapter(dialogListAdapter);
        ArrayList<GenericDialog> dialogs = new ArrayList<>();
        StreamSupport.stream(logic.loadLocalChats())
                .forEach(chatEntity -> dialogs.add(new GenericDialog(chatEntity)));
        dialogListAdapter.setItems(dialogs);
        loadRemoteContactList();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void openChat(String chatID) {
        Intent intent = new Intent(AppHelper.getContext(), ChatActivity.class);
        intent.putExtra("chatID", chatID);
        intent.putExtra("chatName", LocalDBWrapper.getChatByChatID(chatID).chatName);
        intent.putExtra("chatAvatar", avatarsMap.get(chatID));
        view.startActivity(intent);
    }

    @Subscribe
    public void onNewChatCreated(NewChatEvent event) {
        dialogListAdapter.upsertItem(new GenericDialog(LocalDBWrapper.getChatByChatID(event.chatID)));
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent event) {
        String chatID = event.chatID;
        GenericDialog dialog = dialogListAdapter.getItemById(chatID);
        if(dialog == null) {
            dialogListAdapter.addItem(new GenericDialog(LocalDBWrapper.getChatByChatID(chatID)));
        }
    }

    @Subscribe
    public void onAuthenticate(AuthenticationStatusEvent event) {
        if(event.authenticationStatus == AuthenticationStatusEvent.CONNECT_AND_LOGIN_SUCCESSFUL) {
            loadRemoteContactList();
        }
    }

    @Override
    public void loadRemoteContactList() {
        CompletableFuture.supplyAsync(() -> logic.getRemoteContacts()).thenAccept((contacts) -> {
            AppHelper.getMainUIThread().post(() -> {
                if(contacts != null) {
                    StreamSupport.stream(contacts).forEach(contact -> {
                        LocalDBWrapper.createChatEntry(contact.getJid().asUnescapedString(), contact.getName() == null ? contact.getJid().asUnescapedString().split("@")[0] : contact.getName());
                        dialogListAdapter.upsertItem(new GenericDialog(LocalDBWrapper.getChatByChatID(contact.getJid().asUnescapedString())));
                    });
                }
            });
        });
    }
}
