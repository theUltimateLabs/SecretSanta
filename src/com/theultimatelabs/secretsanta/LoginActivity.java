package com.theultimatelabs.secretsanta;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;




public class LoginActivity extends Activity {

	private MyApp Globals;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Globals = (MyApp) getApplication();
		
		findViewById(R.id.loginButton).setOnClickListener(new LoginListener("login"));
		findViewById(R.id.createButton).setOnClickListener(new LoginListener("create"));
		findViewById(R.id.demoButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginActivity.this.startActivity(new Intent(LoginActivity.this,MainActivity.class));
				LoginActivity.this.finish();
			}
		});
		
	}
	
	private class LoginListener implements OnClickListener {
		String mType;
		
		LoginListener(String type) {
			mType = type;
		}
		
		@Override
		public void onClick(View arg0) {
			final String listname = ((EditText) LoginActivity.this.findViewById(R.id.listnameText)).getText().toString();
			String password = ((EditText) LoginActivity.this.findViewById(R.id.passwordText)).getText().toString();
			//Hash the password for privacy and security
			byte [] passwordHashBytes;
			try {
				passwordHashBytes = MessageDigest.getInstance("SHA-1").digest(password.getBytes("UTF-8"));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			}
			
			final String passwordHash = bytesToHex(passwordHashBytes);
			
			String url = String.format("/%s?listname=%s&password=%s",mType,listname,passwordHash);
			new URLFetch(Constants.HOST) {
				protected void onPostExecute(String result) {
					if (result!=null) {
						Log.i("RESULT",result.toString());
						Globals.listname = listname;
						Globals.password = passwordHash;
						try {
							JSONObject historiesJson = new JSONObject(result);
							Iterator<String> yearIter = historiesJson.keys();
							while(yearIter.hasNext()) {
								String year = yearIter.next();
								Integer yearInt = (Integer) Integer.parseInt(year);
								JSONObject solution = historiesJson.getJSONObject(year);
								Iterator<String> gifterIter = solution.keys();
								if (gifterIter.hasNext()) {
									Map<String, String> history =  new HashMap<String,String>();
									while(gifterIter.hasNext()) {
										String gifter = gifterIter.next();
										String giftee = solution.getString(gifter);
										history.put(gifter, giftee);
									}
									Globals.histories.put(yearInt,history);
								}
								
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						LoginActivity.this.startActivity(new Intent(LoginActivity.this,MainActivity.class));
						LoginActivity.this.finish();
					}
					else {
						if (mType.equals("login")) {
							Toast.makeText(LoginActivity.this, "Unable to login, check listname and password", Toast.LENGTH_LONG).show();
						}
						else if (mType.equals("create")) {
							Toast.makeText(LoginActivity.this, "Unable to create new listname, try a different listname", Toast.LENGTH_LONG).show();
						}
					}
			     }
			}.execute(url);
		}
	}
	
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
}
