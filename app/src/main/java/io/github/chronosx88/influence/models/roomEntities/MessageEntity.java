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

@Entity(tableName = "messages")
public class MessageEntity {
    @PrimaryKey(autoGenerate = true) public long messageID; // Global message ID
    @ColumnInfo public String chatID; // Chat ID
    @ColumnInfo public String messageUid;
    @ColumnInfo public String senderJid;
    @ColumnInfo public long timestamp; // Timestamp
    @ColumnInfo public String text; // Message text
    @ColumnInfo public boolean isSent; // Send status indicator
    @ColumnInfo public boolean isRead; // Message Read Indicator

    public MessageEntity(String chatID, String messageUid, String senderJid, long timestamp, String text, boolean isSent, boolean isRead) {
        this.chatID = chatID;
        this.messageUid = messageUid;
        this.senderJid = senderJid;
        this.timestamp = timestamp;
        this.text = text;
        this.isSent = isSent;
        this.isRead = isRead;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
