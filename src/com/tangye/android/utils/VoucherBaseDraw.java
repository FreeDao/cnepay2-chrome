package com.tangye.android.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.cnepay.android.pos2.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class VoucherBaseDraw extends View {

	private Paint paint = null;
	private FontMetrics fm = null;
	private Item items[] = null;
	private int mWidth, mHeight;
	
	private final static String TAG = "VoucherBaseDraw";
	
	public static class Item {
		Bitmap bitmap = null;
		String text = null;
		float fontSize, fontScaleX, x, top, bottom, height;
		boolean bold;
		
		public Item(String content, float size, float scaleX, float left, float mTop, float mBottom, boolean isBold) {
			text = content;
			fontSize = size;
			fontScaleX = scaleX;
			x = left;
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
		}
	}

	public VoucherBaseDraw(Context context) {
		super(context);
		init();
	}
	
	public VoucherBaseDraw(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VoucherBaseDraw(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public boolean setResource(Item source[]) {
		if (source == null) {
			throw new IllegalArgumentException("source should not be null");
		}
		items = source;
		if(mWidth == 0) return false;
		mHeight = configureH();
		if(mHeight == 0) return false;
		
		Log.i(TAG, "setResource width: " + mWidth + " height: " + mHeight);
		LayoutParams lp = getLayoutParams();
		lp.height = mHeight;
		setLayoutParams(lp);
		invalidate();
		return false;
	}
	
	private void init() {
		paint = new Paint();
		setDrawingCacheEnabled(true);
		
		// test
		
		this.postDelayed(new Runnable() {
			public void run() {
				Item source[] = {
					new Item("商户名称: 7天酒店北京德胜门店", 30, 0.6f, 20, 20, 0, true),
					new Item("商户编号: BADFSFD24343DV4", 20, 1, 20, 0, 0, false),
					new Item("终端编号: 23487510", 20, 1, 20, 0, 0, false),
					new Item("用户签名: ", 30, 1, 20, 0, 100, false),
					new Item(
							BitmapFactory.decodeResource(getContext().getResources(), R.drawable.icon),
							100, 40, -100, 0),
					new Item("卡号: 622202*********2125 S", 30, 0.6f, 20, 0, 20, true)
				};
				setResource(source);
			}
		}, 2000);
		
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
        mWidth = w;
        mHeight = h;
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
				h += (int) (Math.ceil(fm.descent - fm.top) + 2 + item.bottom + item.top);
			} else {
				h += item.bottom + item.top + item.height;
			}
		}
		return h;
	}

	private void makeCanvas(Canvas canvas) {				
		canvas.save();
		int h;
		Paint bg = new Paint();
		bg.setColor(Color.WHITE);
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		
		for(int i = 0; i < items.length; i++) {
			Item item = items[i];
			if (item.text != null) {
				paint.setTextSize(item.fontSize);
				paint.setTextScaleX(item.fontScaleX);
				paint.setFakeBoldText(item.bold);
				fm = paint.getFontMetrics();
				h = (int) (Math.ceil(fm.descent - fm.top) + 2 + item.bottom + item.top);
				canvas.drawRect(0, 0, mWidth, h, bg);
				canvas.drawText(item.text, item.x, fm.descent - fm.top, paint);
				canvas.translate(0, h);
			} else {
				h = (int) (item.bottom + item.height + item.top);
				canvas.drawRect(0, 0, mWidth, h, bg);
				Bitmap s = item.bitmap;
				if (s != null) {
					Matrix m = new Matrix();
					float scale = item.height / s.getHeight();
					m.postScale(scale, scale);
					m.postTranslate(item.x, item.top);
					canvas.drawBitmap(s, m, null);
				}
				canvas.translate(0, h);
			}
		}
		
		canvas.restore();
	}

	public OutputStream getBitmapOutputStream(/*CompressFormat*/) {
		int fixW = 300;
		int fixH = Math.round(300f / (float)mWidth * mHeight);
		Bitmap drawingCache = getDrawingCache();
		drawingCache = Bitmap.createScaledBitmap(drawingCache, fixW, fixH, true);
		OutputStream os = new ByteArrayOutputStream();
		drawingCache.compress(CompressFormat.PNG, 0, os);
		drawingCache.recycle();
		return os;
	}

}
