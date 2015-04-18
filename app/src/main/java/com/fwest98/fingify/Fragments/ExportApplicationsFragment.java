package com.fwest98.fingify.Fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fwest98.fingify.Data.Application;
import com.fwest98.fingify.Helpers.ExceptionHandler;
import com.fwest98.fingify.Helpers.ExtendedTotp;
import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;

public class ExportApplicationsFragment extends Fragment {
    private ArrayList<Application> applications;
    private int index = -1;

    private EditText totpCheck;
    private TextView description;
    private ImageView qrCodeView;
    private Button cancelButton;
    private Button continueButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_exportapplications, container, false);
        totpCheck = (EditText) rootView.findViewById(R.id.fragment_exportapplications_check);
        description = (TextView) rootView.findViewById(R.id.fragment_exportapplications_description);
        qrCodeView = (ImageView) rootView.findViewById(R.id.fragment_exportapplications_qr);
        cancelButton = (Button) rootView.findViewById(R.id.fragment_exportapplications_cancel);
        continueButton = (Button) rootView.findViewById(R.id.fragment_exportapplications_continue);

        description.setText(R.string.fragment_exportapplications_startdesc);
        qrCodeView.setVisibility(View.INVISIBLE);
        totpCheck.setVisibility(View.INVISIBLE);
        cancelButton.setOnClickListener((v) -> getActivity().finish());
        continueButton.setOnClickListener(continueListener);

        applications = Application.getApplications(getActivity());

        return rootView;
    }

    private View.OnClickListener skipListener = (v) -> setupNextApplication();

    private View.OnClickListener continueListener = (v) -> {
        if(index == -1) { // First item
            cancelButton.setText(R.string.common_skip);
            cancelButton.setOnClickListener(skipListener);

            qrCodeView.setVisibility(View.VISIBLE);
            totpCheck.setVisibility(View.VISIBLE);
        } else { // Check TOTP code
            String checkCode = totpCheck.getText().toString();
            Application application = applications.get(index);
            boolean success = !"".equals(checkCode) && new ExtendedTotp(application.getSecret()).verify(checkCode);
            if(!success) {
                ExceptionHandler.handleException(new Exception(getString(R.string.fragment_exportapplications_error_totpcheck)), getActivity(), true);
                return;
            }
        }

        setupNextApplication();
    };

    private void setupNextApplication() {
        index++;

        if(index == applications.size()) {
            ExceptionHandler.handleException(new Exception(getString(R.string.fragment_exportapplications_success)), getActivity(), false);
            getActivity().finish();
            return;
        }

        Application application = applications.get(index);
        String applicationUri = application.generateUri();
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix qrcodematrix = writer.encode(applicationUri, BarcodeFormat.QR_CODE, 400, 400);
            Bitmap qrCode = HelperFunctions.toBitmap(qrcodematrix);

            qrCodeView.setImageBitmap(qrCode);
        } catch (WriterException e) {
            ExceptionHandler.handleException(new Exception(getString(R.string.fragment_exportapplications_error_writer), e), getActivity(), true);
            index = index - 1;

            return;
        }

        description.setText(getString(R.string.fragment_exportapplications_codedesc_before) + application.getLabel() + getString(R.string.fragment_exportapplications_codedesc_after));
        totpCheck.setText("");
    }
}
