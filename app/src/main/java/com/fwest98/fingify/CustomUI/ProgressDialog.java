package com.fwest98.fingify.CustomUI;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fwest98.fingify.R;

import me.zhanghai.android.materialprogressbar.IndeterminateProgressDrawable;

public class ProgressDialog {
    public static AlertDialog create(String title, String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        ((TextView) dialogView.findViewById(android.R.id.content)).setText(message);
        ((ProgressBar) dialogView.findViewById(R.id.dialog_progressbar)).setIndeterminateDrawable(new IndeterminateProgressDrawable(context));

        builder.setView(dialogView)
                .setTitle(title)
                .setCancelable(false);

        return builder.create();
    }
}
