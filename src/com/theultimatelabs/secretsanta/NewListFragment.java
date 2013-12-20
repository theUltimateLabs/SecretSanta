package com.theultimatelabs.secretsanta;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewListFragment extends Fragment {

	private NewListAdapter mNewListAdapter;
	private SharedPreferences mPrefs;
	private MyApp Globals;

	static public class Participent {
		public String name;
		public String email;
		public List<Participent> familyMembers;		
		MyApp Globals;

		public Participent(String nameIn, String emailIn) {
			familyMembers = new ArrayList<Participent>();
			name = nameIn;
			email = emailIn;
		}

		public Participent(String nameIn) {
			familyMembers = new ArrayList<Participent>();
			name = nameIn;
			email = null;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Globals = (MyApp) getActivity().getApplication();
	    mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		setHasOptionsMenu(true);
		
		Participent bob = new Participent("RobB", "robcb85@gmail.com");
		bob.familyMembers.add(new Participent("Mary"));
		//bob.familyMembers.add(new Participent("Jude"));
		Globals.newListFamiles.add(bob);

		Participent sara = new Participent("rob", "rob@theultimatelabs.com");
		sara.familyMembers.add(new Participent("Mike",
				"rob+mike@theultimatelabs.com"));
		//sara.familyMembers.add(new Participent("Luke"));
		Globals.newListFamiles.add(sara);

		mNewListAdapter = new NewListAdapter(Globals.newListFamiles, getActivity());
		// setContentView(R.layout.new_list);
	}
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.list, container, false);
	    
	    ExpandableListView newListView = (ExpandableListView) view.findViewById(R.id.newList);
		
		newListView.setAdapter(mNewListAdapter);
		
		newListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				int itemType = ExpandableListView.getPackedPositionType(id);
				final int groupPosition = ExpandableListView
						.getPackedPositionGroup(id);
				final int childPosition = ExpandableListView
						.getPackedPositionChild(id);
				final String name = (String) ((TextView) view
						.findViewById(R.id.labelText)).getText();

				if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					new AlertDialog.Builder(getActivity())
							.setTitle("Delete")
							.setMessage(
									String.format(
											"Are you you want to delete %s from the list?",
											name))
							.setPositiveButton("Delete",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mNewListAdapter.removeMember(
													groupPosition,
													childPosition);
										}
									}).setNegativeButton("Cancel", null).show();
					return true; // true if we consumed the click, false if not
				} else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
					new AlertDialog.Builder(getActivity())
							.setTitle("Delete")
							.setMessage(
									String.format(
											"Are you you want to delete %s and all their family members from the list?",
											name))
							.setPositiveButton("Delete",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mNewListAdapter
													.removeFamily(groupPosition);
										}
									}).setNegativeButton("Cancel", null).show();
					return true; // true if we consumed the click, false if not
				} else {
					// null item; we don't consume the click
					return false;
				}
			}
		});

		((Button) view.findViewById(R.id.submitButton))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						
