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
import android.view.View;
import android.widget.Toast;


public class PicDrawTransTransfer extends View{
	private Paint paint = null;
	private Bitmap b1 = null;
	private Bitmap b2 = null;
	private Matrix matrix1, matrix2;
	private Map<String, String> mapValue = new HashMap<String, String>();
	Context context;

	public PicDrawTransTransfer(Context context, String[] allString) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		paint = new Paint();
		b1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.unionpay);
		String path2 = getSDPath();
		if(null == path2)
		{
			//TODO提示没有SD卡
			Toast.makeText(context, "手机没有sd卡", Toast.LENGTH_SHORT).show();
			return;
		}
		String path3 = path2+"/personInfo/Screen_1.png";
		b2 = BitmapFactory.decodeFile(path3);
		matrix1 = new Matrix();
		matrix2 = new Matrix();
		mapValue.put("terminalNo", allString[0]);
		mapValue.put("cardNo", allString[1]);
		mapValue.put("batchNo", allString[2]);
		mapValue.put("voucherNo", allString[3]);
		mapValue.put("authNo", allString[4]);
		mapValue.put("referNo", allString[5]);
		mapValue.put("dealDate", allString[6]);
		mapValue.put("dealTime", allString[7]);
		mapValue.put("amount", allString[8]);
		mapValue.put("inCardNo", allString[11]);
	}
	
	public void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, 290, 505, paint);
		matrix1.postScale(0.68f, 0.68f);
		matrix1.postTranslate(110, 3);
		canvas.drawBitmap(b1, matrix1, paint);
		matrix2.postScale(0.2f, 0.2f);
		matrix2.postTranslate(80, 350);
		canvas.drawBitmap(b2, matrix2, paint);
		paint.setColor(Color.BLACK);
		canvas.drawText("终端号(TERMINAL NO):", 10, 65, paint);
		canvas.drawText(mapValue.get("terminalNo"), 15, 80, paint);
		canvas.drawText("转出卡号(CARD NO):", 15, 95, paint);
		canvas.drawText(mapValue.get("cardNo"), 15, 110, paint);
		canvas.drawText("转入卡号(CARD NO):", 15, 125, paint);
		canvas.drawText(mapValue.get("inCardNo"), 15, 140, paint);
		canvas.drawText("交易类型(TRANS TYPE):", 10, 185, paint);
		canvas.drawText("消费/SALE(S)", 15, 200, paint);
		canvas.drawText("批次号(BATCH NO):", 10, 215, paint);
		canvas.drawText(mapValue.get("batchNo"), 121, 215, paint);
		canvas.drawText("凭证号(VOUCHER NO):", 10, 230, paint);
		canvas.drawText(mapValue.get("voucherNo"), 140, 230, paint);
		canvas.drawText("授权码(AUTH NO):", 10, 245, paint);
		canvas.drawText(mapValue.get("authNo"), 114, 245, paint);
		canvas.drawText("参考号(REFER NO):", 10, 260, paint);
		canvas.drawText(mapValue.get("referNo"), 122, 260, paint);
		canvas.drawText("交易日期(DATE):", 10, 275, paint);
		canvas.drawText(mapValue.get("dealDate"), 110, 275, paint);
		canvas.drawText("交易时间(TIME):", 10, 290, paint);
		canvas.drawText(mapValue.get("dealTime"), 110, 290, paint);
		canvas.drawText("操作员号(OPERATOR NO): 01", 10, 305, paint);
		canvas.drawText("金额(AMOUNT):  RMB", 10, 320, paint);
		canvas.drawText(mapValue.get("amount"), 130, 320, paint);
		canvas.drawText("备注(REFERENCE):", 10, 340, paint);
		canvas.drawText("持卡人签名CARDHOLDER SIGNATURE:", 10, 355, paint);
		canvas.drawText("本人确认以上交易", 10, 415, paint);
		canvas.drawText("同意将其记入本卡账户", 10, 430, paint);
		canvas.drawText("商户存根(MERCHANT COPY)", 10, 495, paint);
		canvas.drawText("I ACKNOWLEDGE SATISFACTORY RECEI", 10, 445, paint);
		canvas.drawText("PT OF RELATIVE GOODS/SERVICE", 10, 460, paint);
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
