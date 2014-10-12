package com.fwest98.fingify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fwest98.fingify.CustomUI.ProgressWheel;
import com.fwest98.fingify.Data.Application;
import com.fwest98.fingify.Helpers.ExtendedClock;
import com.fwest98.fingify.Helpers.ExtendedTotp;
import com.fwest98.fingify.R;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsAdapter extends ArrayAdapter<Application> {
    private Context context;
    private List<Application> applications;
    private List<ExtendedTotp> TOTPs;

    public ApplicationsAdapter(Context context, int resource, List<Application> objects) {
        super(context, resource, objects);
        this.context = context;
        this.applications = objects;
        this.TOTPs = new ArrayList<>();
        for(Application application : applications) {
            TOTPs.add(new ExtendedTotp(application.getSecret(), new ExtendedClock()));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.application_list_item, parent, false);
        TextView title = (TextView) rowView.findViewById(R.id.application_item_label);
        ProgressWheel wheel = (ProgressWheel) rowView.findViewById(R.id.application_item_wheel);

        title.setText(applications.get(position).getLabel());
        wheel.setProgress((int) (TOTPs.get(position).getClock().getTimeLeft() * 100));

        return rowView;
    }
}
