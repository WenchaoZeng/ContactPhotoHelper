package name.zwc.cph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract.CommonDataKinds.Phone;


public class Helpers
{
	public static String countryCode = "86";
	
	public static String[] getMobileNumbersFromContact(Context context)
	{
		List<String> numbers = new ArrayList<String>();
		
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(Phone.CONTENT_URI, new String[] { Phone.NUMBER }, null, null, null);
		try
		{
			while (cursor.moveToNext())
			{
				String phone = cursor.getString(0);
				phone = correctPhoneNumber(phone);
			    numbers.add(phone);
			}
		}
		finally
		{
			cursor.close();
		}

		return numbers.toArray(new String[0]);
	}
	
	public static String correctPhoneNumber(String number)
	{
		number = number.replace("-", "").replace("+", "");
		if (!number.startsWith(countryCode))
		{
			number = countryCode + number;
		}
		return number;
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
