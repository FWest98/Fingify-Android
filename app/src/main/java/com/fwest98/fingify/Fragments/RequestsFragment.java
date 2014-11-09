package com.fwest98.fingify.Fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fwest98.fingify.Adapters.RequestsAdapter;
import com.fwest98.fingify.Data.Account;
import com.fwest98.fingify.Data.Request;
import com.fwest98.fingify.R;

import java.util.ArrayList;

public class RequestsFragment extends ListFragment {
    private ArrayList<Request> requests = new ArrayList<>();

    //region Setup

    public static RequestsFragment newInstance() {
        RequestsFragment fragment = new RequestsFragment();

        return fragment;
    }

    public RequestsFragment() {

    }

    //endregion
    //region Lifecycle

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getActivity().getString(R.string.fragment_requests_empty));

        createRequestsList();
    }



    @Override
    public void onStart() {
        super.onStart();
    }

    private void onRequestsLoaded(ArrayList<Request> requests) {
        this.requests = requests;
        setListAdapter(new RequestsAdapter(getActivity(), R.layout.request_list_item, requests));
    }

    //endregion

    private void createRequestsList() {
        if(!Account.isSet()) {
            showLoginRequired();
            return;
        }
        Account.getInstance(getActivity()).getRequests(data -> onRequestsLoaded((ArrayList<Request>) data), ex -> {
            // Set empty view
            setListAdapter(new RequestsAdapter(getActivity(), R.layout.request_list_item, new ArrayList<>()));
        });
    }

    private void showLoginRequired() {
        /* Create view to notify the user to log in */
        LinearLayout loginLayout = new LinearLayout(getActivity());
        loginLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loginLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        loginLayout.setOrientation(LinearLayout.VERTICAL);

        TextView loginText = new TextView(getActivity());
        loginText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        loginText.setText(R.string.fragment_requests_login_required);
        loginText.setTextSize(22);

        Button loginButton = new Button(getActivity());
        loginButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        loginButton.setText(R.string.common_login);
        loginButton.setOnClickListener(v -> {
            Account.getInstance(getActivity()).login(d -> {
                ((ViewGroup) getListView().getParent()).removeView(loginLayout);
                setEmptyText(getActivity().getString(R.string.fragment_requests_empty));
            });
        });

        loginLayout.addView(loginText);
        loginLayout.addView(loginButton);

        getListView().setEmptyView(loginButton);
        setEmptyText("");

        ((ViewGroup) getListView().getParent()).addView(loginLayout);
        setListAdapter(new RequestsAdapter(getActivity(), R.layout.application_list_item, new ArrayList<>()));
    }

    public void reCreateRequestsList() {
        createRequestsList();
    }
}
