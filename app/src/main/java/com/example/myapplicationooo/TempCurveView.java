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
    private int scrollX = 0;

    public TempCurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        maxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maxPaint.setColor(Color.parseColor("#FF6D00")); // Orange for Max
        maxPaint.setStyle(Paint.Style.STROKE);
        maxPaint.setStrokeWidth(6f);

        minPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minPaint.setColor(Color.parseColor("#00BAFF")); // Blue for Min
        minPaint.setStyle(Paint.Style.STROKE);
        minPaint.setStrokeWidth(6f);

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

    public void setScrollOffset(int scrollX) {
        this.scrollX = scrollX;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (maxTemps == null || maxTemps.size() < 2) return;

        float height = getHeight();
        // Drawing area within the view
        float paddingVertical = height * 0.15f; 
        float drawHeight = height - (paddingVertical * 2);

        double absoluteMin = 100, absoluteMax = -100;

        for (double t : maxTemps) {
            if (t > absoluteMax) absoluteMax = t;
            if (t < absoluteMin) absoluteMin = t;
        }
        for (double t : minTemps) {
            if (t > absoluteMax) absoluteMax = t;
            if (t < absoluteMin) absoluteMin = t;
        }

        // Add padding to temperature range to avoid points touching the edge
        absoluteMax += 2;
        absoluteMin -= 2;
        double range = absoluteMax - absoluteMin;
        if (range <= 0) range = 1;

        Path maxPath = new Path();
        Path minPath = new Path();

        List<Float> xCoords = new ArrayList<>();
        List<Float> yMaxCoords = new ArrayList<>();
        List<Float> yMinCoords = new ArrayList<>();

        for (int i = 0; i < maxTemps.size(); i++) {
            float x = i * itemWidth + (itemWidth / 2f) - scrollX;
            float yMax = (float) (paddingVertical + drawHeight - ((maxTemps.get(i) - absoluteMin) / range * drawHeight));
            float yMin = (float) (paddingVertical + drawHeight - ((minTemps.get(i) - absoluteMin) / range * drawHeight));

            xCoords.add(x);
            yMaxCoords.add(yMax);
            yMinCoords.add(yMin);

            if (i == 0) {
                maxPath.moveTo(x, yMax);
                minPath.moveTo(x, yMin);
            } else {
                float prevX = xCoords.get(i - 1);
                float prevYMax = yMaxCoords.get(i - 1);
                float prevYMin = yMinCoords.get(i - 1);
                
                maxPath.cubicTo((prevX + x) / 2, prevYMax, (prevX + x) / 2, yMax, x, yMax);
                minPath.cubicTo((prevX + x) / 2, prevYMin, (prevX + x) / 2, yMin, x, yMin);
            }
        }

        canvas.drawPath(maxPath, maxPaint);
        canvas.drawPath(minPath, minPaint);

        // Draw points only
        for (int i = 0; i < xCoords.size(); i++) {
            float x = xCoords.get(i);
            if (x > -50 && x < getWidth() + 50) {
                float yMax = yMaxCoords.get(i);
                float yMin = yMinCoords.get(i);
                
                canvas.drawCircle(x, yMax, 8f, pointPaint);
                canvas.drawCircle(x, yMin, 8f, pointPaint);
            }
        }
    }
}
