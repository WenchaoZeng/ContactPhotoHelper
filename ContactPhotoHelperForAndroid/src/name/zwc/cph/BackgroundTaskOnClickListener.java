package name.zwc.cph;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

public class BackgroundTaskOnClickListener implements OnClickListener
{
	public Activity context;
	public View view;
	public Runnable task;
	public BackgroundTaskOnClickListener(Activity context, int viewID, Runnable task)
	{
		this.context = context;
		this.view = context.findViewById(viewID);
		this.task = task;
	}
	
	protected Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what)
			{
			case 1: // enableButton
				view.setEnabled(true);
				break;
			default:
				break;
			}
		};
	};
	protected void enableButton()
	{
		Message message = new Message();
		message.what = 1;
		handler.sendMessage(message);
	}
	
	public void onClick(View v)
	{
		view.setEnabled(false);

		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					task.run();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				finally
				{
					enableButton();
				}
			}
		}).start();
	}
}
