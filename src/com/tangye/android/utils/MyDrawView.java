package com.tangye.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MyDrawView extends View {
	
	private static final float TOUCH_TOLERANCE = 4;
	private Paint   mPaint;
	private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private float   mX;
    private float   mY;
    private boolean isDrag;

    public MyDrawView(Context context) {
        super(context);
        init();
    }

    public MyDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyDrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
	    mPaint = new Paint();
	    mPaint.setStrokeWidth(5.0f);
	    mPaint.setColor(Color.BLACK);
	    mPaint.setAntiAlias(true);
	    mPaint.setDither(true);
	    mPaint.setStyle(Paint.Style.STROKE);
	    mPaint.setStrokeJoin(Paint.Join.ROUND);
	    mPaint.setStrokeCap(Paint.Cap.ROUND);
	    isDrag = false;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
	    canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }
	
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }
	
	private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
	
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);      
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }
    
    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();
    }
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrag = true;
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if(isDrag) {
                    touch_move(x, y);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isDrag) {
                    touch_up();
                    invalidate();
                }
                isDrag = false;
                break;
        }
        return true;
    }
        
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mCanvas == null) {
            mBitmap = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
    }
    
    public void clear() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath.reset();
        invalidate();
    }
    
    public Bitmap getBitmap() {
        return mBitmap;
    }
}
