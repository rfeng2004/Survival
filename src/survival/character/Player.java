package survival.character;

import javafx.scene.shape.*;
import survival.ui.*;

import javafx.scene.paint.Color;

public class Player
{
	private Circle c; //shape drawn on screen
	private Rectangle healthBar, currentHealth;
	private double x, y;
	public static final double SIZE = 50, ACCELERATION = 0.2, FRICTION = ACCELERATION/2, MAX_SPEED = 5;
	private double vx, vy;
	private final SurvivalGamePane parent;
	public static final double MAX_HEALTH = 100;
	private double hp;
	private int superPowerTime;
	private String host;

	private Color DEFAULT, POWERED, POWERHUE;

	public Player(double x, double y, SurvivalGamePane p)
	{
		this.x = x;
		this.y = y;
		parent = p;
		hp = MAX_HEALTH;
		superPowerTime = 0;
		vx = 0;
		vy = 0;
		c = new Circle(x, y, SIZE);
		DEFAULT = Color.MOCCASIN;
		POWERED = Color.GOLDENROD;
		POWERHUE = Color.DEEPSKYBLUE;
		c.setFill(DEFAULT);
		healthBar = new Rectangle(x-SIZE, y-8*SIZE/5, 2*SIZE, SIZE/5);
		healthBar.setFill(Color.RED);
		currentHealth = new Rectangle(x-SIZE, y-8*SIZE/5, 2*SIZE, SIZE/5);
		currentHealth.setFill(Color.GREEN);
	}

	public SurvivalGamePane getParent()
	{
		return parent;
	}

	public Circle getShape()
	{
		return c;
	}

	public void setLocation(double x, double y)
	{
		this.x = x;
		this.y = y;
		refresh();
	}

	public void setVelocity(double vx, double vy)
	{
		this.vx = vx;
		this.vy = vy;
	}

	public void setHP(double hp)
	{
		this.hp = hp;
	}

	public void setSuperPowerTime(int superPowerTime)
	{
		this.superPowerTime = superPowerTime;
	}

