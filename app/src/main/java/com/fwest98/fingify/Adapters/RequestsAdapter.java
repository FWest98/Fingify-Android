package com.fwest98.fingify.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fwest98.fingify.Data.Account;
import com.fwest98.fingify.Data.Request;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.FingerprintManager;
import com.fwest98.fingify.R;

import java.util.List;

public class RequestsAdapter extends ArrayAdapter<Request> {
    private Activity context;
    private List<Request> requests;

    public RequestsAdapter(Activity context, int resource, List<Request> objects) {
        super(context, resource, objects);
        this.context = context;
        this.requests = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = (convertView == null) ? inflater.inflate(R.layout.request_list_item, parent, false) : convertView;
        TextView title = (TextView) rowView.findViewById(R.id.request_item_name);
        TextView dateDevice = (TextView) rowView.findViewById(R.id.request_item_date_and_device);
        ImageView state = (ImageView) rowView.findViewById(R.id.request_item_state);
        LinearLayout buttons = (LinearLayout) rowView.findViewById(R.id.request_item_buttons);
        Button acceptButton = (Button) buttons.findViewById(R.id.request_item_accept);
        Button rejectButton = (Button) buttons.findViewById(R.id.request_item_reject);

        Request request = requests.get(position);

        title.setText(request.getApplicationName());
        dateDevice.setText((request.isThisDevice() ? context.getString(R.string.fragment_requests_list_item_thisdevice) : context.getString(R.string.fragment_requests_list_item_otherdevice)) + ", " + request.getRequestTime().toLocaleString());
        if(request.isAnswered() && request.isAccepted()) {
            state.setImageDrawable(context.getResources().getDrawable(R.drawable.state_done));
        } else if(request.isAnswered()) {
            state.setImageDrawable(context.getResources().getDrawable(R.drawable.state_rejected));
        }

        if(request.isAnswered() || !request.isThisDevice()) {
            buttons.setVisibility(View.GONE);
        } else {
            acceptButton.setOnClickListener(new RequestResponseListener(request, true, position));
            rejectButton.setOnClickListener(new RequestResponseListener(request, false, position));
        }

        return rowView;
    }

    public class RequestResponseListener implements View.OnClickListener {
        private Request request;
        private boolean accept;
        private int position;

        public RequestResponseListener(Request request, boolean accept, int position) {
            this.request = request;
            this.accept = accept;
            this.position = position;
        }
        @Override
        public void onClick(View v) {
            // Fingerprints if necessary
            FingerprintManager.authenticate(context, s -> {
                if (s == FingerprintManager.FingerprintResponses.FAILED) {
                    // Fingerprint things failed
                    ExceptionHandler.handleException(new Exception(context.getString(R.string.fingerprint_authentication_failed_tryagain)), context, false);
                } else {
                    // Handle response
                    Account.getInstance(context).handleRequest(accept, request, data -> {
                        // Success!
                        // Disable buttons and set state
                        Request newRequest = new Request(request.getApplicationName(), request.getRequestTime(), true, true, accept);
                        requests.set(position, newRequest);
                        notifyDataSetChanged();
                    }, exception -> {
                        ExceptionHandler.handleException((Exception) exception, context, true);
                    });
                }
            });
        }
    }
}
