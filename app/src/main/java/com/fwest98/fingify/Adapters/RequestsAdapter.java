package com.fwest98.fingify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fwest98.fingify.Data.Request;
import com.fwest98.fingify.R;

import java.util.List;

public class RequestsAdapter extends ArrayAdapter<Request> {
    private Context context;
    private List<Request> requests;
    private int resource;

    public RequestsAdapter(Context context, int resource, List<Request> objects) {
        super(context, resource, objects);
        this.context = context;
        this.requests = objects;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.request_list_item, parent, false);
        TextView title = (TextView) rowView.findViewById(R.id.request_item_name);
        TextView dateDevice = (TextView) rowView.findViewById(R.id.request_item_date_and_device);
        ImageView state = (ImageView) rowView.findViewById(R.id.request_item_state);
        LinearLayout buttons = (LinearLayout) rowView.findViewById(R.id.request_item_buttons);
        Button acceptButton = (Button) buttons.findViewById(R.id.request_item_accept);
        Button rejectButton = (Button) buttons.findViewById(R.id.request_item_reject);

        Request request = requests.get(position);

        title.setText(request.getApplicationName());
        dateDevice.setText((request.isThisDevice() ? "This device" : "Other device") + ", " + request.getRequestTime().toLocaleString());
        if(request.isAnswered() && request.isAccepted()) {
            state.setImageDrawable(context.getResources().getDrawable(R.drawable.state_done));
        } else if(request.isAnswered()) {
            state.setImageDrawable(context.getResources().getDrawable(R.drawable.state_rejected));
        }

        if(request.isAnswered()) {
            buttons.setVisibility(View.GONE);
        } else {
            acceptButton.setOnClickListener(new RequestResponseListener(request, true));
            rejectButton.setOnClickListener(new RequestResponseListener(request, false));
        }

        return rowView;
    }

    public static class RequestResponseListener implements View.OnClickListener {
        private Request request;
        private boolean accept;

        public RequestResponseListener(Request request, boolean accept) {
            this.request = request;
            this.accept = accept;
        }
        @Override
        public void onClick(View v) {

        }
    }
}
