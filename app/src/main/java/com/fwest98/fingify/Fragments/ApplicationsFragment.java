package com.fwest98.fingify.Fragments;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.fwest98.fingify.Activities.ApplicationDetailActivity;
import com.fwest98.fingify.Adapters.NewApplicationsAdapter;
import com.fwest98.fingify.Data.ApplicationManager;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.Helpers.SortableRecyclerViewCallback;
import com.fwest98.fingify.Helpers.TotpCountdown;
import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.R;

import java.util.ArrayList;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class ApplicationsFragment extends Fragment implements NewApplicationFragment.onResultListener {
    private static final String ISREADY_KEY = "isReady";
    private static final String ARRAY_KEY = "array";

    private TotpCountdown countdown;
    private ArrayList<Application> applications;
    private boolean awaitingFingerprints = false;
    private boolean isSetup = false;

    private ApplicationsFragmentCallbacks callbacks = null;

    private RecyclerView recyclerView;
    private NewApplicationsAdapter applicationsAdapter;
    private LinearLayoutManager layoutManager;
    private SortableRecyclerViewCallback sortableRecyclerViewCallback;
    private AppCompatSpinner sortingSpinner;
    private FabSpeedDial fabSpeedDial;

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

        applications = ApplicationManager.getApplications(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_applications, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        // RecyclerView content manager
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // Content
        applicationsAdapter = new NewApplicationsAdapter(applications, getActivity());
        applicationsAdapter.addOnItemClickListener((viewHolder, application) -> {
            // Use fragment transition, iets met postpone!!

            /*ApplicationDetailFragment newFragment = new ApplicationDetailFragment();
            TransitionSet enterSet = new TransitionSet();
            enterSet.addTransition(new ChangeBounds());
            enterSet.addTransition(new ChangeTransform());

            TransitionSet exitSet = new TransitionSet();
            exitSet.addTransition(new ChangeBounds());
            exitSet.addTransition(new ChangeTransform());
            setSharedElementReturnTransition(exitSet);

            setExitTransition(new Fade());

            newFragment.setSharedElementEnterTransition(enterSet);
            newFragment.setEnterTransition(new Fade());

            getFragmentManager().beginTransaction()
                    .add(R.id.container, newFragment)
                    .addSharedElement(viewHolder.itemView.findViewById(R.id.application_item), "box")
                    .addSharedElement(viewHolder.applicationName, "title")
                    .*/

            Intent i = new Intent(getActivity(), ApplicationDetailActivity.class);

            Pair<View, String> boxPair = Pair.create(viewHolder.itemView.findViewById(R.id.application_item), "box");
            Pair<View, String> titlePair = Pair.create(viewHolder.applicationName, "title");

            ActivityOptions transition = ActivityOptions.makeSceneTransitionAnimation(getActivity(), titlePair, boxPair);
            startActivity(i, transition.toBundle());
        });
        recyclerView.setAdapter(applicationsAdapter);

        // Touch helper for sorting
        sortableRecyclerViewCallback = new SortableRecyclerViewCallback(applicationsAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(sortableRecyclerViewCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        // Lines between applications
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Sorting of applications
        sortingSpinner = (AppCompatSpinner) rootView.findViewById(R.id.spinner);
        ArrayAdapter<String> sortingAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.spinner_title,
                NewApplicationsAdapter.sortings.keySet().toArray(new String[]{})
        );
        sortingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortingSpinner.setAdapter(sortingAdapter);
        sortingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int newSorting = NewApplicationsAdapter.sortings.get(sortingSpinner.getSelectedItem());
                applicationsAdapter.setSorting(newSorting);
                sortableRecyclerViewCallback.setAllowsDrag(newSorting == NewApplicationsAdapter.SORTING_CUSTOM);
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Floating Action Button Speed Dial
        fabSpeedDial = (FabSpeedDial) rootView.findViewById(R.id.fab_speeddial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                // Handle clicks
                NewApplicationFragment fragment;
                switch(menuItem.getItemId()) {
                    case R.id.application_add_code:
                        fragment = NewApplicationFragment.newInstance(ApplicationsFragment.this, NewApplicationFragment.AddMode.CODE);
                        fragment.show(getFragmentManager(), "dialog");
                        return true;
                    case R.id.application_add_scan:
                        fragment = NewApplicationFragment.newInstance(ApplicationsFragment.this, NewApplicationFragment.AddMode.SCAN);
                        fragment.show(getFragmentManager(), "dialog");
                        return true;
                }
                return super.onMenuItemSelected(menuItem);
            }
        });

        return rootView;
    }

    @Override
    public void onResult(Application newApplication) {
        applicationsAdapter.addApplication(newApplication);
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
        /*LinearLayout emptyLayout = new LinearLayout(getActivity());
        emptyLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        emptyLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);

        TextView emptyText = new TextView(getActivity());
        emptyText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyText.setText(R.string.fingerprint_authentication_failed);
        emptyText.setTextSize(22);

        Button retryButton = new Button(getActivity());
        retryButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        retryButton.setText(R.string.common_tryagain);
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
        callbacks.onDisable();*/
    }

    private void createApplicationsList() {
        /* Create dummy application list */
        /*applications = ApplicationManager.getApplications(getActivity());

        setListAdapter(new ApplicationsAdapter(getActivity(),
                R.layout.application_list_item, applications));

        startCountdown();*/
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
        //if(!isSetup) validateFingerprint();

        /*ListView listView = getListView();
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
                        *//* The user wants to edit an application *//*
                        if (((ApplicationsAdapter) getListAdapter()).getCheckedCount() > 1) { // This is not supported
                            ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.fragment_applications_context_edit_error_nomultiple)), getActivity(), false);
                            return false;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.dialog_editapplication_title)
                                .setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_editapplication, null))
                                .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
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

                            if (ApplicationManager.labelExists(applicationName, getActivity())) {
                                // Label exists
                                ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.dialog_newapplication_error_duplicateLabel)), getActivity(), false);
                                return;
                            }

                            Application oldApplication = ((ApplicationsAdapter) getListAdapter()).getCheckedApplications().get(0);
                            Application newApplication = new Application(applicationName, oldApplication.getSecret(), oldApplication.getUser());

                            ApplicationManager.updateApplication(oldApplication, newApplication, getActivity());

                            reCreateApplicationsList();
                            dialog.dismiss();
                            mode.finish();
                        });

                        return true;
                    }

                    case R.id.fragment_applications_actions_delete: {
                        *//* The user wants to delete (an) application(s) *//*
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.dialog_removeapplication_title)
                                .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
                                })
                                .setPositiveButton(R.string.dialog_removeapplication_submit, (dialog, which) -> {
                                    List<Application> applicationsToRemove = ((ApplicationsAdapter) getListAdapter()).getCheckedApplications();

                                    ApplicationManager.removeApplications(applicationsToRemove, getActivity());

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
        });*/
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
        /*ApplicationsAdapter applicationsAdapter = (ApplicationsAdapter) getListAdapter();
        if(applicationsAdapter != null) applicationsAdapter.updateViews();*/
    }

    //region Interfaces

    public static interface ApplicationsFragmentCallbacks {
        public void onDisable();
        public void onEnable();
    }

    //endregion
}
