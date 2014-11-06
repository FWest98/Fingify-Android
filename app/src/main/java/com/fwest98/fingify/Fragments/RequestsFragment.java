package com.fwest98.fingify.Fragments;

import android.app.ListFragment;
import android.os.Bundle;

import com.fwest98.fingify.Adapters.RequestsAdapter;
import com.fwest98.fingify.Data.Account;
import com.fwest98.fingify.Data.Request;
import com.fwest98.fingify.R;

import java.util.ArrayList;
import java.util.Calendar;

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
        Account.getInstance(getActivity()).getRequests(data -> onRequestsLoaded((ArrayList<Request>) data), ex -> {
            // Set empty view
            ArrayList<Request> requests = new ArrayList<>();
            requests.add(new Request("Some accepted", Calendar.getInstance().getTime(), true, false, true));
            requests.add(new Request("Some todo", Calendar.getInstance().getTime(), false, true, false));
            requests.add(new Request("Some todo", Calendar.getInstance().getTime(), true, true, false));
            setListAdapter(new RequestsAdapter(getActivity(), R.layout.request_list_item, requests));
        });
    }

    public void reCreateRequestsList() {
        createRequestsList();
    }
}
