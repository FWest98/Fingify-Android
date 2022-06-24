package com.fwest98.fingify.Adapters;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fwest98.fingify.Helpers.SortableRecyclerViewCallback;
import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.R;
import com.fwest98.fingify.Settings.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

public class NewApplicationsAdapter extends RecyclerView.Adapter<NewApplicationsAdapter.ViewHolder> implements SortableRecyclerViewCallback.ItemTouchHelperAdapter {
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView applicationName;
        private OnClickListener listener = (x) -> {};

        public ViewHolder(View itemView) {
            super(itemView);
            this.applicationName = (TextView) itemView.findViewById(R.id.application_item_label);
            itemView.setOnClickListener(this);
        }

        protected void setOnClickListener(OnClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            listener.onClick(this);
        }

        protected interface OnClickListener {
            void onClick(ViewHolder v);
        }
    }

    private List<Application> applications;
    @Getter private int currentSorting;
    private Context context;
    private ArrayList<Integer> customOrder;
    private ArrayList<OnItemClickListener> listeners = new ArrayList<>();
    private ViewHolder.OnClickListener itemClickListener = v -> {
        for(OnItemClickListener listener : listeners) {
            Application application = applications.get(v.getAdapterPosition());
            listener.onItemClick(v, application);
        }
    };

    public static final ArrayMap<String, Integer> sortings = new ArrayMap<String, Integer>(3) {{
        put("A-Z", SORTING_A_Z);
        put("Z-A", SORTING_Z_A);
        put("Custom", SORTING_CUSTOM);
    }};
    public static final int SORTING_A_Z = 1;
    public static final int SORTING_Z_A = 2;
    public static final int SORTING_CUSTOM = 3;
    private final ArrayMap<Integer, Comparator<? super Application>> sortComparators = new ArrayMap<Integer, Comparator<? super Application>>(3) {{
        put(SORTING_A_Z, (lhs, rhs) -> lhs.getLabel().compareTo(rhs.getLabel()));
        put(SORTING_Z_A, (lhs, rhs) -> -1 * lhs.getLabel().compareTo(rhs.getLabel()));
        put(SORTING_CUSTOM, (lhs, rhs) -> customOrder.indexOf(lhs.getId()) - customOrder.indexOf(rhs.getId()));
    }};

    public NewApplicationsAdapter(List<Application> applications, Context context) {
        this.applications = applications;
        this.context = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> stringOrder = Arrays.asList(prefs.getString(Constants.APPLICATIONS_CUSTOM_ORDER, "").split(","));
        customOrder = new ArrayList<>();
        for(String item : stringOrder) {
            if(item == null || item.isEmpty()) continue;
            customOrder.add(Integer.parseInt(item));
        }
        for(Application application : applications) {
            if(!customOrder.contains(application.getId())) customOrder.add(application.getId());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.application_overview, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Application application = applications.get(position);
        holder.applicationName.setText(application.getLabel());
        holder.setOnClickListener(itemClickListener);
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if(fromPosition < toPosition) {
            for(int i = fromPosition; i < toPosition; i++) {
                Collections.swap(applications, i, i + 1);
            }
        } else {
            for(int i = fromPosition; i > toPosition; i--) {
                Collections.swap(applications, i, i - 1);
            }
        }
        int temp = customOrder.get(fromPosition);
        customOrder.set(fromPosition, customOrder.get(toPosition));
        customOrder.set(toPosition, temp);

        notifyItemMoved(fromPosition, toPosition);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(Constants.APPLICATIONS_CUSTOM_ORDER, TextUtils.join(",", customOrder)).apply();
    }

    public void addOnItemClickListener(OnItemClickListener listener) {
        this.listeners.add(listener);
    }

    public void setSorting(int sorting) {
        currentSorting = sorting;
        Collections.sort(applications, sortComparators.get(sorting));
        notifyDataSetChanged();
    }

    public void addApplication(Application newApplication) {
        applications.add(newApplication);
        customOrder.add(newApplication.getId()); // add current at the end of custom ordering
        Collections.sort(applications, sortComparators.get(currentSorting));
        int newIndex = applications.indexOf(newApplication);
        notifyItemInserted(newIndex);
    }

    public interface OnItemClickListener {
        void onItemClick(ViewHolder viewHolder, Application application);
    }
}
