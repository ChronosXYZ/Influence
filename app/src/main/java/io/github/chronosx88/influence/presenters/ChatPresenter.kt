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

package io.github.chronosx88.influence.presenters

import android.graphics.BitmapFactory
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.gson.Gson
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.AvatarImageLoader
import io.github.chronosx88.influence.helpers.LocalDBWrapper
import io.github.chronosx88.influence.logic.ChatLogic
import io.github.chronosx88.influence.models.GenericMessage
import io.github.chronosx88.influence.models.appEvents.LastMessageEvent
import io.github.chronosx88.influence.models.appEvents.NewMessageEvent
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import io.github.chronosx88.influence.models.roomEntities.MessageEntity
import java9.util.concurrent.CompletableFuture
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.stringprep.XmppStringprepException

class ChatPresenter(private val view: CoreContracts.IChatViewContract, private val chatID: String) : CoreContracts.IChatPresenterContract {
    private val logic: CoreContracts.IChatLogicContract
    private val chatEntity: ChatEntity?
    private val gson: Gson
    private val chatAdapter: MessagesListAdapter<GenericMessage>

    init {
        this.logic = ChatLogic(LocalDBWrapper.getChatByChatID(chatID)!!)
        this.chatEntity = LocalDBWrapper.getChatByChatID(chatID)
        gson = Gson()
        val holdersConfig = MessageHolders()
        holdersConfig.setIncomingTextLayout(R.layout.item_incoming_text_message_custom)
        chatAdapter = MessagesListAdapter(AppHelper.getJid(), holdersConfig, AvatarImageLoader())
        view.setAdapter(chatAdapter)
        EventBus.getDefault().register(this)
    }

    override fun sendMessage(text: String): Boolean {
        val message: MessageEntity? = logic.sendMessage(text)
        if(message != null) {
            val message = GenericMessage(message)
            chatAdapter.addToStart(message, true)
            EventBus.getDefault().post(LastMessageEvent(chatEntity!!.jid, message))
            return true
        }
        return false
    }

    override fun loadLocalMessages() {
        val entities: List<MessageEntity>? = LocalDBWrapper.getMessagesByChatID(chatID)
        val messages = ArrayList<GenericMessage>()
        if(entities != null) {
            entities.forEach {
                messages.add(GenericMessage(it))
            }
        }
        chatAdapter.addToEnd(messages, true)
    }

    override fun onDestroy() {
        //
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onNewMessage(event: NewMessageEvent) {
        if(event.chatID.equals(chatEntity!!.jid)) {
            val messageID = event.messageID
            chatAdapter.addToStart(GenericMessage(LocalDBWrapper.getMessageByID(messageID)), true)
        }
    }
}
