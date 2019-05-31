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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import io.github.chronosx88.influence.XMPPConnectionService;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;

public class MainLogic implements CoreContracts.IMainLogicContract {
    private static final String LOG_TAG = MainLogic.class.getName();

    private Context context;

    public MainLogic() {
        this.context = AppHelper.getContext();
    }

    @Override
    public void startService() {
        context.startService(new Intent(context, XMPPConnectionService.class));
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                XMPPConnectionService.XMPPServiceBinder binder = (XMPPConnectionService.XMPPServiceBinder) service;
                AppHelper.setXmppConnection(binder.getConnection());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                AppHelper.setXmppConnection(null);
            }
        };
        AppHelper.setServiceConnection(connection);
        context.bindService(new Intent(context, XMPPConnectionService.class), connection,Context.BIND_AUTO_CREATE);
    }

    @Override
    public void logout() {
        LocalDBWrapper.clearDatabase();
        AppHelper.resetLoginCredentials();
        context.unbindService(AppHelper.getServiceConnection());
        context.stopService(new Intent(context, XMPPConnectionService.class));
        AppHelper.setXmppConnection(null);
        AppHelper.setServiceConnection(null);
        AppHelper.setJid(null);
    }
}
