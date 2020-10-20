package it.unisa.studenti.porcelli.j.sudoku;

/**
Copyright 2017 Universita' degli Studi di Salerno
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 A P2P Sudoku challege games. Each user can place a number of the
 sudoku game, if it is not already placed takes 1 point, if it is already placed and it is rights takes
 0 point, in other case receive -1 point. The games is based on 9 x 9 matrix.
 All users that play to a game are automatically informed when a users increment its score, and when
 the game is finished.
*/

public interface SudokuGame {
	/**
	 * Creates a new sudoku game.
	 * @param _game_name a String, the sudoku game name.
	 * @return integer matrix containing the grid of the sudoku game created.
	 */
	public Integer[][] generateNewSudoku(String _game_name);
	
	
	/**
	 * Joins in a game.
	 * @param _game_name a String, the sudoku game name.
	 * @param _nickname a String, the name of the user.
	 * @return true if the join is successful, false otherwise.
	 */
	public boolean join(String _game_name, String _nickname);
	
	
	/**
	 * Gets the Sudoku matrix game, with only the number placed by the user.
	 * @param _game_name a String, the sudoku game name.
	 * @return the integer matrix of the sudoku game.
	 */
	public Integer[][] getSudoku(String _game_name);
	
	
	/**
	 * Places a new solution number in the game.
	 * @param _game_name a String, the sudoku game name.
	 * @param _i the position on the row.
	 * @param _j the position on the column.
	 * @param _number the solution number.
	 * @return the integer score of the placed number.
	 */
	public Integer placeNumber(String _game_name, int _i, int _j, int _number);
	
	
}