package survival.ui;

import java.util.ArrayList;
import javafx.scene.layout.Pane;
import survival.character.Enemy;

public abstract class SurvivalGamePane extends Pane
{
	public abstract ArrayList<Enemy> getEnemies();
	public abstract void startGame();
}
