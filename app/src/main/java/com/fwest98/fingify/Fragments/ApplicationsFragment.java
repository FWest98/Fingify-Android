package com.fwest98.fingify.Fragments;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fwest98.fingify.Adapters.ApplicationsAdapter;
import com.fwest98.fingify.Data.Account;
import com.fwest98.fingify.Data.Application;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.Helpers.TotpCountdown;
import com.fwest98.fingify.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApplicationsFragment extends ListFragment {
    private static final String ISREADY_KEY = "isReady";
    private static final String ARRAY_KEY = "array";

    private TotpCountdown countdown;
    private ArrayList<Application> applications;
    private boolean awaitingFingerprints = false;
    private boolean isSetup = false;

    private ApplicationsFragmentCallbacks callbacks = null;

    //region Setup

    public static ApplicationsFragment newInstance(ApplicationsFragmentCallbacks callbacks) {
        ApplicationsFragment fragment = new ApplicationsFragment();
        fragment.callbacks = callbacks;

        return fragment;
    }

    public ApplicationsFragment() {
        callbacks = new ApplicationsFragmentCallbacks() {
            @Override
            public void onDisable() {

            }

            @Override
            public void onEnable() {

            }
        };
    }

    //endregion
    //region Lifecycle


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbacks.onDisable();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No accounts yet! Get started and create a new one!");

        //validateFingerprint();
    }

    private void validateFingerprint() {
        /* Verify fingerprint if needed */
        awaitingFingerprints = true;
        FingerprintManager.authenticate(getActivity(), result -> {
            awaitingFingerprints = false;
            callbacks.onEnable();
            if (result == FingerprintManager.FingerprintResponses.NOT_SUPPORTED) {
                // Show a toast for information
                ExceptionHandler.handleException(new Exception(getString(R.string.fingerprint_authentication_not_supported)), getActivity(), true);
                createApplicationsList();
            } else if (result == FingerprintManager.FingerprintResponses.DISABLED) {
                createApplicationsList();
            } else if (result == FingerprintManager.FingerprintResponses.FAILED) {
                showFailedVerification();
            } else {
                createApplicationsList();
            }
        });
    }

    private void showFailedVerification() {
        /* Create view to notify the user of the failed authentication. Provide a retry button, disable further access */
        LinearLayout emptyLayout = new LinearLayout(getActivity());
        emptyLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        emptyLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);

        TextView emptyText = new TextView(getActivity());
        emptyText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyText.setText(R.string.fingerprint_authentication_failed);
        emptyText.setTextSize(22);

        Button retryButton = new Button(getActivity());
        retryButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        retryButton.setText(R.string.fingerprint_authentication_retry);
        retryButton.setOnClickListener(v -> {
            ((ViewGroup) getListView().getParent()).removeView(emptyLayout);
            validateFingerprint();
        });

        emptyLayout.addView(emptyText);
        emptyLayout.addView(retryButton);

        getListView().setEmptyView(emptyLayout);
        setEmptyText("");

        ((ViewGroup) getListView().getParent()).addView(emptyLayout);
        setListAdapter(new ApplicationsAdapter(getActivity(), R.layout.application_list_item, new ArrayList<>()));

        // Hide menu
        callbacks.onDisable();
    }

    private void createApplicationsList() {
        /* Create dummy application list */
        applications = Application.getApplications(getActivity());

        setListAdapter(new ApplicationsAdapter(getActivity(),
                R.layout.application_list_item, applications));

        startCountdown();
    }

    public void reCreateApplicationsList() {
        createApplicationsList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(awaitingFingerprints && isSetup) {
            // The fingerprints crashed.....
            showFailedVerification();
            awaitingFingerprints = false;
        } else if(!awaitingFingerprints && isSetup) {
            startCountdown();
        }
        if(!isSetup) isSetup = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!isSetup) validateFingerprint();

        ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                ((ApplicationsAdapter) getListAdapter()).setChecked(position, checked);
                int count = ((ApplicationsAdapter) getListAdapter()).getCheckedCount();
                if (count > 1) { // More than 1 item -> no edit button
                    mode.getMenu().findItem(R.id.fragment_applications_actions_edit).setVisible(false);
                } else {
                    mode.getMenu().findItem(R.id.fragment_applications_actions_edit).setVisible(true);
                }
                mode.setTitle(count + " " + getActivity().getString(R.string.fragment_applications_context_title));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.applications_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.fragment_applications_actions_edit: {
                        /* The user wants to edit an application */
                        if (((ApplicationsAdapter) getListAdapter()).getCheckedCount() > 1) { // This is not supported
                            ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.fragment_applications_context_edit_error_nomultiple)), getActivity(), false);
                            return false;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.dialog_editapplication_title)
                                .setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_editapplication, null))
                                .setNegativeButton(R.string.dialog_editapplication_cancel, (dialog, which) -> {
                                })
                                .setPositiveButton(R.string.dialog_editapplication_submit, (dialog, which) -> {
                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                            String applicationName = ((EditText) dialog.findViewById(R.id.dialog_editapplication_name)).getText().toString();
                            if ("".equals(applicationName)) {
                                ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.dialog_newapplication_error_noname)), getActivity(), false);
                                return;
                            }

                            if (Application.labelExists(applicationName, getActivity())) {
                                // Label exists
                                ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.dialog_newapplication_error_duplicateLabel)), getActivity(), false);
                                return;
                            }

                            Application oldApplication = ((ApplicationsAdapter) getListAdapter()).getCheckedApplications().get(0);
                            Application.removeApplication(oldApplication, getActivity());

                            Application newApplication = new Application(applicationName, oldApplication.getSecret(), oldApplication.getUser());
                            Application.addApplication(newApplication, getActivity());

                            HashMap<Application, Application> changes = new HashMap<>();
                            changes.put(oldApplication, newApplication);

                            Account.getInstance(getActivity()).updateApplications(changes, result -> {
                            });

                            reCreateApplicationsList();
                            dialog.dismiss();
                            mode.finish();
                        });

                        return true;
                    }

                    case R.id.fragment_applications_actions_delete: {
                        /* The user wants to delete (an) application(s) */
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.dialog_removeapplication_title)
                                .setNegativeButton(R.string.dialog_removeapplication_cancel, (dialog, which) -> {
                                })
                                .setPositiveButton(R.string.dialog_removeapplication_submit, (dialog, which) -> {
                                    List<Application> applicationsToRemove = ((ApplicationsAdapter) getListAdapter()).getCheckedApplications();

                                    for (Application application : applicationsToRemove) {
                                        Application.removeApplication(application, getActivity());
                                    }

                                    Account.getInstance(getActivity()).removeApplications(applicationsToRemove, result -> {
                                    });

                                    ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.dialog_removeapplication_notice)), getActivity(), false);

                                    reCreateApplicationsList();
                                    mode.finish();
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                    }
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                ((ApplicationsAdapter) getListAdapter()).unCheckAll();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCountdown();
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

    //region Interfaces

    public static interface ApplicationsFragmentCallbacks {
        public void onDisable();
        public void onEnable();
    }

    //endregion
}
