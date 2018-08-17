package com.example.think.tank.view;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.think.tank.R;

/**
 * Created by THINK on 2018/8/13.
 */

public class SteeringWheelView extends android.support.v7.widget.AppCompatButton {
    private static final String TAG = "SteeringWheelView";
    public static final int SUGGEST_SIZE = 200;

    private Paint innerPaint;
    private Paint outerPaint;
    private Paint arcPaint;

    private Context mContext;

    Path arcPath = new Path();

    private float touchX;
    private float touchY;

    private IDirectionListener listener;

    /**
     * Shader的默认变色角度为90度
     */
    private float orignDegree = -90;
    private float degree = 0;

    private boolean mIsTouching = false;

    public SteeringWheelView(Context context) {
        super(context);
        init(context);
    }

    public SteeringWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SteeringWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        this.mContext = context;

        innerPaint = new Paint();
        innerPaint.setColor(getResources().getColor(R.color.lightblue));
        innerPaint.setStrokeWidth(5);
        innerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        innerPaint.setMaskFilter(new EmbossMaskFilter(new float[]{150, 800, 1000}, 0.5f, 1, 50));

        outerPaint = new Paint();
        outerPaint.setColor(getResources().getColor(R.color.lightblue));
        outerPaint.setStrokeWidth(25);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));

        arcPaint = new Paint();
        arcPaint.setColor(Color.BLACK);
        arcPaint.setStrokeWidth(8);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMeasureSize(widthMeasureSpec), getMeasureSize(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        Shader shader = new ComposeShader(new RadialGradient(getWidth() / 2, getHeight() / 2, 30,
                getResources().getColor(R.color.lightblue), Color.TRANSPARENT, Shader.TileMode.REPEAT),
                new SweepGradient(getWidth() / 2, getHeight() / 2,
                        new int[]{Color.WHITE, Color.TRANSPARENT, Color.WHITE}, new float[]{0.0f, 0.5f, 1.0f}),
                PorterDuff.Mode.ADD);

        outerPaint.setShader(shader);
    }

    private int getMeasureSize(int measureSpec) {
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);

        int result = size;
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(size, SUGGEST_SIZE);
            }
        }

        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouching = true;
                touchX = event.getX();
                touchY = event.getY();

                getDirection();
                degree = getRotateDegree(getWidth() / 2, getHeight() / 2, getWidth() / 2, 0, touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                touchX = event.getX();
                touchY = event.getY();

                getDirection();
                degree = getRotateDegree(getWidth() / 2, getHeight() / 2, getWidth() / 2, 0, touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                mIsTouching = false;
                Log.i(TAG, "onTouchEvent: up");
                if (null != listener) {
                    listener.onStop();
                }
                degree = 0;
                break;
        }
        postInvalidate();
        return super.onTouchEvent(event);
    }

    /**
     * 获取夹角
     * @param cX
     * @param cY
     * @param fX
     * @param fY
     * @param sX
     * @param sY
     * @return
     */
    private float getRotateDegree(float cX, float cY, float fX, float fY, float sX, float sY) {
        double ma_x = fX - cX;
        double ma_y = fY - cY;
        double mb_x = sX - cX;
        double mb_y = sY - cY;
        double v1 = (ma_x * mb_x) + (ma_y * mb_y);
        double ma_val = Math.sqrt(ma_x * ma_x + ma_y * ma_y);
        double mb_val = Math.sqrt(mb_x * mb_x + mb_y * mb_y);
        double cosM = v1 / (ma_val * mb_val);
        double angleAMB = sX > cX ? Math.acos(cosM) * 180 / Math.PI : -(Math.acos(cosM) * 180 / Math.PI);

        Log.i(TAG, "getRotateDegree: " + angleAMB);
        return (float) angleAMB;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 3, innerPaint);

        if (mIsTouching) {
            canvas.save();
            canvas.rotate(orignDegree + degree, getWidth() / 2, getHeight() / 2);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 3 + 15, outerPaint);
            canvas.restore();
        }

        drawArc();
        canvas.drawPath(arcPath, arcPaint);

        canvas.save();
        canvas.rotate(90, getWidth() / 2, getHeight() / 2);
        drawArc();
        canvas.drawPath(arcPath, arcPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(180, getWidth() / 2, getHeight() / 2);
        drawArc();
        canvas.drawPath(arcPath, arcPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(270, getWidth() / 2, getHeight() / 2);
        drawArc();
        canvas.drawPath(arcPath, arcPaint);
        canvas.restore();
    }

    /**
     * 画出方向盘中的三角形
     */
    private void drawArc() {
        arcPath.reset();
        arcPath.moveTo(getWidth() / 2, getHeight() / 2 - getHeight() / 3 + 15);
        arcPath.lineTo(getWidth() / 2 - 10, getHeight() / 2 - getHeight() / 3 + 35);
        arcPath.lineTo(getWidth() / 2 + 10, getHeight() / 2 - getHeight() / 3 + 35);
        arcPath.close();
    }

    /**
     * y = x
     * y = -x
     */
    public void getDirection() {
        if (touchX - getWidth() / 2 < touchY - getHeight() / 2 && touchY - getHeight() / 2 > - touchX + getWidth() / 2) {
            Log.i(TAG, "getDirection: " + "下");
            if (null != listener) {
                listener.onDown();
            }
        } else if (touchX - getWidth() / 2 < touchY - getHeight() / 2 && touchY - getHeight() / 2 < - touchX + getWidth() / 2) {
            Log.i(TAG, "getDirection: " + "左");
            if (null != listener) {
                listener.onLeft();
            }
        } else if (touchX - getWidth() / 2 > touchY - getHeight() / 2 && touchY - getHeight() / 2 < - touchX + getWidth() / 2) {
            Log.i(TAG, "getDirection: " + "上");
            if (null != listener) {
                listener.onUp();
            }
        } else if (touchX - getWidth() / 2 > touchY - getHeight() / 2 && touchY - getHeight() / 2 > - touchX + getWidth() / 2) {
            Log.i(TAG, "getDirection: " + "右");
            if (null != listener) {
                listener.onRight();
            }
        }
    }

    public void setListener(IDirectionListener listener) {
        this.listener = listener;
    }

    public interface IDirectionListener {
        void onUp();
        void onDown();
        void onLeft();
        void onRight();

        /**
         * 手指抬起
         */
        void onStop();
    }

}
