package com.fwest98.fingify.Fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Settings.Constants;

public class AppPreferencesFragment extends PreferenceFragment {
    private CheckBoxPreference fingerprintPreference;
    private ListPreference notificationPopupSetting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_app);

        if(!PreferenceManager.getDefaultSharedPreferences(getActivity()).contains(Constants.FINGERPRINT_AUTHENTICATION_SETTING) &&
                FingerprintManager.isFingerPrintSupported(getActivity())) {
            // Show Dialog that it's strongly adviced to enable fingerprints
            FingerprintManager.createAdviceDialog(getActivity());
        }

        fingerprintPreference = (CheckBoxPreference) findPreference(Constants.FINGERPRINT_AUTHENTICATION_SETTING);
        fingerprintPreference.setEnabled(FingerprintManager.isFingerPrintSupported(getActivity()));
        fingerprintPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            // Preference changed
            if((boolean) newValue) {
                 // Now it's on
                FingerprintManager.authenticate(getActivity(), (result) -> {
                    if(result == FingerprintManager.FingerprintResponses.SUCCEEDED) {
                        // It succeeded, enable fingerprints
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(Constants.FINGERPRINT_AUTHENTICATION_SETTING, true).commit();
                    } else {
                        // It failed, disable fingerprints
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(Constants.FINGERPRINT_AUTHENTICATION_SETTING, false).commit();
                    }
                }, true);
            }

            return true;
        });

        notificationPopupSetting = (ListPreference) findPreference(Constants.NOTIFICATION_POPUP_SETTING);
        notificationPopupSetting.setSummary(notificationPopupSetting.getEntry());
        notificationPopupSetting.setOnPreferenceChangeListener((preference, newValue) -> {
            getActivity().recreate();
            return true;
        });
    }
}
