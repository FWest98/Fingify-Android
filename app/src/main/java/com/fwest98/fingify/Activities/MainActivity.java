package com.fwest98.fingify.Activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.fwest98.fingify.Data.AccountManager;
import com.fwest98.fingify.Fragments.ApplicationsFragment;
import com.fwest98.fingify.Fragments.NewApplicationFragment;
import com.fwest98.fingify.Fragments.RequestsFragment;
import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.R;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;


public class MainActivity extends BaseActivity implements NewApplicationFragment.onResultListener, ApplicationsFragment.ApplicationsFragmentCallbacks, RequestsFragment.onLoadStateChangedListener {
    private Menu menu;
    private MenuItem refreshItem;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private Fragment currentFragment;
    private long drawerSelection = 0;

    private Drawer drawer;
    private AccountHeader drawerHeader;
    private ProfileDrawerItem profileDrawerItem;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_applications);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        AccountManager.initialize(this);

        if(savedInstanceState == null) {
            // Defaults
            currentFragment = ApplicationsFragment.newInstance(this);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, currentFragment)
                    .commit();
        } else {
            currentFragment = getFragmentManager().findFragmentById(R.id.container);
            drawerSelection = savedInstanceState.getLong("drawerSelection");
        }

        /* Navigation Drawer */
        if(AccountManager.isSet()) {
            profileDrawerItem = new ProfileDrawerItem().withName(AccountManager.getUsername());
        } else {
            profileDrawerItem = new ProfileDrawerItem().withName("Not logged in");
        }

        drawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(profileDrawerItem)
                .withCompactStyle(!AccountManager.isSet())
                .withOnAccountHeaderListener((a, b, c) -> false)
                .withSavedInstance(savedInstanceState)
                .withProfileImagesVisible(false)
                .withSelectionListEnabled(false)
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(drawerHeader)
                .withCloseOnClick(true)
                .withActionBarDrawerToggleAnimated(true)
                .withSavedInstance(savedInstanceState)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if(drawerItem == null) return false;

                    drawer.closeDrawer();
                    drawerSelection = drawerItem.getIdentifier();

                    return true;
                })
                .addDrawerItems(
                        new PrimaryDrawerItem()
                            .withName(R.string.activity_applications_tab_accounts)
                            .withIdentifier(1)
                            .withIcon(GoogleMaterial.Icon.gmd_vpn_key)
                            .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                currentFragment = ApplicationsFragment.newInstance(this);
                                getFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
                                return true;
                            }),
                        new PrimaryDrawerItem()
                            .withName(R.string.activity_applications_tab_requests)
                            .withIdentifier(2)
                            .withIcon(GoogleMaterial.Icon.gmd_notifications)
                            .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                currentFragment = RequestsFragment.newInstance(this);
                                getFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
                                return true;
                            }),
                        new PrimaryDrawerItem()
                            .withName("Devices")
                            .withIdentifier(3)
                            .withIcon(GoogleMaterial.Icon.gmd_devices),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                            .withName(R.string.activity_applications_action_settings)
                            .withIdentifier(4)
                            .withIcon(GoogleMaterial.Icon.gmd_settings)
                            .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                drawer.setSelection(drawerSelection);
                                Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
                                startActivity(preferencesIntent);
                                return true;
                            })
                )
                .build();

        drawer.setSelection(drawerSelection);
        drawerLayout = drawer.getDrawerLayout();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.material_drawer_open, R.string.material_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        View.OnClickListener navigationOnClickListener = view -> {
            if (drawer.isDrawerOpen()) {
                drawer.closeDrawer();
            } else {
                drawer.openDrawer();
            }
        };

        toolbar.setNavigationOnClickListener(navigationOnClickListener);
        actionBarDrawerToggle.setToolbarNavigationClickListener(navigationOnClickListener);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        /*if(!PreferenceManager.getDefaultSharedPreferences(this).contains(Constants.FINGERPRINT_AUTHENTICATION_SETTING) &&
                FingerprintManager.isFingerPrintSupported(this)) {
            // Show Dialog that it's strongly adviced to enable fingerprints
            FingerprintManager.createAdviceDialog(this);
        }*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = drawer.saveInstanceState(outState);
        outState = drawerHeader.saveInstanceState(outState);

        outState.putLong("drawerSelection", drawerSelection);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        menu.findItem(R.id.activity_applications_action_newapplication).setVisible(true);

        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
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
                NewApplicationFragment fragment = NewApplicationFragment.newInstance(this, NewApplicationFragment.AddMode.SCAN);
                fragment.show(getFragmentManager(), "dialog");
                return true;
            case R.id.activity_applications_action_refresh:
                if(currentFragment instanceof RequestsFragment)
                    ((RequestsFragment) currentFragment).reCreateRequestsList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult(Application newApplication) {
        if(currentFragment instanceof ApplicationsFragment)
            ((ApplicationsFragment) currentFragment).reCreateApplicationsList();
    }

    @Override
    public void onDisable() {
        if(menu == null) return;
        for(int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
    }

    @Override
    public void onEnable() {
        if(menu == null) return;
        for(int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(true);
        }
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
