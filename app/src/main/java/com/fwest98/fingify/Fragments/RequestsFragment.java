package com.fwest98.fingify.Fragments;

import android.app.ListFragment;
import android.os.Bundle;

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
        setEmptyText("No requests found");

        Account.getInstance(getActivity()).getRequests(data -> onRequestsLoaded((ArrayList<Request>) data), ex -> {
            // Set empty view
            setListAdapter(new RequestsAdapter(getActivity(), R.layout.application_list_item, new ArrayList<>()));
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void onRequestsLoaded(ArrayList<Request> requests) {
        this.requests = requests;
    }

    //endregion
}
