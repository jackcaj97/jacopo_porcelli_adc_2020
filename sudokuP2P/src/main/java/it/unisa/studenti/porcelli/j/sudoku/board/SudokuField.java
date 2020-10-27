package it.unisa.studenti.porcelli.j.sudoku.board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Class that handles a whole sudoku board.
 *
 */
public class SudokuField implements Serializable {

	private final int blockSize;	// Size of a single block of the board. In a standard sudoku matrix a block is made up of 3*3 cells.
	private final int fieldSize;	// Size of a line/column of the matrix, but also of a single block.
	private SudokuCell[][] field;

	public SudokuField(final int blocks) {
		blockSize = blocks;
		fieldSize = blockSize * blockSize;
		field = new SudokuCell[fieldSize][fieldSize];
		
		for (int i = 0; i < fieldSize; ++i) {
			for (int j = 0; j < fieldSize; ++j) {
				field[i][j] = new SudokuCell();
			}
		}
	}

	public int blockSize() {
		return blockSize;
	}

	public int fieldSize() {
		return fieldSize;
	}

	public int variantsPerCell() {
		return fieldSize;
	}

	public int numberOfCells() {
		return fieldSize * fieldSize;
	}

	public void clear(final int row, final int column) {
		field[row - 1][column - 1].clear();
	}

	public void clearAllCells() {
		for (int i = 0; i < fieldSize; ++i) {
			for (int j = 0; j < fieldSize; ++j) {
				field[i][j].clear();
			}
		}
	}

	public void reset(final int row, final int column) {
		field[row - 1][column - 1].reset();
	}

	public void resetAllCells() {
		for (int i = 0; i < fieldSize; ++i) {
			for (int j = 0; j < fieldSize; ++j) {
				field[i][j].reset();
			}
		}
	}

	public boolean isFilled(final int row, final int column) {
		return field[row - 1][column - 1].isFilled();
	}

	public boolean allCellsFilled() {
		for (int i = 0; i < fieldSize; ++i) {
			for (int j = 0; j < fieldSize; ++j) {
				if (!field[i][j].isFilled()) {
					return false;
				}
			}
		}
		return true;
	}

	public int numberOfFilledCells() {
		int filled = 0;
		for (int i = 1; i <= fieldSize; ++i) {
			for (int j = 1; j <= fieldSize; ++j) {
				if (isFilled(i, j)) {
					++filled;
				}
			}
		}
		return filled;
	}

	public int numberOfHiddenCells() {
		return numberOfCells() - numberOfFilledCells();
	}

	public int get(final int row, final int column) {
		return field[row - 1][column - 1].get();
	}

	public void set(final int number, final int row, final int column) {
		field[row - 1][column - 1].set(number);
	}

	public void hide(final int row, final int column) {
		field[row - 1][column - 1].hide();
	}

	public void show(final int row, final int column) {
		field[row - 1][column - 1].show();
	}

	public void tryNumber(final int number, final int row, final int column) {
		field[row - 1][column - 1].tryNumber(number);
	}

	public boolean numberHasBeenTried(final int number, final int row, final int column) {
		return field[row - 1][column - 1].isTried(number);
	}

	public int numberOfTriedNumbers(final int row, final int column) {
		return field[row - 1][column - 1].numberOfTried();
	}

	public boolean checkNumberBox(final int number, final int row, final int column) {
		int r = row, c = column;
		if (r % blockSize == 0) {
			r -= blockSize - 1;
		} else {
			r = (r / blockSize) * blockSize + 1;
		}
		if (c % blockSize == 0) {
			c -= blockSize - 1;
		} else {
			c = (c / blockSize) * blockSize + 1;
		}
		for (int i = r; i < r + blockSize; ++i) {
			for (int j = c; j < c + blockSize; ++j) {
				if (field[i - 1][j - 1].isFilled() && (field[i - 1][j - 1].get() == number)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean checkNumberRow(final int number, final int row) {
		for (int i = 0; i < fieldSize; ++i) {
			if (field[row - 1][i].isFilled() && field[row - 1][i].get() == number) {
				return false;
			}
		}
		return true;
	}

	public boolean checkNumberColumn(final int number, final int column) {
		for (int i = 0; i < fieldSize; ++i) {
			if (field[i][column - 1].isFilled() && field[i][column - 1].get() == number) {
				return false;
			}
		}
		return true;
	}

	public boolean checkNumberField(final int number, final int row, final int column) {
		return (checkNumberBox(number, row, column)
				&& checkNumberRow(number, row)
				&& checkNumberColumn(number, column));
	}

	public int numberOfPossibleVariants(final int row, final int column) {
		int result = 0;
		for (int i = 1; i <= fieldSize; ++i) {
			if (checkNumberField(i, row, column)) {
				++result;
			}
		}
		return result;
	}

	public boolean isCorrect() {
		for (int i = 0; i < fieldSize; ++i) {
			for (int j = 0; j < fieldSize; ++j) {
				if (field[i][j].isFilled()) {
					int value = field[i][j].get();
					field[i][j].hide();
					boolean correct = checkNumberField(value, i + 1, j + 1);
					field[i][j].show();
					if (!correct) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public Index nextCell(final int row, final int column) {
		int r = row, c = column;
		if (c < fieldSize) {
			++c;
		} else {
			c = 1;
			++r;
		}
		return new Index(r, c);
	}

	public Index cellWithMinVariants() {
		int r = 1, c = 1, min = 9;
		for (int i = 1; i <= fieldSize; ++i) {
			for (int j = 1; j <= fieldSize; ++j) {
				if (!field[i - 1][j - 1].isFilled()) {
					if (numberOfPossibleVariants(i, j) < min) {
						min = numberOfPossibleVariants(i, j);
						r = i;
						c = j;
					}
				}
			}
		}
		return new Index(r, c);
	}

	public int getRandomIndex() {
		return (int) (Math.random() * 10) % fieldSize + 1;
	}
}