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

package io.github.chronosx88.influence.models.daos;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

@Dao
public interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addChat(ChatEntity chatEntity);

    @Query("DELETE FROM chats WHERE jid = :jid")
    void deleteChat(String jid);

    @Query("SELECT * FROM chats")
    List<ChatEntity> getAllChats();

    @Query("SELECT * FROM chats WHERE jid = :jid")
    List<ChatEntity> getChatByChatID(String jid);

    @Update
    void updateChat(ChatEntity chat);

    @Query("DELETE FROM chats")
    void clearChats();

    @Query("UPDATE chats SET unreadMessagesCount = :unreadMessagesCount WHERE jid = :chatID")
    void updateUnreadMessagesCount(String chatID, int unreadMessagesCount);
}
