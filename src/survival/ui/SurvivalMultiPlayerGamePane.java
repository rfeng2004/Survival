package survival.ui;

import java.net.*;
import java.text.DecimalFormat;
import java.util.*;

import javafx.animation.AnimationTimer;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import networking.frontend.*;
import survival.character.*;

public class SurvivalMultiPlayerGamePane extends SurvivalGamePane implements NetworkListener
{
	private Player me;
	private boolean[] pressed = new boolean[4]; //0 = right, 1 = left, 2 = up, 3 = down
	private static final KeyCode[] dir = {KeyCode.RIGHT, KeyCode.LEFT, KeyCode.UP, KeyCode.DOWN};
	private static final KeyCode[] daws = {KeyCode.D, KeyCode.A, KeyCode.W, KeyCode.S};
	private int spawnTime, curTime;
	private Label scoreLabel;
	private double score;
	private Button networkManagement, beginGame;
	private AnimationTimer playerMovement, enemySpawner, network;

	private static final int MIN_SPAWN_DIST = 40000;
	private static final double ENEMY_SPAWN_CHANCE = 0.75;
	private static final double TRACKERBOSS_SPAWN_CHANCE = ENEMY_SPAWN_CHANCE/20;
	private static final double SHOOTERBOSS_SPAWN_CHANCE = ENEMY_SPAWN_CHANCE/50;
	private static final double POWERUP_SPAWN_CHANCE = 1-ENEMY_SPAWN_CHANCE;
	private static final double SUPERPOWER_SPAWN_CHANCE = POWERUP_SPAWN_CHANCE/3;
	private static final DecimalFormat FORMATTER = new DecimalFormat("#0.00");

	private ArrayList<Player> players;
	private ArrayList<Enemy> enemies;
	private static final String BEGIN_GAME = "BEGIN_GAME";
	private static final String INIT = "INIT";
	private static final String PLAYER_STATUS = "PLAYER_STATUS";
	private static final String PLAYER_DEATH = "PLAYER_DEATH";
	private static final String ENEMY_SPAWN = "ENEMY_SPAWN";
	private static final String ENEMY_MOVE = "ENEMY_MOVE";
	private static final String ENEMY_DEATH = "ENEMY_DEATH";
	private NetworkMessenger nm;
	private NetworkManagementPanel nmp;
	
	private boolean gameInProgress;


	public SurvivalMultiPlayerGamePane()
	{
		this(1000, 700);
	}

