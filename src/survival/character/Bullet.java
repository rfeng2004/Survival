package survival.character;

//bullet shot from shooter boss
public class Bullet extends Enemy
{	
	public Bullet(double x, double y, double s, int dir)
	{
		super(x, y, s);
		changeTime = Integer.MAX_VALUE;
		STEP = 6; //bullets are fast;
		if(dir < 4)
		{
			dirs[dir] = true;
		}
		else if(dir < 6)
		{
			dirs[dir-4] = true;
			dirs[(dir-2)%4] = true;
		}
		else if(dir == 6)
		{
			dirs[0] = true;
			dirs[3] = true;
		}
		else
		{
			dirs[1] = true;
			dirs[2] = true;
		}
	}
	
	public int move()
	{
		if(x == players.get(0).getParent().getPrefWidth()-SIZE || x == 0) die();
		else if(y == players.get(0).getParent().getPrefHeight()-SIZE || y == 0) die();
		return super.move();
	}
}
