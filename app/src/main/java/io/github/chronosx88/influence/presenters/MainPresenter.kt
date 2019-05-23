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

package io.github.chronosx88.influence.presenters

import android.content.Intent
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.LocalDBWrapper
import io.github.chronosx88.influence.logic.MainLogic
import io.github.chronosx88.influence.models.appEvents.AuthenticationStatusEvent
import io.github.chronosx88.influence.models.appEvents.NewChatEvent
import io.github.chronosx88.influence.views.LoginActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainPresenter(private val view: CoreContracts.IMainViewContract) : CoreContracts.IMainPresenterContract {
    private val logic: CoreContracts.IMainLogicContract = MainLogic()

    override fun initConnection() {
        logic.startService()
    }

    override fun startChatWithPeer(username: String) {
        if(!username.contains("@")) {
            view.showSnackbar(AppHelper.getContext().getString(R.string.invalid_jid_error))
            return
        }
        LocalDBWrapper.createChatEntry(username, username.split("@")[0])
        EventBus.getDefault().post(NewChatEvent(username))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAuthenticate(event: AuthenticationStatusEvent) {
        when(event.authenticationStatus) {
            AuthenticationStatusEvent.INCORRECT_LOGIN_OR_PASSWORD -> {
                val intent = Intent(AppHelper.getContext(), LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                AppHelper.getContext().startActivity(intent)
                view.finishActivity()
            }
        }
    }

    override fun logoutFromAccount() {
        logic.logout()
    }

    override fun onStart() {
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
    }
}
