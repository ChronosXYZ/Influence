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

package io.github.chronosx88.influence.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.github.chronosx88.influence.models.GenericDialog
import io.github.chronosx88.influence.models.GenericMessage
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import io.github.chronosx88.influence.models.roomEntities.MessageEntity
import java9.util.concurrent.CompletableFuture
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smackx.mam.MamManager

interface CoreContracts {

    interface IGenericView {
        fun loadingScreen(state: Boolean)
    }

    // -----ChatList-----

    interface IDialogListLogicContract {
        fun loadLocalChats(): List<ChatEntity>
        fun getRemoteContacts(): Set<RosterEntry>?
    }

    interface IDialogListPresenterContract {
        fun openChat(chatID: String)
        fun onStart()
        fun onStop()
        fun loadRemoteContactList()
    }

    interface IDialogListViewContract {
        fun setDialogAdapter(adapter: DialogsListAdapter<GenericDialog>)
        fun startActivity(intent: Intent)
        fun getActivityContext(): Context?
        fun getFragmentObject(): Fragment
    }

    // -----MainActivity-----

    interface IMainLogicContract {
        fun startService()
        fun logout()
    }

    interface IMainPresenterContract {
        fun initConnection()
        fun startChatWithPeer(username: String)
        fun logoutFromAccount()
        fun onStart()
        fun onStop()
        fun onDestroy()
    }

    interface IMainViewContract {
        fun showSnackbar(message: String)
        fun showProgressBar(state: Boolean)
        fun finishActivity()
    }

    // -----ChatActivity-----

    interface IChatLogicContract {
        fun sendMessage(text: String): MessageEntity?
        fun getUserStatus(): Boolean
        fun loadMessagesFromMAM(): CompletableFuture<MamManager.MamQuery?>
        fun loadRecentPageMessages(): CompletableFuture<MamManager.MamQuery?>
        fun loadLocalMessages(): List<MessageEntity>?
    }

    interface IChatPresenterContract {
        fun sendMessage(text: String): Boolean
        fun loadLocalMessages()
        fun loadMoreMessages()
        fun loadRecentPageMessages()
        fun onDestroy()
        fun onOptionsItemSelected(item: MenuItem)
    }

    interface IChatViewContract {
        fun setAdapter(adapter: MessagesListAdapter<GenericMessage>)
        fun setUserStatus(status: String)
        fun getActivityObject(): Activity
        fun showToast(text: String)
    }

    // -----SettingsFragment-----

    interface ISettingsLogic // TODO

    interface ISettingsPresenter // TODO

    interface ISettingsView {
        fun loadingScreen(state: Boolean)
        fun showMessage(message: String)
    }

    // -----LoginActivity-----
    interface ILoginViewContract : IGenericView
}
