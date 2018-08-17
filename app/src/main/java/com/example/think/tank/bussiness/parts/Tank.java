package com.example.think.tank.bussiness.parts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.example.think.tank.bussiness.Direction;
import com.example.think.tank.bussiness.ITankHitHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by THINK on 2018/8/8.
 */

public abstract class Tank extends Movable implements Shape{
    protected static final String TAG = "Tank";
    public static final float TANK_SPEED = 2.0f;
    public static final int TRI_POINT_NUMBER = 10;

    public final float WIDTH = 70;

    public final float HEIGHT = 100;

    protected final float PADDING_LEFT = 10;
    protected final float PADDING_RIGHT = PADDING_LEFT;
    protected final float PADDING_DOWN = PADDING_LEFT;
    protected final float PADDING_UP = 20;

    protected static final float TIME_STEP = 50;

    protected static final float MISSILE_PADDING = 10;

    protected Path path;
    protected Paint paint;

    /**
     * pg
     */
    protected Paint cannonTubePaint;

    protected Rect rect;

    /**
     * ld
     */
    protected Paint trackPaint;

    protected float triStep;

    protected List<FPoint> triPoints;

    protected Path trackPath;

    protected float traceWidth = 4;

    protected float tracePadding = PADDING_LEFT - traceWidth;

    protected float currentTimes = 0;

    protected long lastMovedTime = 0;

    protected ITankHitHandler hitHandler;

    public void init() {
        speed = TANK_SPEED;
        path = new Path();
        trackPath = new Path();

        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);

        cannonTubePaint = new Paint();
        cannonTubePaint.setStrokeWidth(8);
        cannonTubePaint.setStyle(Paint.Style.STROKE);
        cannonTubePaint.setColor(Color.WHITE);

        trackPaint = new Paint();
        trackPaint.setStrokeWidth(traceWidth);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(Color.GRAY);

