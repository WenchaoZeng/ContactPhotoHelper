package name.zwc.cph;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;

public class UploadActivity extends BaseActivity
{
	static String TEMP_PATH = Environment.getExternalStorageDirectory() + "/temp.jpg";
	static final int CAMERA_REQUEST_CODE = 1;
	Bitmap currentBitmap = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        
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
        ((Button)findViewById(R.id.buttonSubmit)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				new Thread(new Runnable()
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
						context.finish();
					}
				}).start();
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode) 
		{
	        case CAMERA_REQUEST_CODE:
	            Bitmap bitmap = BitmapFactory.decodeFile(TEMP_PATH);
	            bitmap = resizeBitmap(bitmap);
	            ((ImageButton)findViewById(R.id.imageButtonPicture)).setImageBitmap(bitmap);
	            currentBitmap = bitmap;
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
