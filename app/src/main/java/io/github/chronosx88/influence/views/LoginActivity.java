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

package io.github.chronosx88.influence.views;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.XMPPConnectionService;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.models.appEvents.AuthenticationStatusEvent;

public class LoginActivity extends AppCompatActivity implements CoreContracts.ILoginViewContract {
    private EditText jidEditText;
    private EditText passwordEditText;
    private TextInputLayout jidInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button signInButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        jidEditText = findViewById(R.id.login_jid);
        passwordEditText = findViewById(R.id.login_password);

        jidInputLayout = findViewById(R.id.jid_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        jidInputLayout.setErrorEnabled(true);
        passwordInputLayout.setErrorEnabled(true);

        signInButton = findViewById(R.id.sign_in_button);
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.AlertDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        signInButton.setOnClickListener((v) -> {
            if(checkLoginCredentials()) {
                saveLoginCredentials();
                doLogin();
            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    public void loadingScreen(boolean state) {
        if(state)
            progressDialog.show();
        else
            progressDialog.dismiss();
    }

    private boolean checkLoginCredentials() {
        jidEditText.setError(null);
        passwordEditText.setError(null);

        jidInputLayout.setError(null);
        passwordInputLayout.setError(null);

        String jid = jidEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditText.setError("Invalid password");
            focusView = passwordEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(jid)) {
            jidEditText.setError("Field is required!");
            focusView = jidEditText;
            cancel = true;
        } else if (!isJidValid(jid)) {
            jidEditText.setError("Invalid JID");
            focusView = jidEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private boolean isJidValid(String jid) {
        return jid.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void saveLoginCredentials() {
        AppHelper.getPreferences().edit()
                .putString("jid", jidEditText.getText().toString())
                .putString("pass", passwordEditText.getText().toString())
                .putBoolean("logged_in", true)
                .apply();
    }

    private void doLogin() {
        loadingScreen(true);
        startService(new Intent(this, XMPPConnectionService.class));
        AppHelper.setServiceConnection(new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                XMPPConnectionService.XMPPServiceBinder binder = (XMPPConnectionService.XMPPServiceBinder) service;
                AppHelper.setXmppConnection(binder.getConnection());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                AppHelper.setXmppConnection(null);
            }
        });
        bindService(new Intent(this, XMPPConnectionService.class), AppHelper.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthenticate(AuthenticationStatusEvent event) {
        switch (event.authenticationStatus) {
            case AuthenticationStatusEvent.CONNECT_AND_LOGIN_SUCCESSFUL: {
                loadingScreen(false);
                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                break;
            }
            case AuthenticationStatusEvent.INCORRECT_LOGIN_OR_PASSWORD: {
                loadingScreen(false);
                passwordInputLayout.setError("Invalid JID/Password");
                AppHelper.resetLoginCredentials();
                break;
            }
            case AuthenticationStatusEvent.NETWORK_ERROR: {
                loadingScreen(false);
                jidInputLayout.setError("Network error");
                AppHelper.resetLoginCredentials();
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
