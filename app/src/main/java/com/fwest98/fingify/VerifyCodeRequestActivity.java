package com.fwest98.fingify;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.fwest98.fingify.Fragments.NewApplicationFragment;

public class VerifyCodeRequestActivity extends Activity implements NewApplicationFragment.onResultListener {
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
            NewApplicationFragment fragment = NewApplicationFragment.newInstance(this);
            fragment.show(getFragmentManager(), "dialog");
        }
    }

    @Override
    public void onResult() {

    }
}