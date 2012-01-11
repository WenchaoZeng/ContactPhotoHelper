package name.zwc.cph;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ContactPhotoHelperForAndroidActivity extends BaseActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ((Button)findViewById(R.id.buttonUpload)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent("name.zwc.cph.UploadActivity");
	    		context.startActivity(intent);
			}
		});
        
        ((Button)findViewById(R.id.buttonDownload)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				new Thread(new Runnable()
				{
					public void run()
					{
						String[] numbers = Helpers.getMobileNumbersFromContact(context);
						MD5[] md5Array = new MD5[numbers.length];
						for (int i = 0; i < numbers.length; ++i)
						{
							md5Array[i] = new MD5();
							md5Array[i].Key = numbers[i];
							md5Array[i].Value = "";
						}
						MD5[] md5UpdatedArray = service.checkUpdate(md5Array);

						downloadPhotos(md5UpdatedArray);
					}
				}).start();
			}
		});
    }
    
    void downloadPhotos(MD5[] md5Array)
    {
    	ContentResolver contentResolver = context.getContentResolver();
    	
    	// 遍历所有电话号码
		Cursor phoneCursor = contentResolver.query(Phone.CONTENT_URI, 
				new String[] { Phone.NUMBER, Phone.CONTACT_ID}, 
				null, 
				null, 
				null);
		int downloadedCount = 0;
		try
		{
			while (phoneCursor.moveToNext())
			{
				// 确定这个号码需要更新图片
				String number = phoneCursor.getString(0);
				MD5 md5 = findMD5(md5Array, number);
				if (md5 == null)
				{
					continue;
				}
				
				// 为联系人实体下载图片
				String contactID = phoneCursor.getString(1);
				downloadPhoto(contactID, md5.Key);
				downloadedCount++;
				show(String.format("下载头像中%s/%s", downloadedCount, md5Array.length));
			}
		}
		finally
		{
			phoneCursor.close();
		}
    	
    	show(String.format("共下载了%s张头像", downloadedCount));
    }
    
    MD5 findMD5(MD5[] md5Array, String rawNumber)
    {
    	String correctedNumber = Helpers.correctPhoneNumber(rawNumber);
    	for (MD5 md5 : md5Array)
		{
    		if (md5.Key.equals(correctedNumber))
    		{
    			return md5;
    		}
		}
    	return null;
    }
    
    void downloadPhoto(String contactID, String number)
    {
    	// 下载图片
    	byte[] photoBytes = service.downloadPicture(number);
    	if (photoBytes == null)
    	{
    		return;
    	}
    	
    	setContactPhoto(contactID, photoBytes);
    }
    
    // Copied from http://stackoverflow.com/questions/7968156/android-change-contact-picture
    void setContactPhoto(String contactID, byte[] photoBytes)
    {
    	Uri rawContactUri = null;
    	Cursor rawContactCursor =  managedQuery(
    	        RawContacts.CONTENT_URI, 
    	        new String[] {RawContacts._ID}, 
    	        RawContacts.CONTACT_ID + " = " + contactID, 
    	        null, 
    	        null);
    	if(!rawContactCursor.isAfterLast()) {
    	    rawContactCursor.moveToFirst();
    	    rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendPath(""+rawContactCursor.getLong(0)).build();
    	}
    	rawContactCursor.close();
    	
    	
    	byte[] photo = photoBytes;
    	
    	
    	ContentValues values = new ContentValues(); 
    	int photoRow = -1; 
    	String where = ContactsContract.Data.RAW_CONTACT_ID + " == " + 
    	    ContentUris.parseId(rawContactUri) + " AND " + Data.MIMETYPE + "=='" + 
    	    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'"; 
    	Cursor cursor = managedQuery(
    	        ContactsContract.Data.CONTENT_URI, 
    	        null, 
    	        where, 
    	        null, 
    	        null); 
    	int idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID); 
    	if(cursor.moveToFirst()){ 
    	    photoRow = cursor.getInt(idIdx); 
    	} 
    	cursor.close(); 
    	values.put(ContactsContract.Data.RAW_CONTACT_ID, 
    	        ContentUris.parseId(rawContactUri)); 
    	values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1); 
    	values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, photo); 
    	values.put(ContactsContract.Data.MIMETYPE, 
    	        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE); 
    	if(photoRow >= 0){ 
    	    this.getContentResolver().update(
    	            ContactsContract.Data.CONTENT_URI, 
    	            values, 
    	            ContactsContract.Data._ID + " = " + photoRow, null); 
    	    } else { 
    	        this.getContentResolver().insert(
    	                ContactsContract.Data.CONTENT_URI, 
    	                values); 
    	    }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add("设置");
    	return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if (item.getTitle().toString().equalsIgnoreCase("设置"))
    	{
    		Intent intent = new Intent("name.zwc.cph.SettingActivity");
    		context.startActivity(intent);
    	}
    	return super.onOptionsItemSelected(item);
    }
}