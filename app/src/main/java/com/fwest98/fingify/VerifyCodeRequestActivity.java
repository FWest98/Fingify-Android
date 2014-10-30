package com.fwest98.fingify;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class VerifyCodeRequestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
                                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        /**/WindowManager.LayoutParams params = getWindow().getAttributes();
        /**params.dimAmount = 1.0f;
        params.alpha = 1.0f;/**/
        /**params.width = 850;/**/
        /**/params.height = 850;/**/
        getWindow().setAttributes(params);/**/

        setContentView(R.layout.activity_verifycoderequest);
    }
}