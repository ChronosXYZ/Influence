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

package io.github.chronosx88.influence.logic

import com.instacart.library.truetime.TrueTime
import org.jivesoftware.smack.packet.Presence
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.stringprep.XmppStringprepException

import java.io.IOException

import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.LocalDBWrapper
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import io.github.chronosx88.influence.models.roomEntities.MessageEntity
import java9.util.concurrent.CompletableFuture
import org.jivesoftware.smackx.mam.MamManager

class ChatLogic(private val chatEntity: ChatEntity) : CoreContracts.IChatLogicContract {
    private val chatID: String
    private var mamManager: MamManager? = null

    init {
        this.chatID = chatEntity.jid
    }

    override fun sendMessage(text: String): MessageEntity? {
        if (AppHelper.getXmppConnection().isConnectionAlive) {
            val jid: EntityBareJid
            try {
                jid = JidCreate.entityBareFrom(chatEntity.jid)
            } catch (e: XmppStringprepException) {
                return null
            }

            val messageUid = AppHelper.getXmppConnection().sendMessage(jid, text)
            while (!TrueTime.isInitialized()) {
                Thread {
                    try {
                        TrueTime.build().initialize()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }.start()
            }

            var timestamp: Long
            try {
                timestamp = TrueTime.now().time
            } catch (e: Exception) {
                // Fallback to Plain Old Java CurrentTimeMillis
                timestamp = System.currentTimeMillis()
            }

            val messageID = LocalDBWrapper.createMessageEntry(chatID, messageUid, AppHelper.getJid(), timestamp, text, true, false)
            return LocalDBWrapper.getMessageByID(messageID)
        } else {
            return null
        }
    }

    override fun getUserStatus(): Boolean {
        if (AppHelper.getXmppConnection() != null) {
            if (AppHelper.getXmppConnection().isConnectionAlive) {
                var presence: Presence? = null
                try {
                    presence = AppHelper.getXmppConnection().getUserPresence(JidCreate.bareFrom(chatID))
                } catch (e: XmppStringprepException) {
                    e.printStackTrace()
                }

                return presence!!.isAvailable
            }
        }
        return false
    }

    override fun loadMessagesFromMAM(): CompletableFuture<MamManager.MamQuery?> {
        return CompletableFuture.supplyAsync {
            if(AppHelper.getXmppConnection() != null) {
                val mamManager: MamManager? = AppHelper.getXmppConnection().mamManager
                if(mamManager != null) {
                    val firstMessageUid = LocalDBWrapper.getChatByChatID(chatID).firstMessageUid
                    if(firstMessageUid != "") {
                        return@supplyAsync mamManager.queryArchive(MamManager.MamQueryArgs.builder()
                                .beforeUid(firstMessageUid)
                                .limitResultsToJid(JidCreate.from(chatID))
                                .setResultPageSizeTo(50)
                                .build())
                    } else {
                        return@supplyAsync null
                    }
                } else {
                    return@supplyAsync null
                }
            } else {
                return@supplyAsync null
            }
        }
    }

    override fun loadLocalMessages(): List<MessageEntity>? {
        return LocalDBWrapper.getMessagesByChatID(chatID)
    }

    override fun loadRecentPageMessages(): CompletableFuture<MamManager.MamQuery?> {
        return CompletableFuture.supplyAsync {
            if(AppHelper.getXmppConnection() != null) {
                val mamManager: MamManager? = AppHelper.getXmppConnection().mamManager
                if(mamManager != null) {
                    return@supplyAsync mamManager.queryMostRecentPage(JidCreate.from(chatID), 20)
                } else {
                    return@supplyAsync null
                }
            } else {
                return@supplyAsync null
            }
        }
    }
}
