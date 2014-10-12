package com.fwest98.fingify;

import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.fwest98.fingify.Fragments.AppPreferencesFragment;

import java.util.List;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return AppPreferencesFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}
