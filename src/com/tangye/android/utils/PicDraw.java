package com.tangye.android.utils;

import java.util.HashMap;
import java.util.Map;

import com.cnepay.android.pos2.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.view.View;


public class PicDraw extends View{

	private Paint paint = null;
	private Bitmap b1 = null;
	private Bitmap b2 = null;
	private Matrix matrix1, matrix2;
	private Map<String, String> mapValue = new HashMap<String, String>();
	Context context;
	private final String TAG = "PicDraw";

	public PicDraw(Context context, String[] allString) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		paint = new Paint();
		b1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.unionpay);
		String path2 = getSDPath();
		Log.v(TAG, "path2 = " + path2);
		if(null == path2)
		{
			//TODO提示没有SD卡
			return;
		}
		String path3 = path2+"/personInfo/Screen_1.png";
		b2 = BitmapFactory.decodeFile(path3);
		matrix1 = new Matrix();
		matrix2 = new Matrix();
		mapValue.put("merchantNo", allString[0]);
		mapValue.put("terminalNo", allString[1]);
		mapValue.put("cardNo", allString[2]);
		mapValue.put("batchNo", allString[3]);
		mapValue.put("voucherNo", allString[4]);
		mapValue.put("authNo", allString[5]);
		mapValue.put("referNo", allString[6]);
		mapValue.put("dealDate", allString[7]);
		mapValue.put("dealTime", allString[8]);
		mapValue.put("amount", allString[9]);
		mapValue.put("merchantName", allString[11]);
		mapValue.put("reference", allString[13]);
	}
	
	public void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, 290, 540, paint);
		//canvas.drawBitmap(b1, 110, 3, paint);
		matrix1.postScale(0.68f, 0.68f);
		matrix1.postTranslate(110, 3);
		canvas.drawBitmap(b1, matrix1, paint);
		matrix2.postScale(0.2f, 0.2f);
		matrix2.postTranslate(80, 395);
		canvas.drawBitmap(b2, matrix2, paint);
		paint.setColor(Color.BLACK);
		
		int y = 50;
		canvas.drawText("商户名(MERCHANT NAME):", 10, y, paint);
		canvas.drawText(mapValue.get("merchantName"), 15, (y = y + 15), paint);
		canvas.drawText("商户号(MERCHANT NO):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("merchantNo"), 15, (y = y + 15), paint);
		canvas.drawText("终端号(TERMINAL NO):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("terminalNo"), 15, (y = y + 15), paint);
		canvas.drawText("卡号(CARD NO):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("cardNo"), 15, (y = y + 15), paint);
		canvas.drawText("收单行名:", 10, (y = y + 35), paint);
		canvas.drawText("中国银行", 70, (y), paint);
		
		canvas.drawText("交易类型(TRANS TYPE):", 10, (y = y + 15), paint);
		canvas.drawText("消费/SALE(S)", 15, (y = y + 15), paint);
		canvas.drawText("批次号(BATCH NO):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("batchNo"), 121, (y), paint);
		canvas.drawText("凭证号(VOUCHER NO):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("voucherNo"), 140, (y), paint);
		canvas.drawText("授权码(AUTH NO):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("authNo"), 114, (y), paint);
		canvas.drawText("参考号(REFER NO):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("referNo"), 122, y, paint);
		canvas.drawText("交易日期(DATE):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("dealDate"), 110, y, paint);
		canvas.drawText("交易时间(TIME):", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("dealTime"), 110, y, paint);
		canvas.drawText("操作员号(OPERATOR NO): 01", 10, (y = y + 15), paint);
		canvas.drawText("金额(AMOUNT):  RMB", 10, (y = y + 15), paint);
		canvas.drawText(mapValue.get("amount"), 130, y, paint);
		canvas.drawText("备注(REFERENCE):", 10, (y = y + 20), paint);
		canvas.drawText(mapValue.get("reference"), 15, (y = y + 15), paint);
		canvas.drawText("持卡人签名CARDHOLDER SIGNATURE:", 10, (y = y + 15), paint);
		canvas.drawText("本人确认以上交易", 10, (y = y + 60), paint);
		canvas.drawText("同意将其记入本卡账户", 10, (y = y + 15), paint);
		canvas.drawText("I ACKNOWLEDGE SATISFACTORY RECEI", 10, (y = y + 15), paint);
		canvas.drawText("PT OF RELATIVE GOODS/SERVICE", 10, (y = y + 15), paint);
		canvas.drawText("商户存根(MERCHANT COPY)", 10, (y = y + 30), paint);
	}
	
	private String getSDPath(){ 
		boolean sdCardExist = Environment.getExternalStorageState()   
				.equals(Environment.MEDIA_MOUNTED); 
				if(sdCardExist)
				{     
					return (Environment.getExternalStorageDirectory()).toString(); 
				}
				return null; 
	}

}
