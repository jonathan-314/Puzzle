package Puzzle;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

class Piece {

	/**
	 * unique id of piece
	 */
	int id;

	/**
	 * which row in picture this piece appears
	 */
	int row;

	/**
	 * which column in picture this piece appears
	 */
	int col;

	/**
	 * x location of piece
	 */
	int x;

	/**
	 * y location of piece
	 */
	int y;

	/**
	 * picture of piece
	 */
	int[][] picture;

	/**
	 * pic of piece
	 */
	BufferedImage pic;

	/**
	 * width of piece
	 */
	int width;

	/**
	 * height of piece
	 */
	int height;

	/**
	 * neighbors of this piece
	 */
	LinkedList<Piece> neighbors = new LinkedList<Piece>();

	/**
	 * parent of piece, union find
	 */
	Piece parent;

	/**
	 * has the user selected this piece
	 */
	boolean selected = false;

	/**
	 * image used to draw border
	 */
	BufferedImage border;

	private final static int[] DX = { 0, 1, 0, -1 };
	private final static int[] DY = { 1, 0, -1, 0 };

	/**
	 * border width (4 pixels)
	 */
	private final static int BORDER_WIDTH = 4;

	private final static int BORDER_COLOR = 256 * 256 * 256 * 255 + 256 * 256 * 50 + 256 * 100 + 255;

	/**
	 * piece constructor
	 * 
	 * @param id   unique id
	 * @param pict array of pixels
	 */
	public Piece(int id, int[][] pict) {
		this.id = id;
		row = this.id / Puzzle.N;
		col = this.id % Puzzle.N;
		this.parent = null;
		picture = pict;
		width = picture.length;
		height = picture[0].length;
		pic = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		border = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * updates image to match color array
	 */
	public void updateImage() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (picture[i][j] == -1) {
					continue; // transparent
				}
				pic.setRGB(i, j, picture[i][j]);
			}
		}

		updateBorder();
	}

	public void updateBorder() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (picture[i][j] == -1) {
					continue; // transparent
				}

				// only consider boundary points
				boolean boundary = false;
				for (int k = 0; k < 4; k++) {
					if (picture[i + DX[k]][j + DY[k]] == -1) {
						boundary = true;
					}
				}
				if (!boundary) {
					continue;
				}

				for (int k = -BORDER_WIDTH; k <= BORDER_WIDTH; k++) {
					for (int l = -BORDER_WIDTH; l <= BORDER_WIDTH; l++) {
						if (i + k < 0 || i + k >= width || j + l < 0 || j + l >= height) {
							continue;
						}
//						if (k * k + l * l <= 1) {
//							border.setRGB(i + k, j + l, 10);
//						} else {
//							if (border.getRGB(i + k, j + l) != 10) {
//								border.setRGB(i + k, j + l, BORDER_COLOR);
//							}
//						}
						border.setRGB(i + k, j + l, BORDER_COLOR);
					}
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return id; // useless, but what if it's not
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Piece) && (((Piece) o).id == this.id);
	}
}