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

package io.github.chronosx88.influence.models;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.List;

import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class GenericDialog implements IDialog {
    private String dialogID;
    private String dialogPhoto;
    private String dialogName;
    private List<GenericUser> users;
    private IMessage lastMessage;
    private int unreadMessagesCount;

    public GenericDialog(ChatEntity chatEntity) {
        dialogID = chatEntity.jid;
        dialogPhoto = chatEntity.jid;
        dialogName = chatEntity.chatName;
        users = new ArrayList<>();
        unreadMessagesCount = chatEntity.unreadMessagesCount;
    }

    @Override
    public String getId() {
        return dialogID;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(IMessage message) {
        lastMessage = message;
    }

    @Override
    public int getUnreadCount() {
        return unreadMessagesCount;
    }

    public void setUnreadMessagesCount(int unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
    }
}
