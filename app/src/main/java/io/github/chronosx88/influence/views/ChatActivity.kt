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

package io.github.chronosx88.influence.views

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.models.GenericMessage
import io.github.chronosx88.influence.presenters.ChatPresenter
import org.jetbrains.anko.find
import org.jetbrains.anko.toast

class ChatActivity : AppCompatActivity(), CoreContracts.IChatViewContract {
    private var messageList: MessagesList? = null
    private var messageInput: MessageInput? = null
    private var chatNameTextView: TextView? = null
    private var chatAvatar: ImageView? = null
    private var userStatus: TextView? = null
    private var presenter: ChatPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent = intent

        val toolbar = findViewById<Toolbar>(R.id.toolbar_chat_activity)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        messageList = find(R.id.messages_list)
        messageList!!.layoutManager = LinearLayoutManager(this)
        chatNameTextView = find(R.id.appbar_username)
        userStatus = find(R.id.user_status_text)
        chatAvatar = find(R.id.profile_image_chat_activity)
        messageInput = find(R.id.message_input)
        messageInput!!.setInputListener {
            presenter!!.sendMessage(it.toString())
        }
        chatNameTextView!!.text = intent.getStringExtra("chatName")
        presenter = ChatPresenter(this, intent.getStringExtra("chatID"))
        loadAvatarFromIntent(intent)
        presenter!!.loadLocalMessages()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        presenter!!.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter!!.onDestroy()
        presenter = null
    }

    override fun setAdapter(adapter: MessagesListAdapter<GenericMessage>) {
        messageList!!.setAdapter(adapter)
    }

    private fun loadAvatarFromIntent(intent: Intent) {
        val avatarBytes: ByteArray? = intent.getByteArrayExtra("chatAvatar")
        if(avatarBytes != null) {
            val avatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.size)
            chatAvatar!!.setImageBitmap(avatar)
        } else {
            val chatName = intent.getStringExtra("chatName")
            val firstLetter = Character.toString(chatName[0].toUpperCase())
            chatAvatar!!.setImageDrawable(TextDrawable.builder()
                    .beginConfig()
                    .width(64)
                    .height(64)
                    .endConfig()
                    .buildRound(firstLetter, ColorGenerator.MATERIAL.getColor(firstLetter)))
        }
    }

    override fun setUserStatus(status: String) {
        userStatus!!.text = status
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_activity_menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        presenter!!.loadRecentPageMessages()
    }

    override fun getActivityObject(): Activity {
        return this
    }

    override fun showToast(text: String) {
        toast(text)
    }
}
