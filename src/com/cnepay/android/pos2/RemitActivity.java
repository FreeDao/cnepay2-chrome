package com.cnepay.android.pos2;

import com.tangye.android.dialog.AlertDialogBuilderWrapper;
import com.tangye.android.dialog.SwipeDialogController;
import com.tangye.android.utils.PublicHelper;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RemitActivity extends UIBaseActivity implements
		View.OnClickListener, OnCNAPSResultListener {

	private SwipeDialogController dialog;
	private TextView bank;
	private EditText txtRemit, txtCard, txtReCard, txtName;
	private String bankid;
	private Button btnNext;

	//private static final String TAG = "RemitActivity";
	private static final int ENABLE_TIMEOUT = 1000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remit);
		setTitle("付款服务");
		setActivityPara(true, true);
		setOnCNAPSResultListener(this); // 增加选择开户银行功能
		setTitleSubmitText("刷卡获得卡号");
		btnSubmit.setOnClickListener(this);
		showTitleSubmit();
		int[] attrs = new int[] { R.layout.swipe_dialog, R.style.dialog,
				R.id.dialog_anim, R.id.dialog_note };
		dialog = new SwipeDialogController(this, attrs);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				stopSwipe();
			}
		});

		txtRemit = (EditText) findViewById(R.id.remit_name);
		txtCard = (EditText) findViewById(R.id.remit_card);
		txtReCard = (EditText) findViewById(R.id.remit_recard);
		txtName = (EditText) findViewById(R.id.remit_remitter);

		bank = (TextView) findViewById(R.id.remit_bank);
		bank.setOnClickListener(this);
		bank.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					onClick(v);
				}
			}
		});

		btnNext = (Button) findViewById(R.id.remit_next);
		btnNext.setOnClickListener(this);

	}
	
	@Override
    protected void onPause() {
        super.onPause();
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_submit:
			if (this.isPlugged()) {
				dialog.setText("请刷卡...");
				dialog.show();
				this.startSwipe();
			} else {
				errText("请插入刷卡器");
			}
			break;
		case R.id.remit_bank:
			if (bankid != null && bankid.length() > 0) {
				AlertDialogBuilderWrapper builder = PublicHelper.getAlertDialogBuilder(RemitActivity.this);
		        builder.setTitle("开户银行信息")
		        .setIcon(android.R.drawable.ic_dialog_info)
		        .setMessage(bank.getText().toString() + "\n联行号：" + bankid + "\n\n是否修改？")
		        .setNegativeButton(android.R.string.cancel, null)
		        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int which) {
		            	chooseBank();
		        	}
		        });
		        builder.create().show();
			} else {
				chooseBank();
			}
			break;
		case R.id.remit_next:
			next();
			break;
		}
	}

	@Override
	public void onComplete(String cn) {
		dialog.dismiss();
		txtCard.setText(ci.getCard(false));
		txtReCard.setText(ci.getCard(false));
	}

	@Override
	public void onError(int err) {
		// 刷卡时，插拔卡可能会引起decodeError
		boolean isDecodeError = true;
		boolean isInterrupt = false;
		if (this.isPlugged()) {
			switch (err) {
			case E_API2_UNRESOLVED:
				errText("无法解析，请重试或者使用其他Android手机");
				break;
			case E_API2_FASTORSLOW:
				errText("刷卡过快或过慢，请重试");
				break;
			case E_API2_UNSTABLE:
				errText("刷卡不稳定，请重试");
				break;
			case E_API2_INVALID_DEVICE:
				errText("非法刷卡器，请使用正规对卡器");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
			default:
				isDecodeError = false;
			}
		} else {
			switch (err) {
			case E_API2_INVALID_DEVICE:
				errText("无法识别该刷卡器，请选择新的刷卡器或者重新拔插");
				break;
			case E_API2_INVALID_KSN:
				errText("非法刷卡器，请使用已注册绑定的刷卡器");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
				break;
			}
		}
		if (isDecodeError || isInterrupt) {
			dialog.dismiss();
		}
	}
	
	@Override
	public void onSwipe() {
		dialog.setText("接受刷卡数据...");
	}

	@Override
	public void onDecoding() {
		dialog.setText("正在解码...");
	}

	@Override
	public void onTimeout() {
		errText("未检测到刷卡动作");
		dialog.dismiss();
	}

	@Override
	public void onCNAPSResult(String BankName, String BankID) {
		bank.setText(BankName);
		bankid = BankID;
	}

	/************ private function *************/

	private void verify_failure(View v, String err) {
		if (v != null) {
			v.requestFocus();
		}
		btnNext.postDelayed(new Runnable() {
			public void run() {
				btnNext.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
		errText(err);
		return;
	}

	private void errText(String txt) {
		Toast t = Toast.makeText(this, txt, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	private void next() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		btnNext.setEnabled(false);
		final String remit = txtRemit.getText().toString();
		final String c1 = txtCard.getText().toString();
		final String c2 = txtReCard.getText().toString();
		final String bname = bank.getText().toString();
		final String user = txtName.getText().toString();
		if (remit.length() == 0) {
			verify_failure(txtRemit, "缺少收款用户姓名");
			return;
		}
		if (c1.length() == 0) {
			verify_failure(txtCard, "卡号信息为空");
			return;
		}
		if (c1.length() < 12 || c1.length() > 19) {
			verify_failure(txtCard, "卡号长度有误");
			return;
		}
		if(!testCard(c1)){
			verify_failure(txtCard, "银行卡号仅含有数字");
			return;
		}
		if (!c1.equals(c2)) {
			verify_failure(txtReCard, "两次的卡号输入不一致");
			txtReCard.setText("");
			return;
		}
		if (bname.length() == 0 || bankid.length() == 0) {
			verify_failure(bank, "银行信息不完整");
			return;
		}
		if (bankid.length() != 12) {
			verify_failure(bank, "联行号通过您的发卡银行客服获取");
			return;
		}
		if (user.length() == 0) {
			verify_failure(txtName, "缺少付款款人姓名");
			return;
		}
		
		AlertDialogBuilderWrapper builder = PublicHelper.getAlertDialogBuilder(RemitActivity.this);
        builder.setTitle("提示")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage("确保以上信息无误，则可继续完成付款操作\n\n是否继续付款？")
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		Intent i = new Intent(RemitActivity.this, Card2CardActivity.class);
        		i.putExtra("info", new String[] {remit, c1, bname, bankid, user});
        		startCallbackActivity(i);
        	}
        });
        builder.create().show();
		btnNext.postDelayed(new Runnable() {
			public void run() {
				btnNext.setEnabled(true);
			}
		}, ENABLE_TIMEOUT);
	}
	
	private boolean testCard(String card){
		if(card == null){
			return false;
		}
		if(card.matches("\\d*")){
			return true;
		}else{
			return false;
		}
			
	}
	/************* end private function **************/

}
