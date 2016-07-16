package com.fwest98.fingify.Fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.fwest98.fingify.Data.AccountManager;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Settings.Constants;

public class AccountPreferencesFragment extends PreferenceFragment {
    private Preference accountDesc, accountLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_account);

        accountDesc = findPreference(Constants.ACCOUNT_DESC_SETTING);
        accountLogin = findPreference(Constants.ACCOUNT_LOGIN_SETTING);

        AccountManager.initialize(getActivity());

        changePreferenceSummaries();

        accountLogin.setOnPreferenceClickListener(preference -> {
            AccountManager.getInstance(getActivity()).login(getActivity(), result -> changePreferenceSummaries());
            return true;
        });
    }

    private void changePreferenceSummaries() {
        if(AccountManager.isSet()) {
            accountDesc.setSummary(getString(R.string.settings_account_myaccount_summary_loggedin) + " " + AccountManager.getUsername());
            accountLogin.setSummary(R.string.settings_account_login_summary_loggedin);
        } else {
            accountDesc.setSummary(R.string.settings_account_myaccount_summary_notloggedin);
            accountLogin.setSummary(R.string.settings_account_login_summary_notloggedin);
        }
    }
}
