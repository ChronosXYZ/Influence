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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.MenuItem
import android.widget.Toast
import com.google.gson.Gson
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
import io.github.chronosx88.influence.models.appEvents.UserPresenceChangedEvent
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import io.github.chronosx88.influence.models.roomEntities.MessageEntity
import java9.util.concurrent.CompletableFuture
import java9.util.stream.StreamSupport
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.toast
import org.jivesoftware.smackx.forward.packet.Forwarded
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ChatPresenter(private val view: CoreContracts.IChatViewContract, private val chatID: String) : CoreContracts.IChatPresenterContract {
    private val logic: CoreContracts.IChatLogicContract
    private val chatEntity: ChatEntity?
    private val gson: Gson
    private val chatAdapter: MessagesListAdapter<GenericMessage>
    private val messageComparator = Comparator<GenericMessage> { o1, o2 -> o1.createdAt.time.compareTo(o2.createdAt.time) }

    init {
        this.logic = ChatLogic(LocalDBWrapper.getChatByChatID(chatID)!!)
        this.chatEntity = LocalDBWrapper.getChatByChatID(chatID)
        gson = Gson()
        val holdersConfig = MessageHolders()
        holdersConfig.setIncomingTextLayout(R.layout.item_incoming_text_message_custom)
        chatAdapter = MessagesListAdapter(AppHelper.getJid(), holdersConfig, AvatarImageLoader(view.getActivityObject()))
        chatAdapter.setLoadMoreListener { _, _ -> loadMoreMessages() }
        view.setAdapter(chatAdapter)
        getUserStatus()
        EventBus.getDefault().register(this)
        AppHelper.setCurrentChatActivity(chatID)
        chatAdapter.setOnMessageLongClickListener {
            val clipboard = AppHelper.getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(it.text, it.text)
            clipboard.primaryClip = clip
            view.showToast(AppHelper.getContext().getString(R.string.copied_to_clipboard))
        }
    }

    override fun sendMessage(text: String): Boolean {
        val message: MessageEntity? = logic.sendMessage(text)
        if(message != null) {
            val message = GenericMessage(message)
            chatAdapter.addToStart(message, true)
            EventBus.getDefault().post(LastMessageEvent(chatEntity!!.jid, message))
            return true
        }
        Toast.makeText(view.getActivityObject(), "Network error!", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun loadLocalMessages() {
        val entities = logic.loadLocalMessages()
        val messages = ArrayList<GenericMessage>()
        if(entities != null) {
            entities.forEach {
                messages.add(GenericMessage(it))
            }
        }
        messages.sortWith(messageComparator)
        chatAdapter.addToEnd(messages, true)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        AppHelper.setCurrentChatActivity("")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onNewMessage(event: NewMessageEvent) {
        if(event.chatID.equals(chatEntity!!.jid)) {
            val messageID = event.messageID
            chatAdapter.addToStart(GenericMessage(LocalDBWrapper.getMessageByID(messageID)), true)
            LocalDBWrapper.updateChatUnreadMessagesCount(chatEntity.jid, 0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onPresenceChanged(event: UserPresenceChangedEvent) {
        if(event.jid == (chatID)) {
            if(event.status) view.setUserStatus(AppHelper.getContext().getString(R.string.online)) else view.setUserStatus(AppHelper.getContext().getString(R.string.offline))
        }
    }

    private fun getUserStatus() {
        CompletableFuture.supplyAsync {
            return@supplyAsync logic.getUserStatus()
        }.thenAccept { status ->
            AppHelper.getMainUIThread().post({
                if(status) {
                    view.setUserStatus(AppHelper.getContext().getString(R.string.online))
                } else {
                    view.setUserStatus(AppHelper.getContext().getString(R.string.offline))
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_clear_chat -> {
                LocalDBWrapper.clearChat(chatID)
                chatAdapter.clear()
                EventBus.getDefault().post(LastMessageEvent(chatID, null))
            }
        }
    }

    override fun loadMoreMessages() {
        logic.loadMessagesFromMAM().thenAccept { query ->
            if(query != null) {
                val adapterMessages = ArrayList<GenericMessage>()
                StreamSupport.stream(query.page.forwarded)
                        .forEach { forwardedMessage ->
                            val message = Forwarded.extractMessagesFrom(Collections.singleton(forwardedMessage))[0]
                            if(message.body != null) {
                                if(LocalDBWrapper.getMessageByUID(message.stanzaId) == null) {
                                    val messageID = LocalDBWrapper.createMessageEntry(chatID, message.stanzaId, message.from.asBareJid().asUnescapedString(), forwardedMessage.delayInformation.stamp.time, message.body, true, true)
                                    adapterMessages.add(GenericMessage(LocalDBWrapper.getMessageByID(messageID)))
                                }
                            }
                        }
                AppHelper.getMainUIThread().post {
                    adapterMessages.sortWith(messageComparator)
                    chatAdapter.addToEnd(adapterMessages, true)
                }
                if(query.messageCount != 0) {
                    chatEntity!!.firstMessageUid = query.mamResultExtensions[0].id
                    LocalDBWrapper.updateChatEntity(chatEntity)
                }
            }
        }
    }

    override fun loadRecentPageMessages() {
        logic.loadRecentPageMessages().thenAccept { query ->
            if(query != null) {
                val adapterMessages = ArrayList<GenericMessage>()
                StreamSupport.stream(query.page.forwarded)
                        .forEach { forwardedMessage ->
                            val message = Forwarded.extractMessagesFrom(Collections.singleton(forwardedMessage))[0]
                            if(message.body != null) {
                                if(LocalDBWrapper.getMessageByUID(message.stanzaId) == null) {
                                    val messageID = LocalDBWrapper.createMessageEntry(chatID, message.stanzaId, message.from.asBareJid().asUnescapedString(), forwardedMessage.delayInformation.stamp.time, message.body, true, true)
                                    adapterMessages.add(GenericMessage(LocalDBWrapper.getMessageByID(messageID)))
                                }
                            }
                        }
                AppHelper.getMainUIThread().post {
                    adapterMessages.sortWith(messageComparator)
                    adapterMessages.forEach {
                        chatAdapter.addToStart(it, true)
                    }
                }
                if(query.messageCount != 0 && chatEntity!!.firstMessageUid == "") {
                    chatEntity.firstMessageUid = query.mamResultExtensions[0].id
                    LocalDBWrapper.updateChatEntity(chatEntity)
                }
                EventBus.getDefault().post(LastMessageEvent(chatID, GenericMessage(LocalDBWrapper.getLastMessage(chatID))))
            }
        }
    }
}
