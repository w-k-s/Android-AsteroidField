package com.google.app.AsteroidField;

import com.google.app.AsteroidField.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
//import android.util.Log;
import android.view.WindowManager;

public class Settings extends PreferenceActivity 
{
	//private static final String TAG = Settings.class.getSimpleName();
	
	protected void onCreate(Bundle savedInstanceState)
	{	
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        super.onCreate(savedInstanceState);
		
		//Log.d(TAG,"Creating Settings");
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Log.d(TAG,"Settings Menu - Destroyed.");
	}
}
