package com.cnepay.android.pos2;

import com.tangye.swipedialog.SwipeDialogController;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends UIBaseActivity implements View.OnClickListener {

	private SwipeDialogController dialog;
	private TextView card;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reg);
		
		btnSubmit.setOnClickListener(this);
		int[] attrs = new int[] {
        	R.layout.swipe_dialog,
        	R.style.dialog,
        	R.id.dialog_anim,
        	R.id.dialog_note
        };
        dialog = new SwipeDialogController(this, attrs); 
        dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				stopSwipe();
			}
        });
        
        card = (TextView) this.findViewById(R.id.reg_account_number);
        card.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.reg_account_number:
			if (this.isPlugged()) {
				dialog.setText("请刷卡...");
				dialog.show();
				this.startSwipe();
			} else {
				Toast.makeText(this, "请插入读卡器", Toast.LENGTH_SHORT);
			}
			break;
		}
	}
	
	@Override
	public void onComplete(String cn) {
		dialog.dismiss();
		card.setText(cn);
	}
	
	@Override
	public void onError(int err) {
		// 刷卡时，插拔卡可能会引起decodeError
		if (this.isPlugged()) {
			boolean isDecodeError = true;
			switch(err) {
			case E_API2_UNRESOLVED:
				errText("无法解析，请重试或者使用其他Android手机");
				break;
			case E_API2_FASTORSLOW:
				errText("刷卡过快或过慢，请重试");
				break;
			case E_API2_UNSTABLE:
				errText("刷卡不稳定，请重试");
				break;
			default:
				isDecodeError = false;	
			}
			if (isDecodeError) {
				dialog.dismiss();
			}
		}
	}
	
	@Override
	public void onDecoding() {
		if (dialog != null) {
			dialog.setText("正在解码...");
		}
	}
	
	@Override
	public void onTimeout() {
		Toast.makeText(this, "timeout", Toast.LENGTH_SHORT).show();
		this.showTitleSubmit();
	}
	
	private void errText(String txt) {
		Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
	}
	
}
