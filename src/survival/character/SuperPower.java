package survival.character;

import javafx.scene.paint.Color;

public class SuperPower extends PowerUp
{
	
	public SuperPower(double x, double y, double s)
	{
		super(x, y, s);
		r.setFill(Color.GOLD);
		STEP = 6; //super power is harder to catch
	}
}
