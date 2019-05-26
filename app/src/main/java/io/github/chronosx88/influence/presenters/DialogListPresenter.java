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

import androidx.appcompat.app.AlertDialog;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.AvatarImageLoader;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.logic.DialogListLogic;
import io.github.chronosx88.influence.models.GenericDialog;
import io.github.chronosx88.influence.models.GenericMessage;
import io.github.chronosx88.influence.models.GenericUser;
import io.github.chronosx88.influence.models.appEvents.AuthenticationStatusEvent;
import io.github.chronosx88.influence.models.appEvents.LastMessageEvent;
import io.github.chronosx88.influence.models.appEvents.NewChatEvent;
import io.github.chronosx88.influence.models.appEvents.NewMessageEvent;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;
import io.github.chronosx88.influence.views.ChatActivity;
import java8.util.stream.StreamSupport;
import java9.util.concurrent.CompletableFuture;

public class DialogListPresenter implements CoreContracts.IDialogListPresenterContract {
    private CoreContracts.IDialogListViewContract view;
    private CoreContracts.IDialogListLogicContract logic;
    private DialogsListAdapter<GenericDialog> dialogListAdapter;
    private Comparator<GenericDialog> dialogComparator = (dialog1, dialog2) -> {
        if(dialog2.getLastMessage() != null && dialog1.getLastMessage() != null) {
            return Long.compare(dialog2.getLastMessage().getCreatedAt().getTime(), dialog1.getLastMessage().getCreatedAt().getTime());
        }
        if(dialog2.getLastMessage() != null) {
            return 1;
        } else if(dialog1.getLastMessage() != null) {
            return -1;
        }
        return 0;
    };

    public DialogListPresenter(CoreContracts.IDialogListViewContract view) {
        this.view = view;
        dialogListAdapter = new DialogsListAdapter<>(R.layout.item_dialog_custom, new AvatarImageLoader(view.getFragmentObject()));
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
        StreamSupport.stream(dialogs)
                .forEach(dialog -> {
                    MessageEntity messageEntity = LocalDBWrapper.getLastMessage(dialog.getId());
                    if(messageEntity != null) {
                        dialog.setLastMessage(new GenericMessage(messageEntity));
                    }
                });
        dialogListAdapter.setItems(dialogs);
        dialogListAdapter.sort(dialogComparator);
        loadRemoteContactList();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void openChat(String chatID) {
        Intent intent = new Intent(AppHelper.getContext(), ChatActivity.class);
        intent.putExtra("chatID", chatID);
        intent.putExtra("chatName", LocalDBWrapper.getChatByChatID(chatID).chatName);
        intent.putExtra("chatAvatar", AppHelper.avatarsCache.get(chatID));
        view.startActivity(intent);
        setUnreadMessagesCount(chatID, 0);
        LocalDBWrapper.updateChatUnreadMessagesCount(chatID, 0);
    }

    @Subscribe
    public void onNewChatCreated(NewChatEvent event) {
        dialogListAdapter.upsertItem(new GenericDialog(LocalDBWrapper.getChatByChatID(event.chatID)));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
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
                        String chatID = contact.getJid().asUnescapedString();
                        LocalDBWrapper.createChatEntry(chatID, contact.getName() == null ? contact.getJid().asUnescapedString().split("@")[0] : contact.getName(), new ArrayList<>());
                        GenericDialog dialog = new GenericDialog(LocalDBWrapper.getChatByChatID(chatID));
                        MessageEntity messageEntity = LocalDBWrapper.getLastMessage(chatID);
                        if(messageEntity != null) {
                            dialog.setLastMessage(new GenericMessage(messageEntity));
                        }
                        dialogListAdapter.upsertItem(dialog);
                        dialogListAdapter.notifyDataSetChanged();
                    });
                }
            });
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLastMessage(LastMessageEvent event) {
        dialogListAdapter.updateDialogWithMessage(event.chatID, event.message);
        GenericDialog dialog = new GenericDialog(LocalDBWrapper.getChatByChatID(event.chatID));
        dialog.setLastMessage(event.message);
        dialogListAdapter.updateItemById(dialog);
        dialogListAdapter.sort(dialogComparator);
    }

    private void setUnreadMessagesCount(String chatID, int unreadMessagesCount) {
        GenericDialog dialog = dialogListAdapter.getItemById(chatID);
        if(dialog != null) {
            dialog.setUnreadMessagesCount(unreadMessagesCount);
            dialogListAdapter.updateItemById(dialog);
        }
    }
}
