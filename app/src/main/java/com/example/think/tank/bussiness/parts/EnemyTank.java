package com.example.think.tank.bussiness.parts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.example.think.tank.bussiness.Direction;

import java.util.List;
import java.util.Random;

/**
 * Created by THINK on 2018/8/9.
 */

public class EnemyTank extends Tank {
    public static final int ENEMYTANK_LIFETIME = 5;

    private int directionNum;
    private Paint enemyPaint;
    private Random random;

    /**
     * 若因为两个坦克相遇或者碰到墙，则改变方向之后再过changePostpone次改变方向，防止两个坦克在一起打结
     */
    private int changePostpone;

    private int lifetime = ENEMYTANK_LIFETIME;

    public void initEnemyTank() {
        directionNum = Direction.values().length;
        random = new Random();

        enemyPaint = new Paint();
        enemyPaint.setStrokeWidth(5);
        enemyPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        enemyPaint.setColor(Color.RED);

        changePostpone = 0;
    }

    public EnemyTank(float x, float y, float actionScopeWidth, float actionScopeHeight, Direction direction) {
        super(x, y, actionScopeWidth, actionScopeHeight, direction);
        initEnemyTank();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(x + WIDTH / 2, y + HEIGHT / 2, WIDTH / 2 - PADDING_UP, enemyPaint);
    }

    @Override
    protected void onFire(Missile missile) {
        if (null != hitHandler) {
            hitHandler.onEnemyFire(missile);
        }
    }

    @Override
    protected void onHited() {
        lifetime--;
        if (lifetime <= 0) {
            if (null != hitHandler) {
                hitHandler.onEnemyTankDestroy(this);
            }
        }
    }

    /**
     * 随意移动
     */
    public void moveAtWill(List<EnemyTank> enemyTanks, OursTank oursTank, List<Wall> walls) {
        //敌方坦克接触其他坦克或者墙
        if (touchTankOrWall(enemyTanks, walls) || touchOursTank(oursTank)) {
            changePostpone = 10;
            setDirection(getNextDirection());
        }
        if (changePostpone <= 0) {
            if (changeDirection(50)) {
                setDirection(getNextDirection());
            }
        } else {
            changePostpone--;
        }

        move();
    }

    private boolean touchOursTank(OursTank oursTank) {
        Rect rect = touchTestingRect();
        Rect rect1 = new Rect((int)oursTank.x, (int)oursTank.y, (int)(oursTank.x + WIDTH), (int) (oursTank.y + HEIGHT));
        return rect.intersect(rect1);
    }

    /**
     * 随意开火
     */
    public void fireAtWill() {
        if (changeDirection(10)) {
            fire();
        }
    }

    /**
     * 随机重新选择和当前方向不同的方向
     * @return
     */
    private Direction getNextDirection() {
        int currentDirection = direction.ordinal();
        int i;

        do {
            i = Math.abs(random.nextInt() % directionNum);
        } while (i == currentDirection);

        return Direction.values()[i];
    }

    /**
     * 1/probability 的概率改变方向
     * @return
     */
    private boolean changeDirection(int probability) {
        int i = random.nextInt() % probability;
        if (i == 0) {
            return true;
        }
        return false;
    }

}
