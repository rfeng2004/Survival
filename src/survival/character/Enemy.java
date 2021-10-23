package survival.character;

import java.util.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class Enemy
{
	protected Rectangle r; //shape drawn on screen
	protected double x, y;
	protected double SIZE;
	protected double STEP;
	public static ArrayList<Player> players = new ArrayList<Player>();
	protected int changeTime;
	protected int curTime;
	protected boolean[] dirs;
	protected boolean dead;
	private String ID;
	
	public Enemy(double x, double y, double s)
	{
		this.x = x;
		this.y = y;
		this.SIZE = s;
		STEP = 4; //default step size
		dead = false;
		dirs = new boolean[4];
		changeTime = (int)(Math.random()*40)+1;
		curTime = 0;
		r = new Rectangle(SIZE, SIZE);
		refresh();
		r.setFill(Color.RED);
		ID = ""+Math.random();
	}
	
	public void draw()
	{
		players.get(0).getParent().getChildren().add(r);
	}
	
	public void setLocation(double x, double y)
	{
		this.x = x;
		this.y = y;
		refresh();
	}
	
	public void setID(String id)
	{
		ID = id;
	}
	
	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}
	
	public Rectangle getShape()
	{
		return r;
	}
	
	public String getID()
	{
		return ID;
	}
	
	private void setDirs()
	{
		Random r = new Random();
		for(int i = 0; i < 4; i++)
		{
			dirs[i] = r.nextBoolean();
		}
	}
	
	public void die()
	{
		dead = true;
		players.get(0).getParent().getChildren().remove(r);
	}
	
	public boolean isDead()
	{
		return dead;
	}
	
	public double getSize()
	{
		return SIZE;
	}
	
	public int move()
	{
		curTime++;
		if(curTime == changeTime)
		{
			setDirs();
			curTime = 0;
		}
		int count = 0;
		for(int i = 0; i < 4; i++)
		{
			if(dirs[i]) count++;
		}
		double multiplier;
		if(count == 2) multiplier = Math.sqrt(2)/2;
		else multiplier = 1;
		if(dirs[0])
		{
			moveRight(multiplier);
		}

		if(dirs[1])
		{
			moveLeft(multiplier);
		}

		if(dirs[2])
		{
			moveUp(multiplier);
		}

		if(dirs[3])
		{
			moveDown(multiplier);
		}
		for(int i = 0; i < players.size(); i++)
		{
			if(Shape.intersect(r, players.get(i).getShape()).getBoundsInLocal().getWidth() != -1)
			{
				return i;
			}
		}
		return -1;
	}
	
	protected void refresh()
	{
		r.setX(x);
		r.setY(y);
	}
	
	private void moveRight(double multiplier)
	{
		x = Math.min(x+multiplier*STEP, players.get(0).getParent().getPrefWidth()-SIZE);
		refresh();
	}
	
	private void moveLeft(double multiplier)
	{
		x = Math.max(0, x-multiplier*STEP);
		refresh();
	}
	
	private void moveUp(double multiplier)
	{
		y = Math.max(0, y-multiplier*STEP);
		refresh();
	}
	
	private void moveDown(double multiplier)
	{
		y = Math.min(y+multiplier*STEP, players.get(0).getParent().getPrefHeight()-SIZE);
		refresh();
	}
}
