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

package io.github.chronosx88.influence.helpers;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.github.chronosx88.influence.models.GenericUser;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class LocalDBWrapper {
    private static final String LOG_TAG = "LocalDBWrapper";
    private static RoomHelper dbInstance = AppHelper.getChatDB();

    public static void createChatEntry(String jid, String chatName, ArrayList<GenericUser> users) {
        dbInstance.chatDao().addChat(new ChatEntity(jid, chatName, users, 0, ""));
    }

    public static long createMessageEntry(String chatID, String messageUid, String senderJid, long timestamp, String text, boolean isSent, boolean isRead) {
        List<ChatEntity> chatEntities = AppHelper.getChatDB().chatDao().getChatByChatID(chatID);
        if(chatEntities.size() < 1) {
            Log.e(LOG_TAG, "Failed to create message entry because chat " + chatID + " doesn't exists!");
            return -1;
        }
        MessageEntity message = new MessageEntity(chatID, messageUid, senderJid, timestamp, text, isSent, isRead);
        long index = dbInstance.messageDao().insertMessage(message);
        return index;
    }

    public static MessageEntity getMessageByID(long messageID) {
        List<MessageEntity> messages = dbInstance.messageDao().getMessageByID(messageID);
        if(messages.isEmpty()) {
            return null;
        }
        return messages.get(0);
    }

    public static MessageEntity getMessageByUID(String messageUID) {
        List<MessageEntity> messages = dbInstance.messageDao().getMessageByUID(messageUID);
        if(messages.isEmpty()) {
            return null;
        }
        return messages.get(0);
    }

    public static List<MessageEntity> getMessagesByChatID(String chatID) {
        List<MessageEntity> messages = dbInstance.messageDao().getMessagesByChatID(chatID);
        if(messages.isEmpty()) {
            return null;
        }
        return messages;
    }

    public static ChatEntity getChatByChatID(String chatID) {
        List<ChatEntity> chats = dbInstance.chatDao().getChatByChatID(chatID);
        if(chats.isEmpty()) {
            return null;
        }
        return chats.get(0);
    }

    public static void updateChatEntity(ChatEntity chatEntity) {
        dbInstance.chatDao().updateChat(chatEntity);
    }

    public static void updateMessage(MessageEntity messageEntity) {
        dbInstance.messageDao().updateMessage(messageEntity);
    }

    public static void clearDatabase() {
        dbInstance.messageDao().clearMessages();
        dbInstance.chatDao().clearChats();
    }

    public static MessageEntity getLastMessage(String chatID) {
        long messageID = dbInstance.messageDao().getLastMessageByChatID(chatID);
        return getMessageByID(messageID);
    }

    public static MessageEntity getFirstMessage(String chatID) {
        long messageID = dbInstance.messageDao().getFirstMessageByChatID(chatID);
        return getMessageByID(messageID);
    }

    public static void updateChatUnreadMessagesCount(String chatID, int unreadMessagesCount) {
        dbInstance.chatDao().updateUnreadMessagesCount(chatID, unreadMessagesCount);
    }

    public static void clearChat(String chatID) {
        dbInstance.messageDao().clearMessagesByChatID(chatID);
    }
}
