package name.zwc.cph;

import android.content.Intent;
import android.os.Bundle;
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
						String[] numbers = Helpers.getMobileNumbersFromContact(context, "86");
						MD5[] md5Array = new MD5[numbers.length];
						for (int i = 0; i < numbers.length; ++i)
						{
							md5Array[i] = new MD5();
							md5Array[i].Key = numbers[i];
							md5Array[i].Value = "";
						}
						MD5[] md5UpdatedArray = service.checkUpdate(md5Array);
						show(String.valueOf(md5UpdatedArray.length));
					}
				}).start();
			}
		});
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