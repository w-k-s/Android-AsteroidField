package com.google.app.AsteroidField;

import com.google.app.AsteroidField.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class About extends Activity {

	WebView webviewAbout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.about);
        
        webviewAbout = (WebView) findViewById(R.id.webviewAbout);
        webviewAbout.getSettings().setLoadWithOverviewMode(true);
		webviewAbout.getSettings().setUseWideViewPort(true);
		//webviewAbout.getSettings().setBuiltInZoomControls(true);
		webviewAbout.getSettings().setAllowFileAccess(false);
		webviewAbout.loadUrl("file:///android_asset/about.html");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}
}
