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

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class GenericMessage implements IMessage, MessageContentType.Image {
    private long messageID;
    private IUser author;
    private long timestamp;
    private String text;
    private String imageUrl;

    public GenericMessage(MessageEntity messageEntity) {
        this.messageID = messageEntity.messageID;
        this.author = new GenericUser(messageEntity.senderJid, messageEntity.senderJid, messageEntity.senderJid);
        this.timestamp = messageEntity.timestamp;
        this.text = messageEntity.text;
        if(messageEntity.text.contains("http") && messageEntity.text.contains(".jpg")) {
            imageUrl = messageEntity.text;
        }
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

    @Nullable
    @Override
    public String getImageUrl() {
        return imageUrl;
    }
}
