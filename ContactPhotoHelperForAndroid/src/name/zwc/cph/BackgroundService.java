package name.zwc.cph;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BackgroundService extends Service
{
    @Override
    public void onCreate()
    {
    }
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
