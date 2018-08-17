package com.example.think.tank.bussiness.parts;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Created by THINK on 2018/8/16.
 */

public class Explode implements Shape {
    private static final float DEFAULT_WIDTH = 40;

    private static final float DEFAULT_HEIGHT = 40;

    private float x;

    private float y;

    private float width;

    private float height;

    private Paint paint;

    public Explode(float x, float y) {
        this.x = x;
        this.y = y;
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;

        paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setShader(new RadialGradient(x, y, Math.min(width, height) / 2,
                new int[]{Color.parseColor("#ffff00"),
                        Color.parseColor("#f0244d"),
                        Color.parseColor("#663333"),
                        Color.TRANSPARENT},
                new float[]{0.0f, 0.4f, 0.6f, 1.0f},
                Shader.TileMode.CLAMP));
        paint.setMaskFilter(new EmbossMaskFilter(new float[]{200, 200, 100}, 0.8f, 0.5f, 5));
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, Math.min(width, height) / 2, paint);
    }
}
