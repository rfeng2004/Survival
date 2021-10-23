package survival.ui;
/*
 * Boss ideas: slow tracker, small bullet shooter (done)
 * Scoring system: should be based on enemy size and bonuses for bosses/enemies killed.
 * Multiplayer ideas:
 * 1v1 or battle royale:
 * - when hitting each other both take some damage
 * - powered up players take less damage from other players
 * Team:
 * - totems that you can control, they slide around and kill opponents
 */

import java.text.DecimalFormat;
import java.util.*;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.animation.*;
import survival.character.*;

public class SurvivalSinglePlayerGamePane extends SurvivalGamePane
{
	private Player player;
	private boolean[] pressed = new boolean[4]; //0 = right, 1 = left, 2 = up, 3 = down
	private static final KeyCode[] dir = {KeyCode.RIGHT, KeyCode.LEFT, KeyCode.UP, KeyCode.DOWN};
	private static final KeyCode[] daws = {KeyCode.D, KeyCode.A, KeyCode.W, KeyCode.S};
	private ArrayList<Enemy> enemies;
	private int spawnTime, curTime;
	private double score;
	private Label scoreLabel;
	private Button restart;
	private AnimationTimer playerMovement, enemySpawner;
	private static final int MIN_SPAWN_DIST = 40000;
	private static final double ENEMY_SPAWN_CHANCE = 0.75;
	private static final double TRACKERBOSS_SPAWN_CHANCE = ENEMY_SPAWN_CHANCE/20;
	private static final double SHOOTERBOSS_SPAWN_CHANCE = ENEMY_SPAWN_CHANCE/50;
	private static final double POWERUP_SPAWN_CHANCE = 1-ENEMY_SPAWN_CHANCE;
	private static final double SUPERPOWER_SPAWN_CHANCE = POWERUP_SPAWN_CHANCE/3;
	private static final DecimalFormat FORMATTER = new DecimalFormat("#0.00");

	public SurvivalSinglePlayerGamePane()
	{
		this(1000, 700);
	}

	public SurvivalSinglePlayerGamePane(int w, int h)
	{
		setPrefSize(w, h);
		player = new Player(Math.random()*(w-2*Player.SIZE)+Player.SIZE, Math.random()*(h-2*Player.SIZE)+Player.SIZE, this);
		player.draw();
		Enemy.players.add(player);
		enemies = new ArrayList<Enemy>();
		scoreLabel = new Label("Score: 0");
		scoreLabel.setPrefSize(150, 50);
		scoreLabel.setLayoutX(25);
		scoreLabel.setLayoutY(0);
		scoreLabel.setFocusTraversable(true);
		scoreLabel.setStyle("-fx-font: 20px Ariel");
		getChildren().add(scoreLabel);
		scoreLabel.requestFocus();
		restart = new Button("Restart");
		restart.setStyle("-fx-font: 20px Ariel");
		restart.setPrefSize(125, 30);
		restart.setLayoutX(175);
		restart.setLayoutY(5);
		restart.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				for(Enemy e : enemies) e.die();
				enemies.clear();
				scoreLabel.setText("Score: 0");
				getChildren().remove(restart);
				playerMovement.start();
				enemySpawner.start();
				player.reset();
				curTime = 0;
				score = 0;
			}
		});
		spawnTime = 100;
		curTime = 0;
		score = 0;

		this.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			public void handle(KeyEvent event)
			{
				KeyCode k = event.getCode();
				for(int i = 0; i < 4; i++)
				{
					if(k == dir[i] || k == daws[i]) pressed[i] = true;
				}
			}
		});
		this.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			public void handle(KeyEvent event)
			{
				KeyCode k = event.getCode();
				for(int i = 0; i < 4; i++)
				{
					if(k == dir[i] || k == daws[i]) pressed[i] = false;
				}
			}
		});

		playerMovement = new AnimationTimer()
		{
			public void handle(long now)
			{
				int count = 0;
				for(int i = 0; i < 4; i++)
				{
					if(pressed[i]) count++;
				}
				double multiplier;
				if(count == 2) multiplier = Math.sqrt(2)/2;
				else multiplier = 1;
				if(pressed[0])
				{
					player.accelerateRight(multiplier);
				}

				if(pressed[1])
				{
					player.accelerateLeft(multiplier);
				}

				if(pressed[2])
				{
					player.accelerateUp(multiplier);
				}

				if(pressed[3])
				{
					player.accelerateDown(multiplier);
				}
				player.move();
				player.decreaseSuperPowerTime();
			}
		};

		enemySpawner = new AnimationTimer()
		{
			public void handle(long now)
			{
				curTime++;
				if(curTime == spawnTime)
				{
					curTime = 0;
					double size = Math.random()*30+20;
					double x, y;
					double cx = player.getX();
					double cy = player.getY();
					do
					{
						x = Math.random()*(1000-2*size)+size;
						y = Math.random()*(700-2*size)+size;
					} while((x-cx)*(x-cx)+(y-cy)*(y-cy)<MIN_SPAWN_DIST);

					double a = Math.random();
					if(a < ENEMY_SPAWN_CHANCE)
					{
						if(a < SHOOTERBOSS_SPAWN_CHANCE)
						{
							ShooterBoss b = new ShooterBoss(x, y, size);
							b.draw();
							enemies.add(b);
							score+=size+30;
							scoreLabel.setText("Score: " + FORMATTER.format(score));
						}
						else if(a < SHOOTERBOSS_SPAWN_CHANCE+TRACKERBOSS_SPAWN_CHANCE)
						{
							TrackerBoss b = new TrackerBoss(x, y, size);
							b.draw();
							enemies.add(b);
							score+=size+30;
							scoreLabel.setText("Score: " + FORMATTER.format(score));
						}
						else
						{
							Enemy e = new Enemy(x, y, size);
							e.draw();
							enemies.add(e);
							score+=size;
							scoreLabel.setText("Score: " + FORMATTER.format(score));
						}
					}
					else
					{
						if(1-a > SUPERPOWER_SPAWN_CHANCE)
						{
							PowerUp p = new PowerUp(x, y, size);
							p.draw();
							enemies.add(p);
						}
						else
						{
							SuperPower sp = new SuperPower(x, y, size);
							sp.draw();
							enemies.add(sp);
						}
					}
				}
				for(int i = 0; i < enemies.size(); i++)
				{
					Enemy enemy = enemies.get(i);
					if(enemy.move() != -1)
					{
						if(player.collide(enemy))
						{
							playerMovement.stop();
							stop();
							getChildren().add(restart);
							break;
						}
						enemy.die();
						if(!(enemy instanceof PowerUp))
						{
							score+=enemy.getSize();
							scoreLabel.setText("Score: " + FORMATTER.format(score));
						}
					}
				}
				for(int i = 0; i < enemies.size(); i++)
				{
					if(enemies.get(i).isDead())
					{
						enemies.remove(i);
						i--;
					}
				}
			}
		};
	}

	public ArrayList<Enemy> getEnemies()
	{
		return enemies;
	}
	
	public void startGame()
	{
		playerMovement.start();
		enemySpawner.start();
	}
}