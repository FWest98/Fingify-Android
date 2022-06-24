package com.fwest98.fingify.Fragments;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.R;

public class NewApplicationCodeFragment extends NewApplicationFragment {
    private EditText codeEdit;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.dialog_newapplication_code_title);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View mainView = inflater.inflate(R.layout.fragment_newapplication_code, container, false);
        codeEdit = (EditText) mainView.findViewById(R.id.fragment_newapplication_code);

        mainView.findViewById(R.id.fragment_newapplication_cancel).setOnClickListener(v -> dismiss());
        mainView.findViewById(R.id.fragment_newapplication_ok).setOnClickListener(v -> {
            String code = codeEdit.getText().toString();

            if("".equals(code)) {
                ExceptionHandler.handleException(new Exception(getString(R.string.dialog_newapplication_code_required)), getActivity(), false);
                return;
            }
            if(code.length() != 32) {
                ExceptionHandler.handleException(new Exception(getString(R.string.dialog_newapplication_code_length)), getActivity(), false);
                return;
            }

            finishResult(new Application("", code, code));
            dismiss();
        });

        return mainView;
    }
}
