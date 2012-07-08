package com.tangye.android.utils;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class VoucherDraw extends View {

	private Paint paint = null;
	private FontMetrics fm = null;
	private Item items[] = null;
	private int mWidth, mHeight;
	
	private final static String TAG = "VoucherDraw";
	public final static String INTENT_EXTRA = "EXTRA";
	
	public static class Item {
		public Bitmap bitmap = null;
		public String text = null;
		public float fontSize, fontScaleX;
		public float x;
		public float top;
		public float bottom;
		public float height;
		public boolean center, bold;
		
		public Item(String content, float size, float scaleX, float left, float mTop, float mBottom, boolean isBold) {
			text = content;
			fontSize = size;
			fontScaleX = scaleX;
			x = left;
			top = mTop;
			bottom = mBottom;
			bold = isBold;
			center = false;
		}
		
		public Item(String content, float size, float scaleX, boolean mCenter, float mTop, float mBottom, boolean isBold) {
			text = content;
			fontSize = size;
			fontScaleX = scaleX;
			x = 0;
			center = mCenter;
			top = mTop;
			bottom = mBottom;
			bold = isBold;
		}
		
		public Item(Bitmap m, float mHeight, float left, float mTop, float mBottom) {
			bitmap = m;
			height = mHeight;
			x = left;
			top = mTop;
			bottom = mBottom;
			//center = false;
		}
		
		public Item(Bitmap m, float mHeight, boolean mCenter, float mTop, float mBottom) {
			bitmap = m;
			height = mHeight;
			x = 0;
			top = mTop;
			bottom = mBottom;
			center = mCenter;
		}
	}

	public VoucherDraw(Context context) {
		super(context);
		init();
	}
	
	public VoucherDraw(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VoucherDraw(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setResource(Item source[]) {
		if (source == null) {
			throw new IllegalArgumentException("source should not be null");
		}
		items = source;
		if(mWidth == 0) { 
			reset(source);
			return;
		}
		mHeight = configureH();
		if(mHeight == 0) {
			reset(source);
			return;
		}
		
		Log.i(TAG, "setResource width: " + mWidth + " height: " + mHeight);
		LayoutParams lp = getLayoutParams();
		lp.height = mHeight;
		setLayoutParams(lp);
		invalidate();
	}
	
	private void reset(final Item source[]) {
		postDelayed(new Runnable() {
			public void run() {
				setResource(source);
			}
		}, 200);
	}
	
	private void init() {
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		// setDrawingCacheEnabled(true);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		makeCanvas(canvas);
	}
	
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "width: " + w + " height: " + h);
        if (w != 0) {
	        mWidth = w;
        }
        if (h != 0) {
        	mHeight = h;
        }
    }

	private int configureH() {
		int h = 0;
		for(int i = 0; i < items.length; i++) {
			Item item = items[i];
			if (item.text != null) {
				paint.setTextSize(item.fontSize);
				paint.setTextScaleX(item.fontScaleX);
				paint.setFakeBoldText(item.bold);
				fm = paint.getFontMetrics();
				h += (int) (FloatMath.ceil(fm.descent - fm.top) + 2 + item.bottom + item.top);
			} else {
				h += item.bottom + item.top + item.height;
			}
		}
		return h;
	}

	private void makeCanvas(Canvas canvas) {				
		canvas.save();
		int h;
		float l;
		Paint bg = new Paint();
		bg.setColor(Color.WHITE);
		canvas.drawRect(0, 0, mWidth, mHeight, bg);
		for(int i = 0; i < items.length; i++) {
			Item item = items[i];
			if (item.text != null) {
				paint.setTextSize(item.fontSize);
				paint.setTextScaleX(item.fontScaleX);
				paint.setFakeBoldText(item.bold);
				fm = paint.getFontMetrics();
				h = (int) (FloatMath.ceil(fm.descent - fm.top) + 2 + item.bottom + item.top);
				l = item.x;
				if(item.center) {
					l += (mWidth - paint.measureText(item.text)) / 2;
				}
				canvas.drawText(item.text, l, fm.descent - fm.top, paint);
				canvas.translate(0, h);
			} else {
				h = (int) (item.bottom + item.height + item.top);
				Bitmap s = item.bitmap;
				if (s != null) {
					Matrix m = new Matrix();
					float scale = item.height / s.getHeight();
					m.postScale(scale, scale);
					l = item.x;
					if (item.center) {
						l += (mWidth - scale * s.getWidth()) / 2;
					}
					m.postTranslate(l, item.top);
					canvas.drawBitmap(s, m, null);
				}
				canvas.translate(0, h);
			}
		}
		canvas.restore();
	}

	public ByteArrayOutputStream getBitmapOutputStream(int quality) {
		int fixW = 300;
		int fixH = Math.round(290.0f / (float)mWidth * mHeight);
		Bitmap drawingCache = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		draw(new Canvas(drawingCache));
		if (drawingCache == null) {
			Log.i(TAG, "no drawing cache!!!");
			return null;
		}
		drawingCache = Bitmap.createScaledBitmap(drawingCache, fixW, fixH, true);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		drawingCache.compress(CompressFormat.JPEG, 60, os);
		drawingCache.recycle();
		return os;
	}

}
