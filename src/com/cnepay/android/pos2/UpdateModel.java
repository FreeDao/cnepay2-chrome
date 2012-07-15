package com.cnepay.android.pos2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class UpdateModel {

	private static final String TAG = "UpdateModel";
	private static final String DEFAULT_URL = "http://www.cnepay.com/airshop/version/version.txt";
	private Context context;

	public UpdateModel(Context ctx) {
		context = ctx.getApplicationContext();
	}

	public int update(String pkgname) {
		return update(pkgname, DEFAULT_URL);
	}

	private int update(String pkgname, String urlget) {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(urlget);
		HttpResponse httpResponse = null;
		int result = UpdateService.UNCHECKED;
		try {
			httpResponse = client.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = handleResp(httpResponse.getEntity(), pkgname);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private int handleResp(HttpEntity resp, String pkgname)
			throws UnsupportedEncodingException, IllegalStateException,
			IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				resp.getContent(), "UTF-8"));
		try {
			String line;
			final int datanum = 4;
			final int pkg = 0;
			final int ver = 1;
			final int src = 2;
			final int ext = 3;
			String[] arr = new String[datanum];
			int i = 0;
			Log.v(TAG, "pkgname = " + pkgname);
			while ((line = reader.readLine()) != null) {
				try {
					Log.i(TAG, line);
					if (i != 0 || line.equalsIgnoreCase(pkgname)) {
						arr[i++] = line;
						if (i == datanum) {
							if (!arr[i - 1].substring(0, 3).equalsIgnoreCase(
									"ext")) {
								arr[i - 1] = null;
							} else {
								arr[i - 1] = arr[i - 1].substring(4);
							}
							break;
						}
					}
				} catch (Exception e) {
					continue;
				}
			}
			Log.v(TAG, "pkg= " + arr[pkg]);
			Log.v(TAG, "ver= " + arr[ver]);
			Log.v(TAG, "src= " + arr[src]);
			Log.v(TAG, "ext= " + arr[ext]);
			if (pkgname.equalsIgnoreCase(arr[pkg])) {
				if (isNewVersion(arr[ver])) {
					Log.v(TAG, "update");
					notify(arr[ver], arr[src], arr[ext]);
					return UpdateService.CHECKED_NEED_UPGRADE;
				}
			}
		} finally {
			reader.close();
			Log.d(TAG, "DONE Handle Http Resp");
		}
		return UpdateService.CHECKED_NO_NEED;
	}

	private boolean isNewVersion(String v) {
		try {
			String current = getVersionName();
			Log.v(TAG, "current version = " + current);
			if (current.equals(v)) {
				return false;
			} else {
				float a = Float.valueOf(v);
				float b = Float.valueOf(current);
				if (a <= b) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private void notify(String version, String src, String ext) {
		if (version != null && src != null && ext != null) {
			Editor edit = context.getSharedPreferences("settings", 0).edit();
			edit.putString("ver", version);
			edit.putString("src", src);
			edit.putString("ext", ext);
			Log.v(TAG, version + "," + src + "," + ext);
			edit.commit();

			Intent intent = new Intent(context, UpdateActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			Log.e(TAG, "Server Configuration Error");
		}
	}

	private String getVersionName() throws Exception {
		PackageManager packageManager = context.getPackageManager(); // getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(
				context.getPackageName(), 0);
		return packInfo.versionName;
	}
}
