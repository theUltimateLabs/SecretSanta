package com.theultimatelabs.secretsanta;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;




public class LoginActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		findViewById(R.id.loginButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String listname = ((EditText) LoginActivity.this.findViewById(R.id.listnameText)).getText().toString();
				String password = ((EditText) LoginActivity.this.findViewById(R.id.passwordText)).getText().toString();
				String url = String.format("/login?listname=%s&password=%s",listname,password);
				new URLFetch(Constants.HOST) {
					protected void onPostExecute(JSONObject result) {
						if (result!=null) {
							Log.i("RESULT",result.toString());
							LoginActivity.this.startActivity(new Intent(LoginActivity.this,ListActivity.class));
						}
				     }
				}.execute(url);
				
			}
		});
		
		findViewById(R.id.createButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
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
	
	
}
