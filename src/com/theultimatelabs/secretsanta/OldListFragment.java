package com.theultimatelabs.secretsanta;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.theultimatelabs.secretsanta.NewListFragment.Participent;




public class OldListFragment extends Fragment {
	
	private SharedPreferences mPrefs;
	private ArrayAdapter mOldListAdapter;
	private MyApp Globals;
	private int mListYear;
	private int mTargetYear;
	private Map<String, String> mHistory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Globals = (MyApp) getActivity().getApplication();
	    mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		setHasOptionsMenu(true);

		mOldListAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
		// setContentView(R.layout.new_list);
	}
	
	void createHistoryRecursive(final List<String> gifters, final List<String> giftees) {
		final String gifter = (String)gifters.remove(0);
		final boolean addGifterBack = giftees.contains(gifter) ? true : false;
		if (addGifterBack) {
			giftees.remove(gifter);
		}
		new AlertDialog.Builder(getActivity())
		.setTitle("Who did "+gifter+" give to in "+mTargetYear+"?")
		.setCancelable(false)
		.setItems((String[]) giftees.toArray(new String[giftees.size()]), 
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String giftee = giftees.remove(which);
				mHistory.put(gifter, giftee);
				mOldListAdapter.add(gifter + " -> " + giftee);
				if (addGifterBack) giftees.add(gifter);
				if (gifters.size() > 0 && giftees.size() > 0)
					createHistoryRecursive(gifters,giftees);
				else
					updateHisorty();
			}
		})
		.setNeutralButton("Skip",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (gifters.size() > 0 && giftees.size() > 0)
					createHistoryRecursive(gifters,giftees);
				else
					updateHisorty();
			}
		})
		.show();
	}
	
	void createHistory() {
		mOldListAdapter.clear();
		mHistory = new HashMap<String,String>();
		List<String> gifters = new ArrayList<String>();
		List<String> giftees = new ArrayList<String>();
		for(Participent  family : Globals.newListFamiles) {
			gifters.add(family.name);
			giftees.add(family.name);
			for (Participent member : family.familyMembers) {
				gifters.add(member.name);
				giftees.add(member.name);
			}
		}
		createHistoryRecursive(gifters, giftees);
		Globals.histories.put(mTargetYear, mHistory);
	}
	
	void updateHisorty() {
		JSONObject json = new JSONObject();
		for (String gifter: mHistory.keySet())
			try {
				json.put(gifter, mHistory.get(gifter));
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		String url = String.format("/history?listname=%s&password=%s&year=%d",Globals.listname,Globals.password,mTargetYear);
		new URLFetch(Constants.HOST) {
			protected void onPostExecute(String result) {
				if (result!=null) {
					Toast.makeText(getActivity(), "New history updated on server", Toast.LENGTH_LONG);
				}
				else {
					Toast.makeText(getActivity(), "Error updating history on server", Toast.LENGTH_LONG);
				}
		     }
		}.execute(url,json.toString());
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		View view = inflater.inflate(R.layout.old_list, container, false);
		
		mTargetYear = getArguments().getInt("year");
		int currentYear = (Calendar.getInstance().get(Calendar.YEAR) );
		mHistory = Globals.histories.get(mTargetYear);
		
		if (mHistory == null) {
			
			if (currentYear != mTargetYear) {
				new AlertDialog.Builder(getActivity())
				.setTitle("Create History?")
				.setMessage("The gift history for "+mTargetYear+" does not exist, would you like to create it?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						createHistory();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
						
				})
				.show();
			}
		}
		else {
			for (String gifter : mHistory.keySet()) {
				mOldListAdapter.add(gifter + " -> " + mHistory.get(gifter));
			}
		}
		
		
		ListView oldListView = (ListView) view.findViewById(R.id.oldList);
		
		oldListView.setAdapter(mOldListAdapter);
		
		((Button)view.findViewById(R.id.reinitializeButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createHistory();
			}
		});
		return view;
		
	}
	
}
