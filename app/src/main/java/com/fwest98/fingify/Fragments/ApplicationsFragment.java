package com.fwest98.fingify.Fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.fwest98.fingify.Adapters.ApplicationsAdapter;
import com.fwest98.fingify.Data.Application;
import com.fwest98.fingify.R;

import org.jboss.aerogear.security.otp.api.Base32;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the
 * interface.
 */
public class ApplicationsFragment extends ListFragment {

    // TODO: Rename and change types of parameters
    public static ApplicationsFragment newInstance() {
        ApplicationsFragment fragment = new ApplicationsFragment();

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ApplicationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<Application> applications = new ArrayList<>();
        applications.add(new Application("Test", Base32.random()));
        applications.add(new Application("Test2", Base32.random()));

        // TODO: Change Adapter to display your content
        setListAdapter(new ApplicationsAdapter(getActivity(),
                R.layout.application_list_item, applications));
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);


    }
}
