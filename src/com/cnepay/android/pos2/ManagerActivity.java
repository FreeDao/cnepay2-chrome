package com.cnepay.android.pos2;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
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
	
	private TextView tv;
    private GridView mGrid;
    private ArrayList<App> mApps = new ArrayList<App>();
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager);
        setTitle("账户管理");
        setRequireLogon();
        tv = (TextView)findViewById(R.id.mananger_notice_text);
        Intent intent = getIntent();
        if(intent.getIntExtra("juage", 0) == 1) {
        	 mApps.add(new App(R.drawable.setpwd, R.string.setpwd_mgr, ManagerActivity.class));
        	 mApps.add(new App(R.drawable.real_name, R.string.real_name_mgr, ManagerActivity.class));
        	 tv.setVisibility(View.VISIBLE);
        } else {
        	tv.setVisibility(View.GONE);
            mApps.add(new App(R.drawable.recharger, R.string.charge_mgr, CreditRechargerActivity.class));
            mApps.add(new App(R.drawable.card2card, R.string.transfer_mgr, ManagerActivity.class));
            mApps.add(new App(R.drawable.setpwd, R.string.setpwd_mgr, ManagerActivity.class));
            mApps.add(new App(R.drawable.real_name, R.string.real_name_mgr, ManagerActivity.class));
            mApps.add(new App(R.drawable.checkrecord, R.string.records_mgr, ManagerActivity.class));
        }
        
        mGrid = (GridView)findViewById(R.id.manager_grid);
        mGrid.setAdapter(new ItemsAdapter());
        mGrid.setOnItemClickListener(this);
	}
	
	class ItemsAdapter extends BaseAdapter {
        public ItemsAdapter() {
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View root;
            if (convertView == null) {
                root = ManagerActivity.this.getLayoutInflater().inflate(R.layout.griditem, null);
            } else {
                root = convertView;
            }
            App app = mApps.get(position);
            ((ImageView)root.findViewById(R.id.grid_icon)).setImageResource(app.icon);
            ((TextView)root.findViewById(R.id.grid_title)).setText(app.title);
            return root;
        }
        public final int getCount() {
            return mApps.size();
        }
        public final Object getItem(int position) {
            return mApps.get(position);
        }
        public final long getItemId(int position) {
            return position;
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        App app = mApps.get(position);
        if(app.intent != null) {
            startActivity(app.intent);
        } else {
            Toast.makeText(this, "This function has not benn realized yet.", Toast.LENGTH_SHORT).show();
        }
    }
    
    class App {
        public int title;
        public int icon;
        public Intent intent;
        public App(int Icon, int Title, Class<?> mClass) {
            intent = new Intent(ManagerActivity.this, mClass);
            title = Title;
            icon = Icon;
        }
    }
}
