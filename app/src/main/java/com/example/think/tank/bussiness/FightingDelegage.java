package com.example.think.tank.bussiness;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.example.think.tank.bussiness.parts.EnemyTank;
import com.example.think.tank.bussiness.parts.Explode;
import com.example.think.tank.bussiness.parts.Missile;
import com.example.think.tank.bussiness.parts.OursTank;
import com.example.think.tank.bussiness.parts.Wall;
import com.example.think.tank.view.FireButton;
import com.example.think.tank.view.SteeringWheelView;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by THINK on 2018/8/8.
 */

public class FightingDelegage implements ITankHitHandler, IMissileHitHandler,
        SteeringWheelView.IDirectionListener, FireButton.IFireListener{
    private static final String TAG = "FightingDelegage";

    /**
     * canvas宽度
     */
    private int mWidth;

    /**
     * canvas高度
     */
    private int mHeight;

    /**
     * 缓冲
     */
    private Canvas mBufferCanvas;
    private Bitmap mBufferBitmap;

    Paint clearPaint = new Paint();

    /**
     * 刷新互斥锁, 刷新界面时缓冲区不能刷新，刷新缓冲区时界面不能刷新
     */
    private Mutex mMutex = new Mutex();

    private boolean mIsRunning;

    /**
     * 敌方子弹
     */
    private List<Missile> enemyMissiles;

    private List<Missile> oursMissiles;

    private List<EnemyTank> enemyTanks;

    private List<Explode> explodes;

    private List<Wall> walls;

    private OursTank oursTank;

    /**
     * 控制消息
     */
    private Queue<Message> mq = new ArrayDeque<>(10);

    /**
     * 同样的方向只放一次
     */
    private Message currentMessage;

    private Object driveLock = new Object();

    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    public FightingDelegage(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        mBufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBufferBitmap);

        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        this.enemyMissiles = new LinkedList<>();
        this.oursMissiles = new LinkedList<>();
        this.enemyTanks = new LinkedList<>();
        this.walls = new LinkedList<>();
        this.explodes = new LinkedList<>();

        test();

        mIsRunning = true;
        executorService.submit(new BufferDrawer());
//        executorService.submit(new MissileDriver());
        executorService.submit(new TankDriver());
    }

    /**
     * 结束时停止线程池
     */
    public void gameOver() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            Log.i("zhoukai", "gameOver: executorService end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示到界面, 将缓冲区中画好的图形绘制到surfaceview的canvas中
     * @param canvas
     */
    public void draw(Canvas canvas) {
        if (null != canvas) {
            if (null != mBufferBitmap) {
                try {
                    mMutex.acquire(Thread.currentThread());

                    clearCanvas(canvas);
                    canvas.drawBitmap(mBufferBitmap, 0, 0, null);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mMutex.release(Thread.currentThread());
                }
            }
        }
    }

    private void clearCanvas(Canvas canvas) {
        canvas.drawPaint(clearPaint);
    }

    public void setmIsRunning(boolean mIsRunning) {
        this.mIsRunning = mIsRunning;
        gameOver();
    }

    /**
     * 绘制缓冲区的线程
     */
    class BufferDrawer implements Runnable {
        @Override
        public void run() {
            while (mIsRunning) {

                try {
                    mMutex.acquire(Thread.currentThread());

                    //绘制前先清除
                    clearCanvas(mBufferCanvas);

                    //绘制开始
                    //子弹
                    for (int i = 0; i < enemyMissiles.size(); i++) {
                        enemyMissiles.get(i).draw(mBufferCanvas);
                    }

                    //我方子弹
                    for (int i = 0; i < oursMissiles.size(); i++) {
                        oursMissiles.get(i).draw(mBufferCanvas);
                    }

                    //敌方坦克
                    for (int i = 0; i < enemyTanks.size(); i++) {
                        enemyTanks.get(i).draw(mBufferCanvas);
                    }

                    //我方坦克
                    oursTank.draw(mBufferCanvas);

                    //爆炸效果
                    for (int i = 0; i < explodes.size(); i++) {
                        explodes.get(i).draw(mBufferCanvas);

                    }
                    explodes.clear();
                    //绘制结束


                    //控制敌方坦克移动, 子弹移动,碰撞检测
                    for (int i = 0; i < enemyTanks.size(); i++) {
                        enemyTanks.get(i).fireAtWill();
                        enemyTanks.get(i).moveAtWill(enemyTanks, oursTank, walls);
                    }

                    for (int i = 0; i < enemyMissiles.size(); i++) {
                        enemyMissiles.get(i).move();
                    }
                    for (int i = 0; i < oursMissiles.size(); i++) {
                        oursMissiles.get(i).move();
                    }

                    //已检查是否出界, 还需要加入是否打到我方坦克和打到墙
                    for (int i = 0; i < enemyMissiles.size(); i++) {
                        if (!enemyMissiles.get(i).isOut()) {
                            enemyMissiles.get(i).hitTank(oursTank);
                        }
                    }

                    for (int i = 0; i < oursMissiles.size(); i++) {
                        if (!oursMissiles.get(i).isOut()) {
                            oursMissiles.get(i).hitTanks(enemyTanks);
                        }
                    }
                    //控制敌方坦克移动, 子弹移动,碰撞检测

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    mMutex.release(Thread.currentThread());
                }

                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.i("zhoukai", "BufferDrawer: end");
        }
    }

    /**
     * 控制敌方坦克移动, 子弹移动, 碰撞检测
     */
    class MissileDriver implements Runnable {
        @Override
        public void run() {
            while (mIsRunning) {
                try {
                    mMutex.acquire(Thread.currentThread());

                    for (int i = 0; i < enemyTanks.size(); i++) {
                        enemyTanks.get(i).fireAtWill();
                        enemyTanks.get(i).moveAtWill(enemyTanks, oursTank, walls);
                    }

                    for (int i = 0; i < enemyMissiles.size(); i++) {
                        enemyMissiles.get(i).move();
                    }
                    for (int i = 0; i < oursMissiles.size(); i++) {
                        oursMissiles.get(i).move();
                    }

                    //已检查是否出界, 还需要加入是否打到我方坦克和打到墙
                    for (int i = 0; i < enemyMissiles.size(); i++) {
                        if (!enemyMissiles.get(i).isOut()) {
                            enemyMissiles.get(i).hitTank(oursTank);
                        }
                    }

                    for (int i = 0; i < oursMissiles.size(); i++) {
                        if (!oursMissiles.get(i).isOut()) {
                            oursMissiles.get(i).hitTanks(enemyTanks);
                        }
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mMutex.release(Thread.currentThread());
                }


                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private float translateX;
    private float translateY;

    /**
     * 控制我方坦克移动和开火
     */
    class TankDriver implements Runnable {
        private boolean isDriving;
        private Direction currentDirection;

        @Override
        public void run() {
            while (mIsRunning) {
                Message message = mq.poll();
                if (null != message || isDriving) {
                    if (null != message) {
                        switch (message) {
                            case UP:
                                isDriving = true;
                                currentDirection = Direction.UP;
                                break;
                            case DOWN:
                                isDriving = true;
                                currentDirection = Direction.DOWN;
                                break;
                            case LEFT:
                                isDriving = true;
                                currentDirection = Direction.LEFT;
                                break;
                            case RIGHT:
                                isDriving = true;
                                currentDirection = Direction.RIGHT;
                                break;
                            case STOP:
                                isDriving = false;
                                currentDirection = null;
                                break;
                            default:
                                break;
                        }
                    }

                    if (null != currentDirection && isDriving) {
                        oursTank.moveWithDirection(currentDirection, enemyTanks, walls);

                        //类似于王者的镜头随着人物移动，但是效果不好，放弃
//                        float movex = mWidth / 2 - (oursTank.getX() + translateX);
//                        float movey = mHeight  / 2 - (oursTank.getY() + translateY);
//                        Log.i(TAG, "run: " + movex + " " + movey);
//                        mBufferCanvas.translate(movex,  movey);
//                        translateX += movex;
//                        translateY += movey;

                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        synchronized (driveLock) {
                            driveLock.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    /**
     * 互斥锁
     *
     */
    static class Mutex {
        private Thread owner;

        public Mutex() {
        }

        /**
         * 进入锁
         * @param thread
         * @throws InterruptedException
         */
        public synchronized void acquire(Thread thread) throws InterruptedException {
            if (owner != null) {
                wait();
            }

            owner = thread;
        }

        /**
         * 释放锁
         * @param thread
         * @throws InterruptedException
         */
        public synchronized void release(Thread thread) {
            if (owner == thread) {
                owner = null;
                notify();
            }
        }

    }

    enum  Message {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        STOP;
    }

    @Override
    public void onEnemyTankDestroy(EnemyTank destroy) {
        enemyTanks.remove(destroy);
    }

    @Override
    public void onOursTankDestroy(OursTank oursTank) {

    }

    @Override
    public void onEnemyFire(Missile missile) {
        missile.setMissileHitHandler(this);
        enemyMissiles.add(missile);
    }

    @Override
    public void onOursFire(Missile missile) {
        missile.setMissileHitHandler(this);
        oursMissiles.add(missile);
    }

    @Override
    public void onHit(Missile missile) {
        enemyMissiles.remove(missile);
        oursMissiles.remove(missile);
        explodes.add(new Explode(missile.getX(), missile.getY()));
    }

    @Override
    public void onUp() {
        Log.w(TAG, "onUp: ");
        if (currentMessage == Message.UP) {
            return;
        }

        mq.offer(Message.UP);
        synchronized (driveLock) {
            driveLock.notifyAll();
        }
    }

    @Override
    public void onDown() {
        Log.i(TAG, "onDown: ");
        if (currentMessage == Message.DOWN) {
            return;
        }

        mq.offer(Message.DOWN);
        synchronized (driveLock) {
            driveLock.notifyAll();
        }
    }

    @Override
    public void onLeft() {
        Log.i(TAG, "onLeft: ");
        if (currentMessage == Message.LEFT) {
            return;
        }

        mq.offer(Message.LEFT);
        synchronized (driveLock) {
            driveLock.notifyAll();
        }
    }

    @Override
    public void onRight() {
        Log.i(TAG, "onRight: ");
        if (currentMessage == Message.RIGHT) {
            return;
        }

        mq.offer(Message.RIGHT);
        synchronized (driveLock) {
            driveLock.notifyAll();
        }
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop: ");
        mq.offer(Message.STOP);

        currentMessage = null;
        synchronized (driveLock) {
            driveLock.notifyAll();
        }
    }

    @Override
    public void onFire() {
        Log.i(TAG, "onFire: ");
        oursTank.fire();
    }

    public void test() {

        enemyTanks.add(new EnemyTank(100, 100, mWidth, mHeight, Direction.UP));
        enemyTanks.add(new EnemyTank(200, 200, mWidth, mHeight, Direction.RIGHT));
        enemyTanks.add(new EnemyTank(300, 300, mWidth, mHeight, Direction.DOWN));
        enemyTanks.add(new EnemyTank(400, 400, mWidth, mHeight, Direction.LEFT));
        enemyTanks.add(new EnemyTank(500, 500, mWidth, mHeight, Direction.UP));
        enemyTanks.add(new EnemyTank(600, 600, mWidth, mHeight, Direction.DOWN));
        enemyTanks.add(new EnemyTank(700, 700, mWidth, mHeight, Direction.RIGHT));
        enemyTanks.add(new EnemyTank(800, 800, mWidth, mHeight, Direction.LEFT));
        enemyTanks.add(new EnemyTank(900, 900, mWidth, mHeight, Direction.LEFT));
        enemyTanks.add(new EnemyTank(1000, 1000, mWidth, mHeight, Direction.LEFT));
        enemyTanks.add(new EnemyTank(1100, 1100, mWidth, mHeight, Direction.LEFT));

        enemyTanks.add(new EnemyTank(mWidth - 100, 100, mWidth, mHeight, Direction.UP));
        enemyTanks.add(new EnemyTank(mWidth - 200, 200, mWidth, mHeight, Direction.RIGHT));
        enemyTanks.add(new EnemyTank(mWidth - 300, 300, mWidth, mHeight, Direction.DOWN));
        enemyTanks.add(new EnemyTank(mWidth - 400, 400, mWidth, mHeight, Direction.LEFT));
        enemyTanks.add(new EnemyTank(mWidth - 500, 500, mWidth, mHeight, Direction.UP));
        enemyTanks.add(new EnemyTank(mWidth - 600, 600, mWidth, mHeight, Direction.DOWN));
        enemyTanks.add(new EnemyTank(mWidth - 700, 700, mWidth, mHeight, Direction.RIGHT));
        enemyTanks.add(new EnemyTank(mWidth - 800, 800, mWidth, mHeight, Direction.LEFT));
        enemyTanks.add(new EnemyTank(mWidth - 900, 900, mWidth, mHeight, Direction.LEFT));
        enemyTanks.add(new EnemyTank(mWidth - 1000, 1000, mWidth, mHeight, Direction.LEFT));
        enemyTanks.add(new EnemyTank(mWidth - 1100, 1100, mWidth, mHeight, Direction.LEFT));

        for (EnemyTank enemyTank : enemyTanks) {
            enemyTank.setHitHandler(this);
        }

        oursTank = new OursTank(mWidth / 2, mHeight / 2, mWidth, mHeight, Direction.LEFT);
        oursTank.setHitHandler(this);
        translateX = 0;
        translateY = 0;
    }

}
