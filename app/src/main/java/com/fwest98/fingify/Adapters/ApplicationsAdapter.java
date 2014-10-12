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
    private List<View> rowViews;

    public ApplicationsAdapter(Context context, int resource, List<Application> objects) {
        super(context, resource, objects);
        this.context = context;
        this.applications = objects;
        this.TOTPs = new ArrayList<>();
        for(Application application : applications) {
            TOTPs.add(new ExtendedTotp(application.getSecret(), new ExtendedClock()));
        }
        this.rowViews = new ArrayList<>(objects.size());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.application_list_item, parent, false);
        TextView title = (TextView) rowView.findViewById(R.id.application_item_label);
        ProgressWheel wheel = (ProgressWheel) rowView.findViewById(R.id.application_item_wheel);
        TextView code = (TextView) rowView.findViewById(R.id.application_item_code);

        title.setText(applications.get(position).getLabel());
        code.setText(TOTPs.get(position).now());
        wheel.setProgress((int) (TOTPs.get(position).getTimeLeft() * 100));

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
}
