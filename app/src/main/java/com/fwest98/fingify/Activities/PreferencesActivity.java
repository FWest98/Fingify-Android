package com.fwest98.fingify.Activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.fwest98.fingify.Fragments.AboutLibsFragment;
import com.fwest98.fingify.Fragments.AccountPreferencesFragment;
import com.fwest98.fingify.Fragments.AppPreferencesFragment;
import com.fwest98.fingify.Fragments.ExportApplicationsFragment;
import com.fwest98.fingify.R;

import java.util.List;

public class PreferencesActivity extends PreferenceActivity {
    private Toolbar toolbar;

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return AppPreferencesFragment.class.getName().equals(fragmentName) ||
                AboutLibsFragment.class.getName().equals(fragmentName) ||
                AccountPreferencesFragment.class.getName().equals(fragmentName) ||
                ExportApplicationsFragment.class.getName().equals(fragmentName);
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText(getTitle());
        root.addView(toolbar, 0);

        toolbar.setNavigationOnClickListener((v) -> finish());
    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        final View result = super.onCreateView(name, context, attrs);
        if(result != null) return result;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            switch(name) {
                case "EditText": return new EditText(this, attrs);
                case "Spinner": return new Spinner(this, attrs);
                case "CheckBox": return new CheckBox(this, attrs);
                case "RadioButton": return new RadioButton(this, attrs);
                case "CheckedTextView": return new CheckedTextView(this, attrs);
            }
        }

        return null;
    }
}
