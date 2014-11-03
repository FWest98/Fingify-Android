package com.fwest98.fingify;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.fwest98.fingify.Adapters.ApplicationActivityPagerAdapter;
import com.fwest98.fingify.Fragments.ApplicationsFragment;
import com.fwest98.fingify.Fragments.NewApplicationFragment;
import com.fwest98.fingify.Fragments.RequestsFragment;
import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.Services.GCMIntentService;
import com.fwest98.fingify.Settings.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ApplicationsActivity extends Activity implements NewApplicationFragment.onResultListener, ApplicationsFragment.ApplicationsFragmentCallbacks {

    private ActionBar actionBar;
    private Menu menu;
    private ApplicationActivityPagerAdapter fragmentPagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applications);

        /* Viewpager things */
        fragmentPagerAdapter = new ApplicationActivityPagerAdapter(getFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.activity_viewPager);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                actionBar.setSelectedNavigationItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        /* Tabs */
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener listener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        actionBar.addTab(
                actionBar.newTab()
                    .setText("Accounts")
                    .setTabListener(listener));
        actionBar.addTab(
                actionBar.newTab()
                    .setTabListener(listener)
                    .setText("Requests"));

        fragmentPagerAdapter.addItem(ApplicationsFragment.newInstance(this));
        fragmentPagerAdapter.addItem(new RequestsFragment());

        if(!PreferenceManager.getDefaultSharedPreferences(this).contains(Constants.FINGERPRINT_AUTHENTICATION_SETTING) &&
                FingerprintManager.isFingerPrintSupported(this)) {
            // Show Dialog that it's strongly adviced to enable fingerprints
            FingerprintManager.createAdviceDialog(this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.applications, menu);
        this.menu = menu;
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
                    GCMIntentService.createNotification(this, false);
                }, 5, TimeUnit.SECONDS);
                return true;
            case R.id.activity_applications_action_account:
                // Open My Account activity
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

    @Override
    public void onDisableMenu() {
        if(menu == null) return;
        for(int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
    }

    @Override
    public void onEnableMenu() {
        if(menu == null) return;
        for(int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(true);
        }
    }
}
