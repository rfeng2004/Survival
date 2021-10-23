package survival.character;

import javafx.scene.paint.Color;

public class PowerUp extends Enemy
{

	public PowerUp(double x, double y, double s)
	{
		super(x, y, s);
		r.setFill(Color.GREEN);
		STEP = 2; //power ups move slower
	}
}
