import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Random;

public class Main extends Application {

	Tile[][] tiles = new Tile[19][19];
	static Token[][] tokens = new Token[2][4];
	int[] possibleMoves = {6, 12, 4, 3, 2, 25, 8};
	int[] repeatableMovesIndices = {0, 1, 5, 6};
	static Text[] movesRemaining = new Text[7];
	static int numberOfRemainingMoves = 0;
	int selected;
	static boolean canThrowDice = true;
	boolean hasMovedThisTurn = false;
	Text turnText = new Text("");
	Text rollAgainText = new Text("");

	static int currentTurn = 1;
	final int tileToMoveCount = 73;

	boolean currentPlayerHasTokenInPlay() {

		for(Token t: tokens[currentTurn]) {
			if(t.getTranslateX() != t.startPosX && t.getTranslateY() != t.startPosY)
				return true;
		}
		return false;
	}

	void ZarAt() {
		System.out.println("Loooooll");
		if(canThrowDice) {
			int counter = 0;
			Random random = new Random();
			for(int i = 0; i < 6; i++)
				if(random.nextInt(2) == 0)
					counter++;

			boolean isRepeatable = false;

			for(int i: repeatableMovesIndices) {
				if(counter == i) {
					isRepeatable = true;
					break;
				}
			}

			if(isRepeatable) {
				numberOfRemainingMoves++;
				rollAgainText.setText("Roll Again");
			}
			else {  // If can't roll again
				if(numberOfRemainingMoves == 0 && !currentPlayerHasTokenInPlay()) { // No valid moves
					endturn();
					return;
				}
				else if(!hasMovedThisTurn) {
					numberOfRemainingMoves++;
					canThrowDice = false;
					rollAgainText.setText("No more Rolls");
				}
				else {
					endturn();
				}
			}

			movesRemaining[counter].setText((Integer.parseInt(movesRemaining[counter].getText()) + 1) + "");
		}

	}

	void drawTile(Pane root, int x, int y, boolean middle) {
		Tile tile = new Tile(x, y, middle);
		tile.setTranslateX(x*40);
		tile.setTranslateY(y*40);
		tile.drawCoords(x, y);
		root.getChildren().add(tile);
		tiles[x][y] = tile;
	}

	void drawKuburs() {
		tiles[10][16].drawKubur();
		tiles[16][10].drawKubur();
		tiles[18][8].drawKubur();
		tiles[16][8].drawKubur();
		tiles[2][10].drawKubur();
		tiles[0][10].drawKubur();
		tiles[2][8].drawKubur();
		tiles[8][2].drawKubur();
		tiles[10][2].drawKubur();
		tiles[8][16].drawKubur();
	}

	void createBoard(Pane root) {

		root.setPrefSize(920, 760);

		for(int x = 0; x < 19; x++) {
			if(x < 8 || x > 10)
				for(int y = 8; y < 11; y++)
					drawTile(root, x, y, false);
			else {
				for(int y = 0; y < 19; y++) {
					if(y > 7 && y < 11)
						continue;
					else
						drawTile(root, x, y, false);
				}
			}
			drawTile(root, 8, 8, true);
		}
		drawKuburs();
	}

	private boolean mustDeleteOpponentsTokens(Tile finish, Token t) {
		boolean mustDelete = false;
		for(Token check: tokens[t.getOpponentType()]) {
			if(check.getTranslateX() == finish.getTranslateX() && check.getTranslateY() == finish.getTranslateY())
				if(!finish.kubur)
					mustDelete = true;
		}
		return mustDelete;
	}

	private void resetToken(Token token) {
		System.out.println(token.startPosX);
		token.setTranslateX(token.startPosX);
		token.setTranslateY(token.startPosY);
		token.counter = 0;
		token.rectangle.setStroke(null);
	}

	private void resetOpponentsTokens(Tile finish, Token moved) {
		for(Token token: tokens[moved.getOpponentType()])
			if(token.getTranslateX() == finish.getTranslateX() && token.getTranslateY() == finish.getTranslateY())
				resetToken(token);
	}

	private void endturn() {
		numberOfRemainingMoves = 0;
		canThrowDice = true;
		hasMovedThisTurn = false;
		for(Text t: movesRemaining)
			t.setText("0");
		rollAgainText.setText("Roll");
		if(currentTurn == 1) {
			currentTurn = 0;
			turnText.setText("Turn: Player 2");
		}
		else {
			currentTurn = 1;
			turnText.setText("Turn: Player 1");
		}
	}

	private void moveToEnd(Token token) {
		token.setTranslateX(tiles[8][8].getTranslateX());
		token.setTranslateY(tiles[8][8].getTranslateY());
		token.rectangle.setWidth(120);
		token.rectangle.setHeight(120);
	}

