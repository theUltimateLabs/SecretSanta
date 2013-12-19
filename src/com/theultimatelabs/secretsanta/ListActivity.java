package com.theultimatelabs.secretsanta;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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




public class ListActivity extends Activity {
	
	private NewListAdapter mNewListAdapter;

	public class Participent {
		public String name;
		public String email;
		public List<Participent> familyMembers;
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
		setContentView(R.layout.new_list);
		// Notice that setContentView() is not used, because we use the root
		// android.R.id.content as the container for each fragment
		// setContentView(R.id.mainlist);
		// setup action bar for tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		//actionBar.ad
		
		
		
		//setContentView(R.layout.new_list);
		ExpandableListView newListView = (ExpandableListView) findViewById(R.id.newList);
		List<Participent> familes = new ArrayList<Participent>();
		
		Participent bob = new Participent("Bob","bob@bob.com");
		bob.familyMembers.add(new Participent("Mary"));
		bob.familyMembers.add(new Participent("Jude"));
		familes.add(bob);
		
		Participent sara = new Participent("Sara","sara@bob.com");
		sara.familyMembers.add(new Participent("Mike","mike@bob.com"));
		sara.familyMembers.add(new Participent("Luke"));
		familes.add(sara);
		
		
		mNewListAdapter = new NewListAdapter(familes, newListView, this);
		newListView.setAdapter(mNewListAdapter);
		
