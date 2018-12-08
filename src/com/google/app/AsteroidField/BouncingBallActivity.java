
package com.google.app.AsteroidField;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BouncingBallActivity extends Activity implements OnLoadCompleteListener, OnClickListener {
	
	private static final String TAG = BouncingBallActivity.class.getSimpleName();

	/*DEFAULT PREFERENCE CONSTANTS-----------------------------------*/
	private static final String		DEFAULT_SPACESHIP_SPEED = "1";
	private static final boolean 	DEFAULT_SOUND = true;
	private static final String 	DEFAULT_OBSTACLES_SPEED = "0";
	
	/*BUNDLE KEYS------------------------------------------*/
	private static final String 	KEY_SCORE_RESULT = "score";
	private static final String 	KEY_HIGHSCORE = "highscore";
	private int highscore;

	/*GRAPHIC COMPONENTS-----------------------------------*/
	private FrameLayout		 		frame;
	private BouncingBallView 		bouncingBallView;
	private ImageButton		 		pauseButton;
	
	private Bitmap 					spaceship;
	private Bitmap 					asteroid;
	private Bitmap 					background;
	private Typeface 				font;
	
	/*SOUND COMPONENTS-----------------------------------*/
	private SoundPool 				soundPool;
	private boolean 				soundIsEnabled = true;
	
	private int 					alienShipCreated = 0;
	private int 					alienShipDestroyed = 0;
	
	/*PREFERENCE VARIABLES-----------------------------------*/
	private int 					speedSpaceship = 0;
	private int 					speedObstacles = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		

		LoadingTask loadTask = new LoadingTask();
		loadTask.execute(this);
	
		
		this.highscore = this.getIntent().getIntExtra(KEY_HIGHSCORE, -1);
		
		Toast instructionsToast = new Toast(this);
		TextView instructionsLabel = new TextView(this);
		instructionsLabel.setText("- Touch screen to make the spaceship go up.\n" +
				"- Release touch to make the spaceship go down.\n" +
				"- Avoid asteroids.");
		instructionsToast.setView(instructionsLabel);
		instructionsToast.setDuration(Toast.LENGTH_LONG);
		instructionsToast.setGravity(Gravity.CENTER, 0, 0);
		instructionsToast.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "BouncingBallActivity Paused");
		bouncingBallView.pause();

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "BouncingBallActivity Resumed");
		
		if(bouncingBallView == null)
			return;
		
		bouncingBallView.resume();
	}

	@Override
	protected void onDestroy() {
		if(soundPool!=null)
			soundPool.release();
		super.onDestroy();
	}

	public void onClick(View v)
	{
		pauseButton.setImageResource(bouncingBallView.toggleGamePauseState()?R.drawable.pause_on:R.drawable.pause_off);
	}
	
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
	{
		if(sampleId==this.alienShipCreated && status==0)
			soundPool.play(sampleId, 1, 1, 0, 0, 1);
	}
	
	class BouncingBallView extends SurfaceView implements Runnable {

		/*--------FINAL CONTSTANTS-------------------------*/
		private final static int FPS = 150;
		private final static int SLEEP_TIME = 1000 / FPS;

		private static final long WAIT_TIME = 2000;
		private static final int DISTANCE_FROM_DANGER_ZONE = 150;
		
		private static final float FONT_SIZE = 25;
		private static final float FONT_SIZE_LARGE = 50;

		private static final float BACKGROUND_SLOWER = 5;
		private static final float BACKGROUND_NORMAL = 10;

		private static final int SCORE_FACTOR_FAST_GAME = 30;
		private static final int SCORE_FACTOR_NORMAL_GAME = 10;
		
		/*--------STRING CONSTANTS-------------------------*/
		private static final String TEXT_GAME_OVER = "Game Over!";
		private static final String TEXT_NEW_HIGHSCORE = "New Highscore!";

		/*--------SurfaceView related components-------------------------*/
		private SurfaceHolder surfaceViewHolder;
		private Canvas canvas;
		//private Context context;

		private Thread drawingThread;
		private boolean drawingThreadIsRunning;
		private boolean isInitialised;
		private boolean gamePaused;
		
		/*--------Game components-------------------------*/
		private Ball ball;
		private ArtificialIntelligence ai;
		
		private float backgroundX;
		private float backgroundSpeedX;

		private short counter = 0;
		private long startTime = 0;


		BouncingBallView(Context context)
		{
			super(context);
			surfaceViewHolder = getHolder();
			
			this.setFocusableInTouchMode(true);

			isInitialised = false;
			backgroundX = 0;
		}

		public void pause()
		{
			drawingThreadIsRunning = false;
			boolean joiningWasSuccessful = false;

			while (!joiningWasSuccessful)
				try {
					drawingThread.join();
					joiningWasSuccessful = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		public void resume()
		{
			drawingThread = new Thread(this);
			counter = 0;

			drawingThreadIsRunning = true;

			drawingThread.start();
		}

		public void run()
		{
		while (drawingThreadIsRunning) {

		/* If service isn't valid, continue */
		if (!surfaceViewHolder.getSurface().isValid())
			continue;

		/*
		 * First action in loop is to check game over Transmit result to
		 * main menu activity if true.
		 */
		if (gameOver())
		{
			if (counter == 1)
			{
				if (System.currentTimeMillis() - startTime > WAIT_TIME)
				{
					Intent i = new Intent();
					i.putExtra(BouncingBallActivity.KEY_SCORE_RESULT,getScore());

					BouncingBallActivity.this.setResult(RESULT_OK, i);
					BouncingBallActivity.this.finish();
				}
			}
		}

		canvas = surfaceViewHolder.lockCanvas();
			
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (!isInitialised)
			init(canvas);
		update(canvas);

		if (canvas != null)
			surfaceViewHolder.unlockCanvasAndPost(canvas);

			}
		}

		private void init(Canvas canvas) {
			ball = new Ball(canvas, Color.GREEN);

			ai = new ArtificialIntelligence(canvas,
					(int) (ball.getX() + DISTANCE_FROM_DANGER_ZONE));
			ai.startFiringAfter(5000);
			ai.setObstacleSpeed(speedObstacles);
			
			if (speedSpaceship == 0)
				this.backgroundSpeedX = BACKGROUND_SLOWER;
			else
				this.backgroundSpeedX = BACKGROUND_NORMAL;
				
			isInitialised = true;
		}

		private void update(Canvas canvas) {
			if (!gameOver() && !gamePaused) {
				updateBackgroundMotion(canvas);
				ball.move();
				ai.updateArtificialIntelligence(ball);
			}
			updateDrawings();

		}

		private void updateBackgroundMotion(Canvas canvas) {
			/*
			 * While the background has not completely left the screen continue
			 * it's motion Otherwise, reset it's position
			 */
			if (backgroundX > canvas.getWidth() * -1) {
				backgroundX -= backgroundSpeedX;
			} else {
				backgroundX = 0;
			}

		}

		private boolean gameOver() {

			if (ball == null)
				return false;

			if (counter == 1)
				return true;

			if (ball.DidCollide()) {
				this.startTime = System.currentTimeMillis();
				counter++;
				if (soundIsEnabled && alienShipDestroyed != 0)
					soundPool.play(alienShipDestroyed, 1, 1, 0, 0, 1);
				return true;
			} else
				return false;
		}

		private void updateDrawings() {

			/*
			 * p.setColor(Color.BLUE); canvas.drawRect(0, 0, canvas.getWidth(),
			 * canvas.getHeight(), p);
			 */
			canvas.drawBitmap(background, backgroundX, 0, null);
			canvas.drawBitmap(background, backgroundX + canvas.getWidth(), 0,
					null);

			ball.draw();
			ai.draw();

			Paint p = new Paint();
			p.setTypeface(font);

			if (!gameOver()) {
				p.setColor(Color.YELLOW);
				p.setTextSize(FONT_SIZE);
				canvas.drawText("Score: " + getScore(), 10,
						canvas.getHeight() - 20, p);
			} else {
				int score = getScore();
				boolean isHighScore = score > BouncingBallActivity.this.highscore;
				String result = isHighScore ? TEXT_NEW_HIGHSCORE
						: TEXT_GAME_OVER;

				p.setTextAlign(Align.CENTER);
				p.setTextSize(FONT_SIZE_LARGE);

				if (isHighScore)
					p.setARGB(200, 124, 252, 0);
				else
					p.setARGB(200, 255, 0, 0);

				canvas.drawText(result, canvas.getWidth() / 2,
						canvas.getHeight() / 2, p);
				canvas.drawText("Score: " + score, canvas.getWidth() / 2,
						canvas.getHeight() / 2 + FONT_SIZE_LARGE + 5, p);
			}
		}

		public int getScore() {
			return ai.getObstacleSpeed()==ArtificialIntelligence.OBSTACLE_SPEED_FAST?(ai.getScore()*SCORE_FACTOR_FAST_GAME):(ai.getScore()*SCORE_FACTOR_NORMAL_GAME);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			if (ball == null)
				return false;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				ball.setTouched(true);
				return true;

			case MotionEvent.ACTION_UP:
				ball.setTouched(false);
				return true;

			}
			return false;
		}
		
		public boolean toggleGamePauseState()
		{
			this.gamePaused = this.gamePaused?false:true;
			return this.gamePaused;
		}
	}

	class LoadingTask extends AsyncTask<Context,Integer,Boolean>
	{

		protected void onPostExecute(Boolean result)
		{
			bouncingBallView.resume();
			//Log.d(TAG, "Content View of Bouncing Ball Activity Set.");
			BouncingBallActivity.this.setContentView(frame);
		}

		protected Boolean doInBackground(Context... contexts)
		{
			final Context context = contexts[0];
			
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(contexts[0]);

			soundIsEnabled = sharedPrefs.getBoolean("sound", DEFAULT_SOUND);
			speedSpaceship = Integer.parseInt(sharedPrefs.getString("speed_spaceship",
					DEFAULT_SPACESHIP_SPEED));
			speedObstacles = Integer.parseInt(sharedPrefs.getString("speed_obstacles", DEFAULT_OBSTACLES_SPEED));

			//Log.d(TAG, "Finished loading preferences.");
			
			if(soundIsEnabled)
			{
				soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				soundPool.setOnLoadCompleteListener(BouncingBallActivity.this);
				
				alienShipCreated = soundPool.load(context,
						R.raw.alien_ship_created, 1);
				alienShipDestroyed = soundPool.load(context,
						R.raw.alien_ship_crash, 1);
			}

			font = Typeface.createFromAsset(context.getAssets(),"effortless.ttf");
			
			//Log.d(TAG, "Creating BouncingBallActivity");
			runOnUiThread(new Runnable()
			{

				public void run() 
				{
					frame = new FrameLayout(context);
					
					LinearLayout buttonPanel = new LinearLayout(context);
					bouncingBallView = new BouncingBallView(context);
					
					pauseButton = new ImageButton(context);
					pauseButton.setImageResource(R.drawable.pause_off);
					pauseButton.setOnClickListener(BouncingBallActivity.this);
					pauseButton.setBackgroundDrawable(null);
					
					buttonPanel.addView(pauseButton);
					
					frame.addView(bouncingBallView);
					frame.addView(buttonPanel);
				}
				
			});
			
			
			//long start = System.currentTimeMillis();
			//Log.d(TAG, "Initialising the game. Decoding resources...");
			spaceship = BitmapFactory.decodeResource(getResources(),
					R.drawable.alien);
			asteroid = BitmapFactory.decodeResource(getResources(),
					R.drawable.comet);
			background = BitmapFactory.decodeResource(getResources(),
					R.drawable.night_sky);
			
			Ball.setBitmap(spaceship);
			ArtificialIntelligence.setObstacleBitmap(asteroid);

			//Log.d(TAG, String.format("Finished decoding resources in %d msecs",
			//		(System.currentTimeMillis() - start)));
				
			return null;
		}
		

		protected void onProgressUpdate(Integer... values)
		{
			super.onProgressUpdate(values);
			
		}
	}

	
}