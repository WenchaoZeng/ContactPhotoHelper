package name.zwc.cph;

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
}
