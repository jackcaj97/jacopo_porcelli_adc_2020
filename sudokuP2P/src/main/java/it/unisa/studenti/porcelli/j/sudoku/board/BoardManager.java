package it.unisa.studenti.porcelli.j.sudoku.board;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class for managing all the interactions with a sudoku board.
 * @author Jacopo Porcelli
 *
 */
public class BoardManager implements Serializable {
	
	private final int DEFAULT_DIFFICULTY = 10;
	
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
	
	public void printMatrix(Integer[][] matrix) {
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
