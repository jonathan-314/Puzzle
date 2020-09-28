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
	 * puzzle instance
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
	 * image
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
	final String imagePath = "Images/image4.jpg";

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
	 * array of puzzle pieces
	 */
	Piece[] pieces;

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
	 * tolerance for connecting pieces together
	 */
	final int tolerance = 10;

	/**
	 * starting time
	 */
	long startTime;

	/**
	 * current time
	 */
	long currentTime;

	/**
	 * is the game over?
	 */
	boolean gameOver = false;

	/**
	 * puzzle constructor
	 */
	public Puzzle() {
		loadImage();

		addMouseListener(this);
		setFocusable(true);
	}

	public void loadImage() {
		try {
			image = ImageIO.read(new File(imagePath));
		} catch (IOException e) {
			System.out.println("file not found");
			System.exit(ABORT);
		}
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();

		M = imageWidth / 90; // around 90 pixels
		N = imageHeight / 60; // around 60 pixels

		edgeWidths = new int[M + 1];
		edgeHeights = new int[N + 1];
		pieces = new Piece[M * N];

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
					Arrays.fill(picture[q], -1);
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
				int radiusRandom = (int) ((1 - Math.random() * 0.5) * radius);

				if (i != M - 1) {
					Piece right = arrangedPieces[i + 1][j];
					c.neighbors[1] = right;
					right.neighbors[0] = c;
					if (Math.random() < 0.5)
						transfer(radiusRandom, margin, right.height / 2 + locationRandom, right, c, c.width - margin,
								c.height / 2 + locationRandom);
					else
						transfer(radiusRandom, c.width - margin, c.height / 2 + locationRandom, c, right, margin,
								right.height / 2 + locationRandom);
				}
				if (j != N - 1) {
					Piece down = arrangedPieces[i][j + 1];
					c.neighbors[3] = down;
					down.neighbors[2] = c;
					if (Math.random() < 0.5)
						transfer(radiusRandom, down.width / 2 + locationRandom, margin, down, c,
								c.width / 2 + locationRandom, c.height - margin);
					else
						transfer(radiusRandom, c.width / 2 + locationRandom, c.height - margin, c, down,
								down.width / 2 + locationRandom, margin);
				}
				c.updateImage();
				pieces[i * N + j] = c;
			}
		}
	}

	/**
	 * initialize graphics
	 */
	public void init() {
		JFrame jf = new JFrame("puzzle");
		jf.setSize(getToolkit().getScreenSize().width, getToolkit().getScreenSize().height);
		jf.add(this);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);

		startTime = System.currentTimeMillis();

		while (true) {
			if (!gameOver)
				currentTime = System.currentTimeMillis();
			jf.repaint();
			try {
				Thread.sleep(77);
			} catch (InterruptedException e) {
				System.out.println("interrupted");
			}
		}
	}

	/**
	 * paint method, called every frame
	 */
	@Override
	public void paint(Graphics g) {
		if (mousePressed) {
			int currMouseX = MouseInfo.getPointerInfo().getLocation().x;
			int currMouseY = MouseInfo.getPointerInfo().getLocation().y - 44;
			int moveX = currMouseX - mouseX;
			int moveY = currMouseY - mouseY;
			for (Piece c : pieces) {
				if (!c.selected)
					continue;
				c.x += moveX;
				c.y += moveY;
			}
			mouseX = currMouseX;
			mouseY = currMouseY;
		}
		int scaledWidth = imageWidth * 200;
		scaledWidth /= imageHeight;
		g.drawImage(image, 0, 0, scaledWidth, 200, null);
		for (int q = pieces.length - 1; q >= 0; q--) { // draw in reverse order
			Piece c = pieces[q];
//			for (int i = 0; i < c.width; i++) {
//				for (int j = 0; j < c.height; j++) {
//					if (c.picture[i][j] == -1)
//						continue;
//					g.setColor(new Color(c.picture[i][j]));
//					g.fillRect(i + c.x, j + c.y, 1, 1);
//				}
//			}
			g.drawImage(c.pic, c.x, c.y, null);
		}

		g.setColor(Color.BLACK);
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
	}

	/**
	 * transfers pixels from one piece to another
	 * 
	 * @param radius radius of region to be transfered
	 * @param cx     center x of source region
	 * @param cy     center y of source region
	 * @param from   from piece
	 * @param to     to piece
	 * @param cx2    center x of destination region
	 * @param cy2    center y of destination region
	 */
	public void transfer(int radius, int cx, int cy, Piece from, Piece to, int cx2, int cy2) {
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				if (i * i + j * j > radius * radius)
					continue;
				int x = i + cx;
				int y = j + cy;
				if (from.picture[x][y] == -1)
					continue; // ignore transparent pixels
				to.picture[cx2 + i][cy2 + j] = from.picture[x][y];
				from.picture[x][y] = -1;
			}
		}
	}

	/**
	 * tests to see if the game is over
	 */
	public void testGameOver() {
		boolean testGameOver = true;
		for (Piece c : pieces) {
			if (find(c) != find(pieces[0])) {
				testGameOver = false;
				break;
			}
		}
		if (testGameOver) {
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
	 * merges to pieces, union find
	 * 
	 * @param a piece 1
	 * @param b piece 2
	 */
	public void merge(Piece a, Piece b) {
		pieces[find(a)].parent = find(b);
	}

	/**
	 * finds the parent of a piece, union find
	 * 
	 * @param o piece
	 * @return
	 */
	public int find(Piece o) {
		if (o.parent != o.id) {
			o.parent = find(pieces[o.parent]);
		}
		return o.parent;
	}

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		game = new Puzzle();
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
		for (Piece c : pieces) {
			if (c.x + margin <= mouseX && mouseX <= c.x + c.width - margin) {
				if (c.y + margin <= mouseY && mouseY <= c.y + c.height - margin) {
					c.selected = true;
					for (Piece d : pieces) {
						if (find(c) == find(d)) {
							d.selected = true; // select all pieces linked to c
						}
					}
					break; // only select one region
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mousePressed = false;
		for (Piece c : pieces) {
			if (!c.selected)
				continue;
			for (Piece n : c.neighbors) {
				if (n == null)
					continue;
				if (find(c) == find(n))
					continue;
				int diffX = n.x - c.x;
				int diffY = n.y - c.y;
				int targetX = edgeWidths[n.row] - edgeWidths[c.row];
				int targetY = edgeHeights[n.col] - edgeHeights[c.col];
				int errorX = targetX - diffX;
				int errorY = targetY - diffY;
				if (Math.abs(errorX) <= tolerance && Math.abs(errorY) <= tolerance) {
					for (Piece d : pieces) {
						if (find(d) != find(n))
							continue;
						d.x += errorX;
						d.y += errorY;
					}
					merge(c, n);
				}
			}
			c.selected = false;
		}
		testGameOver();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
