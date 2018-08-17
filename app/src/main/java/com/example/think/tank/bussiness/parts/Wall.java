package com.example.think.tank.bussiness.parts;

import android.graphics.Canvas;

/**
 * Created by THINK on 2018/8/10.
 */

public abstract class Wall implements Shape {

    public static final float DEFAULT_WIDTH = 30;

    public static final float DEFAULT_HEIGHT = 30;

    /**
     * 左上角
     */
    private float x, y;

    private float width, height;

    public Wall(float x, float y) {
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Wall(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Canvas canvas) {

        onDraw(canvas);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    protected abstract void onDraw(Canvas canvas);
}
