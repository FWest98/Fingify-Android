package com.fwest98.fingify;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.fwest98.fingify.Data.Account;
import com.fwest98.fingify.Data.Application;
import com.fwest98.fingify.Data.Request;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.Services.GCMIntentService;

import java.util.Calendar;

import lombok.Setter;

public class VerifyCodeRequestActivity extends Activity {
    public static final String INTENT_ACCEPT = "CODE_REQUEST_ACCEPT";
    public static final String INTENT_REJECT = "CODE_REQUEST_REJECT";

    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if(intent.getStringExtra("applicationName") == null || (application = Application.getApplication(intent.getStringExtra("applicationName"), this)) == null) {
            ExceptionHandler.handleException(new Exception(getString(R.string.activity_verifycoderequest_noapplication)), this, true);
            GCMIntentService.removeNotification(this);
            finish();
        }

        if(intent.getAction() == INTENT_ACCEPT) {
            handle(true);
            return;
        } else if(intent.getAction() == INTENT_REJECT) {
            handle(false);
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
                                WindowManager.LayoutParams.FLAG_DIM_BEHIND);


        WindowManager.LayoutParams params = getWindow().getAttributes();
        getWindow().setAttributes(params);

        setContentView(R.layout.activity_verifycoderequest);

        if(!HelperFunctions.isScreenLocked(this)) {
            /* Transparent background */
            findViewById(R.id.activity_container).setBackgroundResource(android.R.color.transparent);
        }

        if(savedInstanceState == null) {
            // Show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_verifycoderequest_title);
            builder.setMessage(getString(R.string.fragment_verifycoderequest_requesttext) + ": " + application.getLabel());
            builder.setNegativeButton(R.string.fragment_requests_list_item_button_reject, (dialog, which) -> {});
            builder.setPositiveButton(R.string.fragment_requests_list_item_button_accept, (dialog, which) -> {});

            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> handle(true));
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> handle(false));

            /*
            VerifyCodeRequestFragment fragment = VerifyCodeRequestFragment.newInstance(new VerifyCodeRequestFragment.stateListener() {
                @Override
                public void onStopped() {
                    finish();
                }

                @Override
                public void onReject() {
                    handle(false);
                }

                @Override
                public void onAccept() {
                    handle(true);
                }
            }, application);
            getFragmentManager().beginTransaction().add(fragment, "dialog").commit();*/
        }
    }

    private void handle(boolean accept) {
        FingerprintManager.authenticate(this, s -> {
            if(s == FingerprintManager.FingerprintResponses.FAILED) {
                // Oh noes.......
                ExceptionHandler.handleException(new Exception(getString(R.string.fingerprint_authentication_failed_tryagain)), this, false);
            } else {
                // Handle response
                Request request = new Request(application.getLabel(), Calendar.getInstance().getTime(), false, true, false);
                Account.getInstance(this).handleRequest(accept, request, data -> {
                    // Finish activity and remove notification
                    GCMIntentService.removeNotification(this);
                    finish();
                }, exception -> {
                    ExceptionHandler.handleException((Exception) exception, this, true);
                });
            }
        });
    }

    public static class VerifyCodeRequestFragment extends DialogFragment {
        @Setter private stateListener listener = new stateListener() {
            @Override
            public void onStopped() {

            }

            @Override
            public void onReject() {

            }

            @Override
            public void onAccept() {

            }
        };
        private Application application = new Application("none", "", "");

        public static VerifyCodeRequestFragment newInstance(stateListener listener, Application application) {
            VerifyCodeRequestFragment fragment = new VerifyCodeRequestFragment();
            fragment.setListener(listener);
            fragment.application = application;

            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.setTitle(R.string.dialog_verifycoderequest_title);
            return dialog;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            View mainView = inflater.inflate(R.layout.fragment_verifycoderequest, container, false);

            mainView.findViewById(R.id.fragment_verifycoderequest_accept).setOnClickListener(v -> listener.onAccept());
            mainView.findViewById(R.id.fragment_verifycoderequest_reject).setOnClickListener(v -> listener.onReject());

            //((TextView) mainView.findViewById(R.id.fragment_verifycoderequest_text)).setText(getString(R.string.fragment_verifycoderequest_requesttext) + ": " + application.getLabel());

            return mainView;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            listener.onStopped();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            listener.onStopped();
        }

        public interface stateListener {
            void onStopped();
            void onReject();
            void onAccept();
        }
    }
}