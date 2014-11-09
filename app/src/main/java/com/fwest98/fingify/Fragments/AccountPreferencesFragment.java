package com.fwest98.fingify.Fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.fwest98.fingify.Data.Account;
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

        Account.initialize(getActivity());

        changePreferenceSummaries();

        accountLogin.setOnPreferenceClickListener(preference -> {
            Account.getInstance(getActivity()).login(result -> changePreferenceSummaries());
            return true;
        });
    }

    private void changePreferenceSummaries() {
        if(Account.isSet()) {
            accountDesc.setSummary(getString(R.string.settings_account_myaccount_summary_loggedin) + " " + Account.getUsername());
            accountLogin.setSummary(R.string.settings_account_login_summary_loggedin);
        } else {
            accountDesc.setSummary(R.string.settings_account_myaccount_summary_notloggedin);
            accountLogin.setSummary(R.string.settings_account_login_summary_notloggedin);
        }
    }
}
