package com.example.think.tank.bussiness.parts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.example.think.tank.bussiness.Direction;
import com.example.think.tank.bussiness.IMissileHitHandler;

import java.util.List;

/**
 * Created by THINK on 2018/8/8.
 */

public class Missile extends Movable implements Shape {
    public static final float MISSILE_SPEED = 32.0f;
    public static final float MISSILE_RADKUS = 5.0f;

    private float radius;

    private Paint paint;

    private IMissileHitHandler missileHitHandler;

    public void init() {
        speed = MISSILE_SPEED;

        this.paint = new Paint();
        this.radius = MISSILE_RADKUS;
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.parseColor("#F1F5FB"));
    }

    public Missile(float x, float y, float actionScopeWidth, float actionScopeHeight, Direction direction) {
        super(x, y, actionScopeWidth, actionScopeHeight, direction);
        init();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, paint);
    }

    @Override
    protected void onMove() {

    }

    public boolean isOut() {
        if (checkOut()) {
            missileHitHandler.onHit(this);
            return true;
        }

        return false;
    }

    public void hitTank(Tank tank) {
        if (tank.isHited(new Rect((int) (x - radius), (int) (y - radius), (int)(x + radius), (int)(y + radius)))) {
            if (null != missileHitHandler) {
                missileHitHandler.onHit(this);
            }
        }
    }

    public void hitTanks(List<? extends Tank> tanks) {
        for (int i = 0; i < tanks.size(); i++) {
            hitTank(tanks.get(i));
        }
    }

    public void setMissileHitHandler(IMissileHitHandler missileHitHandler) {
        this.missileHitHandler = missileHitHandler;
    }
}
