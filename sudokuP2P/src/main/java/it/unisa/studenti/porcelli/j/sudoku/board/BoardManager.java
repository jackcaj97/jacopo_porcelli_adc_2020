package it.unisa.studenti.porcelli.j.sudoku.board;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

/**
 * Class for managing all the interactions with a sudoku board.
 * @author Jacopo Porcelli
 *
 */
public class BoardManager implements Serializable {
	
	private final int DEFAULT_DIFFICULTY = 5;
	
	private SudokuField field;			// Actual sudoku board.
	private ArrayList<String> players;	// Players list
	private ArrayList<Integer> scores;	// Players' scores list.
	
	private String name;
	private int difficulty;
	
	public BoardManager(String name) {
		
		players = new ArrayList<String>();
		scores = new ArrayList<Integer>();
		
		this.name = name;
		
		field = new SudokuField(3);
		
		generateFullField(1, 1);
	}
	
	public BoardManager() {
		
	}
	
	/**
	 * Generates the board for the sudoku game already filled with numbers.
	 * @param row index of the first row.
	 * @param column index of the first column.
	 */
	private void generateFullField(final int row, final int column) {
	    if (!field.isFilled(field.fieldSize(), field.fieldSize())) {
	        while (field.numberOfTriedNumbers(row, column) < field.variantsPerCell()) {
	            int candidate = 0;
	            do {
	                candidate = field.getRandomIndex();
	            } while (field.numberHasBeenTried(candidate, row, column));
	            if (field.checkNumberField(candidate, row, column)) {
	                field.set(candidate, row, column);
	                Index nextCell = field.nextCell(row, column);
	                if (nextCell.i <= field.fieldSize()
	                        && nextCell.j <= field.fieldSize()) {
	                    generateFullField(nextCell.i, nextCell.j);
	                }
	            } else {
	                field.tryNumber(candidate, row, column);
	            }
	        }
	        if (!field.isFilled(field.fieldSize(), field.fieldSize())) {
	            field.reset(row, column);
	        }
	    }
	}
	
	
	/** 
	 * Applies the chosen difficulty for the board by removing a certain amount of numbers from it.
	 */
	public void applyDifficulty(Integer[][] board, int difficulty) {
		int numsToDelete = DEFAULT_DIFFICULTY*difficulty;
		if(difficulty == 0)
			numsToDelete = 1;
		
		for(int d = 0; d < numsToDelete; d++) {
			int i = (int) (Math.random() * 10) % 9;
			int j = (int) (Math.random() * 10) % 9;
			
			board[i][j] = 0;
		}
	}
	
	
	/**
	 * Prints on stdout the sudoku board created.
	 */
	public void printBoard() {
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				System.out.print(field.get(i+1, j+1) + " ");
				if(j%3 == 2)
					System.out.print("|");
			}
			System.out.print("\n");
			if(i%3 == 2) {
				for(int k = 0; k < 10; k++)
					System.out.print("__");
				System.out.print("\n");
			}
		}
	}
	
	public SudokuPanel printMatrix(Integer[][] matrix) {
		SudokuPanel panel = null;
		SwingUtilities.invokeLater(() -> {
            SudokuPanel.createAndShowGui(matrix);
        });
		
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				System.out.print(matrix[i][j] + " ");
				if(j%3 == 2)
					System.out.print("|");
			}
			System.out.print("\n");
			if(i%3 == 2) {
				for(int k = 0; k < 10; k++)
					System.out.print("__");
				System.out.print("\n");
			}
		}
		
		return panel;
	}
	
	/**
	 * Returns the board in a bidimensional array.
	 * @return
	 */
	public Integer[][] getAsMatrix() {
		Integer matrix[][] = new Integer[9][9];
		
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				matrix[i][j] = field.get(i+1, j+1);
			}
		}
		
		return matrix;
	}
	
	/**
	 * Tries to place a number in the matrix.
	 * @param matrix Matrix to fill with the new number.
	 * @param i Row in which to place the number
	 * @param j Column in which to place the number
	 * @param number Number to place.
	 * @return Score obtained, 0 if the position was already filled, -1 if the position in incorrect and 1 if the position is correct.
	 */
	public int placeNumInMatrix(Integer[][] matrix, int i, int j, int number, SudokuPanel panel) {
		
		if(matrix[i][j] != 0) {	// Number already placed.
			return 0;
		}
		else {
			if(checkPosition(matrix, i, j, number)) {	// Number correctly placed.
				matrix[i][j] = number;
				panel.placeValue(i, j, number);
				return 1;
			}
			else {
				return -1;
			}
		}
	}
	
	private boolean checkPosition(Integer[][] matrix, int i, int j, int number) {
		
		return checkRow(matrix, i, number) && checkColumn(matrix, j, number) && checkSubGrid(matrix, i, j, number);
	}
	
	private boolean checkRow(Integer[][] matrix, int row, int number) {
		
		for(int k = 0; k < 9; k++) {
			if(matrix[row][k] == number)
				return false;
		}
		
		return true;
	}
	
	private boolean checkColumn(Integer[][] matrix, int column, int number) {
		for(int k = 0; k < 9; k++) {
			if(matrix[k][column] == number)
				return false;
		}
		
		return true;
	}
	
	private boolean checkSubGrid(Integer[][] matrix, int i, int j, int number) {
		
		int r = i, c = j;	// Start of sub grid.
		
		r = (r / 3) * 3;
		c = (c / 3) * 3;
		
		for (int m = r; m < r + 3; m++) {
			for (int n = c; n < c + 3; n++) {
				if (matrix[m][n] == number) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean isCompleted(Integer[][] matrix) {
		for(int i = 0; i < 9; i++)
			for(int j = 0; j < 9; j++)
				if(matrix[i][j] == 0)
					return false;
		
		return true;
	}

	
	// Getters and Setters
	
	public SudokuField getField() {
		return field;
	}

	public void setField(SudokuField field) {
		this.field = field;
	}

	public ArrayList<String> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<String> players) {
		this.players = players;
	}

	public ArrayList<Integer> getScores() {
		return scores;
	}

	public void setScores(ArrayList<Integer> scores) {
		this.scores = scores;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
	
}
