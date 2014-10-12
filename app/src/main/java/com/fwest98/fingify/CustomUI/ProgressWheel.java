package com.fwest98.fingify.CustomUI;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.fwest98.fingify.R;

import lombok.Getter;

public class ProgressWheel extends View {
    private static final int DEFAULT_COLOR = 0x000000FF;

    @Getter private int progress = 0;
    public void setProgress(int progress) {
        if(progress > 100 || progress < 0) {
            throw new IllegalArgumentException("Progress between 0 and 100");
        }
        this.progress = progress;
        invalidate();
    }

    private int circleColor = 0x000000FF;

    private Paint circlePaint = new Paint();
    private Paint remainingPaint = new Paint();

    public ProgressWheel(Context context) {
        super(context, null);
    }

    public ProgressWheel(Context context, AttributeSet attrs) {
        super(context, attrs);

        int defColor = DEFAULT_COLOR;
        Resources.Theme theme = context.getTheme();
        TypedArray appearance = theme.obtainStyledAttributes(attrs, R.styleable.ProgressWheel, 0, 0);
        if(appearance != null) {
            int n = appearance.getIndexCount();
            for(int i = 0; i < n; i++) {
                int attr = appearance.getIndex(i);

                switch (attr) {
                    case R.styleable.ProgressWheel_circleColor:
                        circleColor = appearance.getColor(attr, defColor);
                        break;
                    case R.styleable.ProgressWheel_progress:
                        progress = appearance.getInteger(attr, 90);
                        break;
                }
            }
        }

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStrokeWidth(0);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(circleColor);

        remainingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        remainingPaint.setColor(circleColor);
    }

    /* Drawings */

    @Override
    protected void onDraw(Canvas canvas) {
        float remainingSectorSweepAngle = (float) (progress * 3.6);
        float remainingSectorStartAngle = 270 - remainingSectorSweepAngle;

        RectF drawingRect = new RectF(1, 1, getWidth() - 1, getHeight() - 1);
        if(remainingSectorStartAngle < 360) {
            canvas.drawArc(
                    drawingRect,
                    remainingSectorStartAngle,
                    remainingSectorSweepAngle, true, remainingPaint
            );
        } else {
            canvas.drawOval(drawingRect, remainingPaint);
        }

        canvas.drawOval(drawingRect, circlePaint);
    }
}
