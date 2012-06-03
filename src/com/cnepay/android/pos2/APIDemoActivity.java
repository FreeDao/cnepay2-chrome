package com.cnepay.android.pos2;

import com.tangye.swipedialog.SwipeDialogController;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class APIDemoActivity extends UIBaseActivity implements View.OnClickListener {

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
        SwipeDialogController sd = new SwipeDialogController(this, attrs); 
        sd.show();
        sd.setText("请刷卡...");
	}
	
	@Override
	public void onPlugin() {
		super.onPlugin();
		this.showTitleSubmit();
	}
	
	@Override
	public void onPlugout() {
		super.onPlugout();
		this.hideTitleSubmit();
	}

	@Override
	public void onClick(View v) {
		this.startSwipe();
		this.hideTitleSubmit();
	}
	
	@Override
	public void onComplete(String cn) {
		Toast.makeText(this, cn, Toast.LENGTH_SHORT).show();
		this.showTitleSubmit();
	}
	
	@Override
	public void onError(int err) {
		Toast.makeText(this, "Error = " + err, Toast.LENGTH_SHORT).show();
		// 刷卡时，插拔卡可能会引起decodeError
		if (this.isPlugged()) {
			this.showTitleSubmit();
		}
	}
	
	@Override
	public void onTimeout() {
		Toast.makeText(this, "timeout", Toast.LENGTH_SHORT).show();
		this.showTitleSubmit();
	}
	
}
