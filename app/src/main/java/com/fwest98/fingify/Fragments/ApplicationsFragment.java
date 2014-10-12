package com.fwest98.fingify.Fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;

import com.fwest98.fingify.Adapters.ApplicationsAdapter;
import com.fwest98.fingify.Data.Application;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.Helpers.TotpCountdown;
import com.fwest98.fingify.R;

import org.jboss.aerogear.security.otp.api.Base32;

import java.util.ArrayList;

public class ApplicationsFragment extends ListFragment {
    private static final String ISREADY_KEY = "isReady";
    private static final String ARRAY_KEY = "array";

    private TotpCountdown countdown;
    private boolean isReady = false;
    private ArrayList<Application> applications;

    //region Setup

    public static ApplicationsFragment newInstance() {
        ApplicationsFragment fragment = new ApplicationsFragment();

        return fragment;
    }

    public ApplicationsFragment() {
    }

    //endregion
    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            isReady = savedInstanceState.getBoolean(ISREADY_KEY, false);
            applications = (ArrayList<Application>) savedInstanceState.getSerializable(ARRAY_KEY);
        }

        if(!isReady) {
        /* Verify fingerprint if needed */
            FingerprintManager.authenticate(getActivity(), result -> {
                if (result == FingerprintManager.FingerprintResponses.NOT_SUPPORTED) {
                    // Show a toast for information
                    ExceptionHandler.handleException(new Exception(getString(R.string.fingerprint_authentication_not_supported)), getActivity(), true);
                    isReady = true;
                    createApplicationsList();
                } else if (result == FingerprintManager.FingerprintResponses.DISABLED) {
                    isReady = true;
                    createApplicationsList();
                } else if (result == FingerprintManager.FingerprintResponses.FAILED) {
                    setEmptyText(getString(R.string.fingerprint_authentication_failed));
                } else {
                    isReady = true;
                    createApplicationsList();
                }
            });
        } else {
            setListAdapter(new ApplicationsAdapter(getActivity(), R.layout.application_list_item, applications));
        }
    }

    private void createApplicationsList() {
        /* Create dummy application list */
        applications = new ArrayList<>();
        applications.add(new Application("Test", Base32.random()));
        applications.add(new Application("Test2", Base32.random()));
        applications.add(new Application("Test", Base32.random()));
        applications.add(new Application("Test2", Base32.random()));
        applications.add(new Application("Test", Base32.random()));
        applications.add(new Application("Test2", Base32.random()));
        applications.add(new Application("Test", Base32.random()));
        applications.add(new Application("Test2", Base32.random()));
        applications.add(new Application("Test", Base32.random()));
        applications.add(new Application("Test2", Base32.random()));

        setListAdapter(new ApplicationsAdapter(getActivity(),
                R.layout.application_list_item, applications));

        startCountdown();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(isReady) startCountdown();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();
        if(isReady) stopCountdown();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ISREADY_KEY, isReady);
        outState.putSerializable(ARRAY_KEY, applications);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void startCountdown() {
        stopCountdown();
        countdown = new TotpCountdown(50);
        countdown.setListener(() -> updateViews());
        countdown.startAndNotifyListener();
    }

    private void stopCountdown() {
        if(countdown == null) return;
        countdown.stop();
        countdown = null;
    }

    //endregion

    private void updateViews() {
        ApplicationsAdapter applicationsAdapter = (ApplicationsAdapter) getListAdapter();
        if(applicationsAdapter != null) applicationsAdapter.updateViews();
    }
}
