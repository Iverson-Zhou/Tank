package com.example.think.tank.bussiness.parts;

import com.example.think.tank.bussiness.Direction;

/**
 * Created by THINK on 2018/8/9.
 */

public abstract class Movable {

    protected float x, y;

    protected float actionScopeWidth;

    protected float actionScopeHeight;

    /**
     * 是否可用状态模式代替
     */
    protected Direction direction;

    protected float speed;

    public Movable(float x, float y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public Movable(float x, float y, float actionScopeWidth, float actionScopeHeight, Direction direction) {
        this(x, y, direction);
        this.actionScopeWidth = actionScopeWidth;
        this.actionScopeHeight = actionScopeHeight;
    }

    public void move() {
        switch (direction) {
            case UP:
                y = y - speed;
                break;
            case DOWN:
                y = y + speed;
                break;
            case LEFT:
                x = x - speed;
                break;
            case RIGHT:
                x = x + speed;
                break;
            default:
        }

        onMove();
    }

    protected boolean checkOut() {
        if (x > actionScopeWidth || x < 0 || y > actionScopeHeight || y < 0) {
            return true;
        }

        return false;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    protected abstract void onMove();
}
