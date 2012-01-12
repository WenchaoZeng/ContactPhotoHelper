package name.zwc.cph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class BaseActivity extends Activity
{
	protected static final WebServices service = new WebServices();
	
	protected Activity context;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
	}
	
	protected Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what)
			{
			case 1: // show method
				Toast.makeText(context, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};
	protected void show(String content)
	{
		Message message = new Message();
		message.what = 1;
		message.obj = content;
		handler.sendMessage(message);
	}
	
	public int getCountryCode()
	{
		SharedPreferences preferences = getSharedPreferences("user", Application.MODE_PRIVATE);
		return preferences.getInt("country_code", 86);
	}
	public void setCountryCode(int value)
	{
		SharedPreferences preferences = getSharedPreferences("user", Application.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("country_code", value);
		editor.commit();
	}
	
	protected void setBackgroundTask(int buttonViewID, Runnable task)
	{
		BackgroundTaskOnClickListener onClickListener = new BackgroundTaskOnClickListener(context, buttonViewID, task);
		findViewById(buttonViewID).setOnClickListener(onClickListener);
	}
	
	public void setMD5Array(MD5[] md5Array)
	{
		SharedPreferences preferences = getSharedPreferences("user", Application.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		String jsonString = new Gson().toJson(md5Array);
		editor.putString("md5_array", jsonString);
		editor.commit();
	}
	public MD5[] getMD5Array()
	{
		SharedPreferences preferences = getSharedPreferences("user", Application.MODE_PRIVATE);
		String jsonString = preferences.getString("md5_array", "[]");
		MD5[] md5ResultArray = new Gson().fromJson(jsonString, MD5[].class);
		return md5ResultArray;
	}
	public void updateMD5Array(MD5[] md5UpdatedArray)
	{
		List<MD5> md5List = new ArrayList<MD5>(Arrays.asList(getMD5Array()));
		for (MD5 updatedMD5 : md5UpdatedArray)
		{
			boolean found = false;
			for (MD5 mdInCache : md5List)
			{
				if (mdInCache.Key.equals(updatedMD5.Key))
				{
					mdInCache.Value = updatedMD5.Value;
					found = true;
					break;
				}
			}
			if (!found)
			{
				md5List.add(updatedMD5);
			}
		}
		setMD5Array(md5List.toArray(new MD5[0]));
	}
}
