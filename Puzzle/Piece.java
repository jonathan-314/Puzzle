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
	// int parent;

	/**
	 * has the user selected this piece
	 */
	boolean selected = false;

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
		// this.parent = id;
		picture = pict;
		width = picture.length;
		height = picture[0].length;
		pic = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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
	}

	@Override
	public int hashCode() {
		return id; // useless, but what if it's not
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Piece) {
			return ((Piece) o).id == this.id;
		}
		return false;
	}
}