package com.google.app.AsteroidField;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Ball
{

	//@WAQQAS SHEIKH: THESE VALUES ARE FINE! DO NOT CHANGE THEM.
	private final float NORMAL_SPEED_Y = 3;
	private final float MAXIMUM_SPEED_Y = -3;
	private final float SPEED_INCREMENT = (float) 0.5;
	//private final float ACCELERATION = 10; 
	private final float SPEED_X = 0;

	
	//@WAQQAS: YOU MAY ADJUST THIS VALUE SLIGHTLY. 
	//private final float TIME = (float) 0.01;
	
	private static float radius = 35;
	private float x;
	private float y;
	private float speedY = NORMAL_SPEED_Y;
	private Paint paint;
	private Canvas canvas;
	
	private static Bitmap bitmap;
	
	private boolean collided = false;
	private boolean touched = false;
	
	public Ball(Canvas canvas, int color)
	{
		this.canvas = canvas;
		
		paint = new Paint();
		paint.setColor(color);
		collided = false;
		
		x = canvas.getWidth()/4 - radius/2;
		y = canvas.getHeight()/2 - radius/2;
	
	}
	
	public static void setBitmap(Bitmap b) {
		bitmap = b;
		radius = bitmap.getWidth()/2;
	}
	
	public void move()
	{

		
		x += SPEED_X;
		y += speedY;
		
		if ( y < radius)
		{
			speedY *= -1; //commented out so that the plane doesn't boucne
			y = radius;
		}
		
		if(y + radius > canvas.getHeight())
		{
			speedY *= -1;  //commented out so that the plane doesn't bounce
			y = canvas.getHeight() - radius;
		}else
		{
			if(touched)
			{
				if(speedY > MAXIMUM_SPEED_Y)
					speedY -= SPEED_INCREMENT;
			}
			else
			{
				if(speedY < NORMAL_SPEED_Y)
						speedY += SPEED_INCREMENT;
			}
		}
		
		if(x > canvas.getWidth()-radius)
			x = radius;
		
	}
	
	public void draw()
	{
		//ballCoordinates.set(x-radius,y-radius, x+radius, y+radius);
		//canvas.drawOval(ballCoordinates, paint);
		canvas.drawBitmap(bitmap, x-radius, y-radius, null);
	}
	
	public void setTouched(boolean touched) {
		this.touched = touched;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getRadius() {
		return radius;
	}
	
	public void setSpeedY(float speedY) {
		this.speedY = speedY;
	}
	
	public float getSpeedY() {
		return speedY;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}

	public void hasCollided(boolean collided)
	{
		this.collided = true;
	}
	
	
	public boolean DidCollide()
	{
		return this.collided;
	}
}
