
package com.cnepay.android.pos2;

import java.io.ByteArrayOutputStream;
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

import com.tangye.android.dialog.CustomProgressDialog;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.utils.PublicHelper;
import com.tangye.android.utils.VoucherDraw;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

abstract public class ConsumeBaseActivity extends UIBaseActivity implements View.OnClickListener {

	private static final String TAG = "ConsumeBaseActivity";
	private static final int SIGNATURE_REQ = 1;
	private static final int IMAGE_QUALITY = 70;
	private static final boolean UPLOAD_ONLY_SIGNATURE = true;
	private static final String UPLOAD_URL;
	static {
		if (PublicHelper.isDebug) {
			UPLOAD_URL = "http://203.81.23.23:58080/tompms/transCurrent/uploadVoucher";
		} else {
			UPLOAD_URL = "http://203.81.23.4:18080/tompms/transCurrent/uploadVoucher";
		}
	}

	private static final int TIMEOUTSOCKET = 10000;
	private static final int TIMEOUTCONNECTION = 10000;
	private static final int SUCCESS = 0;
	private static final int FAILURE = 1;

	protected String[] m = null;

	private String voucherPath = null; // 记录保存路径
	private String voucherFileName = null; // 记录文件名
	private String traceID = null;
	private String signaturePath = null; // 签名
	private Button btnFinish, btnSign;
	private VoucherDraw view;
	private Handler mHandler;
	private CustomProgressDialog progressDialog;

	public void setContentView(int view) {
		throw new RuntimeException("should not call in child activity");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.consume); // should call super method
		setActivityPara(true, true);

		btnFinish = (Button)findViewById(R.id.charge_finish);
		btnFinish.setOnClickListener(this);
		btnSign = (Button)findViewById(R.id.charge_sign);
		btnSign.setOnClickListener(this);
		view = (VoucherDraw)findViewById(R.id.consume_ticket);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case SUCCESS:
						if (progressDialog != null) {
							progressDialog.dismiss();
							makeNotice("上传成功");
							finish();
						}
						break;
					case FAILURE:
						if (progressDialog != null) {
							progressDialog.cancel();
							String temp = (String)msg.obj;
							if (temp != null) {
								makeNotice(temp);
							} else {
								makeNotice("上传失败");
							}
						}
						break;
				}
				btnFinish.setEnabled(true);
			}
		};
		String extra = POSHelper.getSessionString();
		if (extra == null) {
			finish();
		}
		m = (String[])getIntent().getStringArrayExtra(extra);
		if (m == null) {
			finish();
		}
		setSource(view, signaturePath);

		// TEST should be in child
		/*
		 * setTraceId(m[12]); setFilePath("consumeHistory", m[13]);
		 */

	}

	public void setTraceId(String traceid) {
		traceID = traceid;
	}

	public void setFilePath(String path, String name) {
		voucherPath = path;
		voucherFileName = name;
	}

	abstract protected void setSource(VoucherDraw view0, String signaturePath0);

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.charge_sign:
				v.setEnabled(false);
				Intent intent = new Intent(ConsumeBaseActivity.this, SignatureActivity.class);
				startActivityForResult(intent, SIGNATURE_REQ);
				break;
			case R.id.charge_finish:
				uploadProcess();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SIGNATURE_REQ) {
			btnSign.setEnabled(true);
			if (resultCode == RESULT_OK) {
				if (data != null) {
					String extra = data.getStringExtra(VoucherDraw.INTENT_EXTRA);
					if (extra != null) {
						File file = new File(extra);
						if (file.exists()) {
							signaturePath = extra;
							setSource(view, signaturePath);
							btnFinish.setEnabled(true);
							if (!UPLOAD_ONLY_SIGNATURE) {
								file.delete();
							}
						}
						return;
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void uploadProcess() {
		btnFinish.setEnabled(false);
		if (voucherPath == null || voucherFileName == null) {
			makeNotice("Unset file name and path");
			finish();
			return;
		}
		String pathSD = getSDPath();
		Log.v(TAG, "pathSD = " + pathSD);
		if (null == pathSD) {
			makeNotice("没有SD卡无法保存");
			btnFinish.setEnabled(true);
			return;
		}
		if (signaturePath == null) {
			makeNotice("请先签名后再上传凭据。");
			btnFinish.setEnabled(true);
			return;
		}
		File signFile = null;
		if (UPLOAD_ONLY_SIGNATURE) {
			signFile = new File(signaturePath);
			if (!signFile.isFile()) {
				makeNotice("签名文件被删除，请重新签名。");
				btnFinish.setEnabled(true);
				return;
			}
		}

		progressDialog = PublicHelper.getProgressDialog(this, // context
				"", // title
				"交易凭证上传中", // message
				true, // 进度是否是不确定的，这只和创建进度条有关
				false);

		String SavePath = pathSD + "/personInfo";
		String consumeHistory = SavePath + "/" + voucherPath + "/";
		try {
			File path = new File(SavePath);
			File createPath = new File(consumeHistory);
			if (!path.exists()) {
				path.mkdirs();
			}
			if (!createPath.exists()) {
				createPath.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
			makeNotice(e.getMessage());
			progressDialog.cancel();
			btnFinish.setEnabled(true);
		}

		ByteArrayOutputStream bos = view.getBitmapOutputStream(IMAGE_QUALITY);
		File file = null;
		FileOutputStream stream = null;
		try {
			file = new File(consumeHistory + "/" + voucherFileName + ".jpg");
			if (file.exists() && file.isDirectory()) {
				file.delete();
			}
			stream = new FileOutputStream(file);
			bos.writeTo(stream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) {
					bos.flush();
					bos.close();
				}
				if (stream != null) {
					stream.flush();
					stream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (UPLOAD_ONLY_SIGNATURE && signFile != null) {
			file = signFile;
		}
		final File upfile = file;
		Log.v(TAG, "upfile = " + upfile);
		new Thread(TAG) {
			public void run() {
				String err = "";
				HttpParams httpParameters;
				httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUTCONNECTION);
				HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUTSOCKET);
				HttpClient client = new DefaultHttpClient(httpParameters);
				HttpPost httppost = new HttpPost(UPLOAD_URL);
				boolean isOK = false;
				try {
					MultipartEntity reqEntity = new MultipartEntity(
							HttpMultipartMode.BROWSER_COMPATIBLE);
					reqEntity.addPart("transId", new StringBody(traceID));
					reqEntity.addPart("img", new FileBody(upfile));

					httppost.setEntity(reqEntity);
					HttpResponse response = client.execute(httppost);
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK) {
						isOK = true;
					} else {
						err = "服务器错误，返回代码：" + statusCode;
					}
				} catch (ClientProtocolException e) {
					err = "协议错误，请重新上传";
				} catch (IOException e) {
					err = "连接错误，请重新上传";
				} catch (Exception e) {
					err = "未知错误";
				}
				if (!isOK) {
					mHandler.obtainMessage(FAILURE, err).sendToTarget();
				} else {
					mHandler.obtainMessage(SUCCESS, err).sendToTarget();
				}
			}
		}.start();
	}

	private void makeNotice(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (UPLOAD_ONLY_SIGNATURE && signaturePath != null) {
			File f = new File(signaturePath);
			f.delete();
		}
	}

}
