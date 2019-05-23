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

package io.github.chronosx88.influence.views.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;

import org.jetbrains.annotations.NotNull;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.presenters.SettingsPresenter;

public class SettingsFragment extends PreferenceFragmentCompat implements CoreContracts.ISettingsView {
    private ProgressDialog progressDialog;
    private SettingsPresenter presenter;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        progressDialog = new ProgressDialog(getContext(), R.style.AlertDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        presenter = new SettingsPresenter(this);
        addPreferencesFromResource(R.xml.main_settings);
    }

    @Override
    public void loadingScreen(boolean state) {
        if(state)
            progressDialog.show();
        else
            progressDialog.dismiss();
    }

    @Override
    public void showMessage(@NotNull String message) {
        Toast.makeText(AppHelper.getContext(), message, Toast.LENGTH_LONG).show();
    }
}
