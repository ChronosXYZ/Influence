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

package io.github.chronosx88.influence.logic;

import com.instacart.library.truetime.TrueTime;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class ChatLogic implements CoreContracts.IChatLogicContract {
    private String chatID;
    private ChatEntity chatEntity;

    public ChatLogic(ChatEntity chatEntity) {
        this.chatEntity = chatEntity;
        this.chatID = chatEntity.jid;
    }

    @Override
    public MessageEntity sendMessage(String text) {
        if (AppHelper.getXmppConnection().isConnectionAlive()) {
            EntityBareJid jid;
            try {
                jid = JidCreate.entityBareFrom(chatEntity.jid);
            } catch (XmppStringprepException e) {
                return null;
            }
            AppHelper.getXmppConnection().sendMessage(jid, text);
            while (!TrueTime.isInitialized()) {
                new Thread(() -> {
                    try {
                        TrueTime.build().initialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            long messageID = LocalDBWrapper.createMessageEntry(chatID, AppHelper.getJid(), TrueTime.now().getTime(), text, false, false);
            return LocalDBWrapper.getMessageByID(messageID);
        } else {
            return null;
        }
    }
}
