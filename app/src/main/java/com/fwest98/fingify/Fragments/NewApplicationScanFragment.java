package com.fwest98.fingify.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fwest98.fingify.Models.Application;
import com.fwest98.fingify.R;

import java.util.Arrays;

import lombok.Setter;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class NewApplicationScanFragment extends NewApplicationFragment implements ZBarScannerView.ResultHandler {
    private ZBarScannerView scannerView;
    @Setter private boolean showCode;

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

        scannerView.setFormats(Arrays.asList(BarcodeFormat.QRCODE));

        mainView.findViewById(R.id.fragment_newapplication_cancel).setOnClickListener(v -> dismiss());
        mainView.findViewById(R.id.fragment_newapplication_noqr).setOnClickListener(v -> {
            NewApplicationFragment fragment = NewApplicationFragment.newInstance(listener, AddMode.CODE);
            getFragmentManager().beginTransaction().remove(this).add(fragment, this.getTag()).commit();
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
            parsedQR = Application.parseUri(result.getContents(), getActivity());
        } catch(IllegalArgumentException e) {
            if(e.getCause() != null && e.getCause() instanceof UnsupportedOperationException) { // HOTP or another code
                // Build AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_newapplication_error_notsupported_text)
                        .setTitle(R.string.dialog_newapplication_error_notsupported_title)
                        .setPositiveButton(R.string.common_tryagain, (dialog, which) -> scannerView.startCamera());
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Invalid code
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_newapplication_error_invalid_text)
                        .setTitle(R.string.dialog_newapplication_error_invalid_title)
                        .setPositiveButton(R.string.common_tryagain, (dialog, which) -> scannerView.startCamera());
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return;
        }

        finishResult(parsedQR);
    }
}
