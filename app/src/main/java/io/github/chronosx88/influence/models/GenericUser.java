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

import com.stfalcon.chatkit.commons.models.IUser;

public class GenericUser implements IUser {
    private String jid;
    private String name;
    private String avatar;

    public GenericUser(String jid, String name, String avatar) {
        this.jid = jid;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public String getId() {
        return jid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }
}
