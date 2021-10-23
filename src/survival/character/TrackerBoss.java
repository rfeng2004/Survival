package survival.character;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class TrackerBoss extends Enemy
{
	private int targetIndex;
	
	public TrackerBoss(double x, double y, double s)
	{
		super(x, y, s+30);
		STEP = 1;
		targetIndex = 0;
		r.resize(SIZE, SIZE);
		r.setFill(Color.MAROON);
	}

	public int move()
	{
		track();
		for(int i = 0; i < players.size(); i++)
		{
			if(Shape.intersect(r, players.get(i).getShape()).getBoundsInLocal().getWidth() != -1)
			{
				return i;
			}
		}
		return -1;
	}

	public void track()
	{
		double px = players.get(targetIndex).getX();
		double py = players.get(targetIndex).getY();
		double d = Math.sqrt((px-x)*(px-x)+(py-y)*(py-y));
		if(players.get(targetIndex).isPowered())
		{
			x -= STEP/d*(px-x);
			y -= STEP/d*(py-y);
		}
		else
		{
			x += STEP/d*(px-x);
			y += STEP/d*(py-y);
		}
		x = Math.max(Math.min(players.get(0).getParent().getPrefWidth()-SIZE, x), 0);
		y = Math.max(Math.min(players.get(0).getParent().getPrefHeight()-SIZE, y), 0);
		refresh();
	}
}
