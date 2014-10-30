package com.fwest98.fingify;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

import lombok.Setter;

public class VerifyCodeRequestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
                                WindowManager.LayoutParams.FLAG_DIM_BEHIND);


        WindowManager.LayoutParams params = getWindow().getAttributes();
        getWindow().setAttributes(params);

        setContentView(R.layout.activity_verifycoderequest);

        if(savedInstanceState == null) {
            VerifyCodeRequestFragment fragment = VerifyCodeRequestFragment.newInstance(this::finish);
            getFragmentManager().beginTransaction().add(fragment, "dialog").commit();
        }
    }


    public static class VerifyCodeRequestFragment extends DialogFragment {
        @Setter private stateListener listener = () -> {};

        public static VerifyCodeRequestFragment newInstance(stateListener listener) {
            VerifyCodeRequestFragment fragment = new VerifyCodeRequestFragment();
            fragment.setListener(listener);

            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.setTitle(R.string.dialog_verifycoderequest_title);
            return dialog;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            listener.onStopped();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            listener.onStopped();
        }

        public interface stateListener {
            void onStopped();
        }
    }
}