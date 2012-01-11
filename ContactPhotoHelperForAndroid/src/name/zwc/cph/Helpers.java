package name.zwc.cph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract.CommonDataKinds.Phone;


public class Helpers
{
	public static String[] getMobileNumbersFromContact(Context context, String countryCode)
	{
		List<String> numbers = new ArrayList<String>();
		
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(Phone.CONTENT_URI, new String[] { Phone.NUMBER }, null, null, null);
		try
		{
			String phone = null;
			while (cursor.moveToNext())
			{
				phone = cursor.getString(0);
				phone = phone.replace("-", "").replace("+", "");
				if (!phone.startsWith(countryCode))
				{
					phone = countryCode + phone;
				}
			    numbers.add(phone);
			}
		}
		finally
		{
			cursor.close();
		}

		return numbers.toArray(new String[0]);
	}
	
	public static byte[] compressBitmap(Bitmap bitmap)
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream))
		{
			try
			{
				byteStream.flush();
				byteStream.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
		return byteStream.toByteArray();
	}
}
