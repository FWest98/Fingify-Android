package com.fwest98.fingify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fwest98.fingify.CustomUI.ProgressWheel;
import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.Helpers.ExtendedClock;
import com.fwest98.fingify.Helpers.ExtendedTotp;
import com.fwest98.fingify.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fj.data.Array;

public class ApplicationsAdapter extends ArrayAdapter<Application> {
    private Context context;
    private List<Application> applications;
    private List<ExtendedTotp> TOTPs;
    private List<View> rowViews;
    private List<Boolean> checked;

    public ApplicationsAdapter(Context context, int resource, List<Application> objects) {
        super(context, resource, objects);
        this.context = context;
        this.applications = objects;
        this.TOTPs = new ArrayList<>();
        for(Application application : applications) {
            TOTPs.add(new ExtendedTotp(application.getSecret(), new ExtendedClock()));
        }
        this.rowViews = new ArrayList<>(objects.size());
        Boolean[] falses = new Boolean[objects.size()];
        Arrays.fill(falses, false);
        this.checked = Arrays.asList(falses);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = (convertView != null) ? convertView : inflater.inflate(R.layout.application_list_item, parent, false);
        TextView title = (TextView) rowView.findViewById(R.id.application_item_label);
        ProgressWheel wheel = (ProgressWheel) rowView.findViewById(R.id.application_item_wheel);
        TextView code = (TextView) rowView.findViewById(R.id.application_item_code);

        title.setText(applications.get(position).getLabel());
        code.setText(TOTPs.get(position).now());
        wheel.setProgress((int) (TOTPs.get(position).getTimeLeft() * 100));

        if(checked.get(position)) {
            rowView.setBackgroundResource(android.R.color.holo_blue_dark);
        }

        rowViews.add(position, rowView);

        return rowView;
    }

    public void updateViews() {
        for(int i = 0; i < rowViews.size() && i < TOTPs.size(); i++) {
            ExtendedTotp totp = TOTPs.get(i);
            View rowView = rowViews.get(i);

            if(totp.isChanged()) {
                ((TextView) rowView.findViewById(R.id.application_item_code)).setText(totp.now());
            }

            ((ProgressWheel) rowView.findViewById(R.id.application_item_wheel)).setProgress((int) (totp.getTimeLeft() * 100));
        }
    }

    public void setChecked(int position, boolean isChecked) {
        checked.set(position, isChecked);
        View rowView = rowViews.get(position);

        if(isChecked) {
            rowView.setBackgroundResource(android.R.color.holo_blue_dark);
        } else {
            rowView.setBackgroundResource(android.R.color.background_light);
        }
    }

    public int getCheckedCount() {
        return Array.iterableArray(checked).filter(s -> s).length();
    }

    public void unCheckAll() {
        Boolean[] falses = new Boolean[checked.size()];
        Arrays.fill(falses, false);
        checked = Arrays.asList(falses);
    }

    public List<Application> getCheckedApplications() {
        List<Application> list = new ArrayList<>();
        for(int i = 0; i < checked.size(); i++) {
            if(checked.get(i)) {
                list.add(applications.get(i));
            }
        }
        return list;
    }
}
