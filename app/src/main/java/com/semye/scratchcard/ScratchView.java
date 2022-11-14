package com.semye.scratchcard;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by yesheng on 16/6/2.
 * 刮刮卡view
 */
public class ScratchView extends View {

    private static final String TAG = "ScratchView";

    private Canvas mSeconedCanvas;//第二层画布

    private Bitmap bottomBitmap;// 底部图层的bitmap

    private Bitmap topBitmap;//顶部图层的bitmap

    private Paint mEraserPaint;// 橡皮擦画笔
    private Path mPath;// 用户绘制的路径
    private int mLastX;// 记录用户上一次触摸的X坐标
    private int mLastY;// 记录用户上一次触摸的Y坐标
    private Paint mTextPaint;// 绘制文本的画笔
    private Rect mTextBound;// 绘制一个矩形，记录刮奖信息文本的宽和高
    private String mBottomText;// 底层的文本

    private String mTopText;//上层的文字
    private float mTextSize;// 文字字体大小
    private int mTextColor;// 文本颜色

    private boolean isFirst = true;

    // 如果用户擦除区域大于60%那么我们就不绘制路径以及涂层
    // 解决该属性引起的并发问题
    // 使用volatile关键字进行修饰，从而保证它在被子线程修改后主线程还能对它的一个可见性
    private volatile boolean isComplete;

    private boolean bottomEnabled = true;//是否显示底部的文字

    private boolean topEnabled = true;//是否显示顶部的文字


    private static final String BOTTOM_TEXT = "谢谢惠顾";

    private static final String TOP_TEXT = "请刮开涂层";

    public ScratchView(Context context) {
        this(context, null);
    }

    public ScratchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScratchView, defStyleAttr, defStyleRes);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.ScratchView_bottom_text) {
                mBottomText = typedArray.getString(attr);
            } else if (attr == R.styleable.ScratchView_top_text) {
                mTopText = typedArray.getString(attr);
            } else if (attr == R.styleable.ScratchView_textColor) {
                mTextColor = typedArray.getColor(attr, Color.BLACK);
            } else if (attr == R.styleable.ScratchView_textSize) {
                mTextSize = typedArray.getDimension(attr, 15);
            }
        }
        typedArray.recycle();
        init();
    }

    /**
     * 设置是否显示底部文字
     *
     * @param enabled 是否显示
     */
    public void showBottomText(boolean enabled) {
        this.bottomEnabled = enabled;
    }

    /**
     * 设置是否显示顶部文字
     *
     * @param enabled 是否显示
     */
    public void showTopText(boolean enabled) {
        this.topEnabled = enabled;
    }


    /**
     * 进行一些初始化操作
     */
    private void init() {
        mPath = new Path();
        mTextBound = new Rect();
        if (mBottomText == null) mBottomText = BOTTOM_TEXT;
        if (mTopText == null) mTopText = TOP_TEXT;
        mTextPaint = PaintUtils.getTextPaint(mTextColor, mTextSize);
        mEraserPaint = PaintUtils.getEraser();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();


        if (bottomBitmap == null) drawBottom(width, height);
        if (topBitmap == null) drawTop(width, height);


        if (isComplete) {
            if (mListener != null && isFirst) {
                isFirst = false;
                mListener.complete();
            }
        }
        if (bottomBitmap != null) canvas.drawBitmap(bottomBitmap, 0, 0, null);

        if ((!isComplete) && topBitmap != null) {
            drawPath();
            canvas.drawBitmap(topBitmap, 0, 0, null);
        }
    }

    /**
     * 绘制表层可刮的图层
     *
     * @param width
     * @param height
     */
    private void drawTop(int width, int height) {
        Log.d(TAG, "top");
        mTextPaint.getTextBounds(mTopText, 0, mTopText.length(), mTextBound);
        topBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mSeconedCanvas = new Canvas(topBitmap);
        mSeconedCanvas.drawRoundRect(new RectF(0, 0, width, height), 0, 0, mEraserPaint);//画矩形
        if (topEnabled)
            mSeconedCanvas.drawText(mTopText, width / 2 - mTextBound.width() / 2, height / 2 + mTextBound.height() / 2, mTextPaint);//画文字
        mSeconedCanvas.drawBitmap(topBitmap, null, new Rect(0, 0, width, height), mEraserPaint);
    }

    /**
     * 绘制底层的图层 显示的内容
     *
     * @param width
     * @param height
     */
    private void drawBottom(int width, int height) {
        Log.d(TAG, "bottom");
        mTextPaint.getTextBounds(mBottomText, 0, mBottomText.length(), mTextBound);
        bottomBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(bottomBitmap);
        if (bottomEnabled)
            mCanvas.drawText(mBottomText, width / 2 - mTextBound.width() / 2, height / 2 + mTextBound.height() / 2, mTextPaint);
        mCanvas.drawBitmap(bottomBitmap, null, new Rect(0, 0, width, height), null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);// 获得用户横向坐标移动的绝对值
                int dy = Math.abs(y - mLastY);// 获得用户纵向坐标移动的绝对值
                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;

        }
        invalidate();
        return true;
    }

    /**
     * 异步计算用户擦除的面积
     */
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();
            // 擦除区域的大小
            float wipeArea = 0;
            // 控件区域总共的像素值
            float totalArea = w * h;
            Bitmap bitmap = topBitmap;// 涂层区域绘制在我们的bitmap上
            // 获取bitmap的所有像素信息
            int[] mPixels = new int[w * h];
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }

            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                // 如果擦除面积大于60%
                if (percent > 60) {
                    // 清楚掉涂层区域
                    isComplete = true;
                    // 因为在子线程中，不能使用invalidate(),必须使用postInvalidate()
                    postInvalidate();
                }
            }

        }
    };


    private void drawPath() {
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mSeconedCanvas.drawPath(mPath, mEraserPaint);
    }

    /**
     * 设置底部的文字
     *
     * @param text 文字内容
     */
    public void setBottomText(String text) {
        this.mBottomText = text;
    }

    /**
     * 设置顶部的文字
     *
     * @param text 文字内容
     */
    public void setTopText(String text) {
        this.mTopText = text;
    }

    /**
     * 刮刮卡刮完的一个回调
     */
    public interface OnScratchCompleteListener {
        void complete();
    }

    private OnScratchCompleteListener mListener;

    /**
     * 提供给外部的接口
     *
     * @param mListener 接口
     */
    public void setOnScratchCompleteListener(OnScratchCompleteListener mListener) {
        this.mListener = mListener;
    }


}