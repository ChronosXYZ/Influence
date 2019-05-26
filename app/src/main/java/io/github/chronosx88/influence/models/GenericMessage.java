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

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class GenericMessage implements IMessage {
    private long messageID;
    private String messageUid;
    private IUser author;
    private long timestamp;
    private String text;

    public GenericMessage(MessageEntity messageEntity) {
        this.messageID = messageEntity.messageID;
        this.messageUid = messageEntity.messageUid;
        this.author = new GenericUser(messageEntity.senderJid, messageEntity.senderJid, messageEntity.senderJid);
        this.timestamp = messageEntity.timestamp;
        this.text = messageEntity.text;
    }

    @Override
    public String getId() {
        return String.valueOf(messageID);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return new Date(timestamp);
    }

    public String getMessageUid() {
        return messageUid;
    }
}
