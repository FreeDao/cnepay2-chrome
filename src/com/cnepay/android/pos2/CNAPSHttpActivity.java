package com.cnepay.android.pos2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tangye.android.dialog.AlertDialogBuilderWrapper;
import com.tangye.android.dialog.CustomProgressDialog;
import com.tangye.android.utils.PublicHelper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CNAPSHttpActivity extends Activity implements
		View.OnClickListener, DialogInterface.OnCancelListener,
		OnItemSelectedListener {
	/** Called when the activity is first created. */
	private static final String TAG = "CNAPSChooserActivity";
	private static final int FIND_SUCCESS = 0;
	private static final int JUMP_SUCCESS = 1;
	private static final int ENABLE_TIMEOUT = 1000;
	private static final int TIMEOUTCONNECTION = 10000;
	private static final int TIMEOUTSOCKET = 10000;
	private static final int FIND_FAILURE = 2;
	
	public static final String INTENT_EXTRA = "extra";

	EditText edKeyword;
	Button buttonFind;
	Handler fHandler;
	CustomProgressDialog progressDialog;
	Map<String, String> bankNames;
	TextView totalResult;
	TextView bankName;
	TextView bankId;
	Button jumpButton;
	String total = null;
	int totalPageNum = 0;
	TextView nowPage = null;
	EditText pageInput;
	LinearLayout hideLayout;
	View notice;
	Button commit;

	private ArrayAdapter<String> adapter;
	Spinner bankNameList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cnapshttp);
		buttonFind = (Button) findViewById(R.id.find_button);
		edKeyword = (EditText) findViewById(R.id.input_keyword);
		bankNameList = (Spinner) findViewById(R.id.show_bankname);
		totalResult = (TextView) findViewById(R.id.total_num);
		nowPage = (TextView) findViewById(R.id.now_page);
		jumpButton = (Button) findViewById(R.id.jump_page);
		pageInput = (EditText) findViewById(R.id.inputPage);
		notice = findViewById(R.id.notice_part);
		hideLayout = (LinearLayout) findViewById(R.id.hide_part);
		bankName = (TextView) findViewById(R.id.cnaps_bankname);
		bankId = (TextView) findViewById(R.id.cnaps_bankid);
		commit = (Button) findViewById(R.id.submit_btn_cnaps);

		fHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case FIND_SUCCESS:
					if (progressDialog != null) {
						progressDialog.cancel();
					}
					if (bankNames == null || total == null) {
						Toast.makeText(CNAPSHttpActivity.this, "无法找到",
								Toast.LENGTH_LONG).show();
						hideLayout.setVisibility(View.GONE);
						return;
					}
					if (bankNames.size() == 0) {
						Toast.makeText(CNAPSHttpActivity.this, "无法找到",
								Toast.LENGTH_LONG).show();
						hideLayout.setVisibility(View.GONE);
						return;
					}
					int totalPage = 0;
					if (Integer.parseInt(total) % 10 == 0) {
						totalPage = Integer.parseInt(total) / 10;
					} else {
						totalPage = Integer.parseInt(total) / 10 + 1;
					}
					totalPageNum = totalPage;
					totalResult.setText(getString(R.string.cnaps_page, totalPage));
					nowPage.setText("第 1 页");
					notice.setVisibility(View.GONE);
					hideLayout.setVisibility(View.VISIBLE);
					String[] m = new String[bankNames.size()];
					Set<String> setKey = bankNames.keySet();
					int i = 0;
					for (String string : setKey) {
						m[i] = string;
						i++;
					}
					adapter = new ArrayAdapter<String>(CNAPSHttpActivity.this,
							R.layout.list_spinner, m);
					adapter.setDropDownViewResource(R.layout.list_item);
					bankNameList.setAdapter(adapter);
					break;
				case JUMP_SUCCESS:
					if (progressDialog != null) {
						progressDialog.cancel();
					}
					if (bankNames == null || total == null) {
						Toast.makeText(CNAPSHttpActivity.this, "跳转错误",
								Toast.LENGTH_LONG).show();
						return;
					}
					int totalPage2 = 0;
					if (Integer.parseInt(total) % 10 == 0) {
						totalPage2 = Integer.parseInt(total) / 10;
					} else {
						totalPage2 = Integer.parseInt(total) / 10 + 1;
					}
					totalPageNum = totalPage2;
					totalResult.setText(getString(R.string.cnaps_page, totalPage2));
					nowPage.setText("第 " + msg.obj + " 页");
					String[] n = new String[bankNames.size()];
					Set<String> setKey2 = bankNames.keySet();
					int l = 0;
					for (String string : setKey2) {
						n[l] = string;
						l++;
					}
					adapter = new ArrayAdapter<String>(CNAPSHttpActivity.this,
							R.layout.list_spinner, n);
					adapter.setDropDownViewResource(R.layout.list_item);
					bankNameList.setAdapter(adapter);
					break;
				case FIND_FAILURE:
					if (progressDialog != null) {
						progressDialog.cancel();
						Toast.makeText(CNAPSHttpActivity.this,
								(String) msg.obj, Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		};
		buttonFind.setOnClickListener(this);
		jumpButton.setOnClickListener(this);
		bankNameList.setOnItemSelectedListener(this);
		commit.setOnClickListener(this);
	}

	public boolean getContentA(String bankKeyword, String page)
			throws UnsupportedEncodingException, IllegalStateException,
			IOException {
		// String s = "http://www.sina.com.cn"
		HttpParams httpParameters;
		String s1;
		String s2;
		boolean isOk = false;
		s2 = URLEncoder.encode(bankKeyword, "UTF-8");
		s1 = "http://203.81.23.23:18080/bankInfoSearch/doSearch";
		// s1 = "http://203.81.23.4:58080/bankInfoSearch/doSearch";
		String adressUrl = s1 + "?queryString=" + s2 + "&pageIndex=" + page
				+ "&pageSize=10";
		httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				TIMEOUTCONNECTION);
		HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUTSOCKET);
		HttpClient client = new DefaultHttpClient(httpParameters);
		HttpPost request = new HttpPost(adressUrl);
		HttpResponse httpResponse = null;
		httpResponse = client.execute(request);
		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			this.handleResp(httpResponse.getEntity());
			isOk = true;
			return isOk;
		} else {
			if (progressDialog != null) {
				progressDialog.cancel();
			}
		}
		return isOk;
	}
	
	private String fix(String bankn) {
		bankn = bankn.replace('(', '（');
		bankn = bankn.replace(')', '）');
		return bankn;
	}

	private void handleResp(HttpEntity resp) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					resp.getContent(), "UTF-8"));
			String line;
			StringBuffer temp = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				temp.append(line);
			}
			JSONArray jsonArray = new JSONArray(temp.toString());
			bankNames = new HashMap<String, String>();
			for (int i = 1; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String bankId = jsonObject.getString("bnkCode");
				String backName = fix(jsonObject.getString("lName"));
				bankNames.put(backName, bankId);
			}
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			total = jsonObject.getString("total");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		progressDialog = null;
		buttonFind.setEnabled(true);
		jumpButton.setEnabled(true);
		commit.setEnabled(true);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.find_button:
			InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			bankNames = null;
			buttonFind.setEnabled(false);
			jumpButton.setEnabled(false);
			commit.setEnabled(false);
			if (edKeyword.getText().toString() == null
					|| edKeyword.getText().toString().length() == 0) {
				verify_failure(edKeyword, "请输入关键字");
				return;
			}
			final String bankKeyword = edKeyword.getText().toString();
			progressDialog = PublicHelper.getProgressDialog(this, // context
					"", // title
					"查找中...", // message
					true, // 进度是否是不确定的，这只和创建进度条有关
					true, this);
			(new Thread(TAG) {
				public void run() {
					boolean isOK = false;
					String err = "";
					try {
						boolean flag = CNAPSHttpActivity.this.getContentA(
								bankKeyword, "1");
						if (flag == true) {
							Message msg = fHandler.obtainMessage(FIND_SUCCESS);
							fHandler.sendMessage(msg);
							isOK = true;
						} else {
							Message msg = fHandler.obtainMessage(FIND_FAILURE);
							err = "服务器返回信息错误";
							msg.obj = err;
							fHandler.sendMessage(msg);
							isOK = false;
						}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						err = "请检查手机的网络信号";
						e.printStackTrace();
					} catch (Exception e) {
						err = "请检查手机的网络信号";
						e.printStackTrace();
					}
					if (!isOK) {
						Message msg = fHandler.obtainMessage(FIND_FAILURE);
						msg.obj = err;
						fHandler.sendMessage(msg);
					}
				}
			}).start();
			break;
		case R.id.jump_page:
			InputMethodManager inputManager2 = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputManager2.hideSoftInputFromWindow(getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			bankNames = null;
			buttonFind.setEnabled(false);
			jumpButton.setEnabled(false);
			commit.setEnabled(false);
			if (totalResult.getText().toString() == null
					|| totalResult.getText().toString().length() == 0) {
				verify_failure(totalResult, "页面出错，请重新查找");
				return;
			}
			if (pageInput.getText().toString().equals("0")) {
				verify_failure(pageInput, "跳转页码不能为0");
				return;
			}
			if (pageInput.getText().toString() == null
					|| pageInput.getText().toString().length() == 0) {
				verify_failure(pageInput, "未输入跳转的页码");
				return;
			}
			int input = Integer.parseInt(pageInput.getText().toString());
			int getTotalNum = totalPageNum;
			if (input > getTotalNum) {
				verify_failure(pageInput, "输入页面大于总页面");
				return;
			}
			final String bankKeywordJ = edKeyword.getText().toString();
			final String numPage = pageInput.getText().toString();
			progressDialog = PublicHelper.getProgressDialog(this, // context
					"", // title
					"查找中...", // message
					true, // 进度是否是不确定的，这只和创建进度条有关
					true, this);
			(new Thread(TAG) {
				public void run() {
					String err = "";
					boolean isOK = false;
					try {
						boolean flag = CNAPSHttpActivity.this.getContentA(
								bankKeywordJ, numPage);
						if (flag == true) {
							Message msg = fHandler.obtainMessage(JUMP_SUCCESS);
							msg.obj = numPage;
							fHandler.sendMessage(msg);
							isOK = true;
						} else {
							Message msg = fHandler.obtainMessage(FIND_FAILURE);
							err = "服务器返回信息错误";
							msg.obj = err;
							fHandler.sendMessage(msg);
							isOK = false;
						}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						err = "请检查手机的网络信号";
						e.printStackTrace();
					}
					if (!isOK) {
						Message msg = fHandler.obtainMessage(FIND_FAILURE);
						msg.obj = err;
						fHandler.sendMessage(msg);
					}
				}
			}).start();
			break;

		case R.id.submit_btn_cnaps:
			commit.setEnabled(false);
			final String a = bankName.getText().toString();
			if (a.length() == 0 || a == null) {
				return;
			}
			final String b = bankId.getText().toString();
			if (b.length() == 0 || b == null) {
				return;
			}
			AlertDialogBuilderWrapper builder = PublicHelper.getAlertDialogBuilder(this);
			builder.setMessage("请再次确保您输入了正确的开户支行信息，如此信息有误，转账会因此失败。若您不清楚您的开户支行信息，请与发卡银行的客服联系。");
			builder.setTitle("提示");
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent();
							i.putExtra(INTENT_EXTRA, new String[] { a, b });
							setResult(RESULT_OK, i);
							finish();
							dialog.dismiss();
						}
					});
			builder.setNegativeButton(android.R.string.cancel,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							commit.setEnabled(true);
						}
					});
			builder.setCancelable(false);
			builder.create().show();
			break;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		UIBaseActivity.updateLastLeaveTime();
	}

	// jumpButton
	private void verify_failure(View v, String err) {
		if (v != null) {
			v.requestFocus();
		}
		buttonFind.postDelayed(new Runnable() {
			public void run() {
				buttonFind.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		jumpButton.postDelayed(new Runnable() {
			public void run() {
				jumpButton.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		commit.postDelayed(new Runnable() {
			public void run() {
				commit.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		makeError(err);
		return;
	}

	public void makeError(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		TextView tv = (TextView) arg1;
		tv.setTextSize(12.0f);
		// TODO Auto-generated method stub
		bankName.setText(bankNameList.getSelectedItem().toString());
		String cnapsNum = bankNames.get(bankNameList.getSelectedItem()
				.toString());
		bankId.setText(cnapsNum);
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		bankName.setText(null);
	}
}