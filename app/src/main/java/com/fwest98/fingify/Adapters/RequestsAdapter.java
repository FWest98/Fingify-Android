package com.fwest98.fingify.Adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.fwest98.fingify.Data.Request;

import java.util.List;

public class RequestsAdapter extends ArrayAdapter<Request> {
    public RequestsAdapter(Context context, int resource, List<Request> objects) {
        super(context, resource, objects);
    }
}
