package com.fwest98.fingify;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.fwest98.fingify.Fragments.ApplicationsFragment;
import com.fwest98.fingify.Fragments.NewApplicationFragment;
import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.Services.GCMIntentService;
import com.fwest98.fingify.Settings.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ApplicationsActivity extends Activity implements NewApplicationFragment.onResultListener {

    private static ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applications);

        ActionBar actionBar = getActionBar();

        if(!PreferenceManager.getDefaultSharedPreferences(this).contains(Constants.FINGERPRINT_AUTHENTICATION_SETTING) &&
                FingerprintManager.isFingerPrintSupported(this)) {
            // Show Dialog that it's strongly adviced to enable fingerprints
            FingerprintManager.createAdviceDialog(this);
        }

        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.activity_container, ApplicationsFragment.newInstance()).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.applications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.activity_applications_action_settings:
                Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferencesIntent);
                return true;
            case R.id.activity_applications_action_newapplication:
                NewApplicationFragment fragment = NewApplicationFragment.newInstance(this);
                fragment.show(getFragmentManager(), "dialog");
                return true;
            case R.id.activity_applications_action_verifydialog:
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    GCMIntentService.createNotification(this, true);
                }, 5, TimeUnit.SECONDS);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.activity_container);
        if(fragment instanceof ApplicationsFragment) { // This is it
            ((ApplicationsFragment) fragment).reCreateApplicationsList();
        }
    }
}
