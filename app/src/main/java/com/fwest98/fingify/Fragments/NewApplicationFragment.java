package com.fwest98.fingify.Fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.R;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class NewApplicationFragment extends DialogFragment implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View mainView = inflater.inflate(R.layout.fragment_newapplication, container, false);
        scannerView = (ZXingScannerView) mainView.findViewById(R.id.fragment_newapplication_barcodescanner);

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
        ExceptionHandler.handleException(new Exception("Content = " + result.getText()), getActivity(), true);
    }
}
