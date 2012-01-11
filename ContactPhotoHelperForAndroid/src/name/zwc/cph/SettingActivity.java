package name.zwc.cph;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Button;

public class SettingActivity extends BaseActivity
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        
        // Read
        int countryCode = getCountryCode();
        ((EditText)findViewById(R.id.editTextCountryCode)).setText(String.valueOf(countryCode));
        
        ((Button)findViewById(R.id.buttonSubmit)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				// Save
				String value = ((EditText)findViewById(R.id.editTextCountryCode)).getText().toString();
				if (value != null && !value.equals(""))
				{
					int countryCode = Integer.parseInt(value);
					setCountryCode(countryCode);
					show("保存成功");
				}
				else 
				{
					setCountryCode(86);
					show("默认中国国家号为86");
				}
				context.finish();
			}
		});
    }
}
