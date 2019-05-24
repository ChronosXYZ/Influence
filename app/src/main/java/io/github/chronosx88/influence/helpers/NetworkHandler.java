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

import com.instacart.library.truetime.TrueTime;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.PresenceEventListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;

import io.github.chronosx88.influence.models.GenericMessage;
import io.github.chronosx88.influence.models.appEvents.LastMessageEvent;
import io.github.chronosx88.influence.models.appEvents.NewMessageEvent;
import io.github.chronosx88.influence.models.appEvents.UserPresenceChangedEvent;

public class NetworkHandler implements IncomingChatMessageListener, PresenceEventListener {
    private final static String LOG_TAG = "NetworkHandler";

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        String chatID = chat.getXmppAddressOfChatPartner().asUnescapedString();
        if(LocalDBWrapper.getChatByChatID(from.asEntityBareJidString()) == null) {
            LocalDBWrapper.createChatEntry(chatID, chat.getXmppAddressOfChatPartner().asBareJid().asUnescapedString());
        }
        long messageID = LocalDBWrapper.createMessageEntry(chatID, from.asUnescapedString(), TrueTime.now().getTime(), message.getBody(), true, false);
        int newUnreadMessagesCount = LocalDBWrapper.getChatByChatID(chatID).unreadMessagesCount + 1;
        LocalDBWrapper.updateChatUnreadMessagesCount(chatID, newUnreadMessagesCount);

        EventBus.getDefault().post(new NewMessageEvent(chatID, messageID));
        EventBus.getDefault().post(new LastMessageEvent(chatID, new GenericMessage(LocalDBWrapper.getMessageByID(messageID))));
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
}