	public SurvivalMultiPlayerGamePane(int w, int h)
	{
		setPrefSize(w, h);
		nmp = new NetworkManagementPanel("SurvivalMultiplayer", 4, this);
		nmp.show();
		me = new Player(Math.random()*(w-2*Player.SIZE)+Player.SIZE, Math.random()*(h-2*Player.SIZE)+Player.SIZE, this);
		try
		{
			me.setHost(InetAddress.getLocalHost().toString());
		}
		catch (UnknownHostException e)
		{
			System.out.println("Error retrieving local host.");
			System.exit(0);
		}
		me.draw();
		players = new ArrayList<Player>();
		players.add(me);
		Enemy.players.add(me);
		enemies = new ArrayList<Enemy>();
		scoreLabel = new Label("Score: 0");
		scoreLabel.setPrefSize(150, 50);
		scoreLabel.setLayoutX(25);
		scoreLabel.setLayoutY(0);
		scoreLabel.setFocusTraversable(true);
		scoreLabel.setStyle("-fx-font: 20px Ariel");
		scoreLabel.requestFocus();
		networkManagement = new Button("Network");
		networkManagement.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				if(!nmp.isShowing())
				{
					nmp.show();
				}
				nmp.bringToFront();
			}
		});
		networkManagement.setStyle("-fx-font: 20px Ariel");
		networkManagement.setPrefSize(150, 30);
		networkManagement.setLayoutX(175);
		networkManagement.setLayoutY(5);
		networkManagement.setFocusTraversable(false);
		beginGame = new Button("Begin Game");
		beginGame.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				if(players.size() > 1)
				{
					enemySpawner.start();
					getChildren().remove(beginGame);
					nm.sendMessage(NetworkDataObject.MESSAGE, BEGIN_GAME);
					gameInProgress = true;
				}
			}
		});
		beginGame.setStyle("-fx-font: 20px Ariel");
		beginGame.setPrefSize(150, 30);
		beginGame.setLayoutX(350);
		beginGame.setLayoutY(5);
		beginGame.setFocusTraversable(false);
		getChildren().addAll(scoreLabel, networkManagement, beginGame);

		spawnTime = 100;
		curTime = 0;
		score = 0;
		gameInProgress = false;

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
				if(nm != null)
				{
					me.setColorScheme(Color.PALEGOLDENROD, Color.DARKGOLDENROD, Color.DODGERBLUE);
				}
				else
				{
					me.setColorScheme(Color.MOCCASIN, Color.GOLDENROD, Color.DEEPSKYBLUE);
				}
				movePlayer(me, pressed);
				for(Player p : players)
				{
					p.decreaseSuperPowerTime();
					if(p != me && Shape.intersect(p.getShape(), me.getShape()).getBoundsInLocal().getWidth() != -1)
					{
						//handle player collision
						if(me.collide(p))
						{
							me.delete();
							players.remove(me);
							Enemy.players.remove(me);
							nm.sendMessage(NetworkDataObject.MESSAGE, PLAYER_DEATH);
							return;
						}
						score += me.damage(p);
						scoreLabel.setText("Score: " + FORMATTER.format(score));
					}
				}
				if(nm != null) nm.sendMessage(NetworkDataObject.MESSAGE, PLAYER_STATUS, 
						me.getX(), me.getY(), me.getVX(), me.getVY(), me.getHP(), me.getSuperPowerTime());
			}
		};
		//do enemy messages by id, generate a random id for each enemy
		enemySpawner = new AnimationTimer()
		{
			public void handle(long now)
			{
				if(nmp.isHostingServer())
				{
					curTime++;
					if(curTime == spawnTime)
					{
						curTime = 0;
						double size = Math.random()*30+20;
						double x, y;
						ArrayList<Double> cx = new ArrayList<Double>();
						ArrayList<Double> cy = new ArrayList<Double>();
						for(Player p : players)
						{
							cx.add(p.getX());
							cy.add(p.getY());
						}
						do
						{
							x = Math.random()*(1000-2*size)+size;
							y = Math.random()*(700-2*size)+size;
						} while(!isValidSpawn(x, y));

						double a = Math.random();
						spawnEnemy(x, y, size, a);
						nm.sendMessage(NetworkDataObject.MESSAGE, ENEMY_SPAWN, x, y, size, a, enemies.get(enemies.size()-1).getID());
					}

					for(int i = 0; i < enemies.size(); i++)
					{
						Enemy e = enemies.get(i);
						e.move();
						nm.sendMessage(NetworkDataObject.MESSAGE, ENEMY_MOVE, e.getID(), e.getX(), e.getY());
					}
				}
				for(int i = 0; i < enemies.size(); i++)
				{
					for(int j = 0; j < players.size(); j++)
					{
						if(i == -1 || j == -1) break;
						Enemy e = enemies.get(i);
						Player p = players.get(j);
						if(Shape.intersect(e.getShape(), p.getShape()).getBoundsInLocal().getWidth() != -1)
						{
							if(p.collide(e))
							{
								p.delete();
								players.remove(j);
								Enemy.players.remove(j);
								if(p == me)
								{
									nm.sendMessage(NetworkDataObject.MESSAGE, PLAYER_DEATH);
									return;
								}
								j--;
							}
							e.die();
							enemies.remove(i);
							nm.sendMessage(NetworkDataObject.MESSAGE, ENEMY_DEATH, e.getID());
							i--;
						}
					}
				}
				if(players.size() <= 1)
				{
					for(Enemy e : enemies) e.die();
					enemies.clear();
					enemySpawner.stop();
					getChildren().add(beginGame);
					gameInProgress = false;
					me.setHP(Player.MAX_HEALTH);
				}
			}
		};

		network = new AnimationTimer()
		{
			public void handle(long now)
			{
				processNetworkMessages();
				if(nmp.isHostingServer() && !getChildren().contains(beginGame) && !gameInProgress)
				{
					getChildren().add(beginGame);
				}
			}
		};
	}

	public void processNetworkMessages()
	{
		if (nm == null)
			return;

		Queue<NetworkDataObject> queue = nm.getQueuedMessages();

		while (!queue.isEmpty())
		{
			NetworkDataObject ndo = queue.poll();

			String host = ndo.getSourceIP();

			if(ndo.messageType.equals(NetworkDataObject.MESSAGE))
			{
				if(ndo.message[0].equals(PLAYER_STATUS))
				{
					for(Player p : players)
					{
						if(p.getHost().equals(host) && p != me)
						{
							double x = (Double) ndo.message[1];
							double y = (Double) ndo.message[2];
							double vx = (Double) ndo.message[3];
							double vy = (Double) ndo.message[4];
							double hp = (Double) ndo.message[5];
							int superPowerTime = (Integer) ndo.message[6];
							p.setLocation(x, y);
							p.setVelocity(vx, vy);
							p.setHP(hp);
							p.setSuperPowerTime(superPowerTime);
						}
					}
				}
				else if(ndo.message[0].equals(PLAYER_DEATH))
				{
					if(me.getHost().equals(host)) continue;
					for (int i = 0; i < players.size(); i++)
					{
						if (players.get(i).getHost().equals(host))
						{
							players.get(i).delete();
							players.remove(i);
							Enemy.players.remove(i);
							i--;
						}
					}
				}
				else if (ndo.message[0].equals(INIT))
				{
					for (Player p : players) 
					{
						if (p.getHost().equals(host))
							continue;
					}
					double x = (Double) ndo.message[1];
					double y = (Double) ndo.message[2];
					Player p = new Player(x, y, this);
					p.setHost(host);
					players.add(p);
					Enemy.players.add(p);
					p.draw();
				}
				else if(ndo.message[0].equals(ENEMY_SPAWN))
				{
					if(!nmp.isHostingServer())
					{
						double x = (Double) ndo.message[1];
						double y = (Double) ndo.message[2];
						double size = (Double) ndo.message[3];
						double a = (Double) ndo.message[4];
						String id = (String) ndo.message[5];
						spawnEnemy(x, y, size, a);
						enemies.get(enemies.size()-1).setID(id);
					}
				}
				else if(ndo.message[0].equals(ENEMY_MOVE))
				{
					if(!nmp.isHostingServer())
					{
						String id = (String) ndo.message[1];
						double x = (Double) ndo.message[2];
						double y = (Double) ndo.message[3];
						for(Enemy e : enemies)
						{
							if(e.getID().equals(id))
							{
								e.setLocation(x, y);
							}
						}
					}
				}
				else if(ndo.message[0].equals(ENEMY_DEATH))
				{
					String id = (String) ndo.message[1];
					for(Enemy e : enemies)
					{
						if(e.getID().equals(id))
						{
							e.die();
							enemies.remove(e);
							break;
						}
					}
				}
				else if(ndo.message[0].equals(BEGIN_GAME))
				{
					if(!nmp.isHostingServer())
					{
						enemySpawner.start();
						gameInProgress = true;
					}
				}
			}
			else if(ndo.messageType.equals(NetworkDataObject.CLIENT_LIST))
			{
				nm.sendMessage(NetworkDataObject.MESSAGE, INIT, me.getX(), me.getY());
			}
			else if(ndo.messageType.equals(NetworkDataObject.DISCONNECT))
			{
				if (ndo.dataSource.equals(ndo.serverHost))
				{
					for(Player p : players) p.delete();
					players.clear();
					Enemy.players.clear();
					players.add(me);
					Enemy.players.add(me);
					me.draw();
				} 
				else
				{
					for (int i = 0; i < players.size(); i++)
					{
						if (players.get(i).getHost().equals(host))
						{
							players.get(i).delete();
							players.remove(i);
							Enemy.players.remove(i);
							i--;
						}
					}
				}
			}
		}
	}

	@Override
	public void connectedToServer(NetworkMessenger nm)
	{
		this.nm = nm;
	}

	@Override
	public void networkMessageReceived(NetworkDataObject ndo)
	{

	}

	@Override
	public ArrayList<Enemy> getEnemies()
	{
		return enemies;
	}

	public void movePlayer(Player p, boolean[] dirs)
	{
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
			p.accelerateRight(multiplier);
		}

		if(dirs[1])
		{
			p.accelerateLeft(multiplier);
		}

		if(dirs[2])
		{
			p.accelerateUp(multiplier);
		}

		if(dirs[3])
		{
			p.accelerateDown(multiplier);
		}
		p.move();
	}

	public void startGame()
	{
		playerMovement.start();
		network.start();
	}

	private boolean isValidSpawn(double x, double y)
	{
		for(Player p : players)
		{
			if((x-p.getX())*(x-p.getX())+(y-p.getY())*(y-p.getY()) < MIN_SPAWN_DIST) return false;
		}
		return true;
	}

	private void spawnEnemy(double x, double y, double size, double a)
	{
		Enemy spawned;
		if(a < ENEMY_SPAWN_CHANCE)
		{
			if(a < SHOOTERBOSS_SPAWN_CHANCE)
			{
				spawned = new ShooterBoss(x, y, size);
			}
			else if(a < SHOOTERBOSS_SPAWN_CHANCE+TRACKERBOSS_SPAWN_CHANCE)
			{
				spawned = new TrackerBoss(x, y, size);
			}
			else
			{
				spawned = new Enemy(x, y, size);
			}
		}
		else
		{
			if(1-a > SUPERPOWER_SPAWN_CHANCE)
			{
				spawned = new PowerUp(x, y, size);
			}
			else
			{
				spawned = new SuperPower(x, y, size);
			}
		}
		spawned.draw();
		enemies.add(spawned);
	}
}
