package com.fwest98.fingify;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fwest98.fingify.Adapters.ApplicationActivityPagerAdapter;
import com.fwest98.fingify.Data.AccountManager;
import com.fwest98.fingify.Fragments.ApplicationsFragment;
import com.fwest98.fingify.Fragments.NewApplicationFragment;
import com.fwest98.fingify.Fragments.RequestsFragment;
import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.Settings.Constants;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;


public class MainActivity extends ActionBarActivity implements NewApplicationFragment.onResultListener, ApplicationsFragment.ApplicationsFragmentCallbacks, RequestsFragment.onLoadStateChangedListener {

    private Menu menu;
    private ApplicationActivityPagerAdapter fragmentPagerAdapter;
    private ViewPager viewPager;
    private MenuItem refreshItem;
    private MaterialTabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applications);
        AccountManager.initialize(this);

        tabHost = (MaterialTabHost) findViewById(R.id.toolbar_tabs);

        /* Viewpager & tab things */
        fragmentPagerAdapter = new ApplicationActivityPagerAdapter(getFragmentManager());

        fragmentPagerAdapter.addItem(ApplicationsFragment.newInstance(this), getString(R.string.activity_applications_tab_accounts));
        fragmentPagerAdapter.addItem(RequestsFragment.newInstance(this), getString(R.string.activity_applications_tab_requests));

        viewPager = (ViewPager) findViewById(R.id.activity_viewPager);
        viewPager.setAdapter(fragmentPagerAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                tabHost.setSelectedNavigationItem(i);
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        MaterialTabListener listener = new MaterialTabListener() {
            @Override
            public void onTabSelected(MaterialTab materialTab) {
                viewPager.setCurrentItem(materialTab.getPosition());
            }

            @Override
            public void onTabReselected(MaterialTab materialTab) {

            }

            @Override
            public void onTabUnselected(MaterialTab materialTab) {

            }
        };

        for(int i = 0; i < fragmentPagerAdapter.getCount(); i++) {
            CharSequence tabTitle = fragmentPagerAdapter.getPageTitle(i);
            tabHost.addTab(tabHost.newTab().setText(tabTitle).setTabListener(listener));
        }

        if(!PreferenceManager.getDefaultSharedPreferences(this).contains(Constants.FINGERPRINT_AUTHENTICATION_SETTING) &&
                FingerprintManager.isFingerPrintSupported(this)) {
            // Show Dialog that it's strongly adviced to enable fingerprints
            FingerprintManager.createAdviceDialog(this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        boolean applicationsList = viewPager.getCurrentItem() == 0;

        getMenuInflater().inflate(R.menu.activity_main, menu);
        menu.findItem(R.id.activity_applications_action_newapplication).setVisible(applicationsList);
        refreshItem = menu.findItem(R.id.activity_applications_action_refresh);
        refreshItem.setVisible(!applicationsList);

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean applicationsList = viewPager.getCurrentItem() == 0;
        menu.findItem(R.id.activity_applications_action_newapplication).setVisible(applicationsList);
        menu.findItem(R.id.activity_applications_action_refresh).setVisible(!applicationsList);
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.activity_applications_action_refresh:
                Fragment reqFragment = fragmentPagerAdapter.getItem(1);
                if(reqFragment == null || !(reqFragment instanceof RequestsFragment)) return true;
                ((RequestsFragment) reqFragment).reCreateRequestsList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult() {
        for(Fragment fragment : fragmentPagerAdapter.getFragments()) {
            if(fragment instanceof ApplicationsFragment) {
                ((ApplicationsFragment) fragment).reCreateApplicationsList();
            }
        }
    }

    @Override
    public void onDisable() {
        if(menu == null) return;
        for(int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        tabHost.setVisibility(View.INVISIBLE);
        fragmentPagerAdapter.setLimit(1);
    }

    @Override
    public void onEnable() {
        if(menu == null) return;
        for(int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(true);
        }
        tabHost.setVisibility(View.VISIBLE);
        fragmentPagerAdapter.setLimit(0);
        invalidateOptionsMenu(); // for correct enabled/disabled
    }

    @Override
    public void onLoadStart() {
        setRefreshButtonState(true);
    }

    @Override
    public void onLoadEnd() {
        setRefreshButtonState(false);
    }

    @Override
    public void onLoadCancel() {
        setRefreshButtonState(false);
    }

    private void setRefreshButtonState(boolean loading) {
        if(refreshItem == null) return;
        if(loading) {
            refreshItem.setActionView(R.layout.actionbar_refresh_progress);
        } else {
            refreshItem.setActionView(null);
        }
    }
}