	public void setColorScheme(Color normal, Color powered, Color powerHue)
	{
		DEFAULT = normal;
		POWERED = powered;
		POWERHUE = powerHue;
		refresh();
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getVX()
	{
		return vx;
	}

	public double getVY()
	{
		return vy;
	}

	public double getHP()
	{
		return hp;
	}

	public int getSuperPowerTime()
	{
		return superPowerTime;
	}

	public boolean isPowered()
	{
		return superPowerTime > 0;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void reset()
	{
		hp = MAX_HEALTH;
		vx = 0;
		vy = 0;
		superPowerTime = 0;
		refresh();
	}

	/**
	 * Handles collisions with enemies (or powerups)
	 * @param e enemy collided
	 * @return if player dies through this collision
	 */
	public boolean collide(Enemy e)
	{
		if(e instanceof PowerUp)
		{
			if(e instanceof SuperPower)
			{
				superPowerTime = 500;
				setSuperPower();
			}
			else
			{
				hp = Math.min(MAX_HEALTH, hp+e.getSize());
			}
		}
		else
		{
			//maybe add boss one shot???
			if(superPowerTime == 0)
			{
				hp = Math.max(0, hp-2*e.getSize()/(2*Enemy.players.size()-1));
			}
		}
		refresh();
		return hp <= 0;
	}

	/**
	 * Handles collisions with other players.
	 * @param p player collided
	 * @return if player dies through this collision
	 */
	public boolean collide(Player p)
	{
		double dx = x-p.getX();
		double dy = y-p.getY();
		vx += 0.5*dx/Math.sqrt(dx*dx+dy*dy);
		vy += 0.5*dy/Math.sqrt(dx*dx+dy*dy);
		double myv = Math.sqrt(vx*vx+vy*vy);
		double pv = Math.sqrt(p.getVX()*p.getVX()+p.getVY()*p.getVY());
		if(superPowerTime == 0 && !p.isPowered())
		{
			hp = Math.max(hp-pv, 0);
		}
		else if(superPowerTime == 0 && p.isPowered())
		{
			hp = Math.max(hp-myv-pv, 0);
		}
		else if(superPowerTime > 0 && p.isPowered())
		{
			hp = Math.max(hp-pv, 0);
		}
		return hp <= 0;
	}

	public double damage(Player p)
	{
		double damage;
		double myv = Math.sqrt(vx*vx+vy*vy)/5;
		double pv = Math.sqrt(p.getVX()*p.getVX()+p.getVY()*p.getVY())/5;
		if(superPowerTime == 0 && !p.isPowered())
		{
			damage = myv;
		}
		else if(superPowerTime == 0 && p.isPowered())
		{
			damage = 0;
		}
		else if(superPowerTime > 0 && !p.isPowered())
		{
			damage = myv+pv;
		}
		else
		{
			damage = myv;
		}
		return damage;
	}

	public void draw()
	{
		parent.getChildren().addAll(c, healthBar, currentHealth);
		c.toBack();
		currentHealth.toBack();
		healthBar.toBack();
	}

	public void delete()
	{
		parent.getChildren().removeAll(c, healthBar, currentHealth);
	}

	public void decreaseSuperPowerTime()
	{
		superPowerTime = Math.max(superPowerTime-1, 0);
		if(superPowerTime > 0) setSuperPower();
	}

	private void refresh()
	{
		c.setCenterX(x);
		c.setCenterY(y);
		healthBar.setX(x-SIZE);
		healthBar.setY(y-8*SIZE/5);
		currentHealth.setX(x-SIZE);
		currentHealth.setY(y-8*SIZE/5);
		currentHealth.setWidth(2*SIZE*hp/MAX_HEALTH);
		if(superPowerTime == 0) setNormal();
		else setSuperPower();
	}

	private void setSuperPower()
	{
		if(superPowerTime > 100)
		{
			c.setFill(POWERED);
			c.setStroke(POWERHUE);
			c.setStrokeWidth(5);
		}
		else if(superPowerTime > 50)
		{
			if(superPowerTime % 10 < 5) setNormal();
			else
			{
				c.setFill(POWERED);
				c.setStroke(POWERHUE);
				c.setStrokeWidth(5);
			}
		}
		else
		{
			if(superPowerTime % 4 < 2) setNormal();
			else
			{
				c.setFill(POWERED);
				c.setStroke(POWERHUE);
				c.setStrokeWidth(5);
			}
		}
	}

	private void setNormal()
	{
		c.setFill(DEFAULT);
		c.setStroke(null);
		c.setStrokeWidth(0);
	}

	public void move()
	{
		if(x+vx < parent.getPrefWidth()-SIZE && x+vx > SIZE) x += vx;
		else
		{
			if(x+vx >= parent.getPrefWidth()-SIZE) x = parent.getPrefWidth()-SIZE;
			else x = SIZE;
			vx = -vx;
		}

		if(y+vy < parent.getPrefHeight()-SIZE && y+vy > SIZE) y += vy;
		else
		{
			if(y+vy >= parent.getPrefHeight()-SIZE) y = parent.getPrefHeight()-SIZE;
			else y = SIZE;
			vy = -vy;
		}

		if(vx > 0)
		{
			vx = Math.max(0, vx - vx/Math.sqrt(vx*vx+vy*vy)*FRICTION);
		}
		else if(vx < 0) vx = Math.min(0, vx - vx/Math.sqrt(vx*vx+vy*vy)*FRICTION);

		if(vy > 0)
		{
			vy = Math.max(0, vy - vy/Math.sqrt(vx*vx+vy*vy)*FRICTION);
		}
		else if(vy < 0) vy = Math.min(0, vy - vy/Math.sqrt(vx*vx+vy*vy)*FRICTION);
		refresh();
	}

	public void accelerateRight(double multiplier)
	{
		if((vx+ACCELERATION)*(vx+ACCELERATION)+vy*vy < MAX_SPEED*MAX_SPEED) vx += ACCELERATION;
	}

	public void accelerateLeft(double multiplier)
	{
		if((vx-ACCELERATION)*(vx-ACCELERATION)+vy*vy < MAX_SPEED*MAX_SPEED) vx -= ACCELERATION;
	}

	public void accelerateUp(double multiplier)
	{
		if(vx*vx+(vy-ACCELERATION)*(vy-ACCELERATION) < MAX_SPEED*MAX_SPEED) vy -= ACCELERATION;
	}

	public void accelerateDown(double multiplier)
	{
		if(vx*vx+(vy+ACCELERATION)*(vy+ACCELERATION) < MAX_SPEED*MAX_SPEED) vy += ACCELERATION;
	}
}
