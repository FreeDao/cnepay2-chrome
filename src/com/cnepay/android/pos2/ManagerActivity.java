package com.cnepay.android.pos2;

import java.util.ArrayList;

import org.taptwo.android.widget.SimpleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;

import com.tangye.android.iso8583.POSHelper;
import com.tangye.android.iso8583.POSSession;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

public class ManagerActivity extends UIBaseActivity  {
	
	private final static String TAG = "ManagerActivity";
	
	private ViewFlow viewFlow;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager);
        setTitle("账户管理");
        setActivityPara(true ,true);
        
        // begin ad
        ViewFlow adflow = (ViewFlow) findViewById(R.id.ad_flow);
        adflow.setAdapter(new AdAdapter(this), 0);
        SimpleFlowIndicator adindic = (SimpleFlowIndicator) findViewById(R.id.ad_indic);
        adflow.setFlowIndicator(adindic);
        // end ad
        
        /*********************************/
        
        // grid viewflow
        viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		MyFlipAdapter adapter = new MyFlipAdapter(this);
		viewFlow.setAdapter(adapter, 0);
		SimpleFlowIndicator indicator = (SimpleFlowIndicator) findViewById(R.id.viewflowindic);
		viewFlow.setFlowIndicator(indicator);
		// end grid vieflow
	}
	
	public static class AdAdapter extends BaseAdapter {

		private final Context ctx;
		ArrayList<Link> links = new ArrayList<Link>();
		public AdAdapter(Context context) {
			ctx = context;
			links.add(new Link(R.drawable.logo_inside_ad1, "http://www.cnepay.com"));
			links.add(new Link(R.drawable.logo_inside_ad2, "http://www.cnepay.com/pay.html"));
			links.add(new Link(R.drawable.logo_inside_ad3, null));
		}
		
		@Override
		public int getCount() {
			return links.size();
		}

		@Override
		public Object getItem(int position) {
			return links.get(position);
		}

		@Override
		public long getItemId(int position) {
			return links.get(position).resId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(ctx, R.layout.ad_item, null);
			}
			final Link l = links.get(position);
			
	        ImageView img = (ImageView)convertView.findViewById(R.id.ad_image);
	        img.setImageResource(l.resId);
	        if (l.link != null) {
	        	img.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Uri uri = Uri.parse(l.link);
						Intent i = new Intent(Intent.ACTION_VIEW, uri);
						ctx.startActivity(i);
					}
				});
	        }
	        return convertView;
		}
		
		public class Link {
			public final String link;
			public final int resId;
			public Link(int res, String l) {
				resId = res;
				link = l;
			}
		}
		
	}
	
	public static class MyFlipAdapter extends BaseAdapter implements OnItemClickListener{
		private ArrayList<ArrayList<App>> mApps = new ArrayList<ArrayList<App>>();
		private LayoutInflater mInflater;
		private Context ct;
		
		private TextView tv;
		private boolean flag = false;

		public MyFlipAdapter(Context context) {
			ct = context;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			POSSession SESSION = POSHelper.getPOSSession();
			ArrayList<App> app1 = new ArrayList<App>();
			if (SESSION != null && !SESSION.isAuthenticated()) {
				flag = false;
				app1.add(new App(R.drawable.setpwd, ChangePasswordActivity.class));
	        	app1.add(new App(R.drawable.real_name, IDPhotoActivity.class));
	        	app1.add(new App(R.drawable.help, null));
	        	mApps.add(app1);
			} else {
				flag = true;
				app1.add(new App(R.drawable.recharger, CreditRechargerActivity.class));
	            app1.add(new App(R.drawable.afford, RemitActivity.class));
	            app1.add(new App(R.drawable.card2card, null));
	            app1.add(new App(R.drawable.inquiry, BalanceEnquiryActivity.class));
	            app1.add(new App(R.drawable.refund, null));
	            app1.add(new App(R.drawable.mobile_charge, MobileChargeActivity.class));
	            app1.add(new App(R.drawable.pay, null));
	            app1.add(new App(R.drawable.setpwd, ChangePasswordActivity.class));
	            app1.add(new App(R.drawable.trace_charge, null));
	            mApps.add(app1);
	            ArrayList<App> app2 = new ArrayList<App>();
	            app2.add(new App(R.drawable.sms, null));
	            app2.add(new App(R.drawable.device_manage, DeviceManageActivity.class));
	            app2.add(new App(R.drawable.user_verify, null));
	            app2.add(new App(R.drawable.real_name, IDPhotoActivity.class));
	            app2.add(new App(R.drawable.more, null));
		        mApps.add(app2);
		        
		        ArrayList<App> app3 = new ArrayList<App>();
		        app3.add(new App(R.drawable.help, null));
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
	        tv = (TextView)convertView.findViewById(R.id.mananger_notice_text);
	        if (!flag){
	        	tv.setVisibility(View.VISIBLE);
	        } else {
	        	tv.setVisibility(View.GONE);
	        }
	        mGrid.setAdapter(new ItemsAdapter(position));
	        mGrid.setOnItemClickListener(this);
	        return convertView;
		}
		
		class ItemsAdapter extends BaseAdapter {
			private int p;
	        public ItemsAdapter(int position) {
	        	p = position;
	        }
	        
	        public int getPosition(){
	        	return p;
	        }
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View root;
	            if (convertView == null) {
	                root = mInflater.inflate(R.layout.griditem, null);
	            } else {
	                root = convertView;
	            }
	            //Log.v(TAG, "p=" + p + "position=" + position);
	            App app = mApps.get(p).get(position);
	            ((ImageView)root.findViewById(R.id.grid_icon)).setImageResource(app.icon);
	            if (app.intent == null) {
	            	((ImageView)root.findViewById(R.id.grid_icon)).getDrawable().setAlpha(180);
	            }
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
	        public int icon;
	        public Intent intent;
	        public App(int Icon, Class<?> mClass) {
	        	if (mClass != null) {
	        		intent = new Intent(ct, mClass);
	        	} else {
	        		intent = null;
	        	}
	            icon = Icon;
	        }
	    }

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			ItemsAdapter tmp = (ItemsAdapter)arg0.getAdapter();
			Log.v(TAG, "adapter position = " + tmp.getPosition());
			//Log.v(TAG, "currentPosition = " + currentPosition);
			App app = mApps.get(tmp.getPosition()).get(position);        
			if(app.intent != null) {            
				ct.startActivity(app.intent);      
			} else {
				Toast.makeText(ct, "该功能还未实现，敬请等待", Toast.LENGTH_SHORT).show();        
			}
		}

	}

}
