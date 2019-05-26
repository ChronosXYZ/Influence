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

package io.github.chronosx88.influence.models.roomEntities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

import io.github.chronosx88.influence.models.GenericUser;

@Entity(tableName = "chats")
public class ChatEntity {
    @PrimaryKey @NonNull public String jid;
    @ColumnInfo public String chatName;
    @ColumnInfo public ArrayList<GenericUser> users;
    @ColumnInfo public int unreadMessagesCount;
    @ColumnInfo public String firstMessageUid;

    public ChatEntity(@NonNull String jid, String chatName, ArrayList<GenericUser> users, int unreadMessagesCount, String firstMessageUid) {
        this.jid = jid;
        this.chatName = chatName;
        this.users = users;
        this.unreadMessagesCount = unreadMessagesCount;
        this.firstMessageUid = firstMessageUid;
    }

    public boolean isPrivateChat() {
        return users.size() == 2;
    }
}
