package com.theultimatelabs.secretsanta;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	private SharedPreferences mPrefs;
	private NewListFragment mNewListFragment;
	private OldListFragment mOldListFragment;
	private MyApp Globals;
	public static final String TAG = "MainActivity";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setListNavigationCallbacks(new YearSpinner(this),	new YearSelectedListener(this));
		Globals = (MyApp) getApplication();
		mNewListFragment = new NewListFragment();
		mOldListFragment = new OldListFragment();			
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		//Log.v(TAG, "onCreateOptionsMenu");
		// return super.onCreateOptionsMenu(menu);
		// new MenuInflater(this).inflate(R.menu.list, menu);
		return super.onCreateOptionsMenu(menu);
		// return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		//Log.v(TAG, "onOptionsItemSelected");

		FragmentManager fm = getFragmentManager();
		switch (item.getItemId()) {
		case R.id.about:
			//startActivity(new Intent(this,AboutActivity.class));
			return true;
		case android.R.id.home:

			if (fm.getBackStackEntryCount() > 0) {
				fm.popBackStack();
			} 
			//if(fm.getBackStackEntryCount() == 0) {
			//	getActionBar().setDisplayHomeAsUpEnabled(false);
			//}
			return true;
		}

		return super.onOptionsItemSelected(item);

	}
	
	public class YearSpinner implements SpinnerAdapter {

		private LayoutInflater mInflater;

		private Context mContext;

		YearSpinner(Context context) {
			mContext = context;
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return 6;
		}

		@Override
		public Object getItem(int position) {
			if (position == 0) {
				return "New List";
			}
			return "" + (Calendar.getInstance().get(Calendar.YEAR) - position + 1);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			//TextView textView = (TextView) convertView;
			if (convertView == null) {
				convertView = mInflater.inflate(android.R.layout.simple_dropdown_item_1line, null);
			}
			TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
			textView.setTextColor(Color.rgb(0xff, 0xff, 0xff));
			textView.setText((String) getItem(position));
			return convertView;
		}

		@Override
		public int getViewTypeCount() {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {

		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return this.getView(position, convertView, parent);
		}

	}

	public class YearSelectedListener implements OnNavigationListener {
		private Activity mContext;
		YearSelectedListener(Activity context) {
			mContext = context;
		}
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			int currentYear = (Calendar.getInstance().get(Calendar.YEAR) );
			Log.v("NAV","" + mContext.getActionBar().getSelectedNavigationIndex() + ":" + itemPosition+":"+currentYear);
			
			if (itemPosition == 0) {
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				if(mNewListFragment.isAdded()) {
				    transaction.show(mNewListFragment);
				} else {
				    transaction.addToBackStack(itemId + "stack_item");
				    transaction.replace(android.R.id.content, mNewListFragment);
				}
				transaction.commit();
			}
			else {
				mOldListFragment = new OldListFragment();
				Bundle args  = new Bundle();
				args.putInt("year", currentYear-itemPosition+1);
				mOldListFragment.setArguments(args);
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(android.R.id.content, mOldListFragment);
				transaction.addToBackStack(itemId + "stack_item");
				transaction.commit();
			}
			
			return true;
		}
	}
	
}
