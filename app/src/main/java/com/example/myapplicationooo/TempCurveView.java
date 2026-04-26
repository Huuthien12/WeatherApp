package com.example.myapplicationooo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TempCurveView extends View {
    private List<Double> maxTemps = new ArrayList<>();
    private List<Double> minTemps = new ArrayList<>();
    private Paint maxPaint, minPaint, pointPaint;
    private int itemWidth;

    public TempCurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        maxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maxPaint.setColor(Color.parseColor("#FF6D00")); // Orange for Max
        maxPaint.setStyle(Paint.Style.STROKE);
        maxPaint.setStrokeWidth(4f);

        minPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minPaint.setColor(Color.parseColor("#00BAFF")); // Blue for Min
        minPaint.setStyle(Paint.Style.STROKE);
        minPaint.setStrokeWidth(4f);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(List<Double> maxTemps, List<Double> minTemps, int itemWidth) {
        this.maxTemps = maxTemps;
        this.minTemps = minTemps;
        this.itemWidth = itemWidth;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (maxTemps.size() < 2) return;

        float height = getHeight();
        double absoluteMin = 100, absoluteMax = -100;

        for (double t : maxTemps) {
            if (t > absoluteMax) absoluteMax = t;
            if (t < absoluteMin) absoluteMin = t;
        }
        for (double t : minTemps) {
            if (t > absoluteMax) absoluteMax = t;
            if (t < absoluteMin) absoluteMin = t;
        }

        // Add some margin to the scale
        absoluteMax += 5;
        absoluteMin -= 5;

        Path maxPath = new Path();
        Path minPath = new Path();

        for (int i = 0; i < maxTemps.size(); i++) {
            float x = i * itemWidth + (itemWidth / 2f);
            float yMax = (float) (height - ((maxTemps.get(i) - absoluteMin) / (absoluteMax - absoluteMin) * height));
            float yMin = (float) (height - ((minTemps.get(i) - absoluteMin) / (absoluteMax - absoluteMin) * height));

            if (i == 0) {
                maxPath.moveTo(x, yMax);
                minPath.moveTo(x, yMin);
            } else {
                // Simplified curve using lineTo. For real cubic curve, use quadTo
                maxPath.lineTo(x, yMax);
                minPath.lineTo(x, yMin);
            }
            canvas.drawCircle(x, yMax, 6f, pointPaint);
            canvas.drawCircle(x, yMin, 6f, pointPaint);
        }

        canvas.drawPath(maxPath, maxPaint);
        canvas.drawPath(minPath, minPaint);
    }
}
