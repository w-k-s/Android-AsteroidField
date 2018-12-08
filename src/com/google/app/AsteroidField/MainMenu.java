package com.google.app.AsteroidField;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.app.AsteroidField.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainMenu extends Activity implements OnClickListener {

	private static final String FILENAME 	= "Scores.txt";
	
	private static final int LABEL_SCORE = 0;
	private static final int LABEL_HIGHSCORE = 1;
	private static final int REQUEST_CODE_SCORE_RESULT = 0;
	private static final String KEY_SCORE_RESULT = "score";
	private static final String KEY_HIGHSCORE = "highscore";
	
	private Button 		buttonPlay, buttonSettings, buttonAbout;
	private TextView	labelScore, labelHighScore;
	
	private int score;
	private int highscore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initiate UI
		//Log.d(TAG,"Creating Main Menu");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.main);
		
		highscore = readHighScoreFromFile();

		buttonPlay 			= (Button) findViewById(R.id.buttonPlay);
		buttonSettings 		= (Button) findViewById(R.id.buttonSettings);
		buttonAbout 		= (Button) findViewById(R.id.buttonAbout);
		
		labelScore 			= (TextView) findViewById(R.id.textviewScore);
		labelHighScore		= (TextView) findViewById(R.id.textviewHighScore);
		
		buttonPlay.setOnClickListener(this);
		buttonSettings.setOnClickListener(this);
		buttonAbout.setOnClickListener(this);

		setTextForLabel(LABEL_HIGHSCORE, highscore);
		setTextForLabel(LABEL_SCORE, 0);
	}

	@Override
	protected void onPause() {
		//Log.d(TAG,"Main Menu - Paused");
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		//Log.d(TAG,"Main Menu - Resumed");
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		//Log.d(TAG,"Main Menu - Exited");
		super.onDestroy();
	}

	public void onClick(View clickedButton) {
		switch (clickedButton.getId()) {
		case R.id.buttonPlay:
			
			Intent gameIntent = new Intent(MainMenu.this,BouncingBallActivity.class);
			gameIntent.putExtra(KEY_HIGHSCORE,this.highscore );
			
			//Log.d(TAG,"Starting "+gameIntent.toString()+" for Result");
			startActivityForResult(gameIntent, REQUEST_CODE_SCORE_RESULT);
			break;

		case R.id.buttonSettings:
			//Log.d(TAG,"Opening Preferences");
			startActivity(new Intent(MainMenu.this,Settings.class));
			break;

		case R.id.buttonAbout:
			startActivity(new Intent(MainMenu.this,About.class));
			break;
		}

	}
	
	@SuppressWarnings("unused")
	private int readHighScoreFromFile()
	{
		//Log.d(TAG,"Reading highscore from file");
		
		try {
			FileInputStream fis = openFileInput(FILENAME);
			byte[] data = new byte[fis.available()];
			fis.read(data);
			String score = new String(data);
			fis.close();
			
			if(score == null)
			{
				//Log.e(TAG,"Highscore read:"+score);
				return 0;
			}else
			{
				//Log.e(TAG,"Highscore read:"+score);
				return Integer.parseInt(score);
			}
		} catch (FileNotFoundException e) {
			//Log.e(TAG,e.toString() + " occured while reading highscore from file");
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			//Log.e(TAG,e.toString() + " occured while reading highscore from file");
			e.printStackTrace();
			return 0;
		}

	}
	
	private void writeHighScoreToFile()
	{
		//Log.e(TAG,"Writing highscore ( "+score +" ) to file");
		try {
			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write(String.valueOf(score).getBytes());
			fos.close();
		
			//Log.e(TAG,"Highscore written");
		} catch (FileNotFoundException e) {
			//Log.e(TAG,e.toString() + " occured while writing new highscore to file");
			e.printStackTrace();
		} catch (IOException e) {
			//Log.e(TAG,e.toString() + " occured while writing new highscore to file");
			e.printStackTrace();
		}
		
	}
	
	private void setTextForLabel(int labelID,int score)
	{
		switch(labelID)
		{
		case LABEL_SCORE:
			labelScore.setText("Score: "+String.valueOf(score));
			break;
		case LABEL_HIGHSCORE:
			labelHighScore.setText("High Score: "+String.valueOf(score));
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode)
		{
		case REQUEST_CODE_SCORE_RESULT:
			if(resultCode==RESULT_OK)
			{
				this.score = data.getIntExtra(KEY_SCORE_RESULT, 0);
				setTextForLabel(LABEL_SCORE, this.score);
				if(this.score > this.highscore)
				{
					this.highscore = this.score;
					writeHighScoreToFile();
					setTextForLabel(LABEL_HIGHSCORE, this.highscore);
				}
				
			}else
			{
				//Log.e(TAG,"Result could not be delivered.");
			}
			break;
		}
	}
}
