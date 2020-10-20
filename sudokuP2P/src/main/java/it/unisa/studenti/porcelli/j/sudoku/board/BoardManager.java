package it.unisa.studenti.porcelli.j.sudoku.board;


/**
 * Class for managing all the interactions with a sudoku board.
 * @author Jacopo Porcelli
 *
 */
public class BoardManager {
	
	private SudokuField field;
	
	public BoardManager() {
		field = new SudokuField(3);
		
		generateFullField(1, 1);
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
	
	/**
	 * Returns the board in a bidimensional array.
	 * @return
	 */
	public int[][] getMatrix() {
		int matrix[][] = new int[9][9];
		
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				matrix[i][j] = field.get(i+1, j+1);
			}
		}
		
		return matrix;
	}
	
}
