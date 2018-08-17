package com.example.think.tank.bussiness.parts;

import android.graphics.Canvas;

/**
 * Created by THINK on 2018/8/13.
 */

public class UnbreakableWall extends Wall {
    public UnbreakableWall(float x, float y) {
        super(x, y);
    }

    public UnbreakableWall(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }
}
