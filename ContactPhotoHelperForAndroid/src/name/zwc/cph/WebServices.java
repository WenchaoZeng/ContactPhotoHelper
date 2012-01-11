package name.zwc.cph;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.graphics.Bitmap;

public class WebServices
{
	protected String host = "http://zwc.name/";
	protected String gateway = host + "caller/call.ashx?h=ContactPhotoHelper";
	
	protected String call(String method, String post, String get)
	{
		try
		{
			// The GET parameters.
			String targetUrl = gateway + "&m=" + method;
			if (get != null)
			{
				targetUrl += "&" + get;
			}

		    // The POST content.
		    URL url = new URL(targetUrl);
		    URLConnection conn = url.openConnection();
		    if (post != null)
		    {
		    	conn.setRequestProperty("Content-Type", "application/jsonrequest");
			    conn.setDoOutput(true);
			    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			    wr.write(post);
			    wr.flush();
			    wr.close();
		    }

		    // Read response.
		    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    StringBuilder builder = new StringBuilder();
		    String line;
		    while ((line = reader.readLine()) != null)
		    {
		    	builder.append(line + "\r\n");
		    }
		    reader.close();
		    
		    return builder.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	public boolean submitPhoto(String phone, Bitmap pic)
	{
		byte[] compressedBytes = Helpers.compressBitmap(pic);
		byte[] base64Bytes = Base64.encode(compressedBytes);
		String base64String = new String(base64Bytes);
		String response = call("SubmitPhoto", base64String, "key=" + phone);
		return response != null && response.trim().toLowerCase().equals("true");
	}
	
	public MD5[] checkUpdate(MD5[] md5Array)
	{
		String post = new Gson().toJson(md5Array);
		String response = call("CheckUpdate", post, null);
		try
		{
			MD5[] md5ResultArray = new Gson().fromJson(response, MD5[].class);
			return md5ResultArray;
		}
		catch (JsonSyntaxException ex)
		{
			return new MD5[0];
		}
	}
	
	public byte[] downloadPicture(String number)
	{
		try
		{
			String url = host + "/ContactPhotoHelperFiles/" + number + ".jpg";
			InputStream inputStream = new URL(url).openConnection().getInputStream();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    int length = -1;
		    byte[] buffer = new byte[4096];
		    while ((length = inputStream.read(buffer)) != -1)
			{
				outputStream.write(buffer, 0, length);
			}
		    return outputStream.toByteArray();
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
