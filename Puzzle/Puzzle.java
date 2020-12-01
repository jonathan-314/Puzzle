package Puzzle;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Puzzle extends JPanel implements MouseListener {

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Puzzle instance
	 */
	public static Puzzle game;

	/**
	 * width of screen
	 */
	int screenWidth = getToolkit().getScreenSize().width;

	/**
	 * height of screen
	 */
	int screenHeight = getToolkit().getScreenSize().height;

	/**
	 * the image of the puzzle
	 */
	BufferedImage image;

	/**
	 * width of image
	 */
	int imageWidth;

	/**
	 * height of image
	 */
	int imageHeight;

	/**
	 * file path of image
	 */
	final static String imagePath = "Images/image7.jpg";

	/**
	 * number of columns in puzzle
	 */
	static int M;

	/**
	 * number of rows in puzzle
	 */
	static int N;

	/**
	 * coordinates of start of each column
	 */
	int[] edgeWidths;

	/**
	 * coordinates of start of each row
	 */
	int[] edgeHeights;

	/**
	 * list of puzzle pieces
	 */
	LinkedList<Piece> pieces = new LinkedList<Piece>();

	/**
	 * margins of piece, for jigsaw thing idk what it's called
	 */
	final int margin = 18;

	/**
	 * radius of each jigsaw thing idk what it's called
	 */
	final int radius = 14;

	/**
	 * x-coordinate of mouse
	 */
	int mouseX;

	/**
	 * y-coordinate of mouse
	 */
	int mouseY;

	/**
	 * is the mouse pressed?
	 */
	boolean mousePressed = false;

	/**
	 * select multiple Pieces at once!!
	 * <p>
	 * WARNING: WORK IN PROGRESS
	 * </p>
	 */
	int multiSelect = 0;

	int multiStartX;
	int multiStartY;

	/**
	 * tolerance for connecting pieces together
	 */
	final int tolerance = 10;

	/**
	 * starting time, used for timing
	 */
	long startTime;

	/**
	 * current time, used for timing
	 */
	long currentTime;

	/**
	 * is the game over?
	 */
	boolean gameOver = false;

	/**
	 * connections between pieces; used for progress
	 */
	int totalConnections;

	/**
	 * how many connections currently; used for progress
	 */
	int currentConnections = 0;

	/**
	 * puzzle constructor
	 */
	public Puzzle(String filePath) {
		loadImage(filePath);
		addMouseListener(this);
		setFocusable(true);
	}

	/**
	 * loads an image
	 * 
	 * @param filePath path to image
	 */
	public void loadImage(String filePath) {
		try {
			image = ImageIO.read(new File(filePath));
		} catch (IOException e) {
			System.out.println("file not found");
			System.exit(ABORT);
		}
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();

		M = imageWidth / 90; // around 90 pixels
		N = imageHeight / 60; // around 60 pixels

		System.out.println("Number of pieces: " + (M * N));

		totalConnections = N * (M - 1) + M * (N - 1);

		edgeWidths = new int[M + 1];
		edgeHeights = new int[N + 1];

		for (int i = 0; i <= M; i++) {
			edgeWidths[i] = imageWidth * i / M;
		}
		for (int i = 0; i <= N; i++) {
			edgeHeights[i] = imageHeight * i / N;
		}

		Piece[][] arrangedPieces = new Piece[M][N];
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				int pieceWidth = edgeWidths[i + 1] - edgeWidths[i];
				int pieceHeight = edgeHeights[j + 1] - edgeHeights[j];
				int[][] picture = new int[margin + pieceWidth + margin][margin + pieceHeight + margin];
				for (int q = 0; q < picture.length; q++) {
					Arrays.fill(picture[q], -1); // transparent
				}
				for (int x = 0; x < pieceWidth; x++) {
					for (int y = 0; y < pieceHeight; y++) {
						picture[margin + x][margin + y] = image.getRGB(x + edgeWidths[i], y + edgeHeights[j]);
					}
				}
				Piece newPiece = new Piece(i * N + j, picture);
				newPiece.x = (int) (Math.random() * screenWidth / 2 + 100);
				newPiece.y = (int) (Math.random() * screenHeight / 2 + 100);
				arrangedPieces[i][j] = newPiece;
			}
		}

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				Piece c = arrangedPieces[i][j];
				// TODO implement randomness for hole shape

				int locationRandom = (int) (Math.random() * 2 * radius) - radius;
				int radiusRandom = (int) ((1 - Math.random() * 0.4) * radius);

				if (i != M - 1) {
					Piece right = arrangedPieces[i + 1][j];
					c.neighbors.add(right);
					right.neighbors.add(c);
					if (Math.random() < 0.5) {
						transfer(radiusRandom, margin, right.height / 2 + locationRandom, right, c, c.width - margin,
								c.height / 2 + locationRandom);
					} else {
						transfer(radiusRandom, c.width - margin, c.height / 2 + locationRandom, c, right, margin,
								right.height / 2 + locationRandom);
					}
				}
				if (j != N - 1) {
					Piece down = arrangedPieces[i][j + 1];
					c.neighbors.add(down);
					down.neighbors.add(c);
					if (Math.random() < 0.5) {
						transfer(radiusRandom, down.width / 2 + locationRandom, margin, down, c,
								c.width / 2 + locationRandom, c.height - margin);
					} else {
						transfer(radiusRandom, c.width / 2 + locationRandom, c.height - margin, c, down,
								down.width / 2 + locationRandom, margin);
					}
				}
				c.updateImage();

				pieces.add(c);
			}
		}
	}

	/**
	 * initialize graphics
	 */
	public void init() {
		JFrame jf = new JFrame("Puzzle");
		jf.setSize(getToolkit().getScreenSize().width, getToolkit().getScreenSize().height);
		jf.add(this);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);

		startTime = System.currentTimeMillis();

		while (true) {
			if (!gameOver) { // only advance time while game is not over
				currentTime = System.currentTimeMillis();
			}
			jf.repaint();
			try {
				Thread.sleep(77); // ~13 fps!!
			} catch (InterruptedException e) {
				System.out.println("interrupted exception");
			}
		}
	}

	/**
	 * paint method, called every frame
	 */
	@Override
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenWidth, screenHeight);
		int currMouseX = MouseInfo.getPointerInfo().getLocation().x;
		int currMouseY = MouseInfo.getPointerInfo().getLocation().y - 44;

		if (multiSelect == 1) {
			g.setColor(Color.DARK_GRAY);
			int upperLeftX = Math.min(multiStartX, currMouseX);
			int upperLeftY = Math.min(multiStartY, currMouseY);
			int selectWidth = multiStartX + currMouseX - 2 * upperLeftX;
			int selectHeight = multiStartY + currMouseY - 2 * upperLeftY;
			g.fillRect(upperLeftX, upperLeftY, selectWidth, selectHeight);
		}

		if (mousePressed && multiSelect != 1) {
			int moveX = currMouseX - mouseX;
			int moveY = currMouseY - mouseY;
			for (Piece c : pieces) {
				if (c.selected) {
					c.x += moveX;
					c.y += moveY;
				}
			}
			mouseX = currMouseX;
			mouseY = currMouseY;
		}
		int scaledWidth = imageWidth * 200;
		scaledWidth /= imageHeight;
		g.drawImage(image, 0, 0, scaledWidth, 200, null);

		// draw in reverse order
		Iterator<Piece> reverse = pieces.descendingIterator();
		while (reverse.hasNext()) { // reverse order
			Piece c = reverse.next();
//			for (int i = 0; i < c.width; i++) {
//				for (int j = 0; j < c.height; j++) {
//					if (c.picture[i][j] == -1) {
//						continue;
//					}
//					g.setColor(new Color(c.picture[i][j]));
//					g.fillRect(i + c.x, j + c.y, 1, 1);
//				}
//			}

			g.drawImage(c.pic, c.x, c.y, null);
		}

		g.setColor(Color.YELLOW);
		g.drawOval(screenWidth / 2 - 50, 8, 15, 15);
		g.drawString("C", screenWidth / 2 - 47, 20);
		g.drawString("Jonathan Guo", screenWidth / 2 - 30, 20);
		long elapsedTime = currentTime - startTime;
		int seconds = (int) ((elapsedTime / 1000) % 60);
		int minutes = (int) ((elapsedTime / 1000) / 60);
		String secondsDisplay = Integer.toString(seconds);
		if (seconds < 10) {
			secondsDisplay = "0" + secondsDisplay;
		}
		String timeDisplay = minutes + ":" + secondsDisplay;
		g.setFont(new Font("helvetica", 20, 30));
		g.drawString(timeDisplay, 100, 240);
		double progress = currentConnections * 100.0d / totalConnections;
		String progressDisplay = String.format("%.2f", progress) + "%";
		g.drawString(progressDisplay, 100, 280);
	}

	/**
	 * transfers pixels from one Piece to another
	 * 
	 * @param radius      radius of region to be transfered
	 * @param cx          center x of source region
	 * @param cy          center y of source region
	 * @param destination destination piece
	 * @param source      source piece
	 * @param cx2         center x of destination region
	 * @param cy2         center y of destination region
	 */
	private void transfer(int radius, int cx, int cy, Piece destination, Piece source, int cx2, int cy2) {
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				if (i * i + j * j > radius * radius) { // outside circle
					continue;
				}
				int x = i + cx;
				int y = j + cy;
				if (destination.picture[x][y] == -1) {
					continue; // ignore transparent pixels
				}
				source.picture[cx2 + i][cy2 + j] = destination.picture[x][y];
				destination.picture[x][y] = -1;
			}
		}
	}

	/**
	 * tests to see if the game is over and ends game if it is
	 */
	public void testGameOver() {
		boolean isGameOver = true;
		for (Piece c : pieces) {
			if (find(c) != find(pieces.getFirst())) { // not all connected
				isGameOver = false;
				break;
			}
		}
		if (isGameOver) {
			endGame();
		}
	}

	/**
	 * ends the game
	 */
	public void endGame() {
		gameOver = true;
		JOptionPane.showMessageDialog(this, "Game Over! You win!");
		System.exit(ABORT);
	}

	/**
	 * merges two pieces, union find
	 * 
	 * @param a piece 1
	 * @param b piece 2
	 */
	private void merge(Piece a, Piece b) {
		find(a).parent = find(b);
	}

	/**
	 * finds the parent of a piece, union find
	 * 
	 * @param o piece
	 * @return parent of o
	 */
	private Piece find(Piece o) {
		if (o.parent != null) {
			o.parent = find(o.parent);
			return o.parent;
		}
		return o;
	}

	/**
	 * overlap function
	 * <p>
	 * calculates if two rectangles overlap
	 * </p>
	 * 
	 * @param rect1 rectangle 1 [upper left x, upper left y, lower right x, lower
	 *              right y]
	 * @param rect2 rectangle 2 [upper left x, upper left y, lower right x, lower
	 *              right y]
	 * @return if the two rectangles overlap
	 */
	private boolean overlap(int[] rect1, int[] rect2) {
		if (between2(rect1[0], rect2[0], rect2[2]) || between2(rect2[0], rect1[0], rect1[2])) {
			if (between2(rect1[1], rect2[1], rect2[3]) || between2(rect2[1], rect1[1], rect1[3])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * between function v2
	 * <p>
	 * calculates if value is between the bounds (no designation for lower / upper)
	 * </p>
	 * 
	 * @param val    value
	 * @param bound1 bound 1
	 * @param bound2 bound 2
	 * @return if value is between the bounds
	 */
	private boolean between2(int val, int bound1, int bound2) {
		return between(val, bound1, bound2) || between(val, bound2, bound1);
	}

	/**
	 * between function
	 * <p>
	 * calculates if value is between lower bound (inclusive) and upper bound
	 * (exclusive)
	 * </p>
	 * 
	 * @param val        value
	 * @param lowerBound lower bound
	 * @param upperBound upper bound
	 * @return if value is between lower bound and upper bound
	 */
	private boolean between(int val, int lowerBound, int upperBound) {
		return lowerBound <= val && val < upperBound;
	}

	/**
	 * Moves selected pieces to the front
	 */
	private void moveToFront() {
		LinkedList<Piece> selectedPieces = new LinkedList<Piece>();
		for (Piece c : pieces) {
			if (c.selected) {
				selectedPieces.addFirst(c);
			}
		}
		for (Piece c : selectedPieces) {
			pieces.remove(c);
			pieces.addFirst(c);
		}
	}

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		game = new Puzzle(imagePath);
		game.init();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePressed = true;
		mouseX = e.getX();
		mouseY = e.getY();

		if (multiSelect == 2) {
			multiSelect = 3;
			return;
		}

		boolean successfulSelect = false;

		for (Piece c : pieces) {
			if (between(mouseX, c.x, c.x + c.width)) {
				if (between(mouseY, c.y, c.y + c.height)) { // within the dimensions of the piece
					if (c.picture[mouseX - c.x][mouseY - c.y] != -1) { // clicked on a non transparent pixel
						c.selected = true;

						successfulSelect = true;
						for (Piece d : pieces) {
							if (find(c) == find(d)) {
								d.selected = true; // select all pieces linked to c
							}
						}
						break; // only select one piece
					}
				}
			}
		}

		moveToFront();

		if (!successfulSelect && multiSelect == 0) {
			multiSelect = 1;
			multiStartX = mouseX;
			multiStartY = mouseY;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mousePressed = false;

		if (multiSelect == 1) {
			int total = 0;
			int upperLeftX = Math.min(multiStartX, e.getX());
			int upperLeftY = Math.min(multiStartY, e.getY());
			int lowerRightX = Math.max(multiStartX, e.getX());
			int lowerRightY = Math.max(multiStartY, e.getY());
			for (Piece c : pieces) {
				if (overlap(new int[] { upperLeftX, upperLeftY, lowerRightX, lowerRightY },
						new int[] { c.x + margin, c.y + margin, c.x + c.width - margin, c.y + c.height - margin })) {
					if (c.selected) {
						continue; // already selected!
					}
					c.selected = true;
					total += 1;
					for (Piece d : pieces) {
						if (find(d) == find(c)) {
							d.selected = true;
						}
					}
				}
			}

			moveToFront();

			multiSelect = 2;
			if (total == 0) {
				multiSelect = 0;
			}
			return;
		}

		if (multiSelect == 2) {
			for (Piece c : pieces) {
				c.selected = false;
			}
		}

		for (Piece c : pieces) {
			if (!c.selected) {
				continue;
			}
			for (Piece n : c.neighbors) {
				if (find(c) == find(n)) { // already connected!
					continue;
				}

				// vector between neighbor and this Piece
				int diffX = n.x - c.x;
				int diffY = n.y - c.y;

				// target vector between neighbor and this Piece
				int targetX = edgeWidths[n.row] - edgeWidths[c.row];
				int targetY = edgeHeights[n.col] - edgeHeights[c.col];

				// error vector (target - actual)
				int errorX = targetX - diffX;
				int errorY = targetY - diffY;
				if (Math.abs(errorX) <= tolerance && Math.abs(errorY) <= tolerance) { // error within tolerance
					for (Piece d : pieces) {
						if (find(d) == find(n)) {
							// move all pieces connected to neighbor by error vector
							d.x += errorX;
							d.y += errorY;
						}
					}
					merge(c, n); // connect them
				}
			}

		}

		multiSelect = 0;

		// calculating number of connections
		int finalConnections = 0;
		for (Piece c : pieces) {
			for (Piece n : c.neighbors) {
				if (find(c) == find(n)) {
					finalConnections++;
				}
			}
			c.selected = false;
		}

		currentConnections = finalConnections / 2; // double counting!

		testGameOver(); // test to see if the game is over
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
