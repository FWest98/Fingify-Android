package com.fwest98.fingify.Fragments;

import android.os.Bundle;

import com.fwest98.fingify.CustomUI.LibsFragment;
import com.fwest98.fingify.R;
import com.mikepenz.aboutlibraries.Libs;

public class AboutLibsFragment extends LibsFragment {
    public AboutLibsFragment() {
        super();
        Bundle bundle = new Bundle();

        //bundle.putBoolean(Libs.BUNDLE_APP_ABOUT_ICON, true);
        //bundle.putBoolean(Libs.BUNDLE_APP_ABOUT_VERSION, true);
        //bundle.putString(Libs.BUNDLE_APP_ABOUT_DESCRIPTION, R.string.settings_about_app_desc);
        bundle.putInt(Libs.BUNDLE_THEME, R.style.AppTheme);
        bundle.putBoolean(Libs.BUNDLE_VERSION, false);


        bundle.putStringArray(Libs.BUNDLE_FIELDS, Libs.toStringArray(R.string.class.getFields()));
        bundle.putStringArray(Libs.BUNDLE_LIBS, new String[]{"projectlombok", "ormlite", "functionaljava", "aerogearotp"});

        bundle.putBoolean(Libs.BUNDLE_VERSION, true);
        bundle.putBoolean(Libs.BUNDLE_LICENSE, true);

        setArguments(bundle);
    }
}
