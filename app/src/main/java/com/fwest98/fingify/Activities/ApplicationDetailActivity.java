package com.fwest98.fingify.Activities;


import android.app.SharedElementCallback;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.fwest98.fingify.Helpers.HelperFunctions;
import com.fwest98.fingify.Helpers.SimpleTransitionListener;
import com.fwest98.fingify.Helpers.TextSizeTransition;
import com.fwest98.fingify.R;

import java.util.List;

public class ApplicationDetailActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //postponeEnterTransition();

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        getWindow().setEnterTransition(new Fade());
        getWindow().setReturnTransition(new Fade());

        setContentView(R.layout.activity_application_detail);

        TransitionSet enterSet = new TransitionSet();
        enterSet.addTransition(new ChangeBounds());
        enterSet.addTransition(new ChangeTransform());
        getWindow().setSharedElementEnterTransition(enterSet);

        TransitionSet returnSet = new TransitionSet();
        returnSet.addTransition(new ChangeBounds());
        returnSet.addTransition(new ChangeTransform());
        getWindow().setSharedElementReturnTransition(returnSet);

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            }
        });

        enterSet.addListener(new SimpleTransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                ViewGroup buttons = (ViewGroup) findViewById(R.id.activity_application_detail_actions_edit).getParent();
                buttons.setVisibility(View.INVISIBLE);
                TextView title = (TextView) findViewById(R.id.toolbar_title);
                float oldSize = title.getTextSize();
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                title.invalidate();

                findViewById(R.id.toolbar).setVisibility(View.VISIBLE);

                TransitionSet toolbarSet = new TransitionSet();
                toolbarSet.setOrdering(TransitionSet.ORDERING_TOGETHER);

                Transition fader = new Fade();
                fader.addTarget(buttons);
                toolbarSet.addTransition(fader);

                Transition fontSize = new TextSizeTransition();
                fontSize.addTarget(R.id.toolbar_title);
                toolbarSet.addTransition(fontSize);

                Transition fontBounds = new ChangeTransform();
                fontBounds.addTarget(R.id.toolbar_title);
                toolbarSet.addTransition(fontBounds);

                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.toolbar), toolbarSet);

                buttons.setVisibility(View.VISIBLE);
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, oldSize);

                Log.e("ANI", "Enter trans");
            }
        });

        returnSet.addListener(new SimpleTransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        findViewById(R.id.container).setOnClickListener(v -> {
            //prepareForExit();
            finishAfterTransition();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.activity_application_detail, menu);
        HelperFunctions.colorAllIcons(menu, Color.BLACK);
        return super.onCreateOptionsMenu(menu);
    }

    private void prepareForExit() {
        TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.toolbar), new Fade());
        findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);

        Log.e("ANI", "Return trans");
    }


}
