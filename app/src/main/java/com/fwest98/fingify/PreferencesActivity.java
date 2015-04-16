package com.fwest98.fingify;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.internal.widget.TintCheckBox;
import android.support.v7.internal.widget.TintCheckedTextView;
import android.support.v7.internal.widget.TintEditText;
import android.support.v7.internal.widget.TintRadioButton;
import android.support.v7.internal.widget.TintSpinner;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.fwest98.fingify.Fragments.AboutLibsFragment;
import com.fwest98.fingify.Fragments.AccountPreferencesFragment;
import com.fwest98.fingify.Fragments.AppPreferencesFragment;

import java.util.List;

public class PreferencesActivity extends PreferenceActivity {
    private Toolbar toolbar;

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return AppPreferencesFragment.class.getName().equals(fragmentName) ||
                AboutLibsFragment.class.getName().equals(fragmentName) ||
                AccountPreferencesFragment.class.getName().equals(fragmentName);
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            toolbar.setTitle(getTitle());
            root.addView(toolbar, 0);
        } else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);

            root.removeAllViews();

            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);

            int height;
            TypedValue typedValue = new TypedValue();
            if(getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true)) {
                height = TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
            } else {
                height = toolbar.getHeight();
            }

            content.setPadding(0, height, 0, 0);
            toolbar.setTitle(getTitle());

            root.addView(content);
            root.addView(toolbar);
        }

        toolbar.setNavigationOnClickListener((v) -> finish());
    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        final View result = super.onCreateView(name, context, attrs);
        if(result != null) return result;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            switch(name) {
                case "EditText": return new TintEditText(this, attrs);
                case "Spinner": return new TintSpinner(this, attrs);
                case "CheckBox": return new TintCheckBox(this, attrs);
                case "RadioButton": return new TintRadioButton(this, attrs);
                case "CheckedTextView": return new TintCheckedTextView(this, attrs);
            }
        }

        return null;
    }
}
