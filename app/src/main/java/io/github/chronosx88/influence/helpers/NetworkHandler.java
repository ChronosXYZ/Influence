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
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import io.github.chronosx88.influence.models.GenericMessage;
import io.github.chronosx88.influence.models.appEvents.LastMessageEvent;
import io.github.chronosx88.influence.models.appEvents.NewMessageEvent;

public class NetworkHandler implements IncomingChatMessageListener {
    private final static String LOG_TAG = "NetworkHandler";

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        if(LocalDBWrapper.getChatByChatID(from.asEntityBareJidString()) == null) {
            LocalDBWrapper.createChatEntry(chat.getXmppAddressOfChatPartner().asUnescapedString(), chat.getXmppAddressOfChatPartner().asBareJid().asUnescapedString());
        }
        long messageID = LocalDBWrapper.createMessageEntry(chat.getXmppAddressOfChatPartner().asUnescapedString(), from.asUnescapedString(), TrueTime.now().getTime(), message.getBody(), true, false);

        EventBus.getDefault().post(new NewMessageEvent(chat.getXmppAddressOfChatPartner().toString(), messageID));
        EventBus.getDefault().post(new LastMessageEvent(chat.getXmppAddressOfChatPartner().toString(), new GenericMessage(LocalDBWrapper.getMessageByID(messageID))));
    }
}