//						Toast.makeText(getActivity(), "Solution found, duration)

						String qparams = "?";
						qparams += "listname=" + Globals.listname;
						qparams += "&password=" + Globals.password;
						qparams += "&restrictWithinFamily="
								+ mPrefs.getBoolean("restrict_within_family",
										true);
						qparams += "&historyRange="
								+ mPrefs.getString("history_range", "2");
						qparams += "&noCycles="
								+ mPrefs.getBoolean("restrict_within_family",
										true);

						Log.v("QUERY", qparams);
						new URLFetch(Constants.HOST) { 
							
						}.execute("/submit" + qparams, mNewListAdapter.getJson().toString());
					}
				});
		
	    return view;
	  }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);		
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Log.v(TAG,"onOptionsItemSelected");
		
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.add_entry:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					ContactsContract.CommonDataKinds.Email.CONTENT_URI),
					Constants.PICK_FAMILY);
			return true;
		case R.id.constraints:
			startActivityForResult(new Intent(getActivity(), ConstraintsActivity.class),
					Constants.SET_CONSTRAINTS);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public void onActivityResult(final int requestCode, int resultCode,
			Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);

		// Log.v("RESULT","result:"+resultCode+" request:"+requestCode);
		if (resultCode == Activity.RESULT_OK) {
			final ContactInfo contact = new ContactInfo(
					getActivity().getContentResolver(), intent);

			if (contact.emails.length <= 0) {
				Toast.makeText(
						getActivity(),
						String.format("No email associated with %s",
								contact.name), Toast.LENGTH_LONG).show();
				return;
			}
			if (contact.name == null || contact.name.length() == 0) {
				contact.name = contact.emails[0];
			}

			if (contact.emails.length > 1) {
				new AlertDialog.Builder(getActivity())
						.setItems(contact.emails,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialogInterface,
											int pos) {
										// Log.v("EMAIL", contact.emails[pos]);
										if (requestCode == Constants.PICK_FAMILY) {
											mNewListAdapter.addFamily(
													contact.name,
													contact.emails[pos]);
										} else {
											mNewListAdapter
													.addFamilyMember(
															requestCode
																	- Constants.PICK_FAMILY_MEMBER,
															contact.name,
															contact.emails[pos]);
										}
										dialogInterface.dismiss();
									}

								})
						.setTitle(
								String.format("Send to which of %s's emails?",
										contact.name)).show();
			} else {
				if (requestCode == Constants.PICK_FAMILY) {
					mNewListAdapter.addFamily(contact.name, contact.emails[0]);
				} else {
					mNewListAdapter.addFamilyMember(requestCode
							- Constants.PICK_FAMILY_MEMBER, contact.name,
							contact.emails[0]);
				}
			}
		}
	}

	public class NewListAdapter extends BaseExpandableListAdapter {

		private List<Participent> mFamiles;
		private LayoutInflater mInflater;
		private Activity mActivity;

		public NewListAdapter(List<Participent> families, Activity activity) {
			mActivity = activity;
			mInflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mFamiles = families;
		}

		// Marshals data from list and returns as JSON array of object
		public JSONArray getJson() {
			JSONArray listJson = new JSONArray();
			for (Participent family : mFamiles) {
				try {
					JSONObject familyJson = new JSONObject();
					familyJson.put("name", family.name);
					familyJson.put("email", family.email);
					JSONArray membersJson = new JSONArray();
					for (Participent member : family.familyMembers) {
						JSONObject memberJson = new JSONObject();
						memberJson.put("name", member.name);
						if (member.email != null) {
							memberJson.put("email", member.email);
						}
						membersJson.put(memberJson);
					}
					familyJson.put("members", membersJson);
					listJson.put(familyJson);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return listJson;
		}

		public boolean isDuplicate(String name) {
			for (Participent family : mFamiles) {
				if (family.name.equals(name)) {
					return true;
				}
				for (Participent member : family.familyMembers) {
					if (member.name.equals(name)) {
						return true;
					}
				}
			}
			return false;
		}

		// Adds a head of family
		public void addFamily(String name, String email) {
			if (isDuplicate(name)) {
				Toast.makeText(mActivity,
						name + " is already in the list so was not added.",
						Toast.LENGTH_LONG).show();
				return;
			}
			mFamiles.add(new Participent(name, email));
			this.notifyDataSetChanged();
		}

		public void addFamilyMember(Participent family, String name,
				String email) {
			if (isDuplicate(name)) {
				Toast.makeText(mActivity,
						name + " is already in the list so was not added.",
						Toast.LENGTH_LONG).show();
				return;
			}
			family.familyMembers.add(new Participent(name, email));
			this.notifyDataSetChanged();
		}

		public void addFamilyMember(int familyPos, String name, String email) {
			Log.d("ADD", "Adding family member " + name);
			addFamilyMember(mFamiles.get(familyPos), name, email);
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			Participent family = mFamiles.get(groupPosition);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.child_row, null);
			}

			Participent member = family.familyMembers.get(childPosition);

			TextView textView = (TextView) convertView
					.findViewById(R.id.labelText);
			textView.setText(member.name);

			textView = (TextView) convertView.findViewById(R.id.subLabelText);
			if (member.email != null) {
				textView.setText(member.email);
				textView.setVisibility(View.VISIBLE);
			} else {
				textView.setVisibility(View.GONE);
			}

			return convertView;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			final Participent family = mFamiles.get(groupPosition);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.group_row, null);
			}

			((TextView) convertView.findViewById(R.id.labelText))
					.setText(family.name);
			((TextView) convertView.findViewById(R.id.subLabelText))
					.setText(family.email);
			((TextView) convertView.findViewById(R.id.addChildButton))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {

							final EditText nameInput = new EditText(mActivity);
							new AlertDialog.Builder(mActivity)
									.setCancelable(true)
									.setTitle("Name")
									.setView(nameInput)
									.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													String name = nameInput
															.getText()
															.toString();
													if (name == null
															|| name.length() == 0) {
														Toast.makeText(
																mActivity,
																String.format("Not a valid name"),
																Toast.LENGTH_LONG)
																.show();
														return;
													}
													NewListAdapter.this
															.addFamilyMember(
																	family,
																	name, null);
												}
											})
									.setNeutralButton(
											"Add From Contacts",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													startActivityForResult(
															new Intent(
																	Intent.ACTION_PICK,
																	ContactsContract.CommonDataKinds.Email.CONTENT_URI),
															Constants.PICK_FAMILY_MEMBER
																	+ groupPosition);
												}
											}).show();
						}
					});

			return convertView;
		}

		public void removeFamily(int groupPosition) {
			mFamiles.remove(groupPosition);
			notifyDataSetChanged();
		}

		public void removeMember(int groupPosition, int childPosition) {
			mFamiles.get(groupPosition).familyMembers.remove(childPosition);
			notifyDataSetChanged();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// Log.d("CHILDREN","SIZE:"+mFamiles.get(groupPosition).familyMembers.size());
			return mFamiles.get(groupPosition).familyMembers.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			// Log.d("FAMILES","SIZE:"+mFamiles.size());
			return mFamiles.size();
		}

		@Override
		public void onGroupCollapsed(int groupPosition) {
			super.onGroupCollapsed(groupPosition);
		}

		@Override
		public void onGroupExpanded(int groupPosition) {
			super.onGroupExpanded(groupPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	

}
