package com.example.think.tank.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Scroller;

import com.example.think.tank.bussiness.FightingDelegage;

/**
 * Created by THINK on 2018/8/8.
 */

public class FightingView extends SurfaceView {
    private static final String TAG = "FightingView";
    private FightingDelegage fightingDelegate;

    private RefreshHelper refreshHelper;

    private Scroller mScroller;

    public FightingView(Context context) {
        super(context);
        init(context);
    }

    public FightingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FightingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        SurfaceHolder holder = getHolder();
        refreshHelper = new RefreshHelper(holder);
        holder.addCallback(refreshHelper);

        setZOrderOnTop(true);

        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mScroller = new Scroller(context);
    }

    private void initDelegate() {
        fightingDelegate = new FightingDelegage(getWidth(), getHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        initDelegate();
    }

    private int measureHeight(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);

        int result;

        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = getSuggestedMinimumWidth();
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.max(result, size);
            }
        }
        return result;
    }

    private int measureWidth(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);

        int result;

        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = getSuggestedMinimumWidth();
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        refreshHelper.setmIsRunning(false);
        refreshHelper.interupt();
        fightingDelegate.setmIsRunning(false);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    private void render(Canvas canvas) {
        if (null != fightingDelegate && null != canvas) {
            fightingDelegate.draw(canvas);
        }

    }

    public FightingDelegage getFightingDelegate() {
        return fightingDelegate;
    }

    class RefreshHelper implements SurfaceHolder.Callback, Runnable {

        private Thread mRefreshThread;
        private SurfaceHolder mHolder;

        private boolean mIsRunning;

        private Canvas mCanvas;

        public RefreshHelper(SurfaceHolder holder) {
            this.mHolder = holder;
            mIsRunning = true;
        }

        public void setmIsRunning(boolean isRunning) {
            this.mIsRunning = isRunning;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "surfaceCreated: ");
            mRefreshThread = new Thread(this);
            mIsRunning = true;
            mRefreshThread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "surfaceChanged: ");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "surfaceDestroyed: ");
            mIsRunning = false;
        }

        @Override
        public void run() {
            while (mIsRunning) {
                    try {
                        mCanvas = mHolder.lockCanvas();
                        render(mCanvas);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (null != mCanvas) {
                            mHolder.unlockCanvasAndPost(mCanvas);
                        }
                    }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i("zhoukai", "run: end");
        }

        public void interupt() {
            if (null != mRefreshThread) {
                mRefreshThread.interrupt();
            }
        }
    }
}
