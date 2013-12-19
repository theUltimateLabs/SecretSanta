package com.theultimatelabs.secretsanta;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactInfo {
	
	public String name = null;
	public Uri photoUri = null;
	public Bitmap photoBitmap = null;
	public String [] emails = null;
	
	public Bitmap getContactPhoto(ContentResolver cr, long userId, long photoId) {
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
		InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
		if (input != null) {
			return BitmapFactory.decodeStream(input);
		} else {
			//Log.d("PHOTO", "first try failed to load photo");
		}

		byte[] photoBytes = null;
		Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);
		Cursor c = cr.query(photoUri, new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, null, null, null);

		try {
			if (c.moveToFirst())
				photoBytes = c.getBlob(0);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			c.close();
		}

		if (photoBytes != null)
			return BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
		else
			Log.d("PHOTO", "second try also failed");
		return null;
	}

	public void ContactInfoEmail(ContentResolver cr, Intent intent) {
		
	
	}
	
	public ContactInfo(ContentResolver cr, Intent intent) {
		
		Uri contactData = intent.getData();
		Cursor dataCursor = cr.query(contactData, null, null, null, null);
		if (!dataCursor.moveToFirst())
			return;

		this.name = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		long contactId = dataCursor.getLong(dataCursor.getColumnIndex(ContactsContract.Contacts._ID));
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		Log.i("contactId", new Long(contactId).toString());
		Log.i("contactUri", contactUri.toString());

		long photoId = dataCursor.getLong(dataCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
		this.photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);
		Log.i("photoId", new Long(photoId).toString());
		Log.i("photoUri", this.photoUri.toString());
		
		this.photoBitmap = this.getContactPhoto(cr, contactId, photoId);
		
		//Log.i("COLUMNS",dataCursor.getColumnNames().toString());
		
		ArrayList<String> emailCollection = new ArrayList<String>();
		int emailIndex = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
		if (emailIndex >= 0) {
			String primaryEmail = dataCursor.getString(emailIndex);
			Log.i("email",primaryEmail);
			
		
			if(primaryEmail!=null) {
				emailCollection.add(primaryEmail);
			}
		}
		Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { new Long(contactId).toString() }, null);
		if (emailCursor.moveToFirst()) {
			while(true) {
				String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				if(!emailCollection.contains(email)) {
					emailCollection.add(email);
				}
				if(!emailCursor.moveToNext()) break;
			}
		}
		emailCursor.close();

		dataCursor.close();

		this.emails = Arrays.copyOf(emailCollection.toArray(), emailCollection.size(), String[].class);
		
	}
}
