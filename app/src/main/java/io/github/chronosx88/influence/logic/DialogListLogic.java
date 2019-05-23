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

package io.github.chronosx88.influence.logic;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.List;
import java.util.Set;

import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class DialogListLogic implements CoreContracts.IDialogListLogicContract {

    @Override
    public List<ChatEntity> loadLocalChats() {
        return AppHelper.getChatDB().chatDao().getAllChats();
    }

    @Override
    public Set<RosterEntry> getRemoteContacts() {
        if(AppHelper.getXmppConnection() != null) {
            return AppHelper.getXmppConnection().getContactList();
        }
        return null;
    }
}
