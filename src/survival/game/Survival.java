package survival.game;

import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.stage.*;
import survival.ui.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class Survival extends Application
{
	public static void main(String[] args)
	{
		launch(args);
	}

	public void start(Stage primaryStage)
	{
		Pane rootNode = new Pane();
		Scene home = new Scene(rootNode, 1000, 700);
		Button sp = new Button("Play Single Player");
		Button mp = new Button("Play Multiplayer");
		Label welcome = new Label("Welcome to Survival!");
		sp.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				SurvivalSinglePlayerGamePane singlePlayer = new SurvivalSinglePlayerGamePane();
				Scene singlePlayerGame = new Scene(singlePlayer, 1000, 700);
				primaryStage.setScene(singlePlayerGame);
				singlePlayer.startGame();
			}
		});
		
		mp.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				SurvivalMultiPlayerGamePane multiPlayer = new SurvivalMultiPlayerGamePane();
				Scene multiPlayerGame = new Scene(multiPlayer, 1000, 700);
				primaryStage.setScene(multiPlayerGame);
				multiPlayer.startGame();
			}
		});
		sp.setPrefSize(300, 100);
		mp.setPrefSize(300, 100);
		sp.setStyle("-fx-font: 30px Ariel");
		mp.setStyle("-fx-font: 30px Ariel");
		sp.setLayoutX(133);
		sp.setLayoutY(400);
		mp.setLayoutX(567);
		mp.setLayoutY(400);
		welcome.setPrefSize(800, 200);
		welcome.setStyle("-fx-font: 70px Ariel");
		welcome.setLayoutX(100);
		welcome.setLayoutY(100);
		welcome.setAlignment(Pos.CENTER);
		rootNode.getChildren().addAll(sp, mp, welcome);

		primaryStage.setTitle("Survival");
		primaryStage.setScene(home);
		primaryStage.setResizable(false);
		primaryStage.show();
	}
	
	public void stop()
	{
		System.exit(0);
	}
}