		newListView.setOnItemLongClickListener(new OnItemLongClickListener() {
		      @Override
		      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		          int itemType = ExpandableListView.getPackedPositionType(id);
		          final int groupPosition = ExpandableListView.getPackedPositionGroup(id);
	        	  final int childPosition = ExpandableListView.getPackedPositionChild(id);
	        	  final String name = (String)((TextView)view.findViewById(R.id.labelText)).getText();
	        	  
		          if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
		        	  new AlertDialog.Builder(ListActivity.this)
		        	  .setTitle("Delete")
		        	  .setMessage(String.format("Are you you want to delete %s from the list?",name))
		        	  .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mNewListAdapter.removeMember(groupPosition, childPosition);
						}
		        	  })
		        	  .setNegativeButton("Cancel", null)
		        	  .show();
		              return true; //true if we consumed the click, false if not
		          } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
		        	  new AlertDialog.Builder(ListActivity.this)
		        	  .setTitle("Delete")
		        	  .setMessage(String.format("Are you you want to delete %s and all their family members from the list?",name))
		        	  .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
				        	  mNewListAdapter.removeFamily(groupPosition);
						}
		        	  })
		        	  .setNegativeButton("Cancel", null)
		        	  .show();
		              return true; //true if we consumed the click, false if not
		          } else {
		              // null item; we don't consume the click
		              return false;
		          }
		      }
		  });
		
		((Button)findViewById(R.id.submitButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new URLFetch(Constants.HOST).execute("/submit","b");
			}
		});
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		//Log.v(TAG, "onCreateOptionsMenu");
		// return super.onCreateOptionsMenu(menu);
		// new MenuInflater(this).inflate(R.menu.list, menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);	    
		return super.onCreateOptionsMenu(menu);
		// return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Log.v(TAG,"onOptionsItemSelected");
		
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.add_entry:
			startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI), Constants.PICK_FAMILY);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onActivityResult(final int requestCode, int resultCode, Intent intent) {
		
		super.onActivityResult(requestCode, resultCode, intent);
		
		//Log.v("RESULT","result:"+resultCode+" request:"+requestCode);
		if (resultCode == Activity.RESULT_OK) {
			final ContactInfo contact = new ContactInfo(this.getContentResolver(), intent);
			
			if (contact.emails.length <= 0){
				Toast.makeText(this, String.format("No email associated with %s", contact.name), Toast.LENGTH_LONG).show();		
				return;
			}
			if (contact.name == null || contact.name.length()==0) {
				contact.name = contact.emails[0];
			}
			
			if(contact.emails.length > 1) {
				new AlertDialog.Builder(this)
				.setItems(contact.emails, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int pos) {
						//Log.v("EMAIL", contact.emails[pos]);
						if(requestCode == Constants.PICK_FAMILY)  {
							mNewListAdapter.addFamily(contact.name,contact.emails[pos]);
						}
						else {
							mNewListAdapter.addFamilyMember(requestCode - Constants.PICK_FAMILY_MEMBER, contact.name, contact.emails[pos]);
						}
						dialogInterface.dismiss();
					}

				})
				.setTitle(String.format("Send to which of %s's emails?",contact.name))
				.show();
			}
			else {
				if(requestCode == Constants.PICK_FAMILY)  {
					mNewListAdapter.addFamily(contact.name,contact.emails[0]);
				}
				else {
					mNewListAdapter.addFamilyMember(requestCode - Constants.PICK_FAMILY_MEMBER, contact.name, contact.emails[0]);
				}
			}			
		}
	}
	
	public class NewListAdapter extends BaseExpandableListAdapter {

		private List<Participent> mFamiles;
		private LayoutInflater mInflater;
		private Activity mActivity;
		private ExpandableListView mListView;
		
		public NewListAdapter(List<Participent> families, ExpandableListView listView, Activity activity) {
			mActivity = activity;
			mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mFamiles = families;
			mListView = listView;
		}
		
		public boolean isDuplicate(String name) {
			for (Participent family : mFamiles) {
				if(family.name.equals(name)) {
					return true;
				}
				for (Participent member : family.familyMembers) {
					if(member.name.equals(name)){
						return true;
					}
				}
			}
			return false;
		}
		
		// Adds a head of family
		public void addFamily(String name, String email) {
			if(isDuplicate(name)) {
				Toast.makeText(mActivity, name + " is already in the list so was not added.", Toast.LENGTH_LONG).show();
				return;
			}
			mFamiles.add(new Participent(name,email));
			this.notifyDataSetChanged();
		}
		
		public void addFamilyMember(Participent family, String name, String email) {
			if(isDuplicate(name)) {
				Toast.makeText(mActivity, name + " is already in the list so was not added.", Toast.LENGTH_LONG).show();
				return;
			}
			family.familyMembers.add(new Participent(name, email));
			this.notifyDataSetChanged();
		}
		
		public void addFamilyMember(int familyPos, String name, String email) {
			Log.d("ADD","Adding family member " + name);
			addFamilyMember(mFamiles.get(familyPos),name,email);
		}
		
		@Override
		public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

			Participent family =  mFamiles.get(groupPosition);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.child_row, null);
			}
			
			Participent member = family.familyMembers.get(childPosition);

			TextView textView = (TextView) convertView.findViewById(R.id.labelText);
			textView.setText(member.name);
			
			if (member.email != null) {
				textView = (TextView) convertView.findViewById(R.id.subLabelText);
				textView.setText(member.email);
				textView.setVisibility(View.VISIBLE);
			}

			return convertView;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			
			final Participent family = mFamiles.get(groupPosition);
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.group_row, null);
			}

			((TextView) convertView.findViewById(R.id.labelText)).setText(family.name);
			((TextView) convertView.findViewById(R.id.subLabelText)).setText(family.email);
			((TextView) convertView.findViewById(R.id.addChildButton)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					final EditText nameInput = new EditText(mActivity);
					new AlertDialog.Builder(mActivity)
					.setCancelable(true)
					.setTitle("Name")
					.setView(nameInput)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String name = nameInput.getText().toString();
							if (name == null || name.length() == 0) {
								Toast.makeText(mActivity, String.format("Not a valid name"), Toast.LENGTH_LONG).show();
								return;
							}
							NewListAdapter.this.addFamilyMember(family, name, null);
						}
					})
					.setNeutralButton("Add From Contacts", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI), Constants.PICK_FAMILY_MEMBER+groupPosition);
						}
					})
					.show();
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
			//Log.d("CHILDREN","SIZE:"+mFamiles.get(groupPosition).familyMembers.size());
			return mFamiles.get(groupPosition).familyMembers.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			//Log.d("FAMILES","SIZE:"+mFamiles.size());
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
