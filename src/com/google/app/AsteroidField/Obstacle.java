package com.google.app.AsteroidField;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

public class Obstacle
{
	//private final float ACCELERATION = 10; 
	//private final float TIME = (float) 0.01;
	
	private Canvas canvas;
	private static Bitmap bitmap;
	
	private static int radius = 35;
	private int x = 500;
	private int y = -20;
	private float speedX = 0;
	private float speedY = 0;
	
	private boolean outOfView = false;
	
	public Obstacle(Canvas canvas)
	{
		this.canvas = canvas;
	}
	
	public void move(Ball ball)
	{

		x += speedX;
		y += speedY;
				
		updateOutOfView();
		detectCollisions(ball);
		//COLLISIONS
			
	}
	
	private void updateOutOfView()
	{
		outOfView = x<-radius?true:false;		
	}

	private void detectCollisions(Ball ball)
	{
		float a = ball.getX() - x;
		float b = ball.getY() - y;
		float c = ball.getRadius() + Obstacle.radius;
		
		a *= a;
		b *= b;
		c *= c;
		
		if(a + b <= c)
			ball.hasCollided(true);
	}
	
	public void draw()
	{
		canvas.drawBitmap(bitmap, x-radius, y-radius, null);
	}
	
	public void setObstaclePosition(PointF obstaclePosition) {
		this.x = (int) obstaclePosition.x;
		this.y = (int) obstaclePosition.y;
	}
	
	public static int getRadius() {
		return radius;
	}

	public boolean hasGoneOutOfView()
	{
		return outOfView;
	}
	
	public void setSpeedX(float speedX) {
		this.speedX = speedX;
	}
	
	public void setSpeedY(float speedY) {
		this.speedY = speedY;
	}
	
	public float getSpeedX() {
		return speedX;
	}
	
	public float getSpeedY() {
		return speedY;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public static void setBitmap(Bitmap b)
	{
		bitmap = b;
		radius = bitmap.getWidth()/2;
	}
}
