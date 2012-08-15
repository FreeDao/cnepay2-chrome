package com.cnepay.android.pos2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tangye.android.dialog.CustomProgressDialog;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.utils.PublicHelper;

public class IDPhotoActivity extends UIBaseActivity implements
		View.OnClickListener {
	private static final String TAG = "IDPhotoActivity";
	// 上传服务器地址
	private static final String UPLOAD_URL;
	static {
		if (PublicHelper.isDebug) {
			UPLOAD_URL = "http://203.81.23.4:18080/tompms/merchant/uploadImg";
		} else {
			UPLOAD_URL = "http://203.81.23.4:18080/tompms/merchant/uploadImg";
		}
	}
	private static final int UPLOAD_SUCCESS = 0;
	private static final int UPLOAD_FAILURE = 1;
	private static final int TIMEOUTSOCKET = 10000;
	private static final int TIMEOUTCONNECTION = 10000;
	private static final int RET_CODE_FRONT = 1001;
	private static final int RET_CODE_BACK = 1002;

	private String filename_front;
	private String filename_back;
	private String filename_gray_front;
	private String filename_gray_back;
	private CustomProgressDialog progressDialog;
	private View v1, v2;
	private ImageView img1, img2;
	private Handler mHandler;
	private File vDirPath;
	private String path;
	private int vHeight;
	private int vWidth;
	private File vFile1;
	private File vFile2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.idphoto);
		setTitle("实名认证");
		setActivityPara(true, true);
		v1 = findViewById(R.id.photo_viewer);
		v2 = findViewById(R.id.photo_viewer2);
		img1 = (ImageView) findViewById(R.id.photo_img_viewer);
		img2 = (ImageView) findViewById(R.id.photo_img_viewer2);
		path = getSDPath();
		if (null == path) {
			makeInfo("没有找到SD卡，无法拍照!");
			return;
		}
		
		v1.setOnClickListener(this);
		v2.setOnClickListener(this);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPLOAD_SUCCESS:
					String deleteP1 = path + "/personInfo/front.jpg";
					String deleteP2 = path + "/personInfo/back.jpg";
					String deleteP3 = path
							+ "/personInfo/cnepay_id_card_gray_front.png";
					String deleteP4 = path
							+ "/personInfo/cnepay_id_card_gray_back.png";
					File deleteFile1 = new File(deleteP1);
					File deleteFile2 = new File(deleteP2);
					File deleteFile3 = new File(deleteP3);
					File deleteFile4 = new File(deleteP4);
					if (deleteFile1.exists()) {
						deleteFile1.delete();
					}
					if (deleteFile2.exists()) {
						deleteFile2.delete();
					}
					if (deleteFile3.exists()) {
						deleteFile3.delete();
					}
					if (deleteFile4.exists()) {
						deleteFile4.delete();
					}
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
						makeInfo("上传成功");
						finish();
					}
					break;
				case UPLOAD_FAILURE:
					if (progressDialog != null) {
						progressDialog.cancel();
						String temp = (String) msg.obj;
						if (temp != null) {
							makeInfo(temp);
						} else {
							makeInfo("上传失败");
						}
					}
					break;
				}
			}
		};
		filename_front = null;
		filename_back = null;
		filename_gray_front = path
				+ "/personInfo/cnepay_id_card_gray_front.png";
		filename_gray_back = path + "/personInfo/cnepay_id_card_gray_back.png";

		String imgPath = path + "/personInfo/front.jpg";
		vFile1 = new File(imgPath);
		String imgPath2 = path + "/personInfo/back.jpg";
		vFile2 = new File(imgPath2);
		if (!vFile1.exists()) {
			vDirPath = vFile1.getParentFile(); // new File(vFile.getParent());
			vDirPath.mkdirs();
		}
		// 上传
		findViewById(R.id.upload_idphoto).setOnClickListener(this);
		if (savedInstanceState != null) {
			String tmp = savedInstanceState.getString("front");
			if (tmp != null) {
				mHandler.post(new DetectRunnable(img1, tmp));
			}
			tmp = savedInstanceState.getString("back");
			if (tmp != null) {
				mHandler.post(new DetectRunnable(img2, tmp));
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		String path = getSDPath();
		if (null == path) {
			makeInfo("没有找到SD卡，无法拍照!");
			return;
		}
		// 查找时间最新的照片文件
		// File f = new File(path + PHOTO_LIB_PATH);
		File f = new File(path + "/personInfo");
		if (!f.isDirectory())
			return;
		File[] fs = f.listFiles();
		File latest = null;
		long l = 0;
		for (int i = 0; i < fs.length; i++) {
			Log.i("time", "" + fs[i].lastModified());
			if (fs[i].lastModified() > l) {
				latest = fs[i];
				l = fs[i].lastModified();
			}
		}
		if (null == latest)
			return;
		final ImageView v;
		if (RET_CODE_FRONT == requestCode) {
			filename_front = latest.getAbsolutePath();
			v = img1;
		} else if (RET_CODE_BACK == requestCode) {
			filename_back = latest.getAbsolutePath();
			v = img2;
		} else {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}
		mHandler.post(new DetectRunnable(v, latest.getAbsolutePath()));
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		if (filename_front != null) {
			outState.putString("front", filename_front);
		}
		if (filename_back != null) {
			outState.putString("back", filename_back);
		}
		super.onSaveInstanceState(outState);
	}
	
	class DetectRunnable implements Runnable {
		private String path;
		private ImageView view;
		
		public DetectRunnable(ImageView v, String file) {
			path = file;
			view = v;
		}
		
		@Override
		public void run() {
			vHeight = view.getMeasuredHeight();
			vWidth = view.getMeasuredWidth();
			if (vHeight == 0 || vWidth == 0) {
				mHandler.postDelayed(this, 100);
				return;
			}
			showPhoto(view, path);
		}
	}

	private void makeInfo(String info) {
		Toast t = Toast.makeText(this, info, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	private boolean prepareFile() {
		// 上传图片的大致尺寸，用于采样
		int upload_height = 400;
		int upload_width = 400;
		try {
			if (filename_front == null) {
				Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
				msg.obj = "没有拍摄身份证正面！";
				mHandler.sendMessage(msg);
				return false;
			}
			File file_front = new File(filename_front);
			if (!file_front.exists()) {
				Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
				msg.obj = "没有拍摄身份证正面！";
				mHandler.sendMessage(msg);
				return false;
			}
			if (filename_back == null) {
				Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
				msg.obj = "没有拍摄身份证背面！";
				mHandler.sendMessage(msg);
				return false;
			}
			File file_back = new File(filename_back);
			if (!file_back.exists()) {
				Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
				msg.obj = "没有拍摄身份证背面！";
				mHandler.sendMessage(msg);
				return false;
			}

			File file_gray_front = new File(filename_gray_front);
			if (file_gray_front.exists())
				file_gray_front.delete();

			File file_gray_back = new File(filename_gray_back);
			if (file_gray_back.exists())
				file_gray_back.delete();

			// 只读取图片尺寸
			BitmapFactory.Options options_front = new BitmapFactory.Options();
			options_front.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filename_front, options_front);
			Boolean scaleByHeight = Math.abs(options_front.outHeight
					- upload_height) >= Math.abs(options_front.outWidth
					- upload_width);
			if (options_front.outHeight * options_front.outWidth * 2 >= 200 * 100 * 2) {
				// Load, scaling to smallest power of 2 that'll get it <=
				// desired dimensions
				double sampleSize = scaleByHeight ? options_front.outHeight
						/ upload_height : options_front.outWidth / upload_width;
				options_front.inSampleSize = (int) Math.pow(2d,
						Math.floor(Math.log(sampleSize) / Math.log(2d)));
			}

			options_front.inJustDecodeBounds = false;
			Bitmap bitmap_front = BitmapFactory.decodeFile(filename_front,
					options_front);
			Bitmap graybmp_front = toGrayscale(bitmap_front);
			bitmap_front.recycle();
			bitmap_front = null;
			FileOutputStream out_front = new FileOutputStream(file_gray_front);
			graybmp_front.compress(CompressFormat.PNG, 90, out_front);
			out_front.close();
			graybmp_front.recycle();
			graybmp_front = null;
			BitmapFactory.Options options_back = new BitmapFactory.Options();
			options_back.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filename_back, options_back);
			scaleByHeight = Math.abs(options_back.outHeight - upload_height) >= Math
					.abs(options_back.outWidth - upload_width);
			if (options_back.outHeight * options_back.outWidth * 2 >= 200 * 100 * 2) {
				// Load, scaling to smallest power of 2 that'll get it <=
				// desired dimensions
				double sampleSize = scaleByHeight ? options_back.outHeight
						/ upload_height : options_back.outWidth / upload_width;
				options_back.inSampleSize = (int) Math.pow(2d,
						Math.floor(Math.log(sampleSize) / Math.log(2d)));
			}

			options_back.inJustDecodeBounds = false;
			Bitmap bitmap_back = BitmapFactory.decodeFile(filename_back,
					options_back);
			Bitmap graybmp_back = toGrayscale(bitmap_back);
			bitmap_back.recycle();
			bitmap_back = null;
			FileOutputStream out_back = new FileOutputStream(file_gray_back);
			graybmp_back.compress(CompressFormat.PNG, 90, out_back);
			out_back.close();
			graybmp_back.recycle();
			graybmp_back = null;
			System.gc();
			return true;
		} catch (Exception ex) {
			Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
			msg.obj = ex.getMessage();
			mHandler.sendMessage(msg);
		}
		return false;
	}

	private boolean uploadFile() {
		File file_gray_front = new File(filename_gray_front);
		if (!file_gray_front.exists()) {
			Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
			msg.obj = "图像处理错误";
			mHandler.sendMessage(msg);
			return false;
		}
		File file_gray_back = new File(filename_gray_back);
		if (!file_gray_back.exists()) {
			Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
			msg.obj = "图像处理错误";
			mHandler.sendMessage(msg);
			return false;
		}
		HttpPost httppost = new HttpPost(UPLOAD_URL);
		String info = "";
		boolean isOK = false;
		POSSession SESSION = POSHelper.getPOSSession(IDPhotoActivity.this,
				false);
		String name = SESSION.getSessionAccount();
		SESSION.close();
		if (name == null) {
			Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
			msg.obj = "商户号不存在，请重新登录";
			mHandler.sendMessage(msg);
			return false;
		}
		POSEncrypt POS = POSHelper.getPOSEncrypt(IDPhotoActivity.this, name);
		POS.addTraceNumber();
		HttpParams httpParameters;
		try {
			// POS.getPOSDecrypt(POS.USERMARK))
			httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					TIMEOUTCONNECTION);
			HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUTSOCKET);
			HttpClient client = new DefaultHttpClient(httpParameters);
			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("merchantNo",
					new StringBody(POS.getPOSDecrypt(POS.USERMARK)));
			// reqEntity.addPart("merchantNo", new
			// StringBody("000000000004771"));
			POS.close();
			reqEntity.addPart("front", new FileBody(file_gray_front));
			reqEntity.addPart("back", new FileBody(file_gray_back));

			httppost.setEntity(reqEntity);
			HttpResponse response = client.execute(httppost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				//String str = EntityUtils.toString(response.getEntity());
				info = "上传成功!";
				isOK = true;
			} else {
				info = "网站错误!";
			}
		} catch (ClientProtocolException e) {
			info = e.getMessage();
		} catch (IOException e) {
			info = "网络连接错误，请重试";
		}
		if (isOK) {
			Message msg = mHandler.obtainMessage(UPLOAD_SUCCESS);
			msg.obj = info;
			mHandler.sendMessage(msg);
		} else {
			Message msg = mHandler.obtainMessage(UPLOAD_FAILURE);
			msg.obj = info;
			mHandler.sendMessage(msg);
		}
		return true;
	}

	private void showPhoto(ImageView view, String filename) {
		Log.i(TAG, "finish width: " + vWidth + " height: " + vHeight);
		File file = new File(filename);
		if (!file.exists())
			return;
		Drawable temp = view.getDrawable();
		if (null != temp && temp.getIntrinsicHeight() > 100) {
			view.setImageDrawable(null);
			temp.setCallback(null);
			System.gc();
		}
		// 只读取图片尺寸
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);
		Boolean scaleByHeight = Math
				.abs(options.outHeight - vHeight) >= Math
				.abs(options.outWidth - vWidth);
		if (options.outHeight * options.outWidth * 2 >= 200 * 100 * 2) {
			// Load, scaling to smallest power of 2 that'll get it <= desired
			double sampleSize = scaleByHeight ? options.outHeight
					/ vHeight : options.outWidth / vWidth;
			options.inSampleSize = (int) Math.pow(2d,
					Math.floor(Math.log(sampleSize) / Math.log(2d)));
			//Log.i(TAG, "photo sample size: " + options.inSampleSize);
		}
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
		view.setImageBitmap(bitmap);
	}

	private Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();
		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix(new float[] { 0.5f, 0.5f, 0.5f, 0, 0,
				0.5f, 0.5f, 0.5f, 0, 0, 0.5f, 0.5f, 0.5f, 0, 0, 0, 0, 0, 1, 0,
				0, 0, 0, 0, 0, 1, 0 });
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		Uri outputFileUri;
		switch(v.getId()) {
		case R.id.photo_viewer:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			outputFileUri = Uri.fromFile(vFile1);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			startActivityForResult(intent, RET_CODE_FRONT);
			break;
		case R.id.photo_viewer2:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			outputFileUri = Uri.fromFile(vFile2);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			startActivityForResult(intent, RET_CODE_BACK);
			break;
		case R.id.upload_idphoto:
			progressDialog = PublicHelper.getProgressDialog(
					IDPhotoActivity.this, // context
					"", // title
					"证件照片正在上传...", // message
					true, // 进度是否是不确定的，这只和创建进度条有关
					false);

			(new Thread(TAG) {
				public void run() {
					if (prepareFile())
						uploadFile();
					if (progressDialog != null) {
						progressDialog.cancel();
					}
				}
			}).start();
			break;
		}
	}
}