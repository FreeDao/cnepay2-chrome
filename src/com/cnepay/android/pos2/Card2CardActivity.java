package com.cnepay.android.pos2;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;

import com.cnepay.android.pos2.PasswordInputMethod.PasswordInputMethodListener;

import com.tangye.android.dialog.AlertDialogBuilderWrapper;
import com.tangye.android.dialog.CustomProgressDialog;
import com.tangye.android.dialog.SwipeDialogController;
import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.POSEncrypt;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;
import com.tangye.android.iso8583.protocol.TransferMessage;
import com.tangye.android.utils.CardInfo;
import com.tangye.android.utils.HandlingFee;
import com.tangye.android.utils.PublicHelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Card2CardActivity extends UIBaseActivity implements
		View.OnClickListener, PasswordInputMethodListener {
	private EditText txtInput, txtPassword;
	private Button[] btns;
	private Button fnButton;
	private View delButton;
	private TextView noteSwipe, card;
	private SwipeDialogController dialog;
	private CashInputMethod cashIM;
	private PasswordInputMethod passwdIM;
	private ViewGroup framePass, layoutMask;
	private ImageView imgCardType, imgCardReader;
	private TransferMessage s;
	private Handler mHandler;
	private boolean isProcessing;
	private String user1, user2, bankid2, bankname2, card2;
	private HandlingFee amountH;
	private CustomProgressDialog progressDialog;

	private final static int SUCCESS = 0;
	private final static int FAILURE = 1;
	private final static String TAG = "Card2CardActivity";
	private final static long MAX_AMOUNT = 5000000;		//以分为单位

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card2card);
		setTitle("付款服务");
		setActivityPara(true, true);
		
		Intent i = getIntent();
		String all[] = i.getStringArrayExtra("info");
		if (all == null || all.length != 5) {
			finish();
		}
		user2 = all[0]; // 收款姓名
		card2 = all[1]; // 收款卡号
		bankname2 = all[2]; // 收款银行
		bankid2 = all[3]; //收款联行号
		user1 = all[4]; //汇款姓名
		
		btnSubmit.setOnClickListener(this);
		initUI();
		cashIM = new CashInputMethod(btns, fnButton, delButton, txtInput);
		passwdIM = new PasswordInputMethod(btns, fnButton, delButton, txtPassword, this);
		cashIM.init();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SUCCESS:
					String[] all = (String[]) msg.obj;
					if (progressDialog != null && all != null && all.length <= 14) {
						// register successfully
						progressDialog.dismiss();
						progressDialog = null; // For not fade card number
						// TODO transfer successfully
						makeError("付款成功!");
						Intent i = new Intent(Card2CardActivity.this, CashTransferActivity.class);
	        			String extra = POSHelper.getSessionString();
	        			if (extra == null) {
	        				makeError("登录过期");
	        				finish();
	        				return;
	        			}
	        			i.putExtra(extra, all);
	        			//startActivity(i);
	        			startResponseActivity(i);
	        			finish();
	        		}
		        	break;
        		case FAILURE:
        			String e = (String) msg.obj;
        			if(progressDialog != null) {
                        progressDialog.cancel();
                    }
        			card.setText("");
        			framePass.setVisibility(View.GONE);
        			noteSwipe.setVisibility(View.VISIBLE);
        			ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
        	                Animation.RELATIVE_TO_SELF, 0.5f, 
        	                Animation.RELATIVE_TO_SELF, 0.5f);
        	        sa.setDuration(200);
        	        imgCardType.startAnimation(sa);
        	        imgCardType.postDelayed(new Runnable() {
        	            public void run() {
        	            	imgCardType.setVisibility(View.GONE);
        	            }
        	        }, 200);
        			cashIM.init();
        			if(isPlugged()) {
        				showTitleSubmit();
        			}
        			if (e != null) {
        				makeError(e);
        			}
        			// TODO need to 冲正
        			break;
        		}
        		// TODO dismiss dialog?
        	}
		};
	}

	@Override
	public void onPasswordCancel() {
		Log.v(TAG, "onPasswordCancel");
		card.setText("");
		framePass.setVisibility(View.GONE);
		noteSwipe.setVisibility(View.VISIBLE);
		ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
                Animation.RELATIVE_TO_SELF, 0.5f, 
                Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(200);
        imgCardType.startAnimation(sa);
        imgCardType.postDelayed(new Runnable() {
            public void run() {
            	imgCardType.setVisibility(View.GONE);
            }
        }, 200);
		cashIM.init();
		if(isPlugged()) {
			showTitleSubmit();
		}
	}

	@Override
	public void onSubmit(String password) {
		// TODO clear screen::::::::::::::::::::::::::::::::::::::
		Log.v(TAG, "onSubmit");
		if(!doRequest(password)) {
			// TODO finish();
			// dismiss progress dialog
			isProcessing = false;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 这个地方我们必须知道layout的信息，不合适
		case R.id.title_submit:
			InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			if (cashIM.getCashInCents() == 0) {
				txtInput.requestFocus();
				makeError("请输入交易金额");
				return;
			}
			if(cashIM.getCashInCents() > MAX_AMOUNT){
				txtInput.requestFocus();
				makeError("付款最大金额为50000元");
				return;
			}
			hideTitleSubmit();
			txtInput.clearFocus();
			dialog.setText("请刷卡...");
			dialog.show();
			startSwipe();
			// noteSwipe.setText("");
			break;
		}
	}

	@Override
	public void onComplete(String cn) {
		dialog.dismiss();
		
		/**
		 * only registered card
		 */
		POSSession session = POSHelper.getPOSSession();
		if (ci != null && session != null) {
			if(!session.getCardNumber().equals(ci.getCard(false))) {
				makeError("只能使用注册卡号付款！");
				cashIM.init();
				if(isPlugged()) {
					showTitleSubmit();
				}
				return;
			}
		}
		//////////////////////////////
		
		card.setText(cn);
		imgCardType.setVisibility(View.VISIBLE);
    	ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, 
    	        Animation.RELATIVE_TO_SELF, 0.5f, 
    	        Animation.RELATIVE_TO_SELF, 0.5f);
    	sa.setDuration(200);
    	imgCardType.startAnimation(sa);
    	
    	amountH = new HandlingFee(cashIM.getCashInCents());
    	StringBuilder info = new StringBuilder();
    	info.append("付款金额: ￥" + amountH.getRaw());
    	info.append("\n手续费用：￥" + amountH.getFee());
    	info.append("\n总共费用：￥" + amountH.getAll());
    	info.append("\n付款账户：" + user1);
    	info.append("\n付款卡号：\n" + cn);
    	info.append("\n收款账户：" + user2);
    	info.append("\n收款卡号：\n" + card2);
    	info.append("\n收款银行：" + bankname2);
    	
    	AlertDialogBuilderWrapper builder = PublicHelper.getAlertDialogBuilder(this);
        builder.setTitle("信息预览")
        .setIcon(android.R.drawable.ic_dialog_info)
        .setMessage(info.toString() + "\n\n是否继续付款？")
        .setCancelable(false)
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				card.setText("");
				ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
		                Animation.RELATIVE_TO_SELF, 0.5f, 
		                Animation.RELATIVE_TO_SELF, 0.5f);
		        sa.setDuration(200);
		        imgCardType.startAnimation(sa);
		        imgCardType.postDelayed(new Runnable() {
		            public void run() {
		            	imgCardType.setVisibility(View.GONE);
		            }
		        }, 200);
				cashIM.init();
				if(isPlugged()) {
					showTitleSubmit();
				}
			}
		})
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		framePass.setVisibility(View.VISIBLE);
        		noteSwipe.setVisibility(View.GONE);
        		passwdIM.init();
        	}
        });
        builder.create().show();    	
	}

	@Override
	public void onDecoding() {
		dialog.setText("正在解码...");
	}
	
	@Override
	public void onSwipe() {
		dialog.setText("接受刷卡数据...");
	}

	@Override
	public void onPlugin() {
		super.onPlugin(); // UIBASE action
		if(imgCardReader.getVisibility() != View.VISIBLE) {
            imgCardReader.setVisibility(View.VISIBLE);
            ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(200);
            imgCardReader.startAnimation(sa);
        }
		if (framePass.getVisibility() == View.GONE) {
        	showTitleSubmit();
        }
		noteSwipe.setText("输入金额，点击【确认刷卡】");
	}

	@Override
	public void onPlugout() {
		super.onPlugout(); // UIBASE action
		if(imgCardReader.getVisibility() == View.VISIBLE) {
            ScaleAnimation sa = new ScaleAnimation(1, 0, 1, 0, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(200);
            imgCardReader.startAnimation(sa);
            imgCardReader.postDelayed(new Runnable() {
                public void run() {
                    imgCardReader.setVisibility(View.GONE);
                }
            }, 200);
        }
		hideTitleSubmit();
		noteSwipe.setText("请插入刷卡器");
	}

	@Override
	public void onTimeout() {
		super.onTimeout();
		makeError("未检测到刷卡动作");
		dialog.dismiss();
		txtInput.requestFocus();
		if(isPlugged()) {
			showTitleSubmit();
		}
	}

	@Override
	public void onError(int err) {
		Log.i(TAG, "error = " + err);
		dialog.dismiss();
		txtInput.requestFocus();
		// 刷卡时，插拔卡可能会引起decodeError
		boolean isDecodeError = true;
		boolean isInterrupt = false;
		if (this.isPlugged()) {
			switch (err) {
			case E_API2_UNRESOLVED:
				makeError("无法解析，请重试或者使用其他Android手机");
				break;
			case E_API2_FASTORSLOW:
				makeError("刷卡过快或过慢，请重试");
				break;
			case E_API2_UNSTABLE:
				makeError("刷卡不稳定，请重试");
				break;
			case E_API2_INVALID_DEVICE:
				makeError("非法刷卡器，请使用正规刷卡器");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
			default:
				isDecodeError = false;
			}
		} else {
			switch (err) {
			case E_API2_INVALID_DEVICE:
				makeError("无法识别该刷卡器，请选择新的刷卡器或者重新拔插");
				noteSwipe.setText("请拔掉此刷卡器，重新插入");
				break;
			case E_API2_INVALID_KSN:
				makeError("非法刷卡器，请使用已注册绑定的刷卡器");
				noteSwipe.setText("请拔掉此刷卡器，重新插入");
				break;
			case E_API2_INTERRUPT:
				isInterrupt = true;
				break;
			}
		}
		if (isDecodeError || isInterrupt) {
			dialog.dismiss();
			if (isPlugged()) {
				showTitleSubmit();
			}
		}
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && isProcessing) {
        	makeError("不能中止交易");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	private void initUI() {
		initNumPad();
		setTitleSubmitText("确认刷卡");
		noteSwipe = (TextView) findViewById(R.id.notation_swipe_c2c);
		imgCardType = (ImageView)findViewById(R.id.card_type);
        imgCardReader = (ImageView)findViewById(R.id.card_indicator);
		framePass = (ViewGroup) findViewById(R.id.password_frame);
		layoutMask = (ViewGroup) findViewById(R.id.layout_mask);
		txtPassword = (EditText) findViewById(R.id.card_password);
		card = (TextView) findViewById(R.id.card_text);
		framePass.setVisibility(View.GONE);
		layoutMask.setClickable(true);
		int[] attrs = new int[] { R.layout.swipe_dialog, R.style.dialog,
				R.id.dialog_anim, R.id.dialog_note };
		dialog = new SwipeDialogController(this, attrs);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				stopSwipe();
			}
		});
	}

	private void initNumPad() {
		txtInput = (EditText) findViewById(R.id.cash_input_c2c);
		btns = new Button[10];
		ViewGroup vg = (ViewGroup) findViewById(R.id.num_pad);
		for (int i = 0; i < 9; i++) {
			ViewGroup vgtmp = ((ViewGroup) vg.getChildAt(i / 3));
			btns[i + 1] = (Button) vgtmp.getChildAt(i % 3);
		}
		ViewGroup vg1 = ((ViewGroup) vg.getChildAt(3));
		btns[0] = (Button) vg1.getChildAt(1);
		TextView t1 = (TextView) vg1.getChildAt(1);
		Log.e("showViewgroup", t1.getText().toString());
		fnButton = (Button) vg1.getChildAt(0);
		delButton = vg1.getChildAt(2);
	}

	private void makeError(String err) {
		Toast t = Toast.makeText(this, err, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	private boolean doRequest(String password) {
	    isProcessing = true;
	    POSSession session = POSHelper.getPOSSession();
		if (ci == null || session == null) {
			makeError("POS机出错！");
			return false;
		}
		
		
		final CardInfo cardInfo = ci;
		final String name = session.getSessionAccount();
		if (name == null || name.length() == 0) {
			makeError("POS机出错！");
			return false;
		}
		try {
			password = session.getPIN(password, cardInfo.getCard(false));
		} catch(Exception e) {
			makeError("请输入4到6位银行卡密码！");
			return false;
		}
		final String passwd = password;
		
		progressDialog = PublicHelper.getProgressDialog(this, // context 
				"",	// title 
				"付款进行中...", // message 
				true, //进度是否是不确定的，这只和创建进度条有关 
				false);
		(new Thread(TAG) {
			public void run() {
				POSEncrypt POS = POSHelper.getPOSEncrypt(Card2CardActivity.this, name);
				POS.addTraceNumber();
				s = new TransferMessage();
				s.setAmountTotal_4(amountH.getAll())
                .setReceiverBankId_5(bankid2)
                .setAmount_6(amountH.getRaw())
                .setCardTracerNumber_11(POS.getPOSDecrypt(POS.TRACENUMBER))
                .setIsPinNeed_22(true)
                .setMaxPinLength_26(10) // TODO hard-code here
                .setCardInfo(cardInfo)
                .setTerminalMark_41(POS.getPOSDecrypt(POS.TERMINAL))
                .setUserMark_42(POS.getPOSDecrypt(POS.USERMARK))
                .setUserName_46(user1)
                .setReceiverBankName_47(bankname2)
                .setReceiverCardNumber_48(card2)
                .setUserPin_52(passwd)
                .setReceiverUserName_56(user2)
                .setSetNumber_60(POS.getPOSDecrypt(POS.SETNUMBER))
                .setUseMac_64();
				POS.close();
				boolean isOk = false;
	            String error = "";
	            String[] allMessage = null;
	            try {
	                if(!s.isBitmapValid()) throw new RuntimeException("BitmapError");
	                IsoMessage resp = s.request();
	                if(resp != null) {
	                	String statusCode = resp.getField(39).toString();
	                	if (statusCode.equals("00")) {
	                		// String merchantNo = resp.getField(42).toString();
	                		String terminalNo = resp.getField(41).toString();
	                		String cardNumber = resp.getField(2).toString();
	                		String batchNo = resp.getField(60).toString().substring(2); // 批次号
	                		String voucherNo = resp.getField(11).toString(); // 流水号
	                		String authNo = null; // 授权码
	                		if(resp.getField(38) == null || resp.getField(38).toString().length() == 0) {
	                			authNo = "000000";
	                		} else {
	                			authNo = resp.getField(38).toString();
	                		}
	                		String referNo = resp.getField(37).toString(); // 参考号
	                		String transactionDate = getTransactionDate(resp.getField(13).toString());
	                		String transactionTime = getTransactionTime(resp.getField(12).toString());
	                		String transactionAmount = amountH.getRaw().toString();
	                		String traceId = resp.getField(59).toString(); // 交易ID
	                		// String merchantName = GBKBase64.decode(resp.getField(55).toString()); // 姓名
	                		long time = System.currentTimeMillis();
	                		Calendar mCalendar=Calendar.getInstance();
	                		mCalendar.setTimeInMillis(time);
	                		int TransactionYear = mCalendar.get(Calendar.YEAR);
	                		String FileName = TransactionYear + resp.getField(13).toString() + resp.getField(12).toString();
	                		allMessage = new String[] {
	                			terminalNo,
	                			cardNumber,
	                			card2,
	                			authNo,
	                			referNo,
	                			batchNo,
	                			voucherNo,
	                			transactionDate,
	                			transactionTime,
	                			user1,
	                			transactionAmount,
	                			traceId,
	                			FileName
	                		};
		                    isOk = true;
	                	} else {
                    		error = getError(statusCode);
	                	}
	                } else {
	                	// Manually stop client from user's aspect
	                	allMessage = null;
	                    isOk = false;
	                }
	            } catch (SocketTimeoutException e) {
	                error = "连接超时，请确保网络稳定性";
	                e.printStackTrace();
	            } catch (IllegalStateException e) {
	                error = "严重错误，请咨询售后";
	                e.printStackTrace();
	            } catch (UnknownHostException e) {
	                error = "无法连接主机，请检查连接性";
	                e.printStackTrace();
	            } catch (IOException e) {
	                error = "请检查手机的网络信号";
	                e.printStackTrace();
	            } catch (Exception e) {
	            	error = "报文错误，请联系客服";
	                Log.i(TAG, "Parse Error: " + e);
		        }
	            if(!isOk) {
	                mHandler.obtainMessage(FAILURE, error).sendToTarget();
	            } else {	            	 
	                mHandler.obtainMessage(SUCCESS, allMessage).sendToTarget();
	            }
	            isProcessing = false;
			}
		}).start();
		return true;
	}
	
	private String getTransactionDate(String code) {
		long time = System.currentTimeMillis();
		Calendar mCalendar=Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int TransactionYear = mCalendar.get(Calendar.YEAR);
		StringBuffer mouthDay = new StringBuffer(code);
		String transactionDate = TransactionYear + "/" + mouthDay.insert(2, "/");
		return transactionDate;
	}
	
	private String getTransactionTime(String code) {
		StringBuffer sb2 = new StringBuffer(code);
		sb2.insert(2, ":");
		sb2.insert(5, ":");
		String transactionTime = sb2.toString();
		return transactionTime;
	}
}
