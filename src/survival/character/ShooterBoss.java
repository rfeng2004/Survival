package survival.character;

import javafx.scene.paint.Color;

//implement shooting in network
public class ShooterBoss extends Enemy
{
	private final int reloadTime;
	private int currentReloadTime;
	
	public ShooterBoss(double x, double y, double s)
	{
		super(x, y, s+30);
		STEP = 2;
		r.setFill(Color.PURPLE);
		currentReloadTime = 0;
		reloadTime = 150;
	}
	
	public void shoot()
	{
		for(int i = 0; i < 8; i++)
		{
			Bullet b = new Bullet(x, y, SIZE/3, i);
			players.get(0).getParent().getEnemies().add(b);
			b.draw();
		}
	}
	
	public int move()
	{
		currentReloadTime++;
		if(currentReloadTime == reloadTime)
		{
			shoot();
			currentReloadTime = 0;
		}
		return super.move();
	}
}
