package com.fwest98.fingify.Fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fwest98.fingify.Adapters.RequestsAdapter;
import com.fwest98.fingify.Data.AccountManager;
import com.fwest98.fingify.Data.RequestManager;
import com.fwest98.fingify.Models.Request;
import com.fwest98.fingify.R;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends ListFragment {
    private List<Request> requests = new ArrayList<>();
    private onLoadStateChangedListener listener = new onLoadStateChangedListener() {
        @Override
        public void onLoadStart() {

        }

        @Override
        public void onLoadEnd() {

        }

        @Override
        public void onLoadCancel() {

        }
    };

    //region Setup

    public static RequestsFragment newInstance(onLoadStateChangedListener listener) {
        RequestsFragment fragment = new RequestsFragment();
        fragment.listener = listener;

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

    private void onRequestsLoaded(List<Request> requests) {
        this.requests = requests;
        setListAdapter(new RequestsAdapter(getActivity(), R.layout.request_list_item, requests));
        listener.onLoadEnd();
    }

    //endregion

    private void createRequestsList() {
        if(!AccountManager.isSet()) {
            showLoginRequired();
            return;
        }
        RequestManager.getRequests(result -> {
            if(result.isRight()) {
                onRequestsLoaded(result.right().value());
            } else {
                // Set empty view
                setListAdapter(new RequestsAdapter(getActivity(), R.layout.request_list_item, new ArrayList<>()));
                listener.onLoadEnd();
            }
        }, getActivity());
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
            AccountManager.getInstance(getActivity()).login(getActivity(), d -> {
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
        listener.onLoadStart();
        createRequestsList();
    }

    public interface onLoadStateChangedListener {
        void onLoadStart();
        void onLoadEnd();
        void onLoadCancel();
    }
}
