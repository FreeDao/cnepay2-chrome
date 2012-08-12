package com.cnepay.android.pos2;

import java.util.ArrayList;

import org.taptwo.android.widget.TitleFlowIndicator;
import org.taptwo.android.widget.TitleProvider;
import org.taptwo.android.widget.ViewFlow;

import com.cnepay.android.pos2.ManagerActivity.MyFlipAdapter.App;
import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ManagerActivity extends UIBaseActivity implements OnItemClickListener {
	
	//private TextView tv;
    private ViewFlow viewFlow;
    
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager);
        setTitle("账户管理");
        setActivityPara(true ,true);
        //tv = (TextView)findViewById(R.id.mananger_notice_text);
        
        viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		MyFlipAdapter adapter = new MyFlipAdapter(this);
		viewFlow.setAdapter(adapter, 0);
		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
		indicator.setTitleProvider(adapter);
		viewFlow.setFlowIndicator(indicator);
        
        
	}
	
	
	class MyFlipAdapter extends BaseAdapter implements TitleProvider {
		private ArrayList<ArrayList<App>> mApps = new ArrayList<ArrayList<App>>();
		private LayoutInflater mInflater;
		private Context ct;
		
		
		private boolean flag = false;

		public MyFlipAdapter(Context context) {
			ct = context;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
			POSSession SESSION = POSHelper.getPOSSession();		
			ArrayList<App> app1 = new ArrayList<App>();
			if(SESSION != null && !SESSION.isAuthenticated()) {
				flag = false;
				app1.add(new App(R.drawable.setpwd, R.string.setpwd_mgr, ChangePasswordActivity.class));
	        	app1.add(new App(R.drawable.real_name, R.string.real_name_mgr, IDPhotoActivity.class));
	        	mApps.add(app1);
			}else{
				flag = true;
				app1.add(new App(R.drawable.recharger, R.string.charge_mgr, CreditRechargerActivity.class));
	            app1.add(new App(R.drawable.card2card, R.string.transfer_mgr, RemitActivity.class));
	            app1.add(new App(R.drawable.mobile_charge, R.string.mobile_charge, MobileChargeActivity.class));
	            app1.add(new App(R.drawable.mobile_charge, R.string.balance_enquiry, BalanceEnquiryActivity.class));
	            app1.add(new App(R.drawable.setpwd, R.string.setpwd_mgr, ChangePasswordActivity.class));
	            app1.add(new App(R.drawable.real_name, R.string.real_name_mgr, IDPhotoActivity.class));
	            app1.add(new App(R.drawable.device_manage, R.string.device_mgr, DeviceManageActivity.class));
	            mApps.add(app1);
	            
	            ArrayList<App> app2 = new ArrayList<App>();
		        app2.add(new App(R.drawable.card2card, R.string.transfer_mgr, RemitActivity.class));
	            app2.add(new App(R.drawable.mobile_charge, R.string.mobile_charge, MobileChargeActivity.class));
	            app2.add(new App(R.drawable.mobile_charge, R.string.balance_enquiry, BalanceEnquiryActivity.class));
	            app2.add(new App(R.drawable.setpwd, R.string.setpwd_mgr, ChangePasswordActivity.class));
		        mApps.add(app2);
		        
		        ArrayList<App> app3 = new ArrayList<App>();
		        app3.add(new App(R.drawable.device_manage, R.string.device_mgr, DeviceManageActivity.class));
		        mApps.add(app3);
			}
	     
		}

		@Override
		public int getCount() {
			return mApps.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.flow_item, null);
			}
			
	        GridView mGrid = (GridView)convertView.findViewById(R.id.manager_grid);
	        TextView tv = (TextView)convertView.findViewById(R.id.mananger_notice_text);
	        if(flag){
	        	tv.setVisibility(View.VISIBLE);
	        }else{
	        	tv.setVisibility(View.GONE);
	        }
	        mGrid.setAdapter(new ItemsAdapter(position));
	        //mGrid.setOnItemClickListener(this);
	        return convertView;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
		 */
		@Override
		public String getTitle(int position) {
			String tmp = "第" + (position + 1) + "页";
			return tmp;
		}
		
		class ItemsAdapter extends BaseAdapter {
			private int p;
	        public ItemsAdapter(int position) {
	        	p = position;
	        }
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View root;
	            if (convertView == null) {
	                root = ((LayoutInflater) ct.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.griditem, null);
	            } else {
	                root = convertView;
	            }
	            //Log.v(TAG, "p=" + p + "position=" + position);
	            App app = mApps.get(p).get(position);
	            ((ImageView)root.findViewById(R.id.grid_icon)).setImageResource(app.icon);
	            ((TextView)root.findViewById(R.id.grid_title)).setText(app.title);
	            return root;
	        }
	        public final int getCount() {
	            return mApps.get(p).size();
	        }
	        public final Object getItem(int position) {
	            return mApps.get(p).get(position);
	        }
	        public final long getItemId(int position) {
	            return position;
	        }
	    }
	    
	    class App {
	        public int title;
	        public int icon;
	        public Intent intent;
	        public App(int Icon, int Title, Class<?> mClass) {
	        	if (mClass != null) {
	        		intent = new Intent(ManagerActivity.this, mClass);
	        	} else {
	        		intent = null;
	        	}
	            title = Title;
	            icon = Icon;
	        }
	    }

	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
}