        triPoints = new ArrayList<>();
        triStep = (HEIGHT - PADDING_UP - PADDING_DOWN - 10) / TRI_POINT_NUMBER;
        for (int i = 0; i < TRI_POINT_NUMBER; i++) {
            FPoint point = new FPoint();
            point.x = x + tracePadding + calculateTri(TIME_STEP * i, 0);
            point.y = (y + PADDING_UP + 5) + triStep * i;
            triPoints.add(point);
        }
    }

    public Tank(float x, float y, float actionScopeWidth, float actionScopeHeight, Direction direction) {
        super(x, y, actionScopeWidth, actionScopeHeight, direction);
        init();
    }

    /**
     * 设置行动方向
     * @param direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    private float calculateTri(float orignTime, float currentTime) {
        float calc = (float) (traceWidth * Math.sin((Math.PI / 80) * (orignTime + currentTime)));
        return calc;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.rotate(rotateDegree(), x + WIDTH / 2, y + HEIGHT / 2);

        //两条波浪线模拟履带
        drawTrack(canvas);

        rect = new Rect((int)(x + PADDING_LEFT), (int)(y + PADDING_UP), (int)(x + WIDTH - PADDING_RIGHT), (int) (y + HEIGHT - PADDING_DOWN));
        //坦克身
        canvas.drawRect(rect, paint);

        onDraw(canvas);

        drawCannonTube(canvas);

        canvas.restore();
    }

    private void drawTrack(Canvas canvas) {
        if (isMoving()) {
            refreshTrack();
        }

        FPoint currentPoint = triPoints.get(0);
        FPoint nextPoint;

        trackPath.reset();
        trackPath.moveTo(currentPoint.x, currentPoint.y);
        for (int i = 1; i < TRI_POINT_NUMBER; i++) {
            nextPoint = triPoints.get(i);
            trackPath.lineTo(nextPoint.x, nextPoint.y);
        }
        canvas.drawPath(trackPath, trackPaint);

        //画右边的履带
        canvas.save();
        canvas.translate(WIDTH - PADDING_RIGHT - traceWidth, 0);
        canvas.drawPath(trackPath, trackPaint);
        canvas.restore();

        currentTimes = currentTimes + TIME_STEP;
    }

    private void refreshTrack() {
        for (int i = 0; i < TRI_POINT_NUMBER; i++) {
            FPoint point = triPoints.get(i);
            point.x = x + tracePadding + calculateTri(TIME_STEP * i, currentTimes);
            point.y = (y + PADDING_UP + 5) + triStep * i;
        }
    }

    private boolean isMoving() {
        return System.currentTimeMillis() - lastMovedTime < 110;
    }

    private void drawCannonTube(Canvas canvas) {
        path.reset();
        path.moveTo(x + WIDTH / 2, y + HEIGHT / 2);
        path.lineTo(x + WIDTH / 2, y);
        path.close();

        canvas.drawPath(path, cannonTubePaint);
    }

    private float rotateDegree() {
        switch (direction) {
            case UP:
                return 0;
            case DOWN:
                return 180;
            case LEFT:
                return 270;
            case RIGHT:
                return 90;
                default:
                    return 0;
        }
    }

    protected Rect touchTestingRect() {
        Rect rect;
        switch (direction) {
            case UP:
                rect = new Rect((int)(x), (int)(y - PADDING_UP), (int)(x + WIDTH), (int) (y));
                break;
            case DOWN:
                rect = new Rect((int)(x), (int)(y + HEIGHT), (int)(x + WIDTH), (int) (y + HEIGHT + PADDING_UP));
                break;
            case LEFT:
                rect = new Rect((int)(x - PADDING_UP), (int)(y), (int)(x), (int) (y + HEIGHT));
                break;
            case RIGHT:
                rect = new Rect((int)(x + WIDTH), (int)(y), (int)(x + WIDTH + PADDING_UP), (int) (y + HEIGHT));
                break;
            default:
                rect = new Rect((int)(x - PADDING_UP), (int)(y), (int)(x), (int) (y + HEIGHT));
                break;
        }

        return rect;
    }

    @Override
    protected void onMove() {
        lastMovedTime = System.currentTimeMillis();
    }

    @Override
    protected boolean checkOut() {
        Rect rect = touchTestingRect();
        if (rect.left < 0 || rect.top < 0 || rect.right > actionScopeWidth || rect.bottom > actionScopeHeight) {
            return true;
        }

        return false;
    }

    protected boolean touchTankOrWall(List<? extends Tank> tanks, List<Wall> walls) {
        //和别的坦克接触
//        Rect rect = new Rect((int)(x - PADDING_DOWN), (int)(y - PADDING_UP), (int)(x + WIDTH + PADDING_DOWN), (int) (y + HEIGHT + PADDING_DOWN));
        Rect rect = touchTestingRect();
        for (int i = 0; i < tanks.size(); i++) {
            Tank tank = tanks.get(i);
            if (tank != this) {
                Rect rect1 = new Rect((int)(tank.x - PADDING_DOWN), (int)(tank.y - PADDING_UP),
                        (int)(tank.x + WIDTH + PADDING_DOWN), (int) (tank.y + HEIGHT + PADDING_DOWN));
                if (rect1.intersect(rect)) {
                    return true;
                }
            }
        }

        //和墙体接触
        for (int i = 0; i < walls.size(); i++) {
            Wall wall = walls.get(i);
            Rect wallRect = new Rect((int) wall.getX(), (int) wall.getY(), (int) (wall.getX() + WIDTH), (int) (wall.getY() + HEIGHT));
            if (rect.intersect(wallRect)) {
                return true;
            }
        }

        //到达边界
        if (checkOut()) {
            return true;
        }
        return false;
    }

    public void fire() {
        FPoint p = getMissilePoint();
        Missile missile = new Missile(p.x, p.y, actionScopeWidth, actionScopeHeight, direction);
        onFire(missile);
    }

    private FPoint getMissilePoint() {
        FPoint p = new FPoint();
        switch (direction) {
            case UP:
                p.x = x + WIDTH / 2;
                p.y = y - MISSILE_PADDING;
                break;
            case DOWN:
                p.x = x + WIDTH / 2;
                p.y = y + HEIGHT +  MISSILE_PADDING;
                break;
            case LEFT:
                p.x = x - MISSILE_PADDING;
                p.y = y + HEIGHT / 2;
                break;
            case RIGHT:
                p.x = x + WIDTH + MISSILE_PADDING;
                p.y = y + HEIGHT / 2;
                break;
            default:
                p.x = x + WIDTH / 2;
                p.y = y - MISSILE_PADDING;
                break;
        }
        return p;
    }

    public void hited() {
        onHited();
    }

    public boolean isHited(Rect rect) {

        Rect myRect = new Rect((int)(x - PADDING_DOWN), (int)(y - PADDING_UP), (int)(x + WIDTH + PADDING_DOWN), (int) (y + HEIGHT + PADDING_DOWN));
        if (myRect.intersect(rect)) {
            hited();
            return true;
        }

        return false;
    }

    protected abstract void onDraw(Canvas canvas);

    protected abstract void onFire(Missile missile);

    protected abstract void onHited();

    public void setHitHandler(ITankHitHandler hitHandler) {
        this.hitHandler = hitHandler;
    }

    static class FPoint {
        public float x;
        public float y;
    }
}
