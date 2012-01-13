package name.zwc.cph;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class ContactPhotoHelperForAndroidActivity extends BaseActivity
{
	static String TEMP_PATH = Environment.getExternalStorageDirectory() + "/temp.jpg";
	static final int CAMERA_REQUEST_CODE = 1;
	Bitmap currentBitmap = null;
	
	protected Handler handler2 = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			TextView textViewMessage = ((TextView)findViewById(R.id.textViewMessage));
			
			switch (msg.what)
			{
			case 1: // show method
				String text = textViewMessage.getText().toString() + (String)msg.obj + "\n";
				textViewMessage.setText(text);
				break;
			case 2: // resertPrint method
				textViewMessage.setText("");
				break;
			case 3: // showUploadingPanel method
			{
				View uploadingView = context.findViewById(R.id.UploadingLayout);
				View mainView = context.findViewById(R.id.MainLayout);
				
				Animation popin = AnimationUtils.loadAnimation(context, R.anim.popin);
				uploadingView.setVisibility(View.VISIBLE);
				uploadingView.startAnimation(popin);
				
				Animation slidein = AnimationUtils.loadAnimation(context, R.anim.slidein);
				mainView.startAnimation(slidein);
				
				break;
			}
			case 4: // hideUploadingPanel method
			{
				View uploadingView = context.findViewById(R.id.UploadingLayout);
				if (uploadingView.getVisibility() == View.VISIBLE)
				{
					View mainView = context.findViewById(R.id.MainLayout);
					
					Animation popin = AnimationUtils.loadAnimation(context, R.anim.popout);
					uploadingView.startAnimation(popin);
					uploadingView.setVisibility(View.GONE);
					
					Animation slidein = AnimationUtils.loadAnimation(context, R.anim.slideout);
					mainView.startAnimation(slidein);
				}
				break;
			}
			default:
				break;
			}
		};
	};
	
	protected void print(String content)
	{
		Message message = new Message();
		message.what = 1;
		message.obj = content;
		handler2.sendMessage(message);
	}
	protected void resertPrint()
	{
		Message message = new Message();
		message.what = 2;
		handler2.sendMessage(message);
	}
	protected void showUploadingPanel()
	{
		Message message = new Message();
		message.what = 3;
		handler2.sendMessage(message);
	}
	protected void hideUploadingPanel()
	{
		Message message = new Message();
		message.what = 4;
		handler2.sendMessage(message);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setBackgroundTask(R.id.buttonUpload, new Runnable()
		{
			public void run()
			{
				View uploadingView = context.findViewById(R.id.UploadingLayout);
				if (uploadingView.getVisibility() == View.GONE)
				{
					showUploadingPanel();
				}
				else
				{
					hideUploadingPanel();
				}
			}
		});
        
        setBackgroundTask(R.id.buttonCheckUpdate, new Runnable()
		{
			public void run()
			{
				hideUploadingPanel();
				checkUpdate();
			}
		});
        
        setBackgroundTask(R.id.buttonDownload, new Runnable()
		{
			public void run()
			{
				hideUploadingPanel();
				setMD5Array(new MD5[0]);
				checkUpdate();
			}
		});
        
        // Click on the image
        ((ImageButton)findViewById(R.id.imageButtonPicture)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(TEMP_PATH)));
				startActivityForResult(intent, CAMERA_REQUEST_CODE);
			}
		});
        
        // Click OK
        setBackgroundTask(R.id.buttonSubmit, new Runnable()
		{
			public void run()
			{
				String phone =  ((EditText)findViewById(R.id.editTextPhoneNumber)).getText().toString();
				if (phone == null || phone.trim().equals(""))
				{
					show("请填写你的手机号码");
					return;
				}
				if (currentBitmap == null)
				{
					show("请点击图片拍照");
					return;
				}
				
				phone = String.valueOf(getCountryCode()) + phone;
				
				boolean result = service.submitPhoto(phone, currentBitmap);
				if (!result)
				{
					show("上传失败");
					return;
				}
				
				show("头像上传成功");
				hideUploadingPanel();
			}
		});
    }
    
    void checkUpdate()
    {
    	resertPrint();
		print("正在检查更新");
		String[] numbers = Helpers.getMobileNumbersFromContact(context);
		MD5[] md5InCacheArray = getMD5Array();
		MD5[] md5Array = new MD5[numbers.length];
		for (int i = 0; i < numbers.length; ++i)
		{
			md5Array[i] = new MD5();
			md5Array[i].Key = numbers[i];
			md5Array[i].Value = "";
			for (MD5 md5 : md5InCacheArray)
			{
				if (md5Array[i].Key.equals(md5.Key))
				{
					md5Array[i].Value = md5.Value;
					break;
				}
			}
		}
		MD5[] md5UpdatedArray = service.checkUpdate(md5Array);
		if (md5UpdatedArray == null)
		{
			print("网络错误");
    		return;
		}
		if (md5UpdatedArray.length <= 0)
    	{
			print("没有可更新的头像");
    		return;
    	}
		print(String.format("发现%s个可更新头像, 正在下载", md5UpdatedArray.length));
		downloadPhotos(md5UpdatedArray);
		updateMD5Array(md5UpdatedArray);
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
			}
		}
		finally
		{
			phoneCursor.close();
		}
    	
		print(String.format("下载完毕, 共下载了%s个头像", downloadedCount));
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
    	nofity(contactID, photoBytes);
    }
    
    void nofity(String contactID, byte[] picBytes)
    {
    	String displayName = getDisplayName(contactID);
    	String message = displayName + " 更新了头像";
    	
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    	
    	int icon = R.drawable.icon;
    	CharSequence tickerText = message;
    	long when = System.currentTimeMillis();

    	Notification notification = new Notification(icon, tickerText, when);
    	notification.flags = Notification.FLAG_AUTO_CANCEL;
    	
    	RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.status_bar_latest_event_content);
    	contentView.setImageViewBitmap(R.id.image, BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length));
    	contentView.setTextViewText(R.id.title, message);
    	contentView.setTextViewText(R.id.text, "来自 联系人图片助手");
    	notification.contentView = contentView;
    	
    	Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactID));
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	notification.contentIntent = contentIntent;
    	
    	mNotificationManager.notify(Integer.parseInt(contactID), notification);
    }
    
    String getDisplayName(String contactID)
    {
    	Cursor contactCursor =  managedQuery(
    	        Contacts.CONTENT_URI, 
    	        new String[] { Contacts.DISPLAY_NAME }, 
    	        Contacts._ID + " = " + contactID, 
    	        null, 
    	        null);
    	if (contactCursor.moveToFirst())
    	{
    		return contactCursor.getString(0);
    	}
    	return "";
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
    	menu.add("退出");
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
    	else if (item.getTitle().toString().equalsIgnoreCase("退出"))
    	{
    		context.finish();
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode) 
		{
	        case CAMERA_REQUEST_CODE:
	        	File file = new File(TEMP_PATH);
	        	if (file.exists())
	        	{
		            Bitmap bitmap = BitmapFactory.decodeFile(TEMP_PATH);
		            bitmap = resizeBitmap(bitmap);
		            ((ImageButton)findViewById(R.id.imageButtonPicture)).setImageBitmap(bitmap);
		            currentBitmap = bitmap;
	        	}
	            break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static Bitmap resizeBitmap(Bitmap bitmap)
	{
		int size = 100;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		float scale = 1;
		int startX = 0;
		int startY = 0;
		if (width > height)
		{
			scale = ((float) size / height);
			startX = (width - height) / 2;
			width = height;
		}
		else
		{
			scale = ((float) size / width);
			startY = (height - width) / 2;
			height = width;
		}
		
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		return Bitmap.createBitmap(bitmap, startX, startY, width, height, matrix, true);
	}
}