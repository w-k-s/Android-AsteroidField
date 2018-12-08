package com.google.app.AsteroidField;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

public class ArtificialIntelligence
{
	public static final int OBSTACLE_SPEED_FAST = 1;
	private Canvas		canvas;
	private int 		startX;
	private int 		gap;
	private PointF[]	defaultPositions;
	private Obstacle[]	obstacles;
	private int			obstacleFired =-1;
	private int			score;
	private int 		obstacleSpeed;
	private long		startTime;
	private long		firingPauseDuration;
	private boolean		obstacleAvoided;
/*
	private int			numFired;
	private int			random1;
	private int			random2;
	private boolean		cycleHasEnded;
	*/
	public ArtificialIntelligence(Canvas canvas, int startX)
	{
		this.canvas 			= canvas;
		this.startX 			= startX;
		this.score 				= 0;
		this.obstacleAvoided	= false;
		//this.numFired 		= 0;
		
		obstacles 				= new Obstacle[6];
		defaultPositions 		= new PointF[6];
		

		for(int i=0;i<obstacles.length;i++)
		{
			obstacles[i] = new Obstacle(canvas);
			defaultPositions[i] = new PointF();
		}
		setDefaultPositions();
		
	}
	
	public static void setObstacleBitmap(Bitmap bitmap)
	{
		Obstacle.setBitmap(bitmap);
	}
	
	private void setDefaultPositions()
	{
		int obstacleRadius 			= Obstacle.getRadius();
		int numberOfObstacles 		= obstacles.length;
		
		int totalGap 				=  (canvas.getWidth() - startX)-(obstacleRadius * numberOfObstacles);
		this.gap					= totalGap /numberOfObstacles;

		defaultPositions[0].set(startX , obstacleRadius*-1);
		
		for(int i=1; i<defaultPositions.length;i++)
		{
			float posX = defaultPositions[i-1].x+gap+obstacleRadius;
			float posY = obstacleIsAtBottom(i)? (obstacleRadius*-1 ):(canvas.getHeight()+obstacleRadius);
			defaultPositions[i].set(posX,posY);
		}

		for(int i=0;i<obstacles.length;i++)
		{
			obstacles[i].setObstaclePosition(defaultPositions[i]);
		}
	}

	public void updateArtificialIntelligence(Ball ball)
	{
		/*
		switch(numFired)
		{
		
		case 0:
			cycleHasEnded = false;
			random1 = 1+new Random().nextInt(obstacles.length - 1);
			random2 = new Random().nextInt(1)==0? (random1 - 1): (random1 + 1);
						
			if(!cycleHasEnded)
			{
				fire(random1, ball);
				numFired++;
				Log.e(this.toString(),String.format("NumFired = %d", numFired));
			}
			break;
			
		case 1:
			obstacles[random1].move(ball);	
			
			if(obstacles[random1].hasGoneOutOfView())
			{
				Log.e(this.toString(),String.format("obstacle %d has gone out of view", random1));
				obstacles[random1].setObstaclePosition(defaultPositions[random1]);
				
				numFired--;
				Log.e(this.toString(),String.format("NumFired = %d", numFired));
			}
			
			if(obstacles[random1].getObstaclePosition().x == ball.getX() - ball.getRadius() - Obstacle.getWidth())
			{
				if(!ball.DidCollide())
					score++;
				
				if(!cycleHasEnded)
				{
				fire(random2,ball);
				obstacles[random2].move(ball);
				numFired++;
				Log.e(this.toString(),String.format("NumFired = %d", numFired));
				}
				
			}

			break;
			
		case 2:
			obstacles[random1].move(ball);
			obstacles[random2].move(ball);
			
			if(obstacles[random1].hasGoneOutOfView())
			{
				obstacles[random1].setObstaclePosition(defaultPositions[random1]);
				
				numFired--;
				Log.e(this.toString(),String.format("NumFired = %d", numFired));
			}
			
			if(obstacles[random2].hasGoneOutOfView())
			{
				if(!ball.DidCollide())
					score++;
				obstacles[random2].setObstaclePosition(defaultPositions[random2]);
				
				numFired--;
				Log.e(this.toString(),String.format("NumFired = %d", numFired));
			}
			cycleHasEnded = true;
			break;
		}*/
		Random random = new Random();
		
		if(obstacleFired == -1)
		{
			int obstacleIndex = random.nextInt(obstacles.length);
			fire(obstacleIndex, ball);
			obstacleAvoided = false;
		}else
		{
			if(obstacles[obstacleFired].getX() < ball.getX())
			{
				if(!obstacleAvoided && !ball.DidCollide() )
				{
					score++;
					obstacleAvoided = true;
				}
			}
			if(obstacles[obstacleFired].hasGoneOutOfView())
			{
				//Put obstacle back
				obstacles[obstacleFired].setObstaclePosition(defaultPositions[obstacleFired]);
				obstacleFired = -1;
				
			}else
			{
				obstacles[obstacleFired].move(ball);
			}
		}
		
	}
	
	private void fire(int index, Ball ball)
	{
		if(!isFiringAllowed())
			return;
	
		float gradient = (ball.getY() - obstacles[index].getY()) / (ball.getX() - obstacles[index].getX());	
		
		float speed_x;
		if(obstacleSpeed == OBSTACLE_SPEED_FAST)
			speed_x = (float) (index==0?-2.75: -3.75);
		else
			speed_x = (float) (index==0?-1.55: -3);
			
		obstacles[index].setSpeedX(speed_x);
		obstacles[index].setSpeedY(gradient*speed_x); //y = mx + c => y = gradient * -3;
		obstacles[index].move(ball);
		obstacleFired = index;
	}

	public void setObstacleSpeed(int obstacleSpeed)
	{
		this.obstacleSpeed = obstacleSpeed;
	}
	
	public int getObstacleSpeed() {
		return obstacleSpeed;
	}
	
	private boolean obstacleIsAtBottom(int index)
	{
		//Even numbered obstacles are at the bottom.
		if(index%2==0)
			return true;
		else return false;
	}

	public void draw()
	{
		if(obstacleFired==-1)
			return;
					
		obstacles[obstacleFired].draw();
		
		/*
		 * This code was used in the beginning to test that the setObstaclePositions()
		 * function was spacing the obstacles correctly.
		 * 
		 * It is no longer needed.
		 * 
		for(Obstacle o: obstacles)
		{
			o.draw();
		}*/
	}
	
	public int getScore()
	{
		return this.score;
	}
	
	public void startFiringAfter(long milliseconds)
	{
		this.firingPauseDuration = milliseconds;
		this.startTime = System.currentTimeMillis();
	}
	
	private boolean isFiringAllowed()
	{ 
		return ((System.currentTimeMillis() - this.startTime) > firingPauseDuration);
	}
}
