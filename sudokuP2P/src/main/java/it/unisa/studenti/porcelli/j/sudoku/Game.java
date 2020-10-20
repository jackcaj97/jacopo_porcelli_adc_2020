package it.unisa.studenti.porcelli.j.sudoku;

import java.io.IOException;

import org.kohsuke.args4j.Option;

import it.unisa.studenti.porcelli.j.sudoku.board.BoardManager;
import it.unisa.studenti.porcelli.j.sudoku.board.Index;
import it.unisa.studenti.porcelli.j.sudoku.board.SudokuField;

public class Game {
	@Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;

	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;

	public static void main(String[] args) throws Exception {
		
		BoardManager board = new BoardManager();
		board.printBoard();
	}
	
}