	private void moveToken(Token token) {
		if(currentTurn == token.type && Integer.parseInt(movesRemaining[selected].getText()) > 0) {
			if(token.counter == 0) {
				if(selected == 1 || selected == 5) {
					if(selected == 1)
						token.counter = 1;
					else if(selected == 5)
						token.counter = 15;
					token.setTranslateX(token.road[token.counter].getTranslateX());
					token.setTranslateY(token.road[token.counter].getTranslateY());
					token.rectangle.setStroke(Color.BLACK);
					movesRemaining[selected].setText((Integer.parseInt(movesRemaining[selected].getText()) - 1) + "");
					numberOfRemainingMoves--;
				}
				hasMovedThisTurn = true;
				return;
			}
			if(token.counter <= tileToMoveCount) {
				hasMovedThisTurn = true;
				if(token.counter < tileToMoveCount) {   // If moved not at end
					token.counter += possibleMoves[selected];
					if(token.counter > tileToMoveCount) {
						moveToEnd(token);
						token.counter = 74;
					}
					else {
						token.setTranslateX(token.road[token.counter].getTranslateX());
						token.setTranslateY((token.road[token.counter].getTranslateY()));
						if(mustDeleteOpponentsTokens(token.road[token.counter], token)) { // If opponents tokens are stacked
							resetOpponentsTokens(token.road[token.counter], token);
						}
					}
				}
				else                                    // If at end
					moveToEnd(token);
				hasMovedThisTurn = true;

				numberOfRemainingMoves--;
				movesRemaining[selected].setText((Integer.parseInt(movesRemaining[selected].getText()) - 1) + "");
				if(numberOfRemainingMoves == 0)
					endturn();
			}

		}
	}

	void addTokens(Pane root) throws MalformedURLException {

		int id = 0;
		for(int x = 4; x < 6; x++) {
			for(int y = 1; y < 3; y++) {
				Token token = new Token(0, id, x * 40, y * 40);
				tokens[0][id] = token;
				token.setTranslateX(x*40);
				token.setTranslateY(y*40);
				token.setOnMouseEntered(event -> moveToken(token));
				root.getChildren().add(token);
				id++;
			}
		}

		id = 0;
		for(int x = 13; x < 15; x++) {
			for(int y = 16; y < 18; y++) {
				Token token = new Token(1, id, x * 40, y * 40);
				tokens[1][id] = token;
				token.setTranslateX(x*40);
				token.setTranslateY(y*40);
				token.setOnMouseEntered(event -> moveToken(token));
				root.getChildren().add(token);
				id++;
			}

		}
	}

	// 6 12 4 3 2 25 8

