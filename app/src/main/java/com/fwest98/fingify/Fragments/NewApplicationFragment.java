package com.fwest98.fingify.Fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fwest98.fingify.Data.ApplicationManager;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.R;

import lombok.Setter;
import lombok.SneakyThrows;

public abstract class NewApplicationFragment extends DialogFragment {
    @Setter protected onResultListener listener = (x) -> {};

    public static NewApplicationFragment newInstance(onResultListener listener, AddMode mode) {
        NewApplicationFragment fragment;
        if(mode == AddMode.SCAN) {
            fragment = new NewApplicationScanFragment();
        } else {
            fragment = new NewApplicationCodeFragment();
        }
        fragment.setListener(listener);

        return fragment;
    }

    protected void finishResult(Application parsedQR) {
        if(ApplicationManager.secretExists(parsedQR.getSecret(), getActivity())) {
            // This application already exists. Notify the user
            ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.dialog_newapplication_error_duplicateSecret)), getActivity(), true);
            dismiss();
            return;
        }

        // Validation succeeded, let user enter the name
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_newapplication_name, null);

        ((TextView) dialogView.findViewById(R.id.dialog_newapplication_user)).append(" "+parsedQR.getUser());
        ((EditText) dialogView.findViewById(R.id.dialog_newapplication_name)).setText(parsedQR.getLabel());

        builder.setView(dialogView)
                .setTitle(R.string.dialog_newapplication_input_title)
                .setPositiveButton(R.string.dialog_newapplication_input_submit, (dialog, which) -> {})
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            @SneakyThrows(ApplicationManager.DuplicateApplicationException.class)
            public void onClick(View v) {
                String applicationName = ((EditText) dialogView.findViewById(R.id.dialog_newapplication_name)).getText().toString();

                if ("".equals(applicationName)) {
                    ExceptionHandler.handleException(new Exception(NewApplicationFragment.this.getActivity().getString(R.string.dialog_newapplication_error_noname)), NewApplicationFragment.this.getActivity(), false);
                    return;
                }

                if (ApplicationManager.labelExists(applicationName, NewApplicationFragment.this.getActivity())) {
                    // Label exists
                    ExceptionHandler.handleException(new Exception(NewApplicationFragment.this.getActivity().getString(R.string.dialog_newapplication_error_duplicateLabel)), NewApplicationFragment.this.getActivity(), false);
                    return;
                }

                Application newApplication = new Application(applicationName, parsedQR.getSecret(), parsedQR.getUser());

                ApplicationManager.addApplication(newApplication, NewApplicationFragment.this.getActivity());

                listener.onResult(newApplication);
                dialog.dismiss();
                NewApplicationFragment.this.dismiss();
            }
        });
    }

    public enum AddMode {
        CODE, SCAN
    }

    public interface onResultListener {
        void onResult(Application newApplication);
    }
}
