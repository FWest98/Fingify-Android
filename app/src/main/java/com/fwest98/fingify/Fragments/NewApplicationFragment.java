package com.fwest98.fingify.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.fwest98.fingify.Data.Account;
import com.fwest98.fingify.Data.Application;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.ExtendedTotp;
import com.fwest98.fingify.R;

import java.util.Arrays;

import lombok.Setter;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class NewApplicationFragment extends DialogFragment implements ZBarScannerView.ResultHandler {
    private ZBarScannerView scannerView;
    @Setter
    private onResultListener listener = () -> {};

    public static NewApplicationFragment newInstance(onResultListener listener) {
        NewApplicationFragment fragment = new NewApplicationFragment();
        fragment.setListener(listener);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.dialog_newapplication_title);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View mainView = inflater.inflate(R.layout.fragment_newapplication, container, false);
        scannerView = (ZBarScannerView) mainView.findViewById(R.id.fragment_newapplication_barcodescanner);

        scannerView.setFormats(Arrays.asList(me.dm7.barcodescanner.zbar.BarcodeFormat.QRCODE));

        mainView.findViewById(R.id.fragment_newapplication_cancel).setOnClickListener(v -> dismiss());
        mainView.findViewById(R.id.fragment_newapplication_noqr).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View dialogView = inflater.inflate(R.layout.dialog_newapplication_code, null);
            builder.setView(dialogView)
                    .setTitle(R.string.dialog_newapplication_code_title)
                    .setPositiveButton("OK", (dialog, which) -> {})
                    .setNegativeButton(R.string.dialog_newapplication_cancel, (dialog, which) -> dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(c -> {
                String code = ((EditText) dialogView.findViewById(R.id.dialog_newapplication_code)).getText().toString();

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
        });

        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
        scannerView.setFlash(false);
        scannerView.setAutoFocus(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        final Application parsedQR;
        try {
            parsedQR = ExtendedTotp.parseUri(result.getContents());
        } catch(IllegalArgumentException e) {
            if(e.getCause() != null && e.getCause() instanceof UnsupportedOperationException) { // HOTP or another code
                // Build AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_newapplication_error_notsupported_text)
                        .setTitle(R.string.dialog_newapplication_error_notsupported_title)
                        .setPositiveButton(R.string.dialog_newapplicatoin_error_notsupported_cancel, (dialog, which) -> this.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Invalid code
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_newapplication_error_invalid_text)
                        .setTitle(R.string.dialog_newapplication_error_invalid_title)
                        .setPositiveButton(R.string.dialog_newapplication_error_invalid_tryAgain, (dialog, which) -> scannerView.startCamera());
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return;
        }

        finishResult(parsedQR);
    }

    private void finishResult(Application parsedQR) {
        if(Application.secretExists(parsedQR.getSecret(), getActivity())) {
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
                .setNegativeButton(R.string.dialog_newapplication_input_cancel, (dialog, which) -> dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String applicationName = ((EditText) dialogView.findViewById(R.id.dialog_newapplication_name)).getText().toString();

            if ("".equals(applicationName)) {
                ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.dialog_newapplication_error_noname)), getActivity(), false);
                return;
            }

            if (Application.labelExists(applicationName, getActivity())) {
                // Label exists
                ExceptionHandler.handleException(new Exception(getActivity().getString(R.string.dialog_newapplication_error_duplicateLabel)), getActivity(), false);
                return;
            }

            Application newApplication = new Application(applicationName, parsedQR.getSecret(), parsedQR.getUser());

            Application.addApplication(newApplication, getActivity());

            Account.getInstance(getActivity()).setApplications(Arrays.asList(newApplication), s -> {});

            listener.onResult();
            dialog.dismiss();
            dismiss();
        });
    }

    public interface onResultListener {
        void onResult();
    }
}