	private void addRemainingMovesTable(Pane root) {

		ToggleGroup group = new ToggleGroup();
		int offset = 40;
		Text moves = new Text("Moves");
		Text remaining = new Text("Remaining");
		moves.setTranslateX(79 * offset/4);
		remaining.setTranslateX(85 * offset/4);
		moves.setTranslateY(offset);
		remaining.setTranslateY(offset);
		root.getChildren().addAll(moves, remaining);

		for(int i = 0; i < possibleMoves.length; i++) {
			RadioButton text = new RadioButton(possibleMoves[i] +"");
			text.setTranslateX(20 * offset - 5);
			text.setTranslateY((i + 1) * offset + 27);
			root.getChildren().add(text);
			text.setToggleGroup(group);
		}

		int xOffset = 15 * offset / 8;

		for(int i = 0; i < possibleMoves.length; i++) {
			Text text = new Text("0");
			text.setTranslateX(20 * offset + xOffset);
			text.setTranslateY((i + 2) * offset);
			root.getChildren().add(text);
			movesRemaining[i] = text;
		}
		group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			selected = group.getToggles().indexOf(newValue);
		});
	}

	private void createDiceButton(Pane root) {
		Button button = new Button("Roll Dice");
		button.setTranslateX(3 * 40);
		button.setTranslateY(18 * 40);
		button.setMinWidth(2 * 40);
		button.setOnAction(event -> ZarAt());
		root.getChildren().add(button);
	}

	private void createCurrentTurnText(Pane root) {
		turnText.setTranslateX(5 * 40 + 20);
		turnText.setTranslateY(18 * 40 + 20);
		turnText.setText("Turn: Player 1");
		root.getChildren().add(turnText);
	}
	private void createRollAgainText(Pane root) {
		rollAgainText.setTranslateX(3 * 40);
		rollAgainText.setTranslateY(17 * 40);
		rollAgainText.setText("Roll");
		root.getChildren().add(rollAgainText);
	}

	//19 tane 40px lik taş gelcek
	@Override
	public void start(Stage primaryStage) throws MalformedURLException {
		Pane root = new Pane();
		createBoard(root);
		addTokens(root);
		addRemainingMovesTable(root);
		createDiceButton(root);
		createCurrentTurnText(root);
		createRollAgainText(root);
		primaryStage.setTitle("Peçiç");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private class Token extends StackPane {
		// Tasın resimleri
		Image ince = new Image(new File("C:\\Users\\Sancho Jimenez\\IdeaProjects\\Pecic\\src\\sample\\Inceler.jpg").toURI().toURL().toExternalForm());
		Image sisko = new Image(new File("C:\\Users\\Sancho Jimenez\\IdeaProjects\\Pecic\\src\\sample\\Siskolar.jpg").toURI().toURL().toExternalForm());

		// İnce mi olacak kalın
		int type;
		int startPosX, startPosY, id, counter = 0;

		Tile[] road = new Tile[tileToMoveCount];
		Rectangle rectangle = new Rectangle(40, 40);

		public Token(int type, int id, int x, int y) throws MalformedURLException {

			this.type = type;
			this.id = id;
			this.startPosX = x;
			this.startPosY = y;

			rectangle.setStroke(null);
			if(type == 1) {
				rectangle.setFill(new ImagePattern(ince));
			}
			else {
				rectangle.setFill(new ImagePattern(sisko));
			}
			createRoad();
			setAlignment(Pos.CENTER);
			getChildren().add(rectangle);
		}

		// region createRoad

		private int moveUp(int startX, int startY, int amount, int counter) {
			for(int y = startY; y > startY - amount; y--) {
				road[counter] = tiles[startX][y];
				counter++;
			}
			return counter;
		}

		private int moveDown(int startX, int startY, int amount, int counter) {
			for(int y = startY; y < startY + amount; y++) {
				road[counter] = tiles[startX][y];
				counter++;
			}
			return counter;
		}

		private int moveLeft(int startX, int startY, int amount, int counter) {
			for(int x = startX; x > startX - amount; x--) {
				road[counter] = tiles[x][startY];
				counter++;
			}
			return counter;
		}

		private int moveRight(int startX, int startY, int amount, int counter) {
			for(int x = startX; x < startX + amount; x++) {
				road[counter] = tiles[x][startY];
				counter++;
			}
			return counter;
		}

		private void createRoad() {
			// Player 1
			int counter = 0;
			if(type == 1) {
				counter = moveUp(10, 16, 6, counter);
				counter = moveRight(11, 10, 8, counter);
				road[counter] = tiles[18][9];
				counter++;
				counter = moveLeft(18, 8, 8, counter);
				counter = moveUp(10, 7, 8, counter);
				road[counter] = tiles[9][0];
				counter++;
				counter = moveDown(8, 0, 8, counter);
				counter = moveLeft(7, 8, 8, counter);
				road[counter] = tiles[0][9];
				counter++;
				counter = moveRight(0, 10, 8, counter);
				counter = moveDown(8, 11, 8, counter);
				moveUp(9, 18, 8, counter);
			}   // Player 2
			else {
				counter = moveDown(8, 2, 6, counter);
				counter = moveLeft(7, 8, 8, counter);
				road[counter] = tiles[0][9];
				counter++;
				counter = moveRight(0, 10, 8, counter);
				counter = moveDown(8, 11, 8, counter);
				road[counter] = tiles[9][18];
				counter++;
				counter = moveUp(10, 18, 8, counter);
				counter = moveRight(11, 10, 8, counter);
				road[counter] = tiles[18][9];
				counter++;
				counter = moveLeft(18, 8, 8, counter);
				counter = moveUp(10, 7, 8, counter);
				moveDown(9, 0, 8, counter);
			}
		}

		//endregion

		private int getOpponentType() {
			if(type == 1)
				return 0;
			else
				return 1;
		}

	}

	private class Tile extends StackPane {

		Text text = new Text();
		int x, y;
		boolean kubur = false;

		public Tile(int x, int y, boolean middle) {
			this.x = x;
			this.y = y;
			Rectangle ren;
			if(middle)
				ren = new Rectangle(120, 120);
			else
				ren = new Rectangle(40, 40);
			ren.setFill(null);
			text.setFont(Font.font(10));
			ren.setStroke(Color.BLACK);
			setAlignment(Pos.CENTER);
			getChildren().addAll(ren, text);
		}

		private void drawKubur() {
			this.kubur = true;
			text.setFont(Font.font(21));
			text.setText("X");
		}

		private void drawCoords(int x, int y) {
			text.setText(String.valueOf(x) + " " + String.valueOf(y));
		}


	}

}
