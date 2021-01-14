package it.unisa.studenti.porcelli.j.sudoku;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import it.unisa.studenti.porcelli.j.sudoku.board.BoardManager;

public class SudokuGameImplTest {
	
	private static SudokuGameImpl p0, p1, p2, p3;
	private static BoardManager bManager;

	static class MessageListenerImpl implements MessageListener {
		int peerid;

		public MessageListenerImpl(int peerid) {
			this.peerid = peerid;
		}

		public Object parseMessage(Object obj) {
			System.out.println("\n["+peerid+"] (Direct Message Received) "+obj+"\n\n");
			return "success";
		}
	}

	@BeforeAll
	static void setup() throws Exception {

		p0 = new SudokuGameImpl(0, "127.0.0.1", new MessageListenerImpl(0));
		p1 = new SudokuGameImpl(1, "127.0.0.1", new MessageListenerImpl(1));
		p2 = new SudokuGameImpl(2, "127.0.0.1", new MessageListenerImpl(2));
		p3 = new SudokuGameImpl(3, "127.0.0.1", new MessageListenerImpl(3));
		
		bManager = new BoardManager();
	}

	@Test
	void testGenerateNewSudoku() {
		assertNotNull(p1.generateNewSudoku("generateGame1"), "game1 not created");
        assertNull(p2.generateNewSudoku("generateGame1"), "created a game that already existed [game1]");	// Cannot create a sudoku with the same name.
        assertNotNull(p3.generateNewSudoku("generateGame2"), "game2 not created");
	}

	@Test
	void testJoin() {
		// Join in a game not created.
		assertFalse(p0.join("joinGame1", "nickname0"), "joined a game not created yet");
		
		p0.generateNewSudoku("joinGame1");
		assertTrue(p0.join("joinGame1", "nickname0"), "game not joined as intended");
		
		// Join with a nickname already used.
		assertFalse(p1.join("joinGame1", "nickname0"), "joined a game with an already used nickname");
		assertTrue(p1.join("joinGame1", "nickname1"), "game not joined as intended with an unused nickname");
		
		assertTrue(p0.getGamesJoined().contains("joinGame1"), "p0 correctly joined the game");
		assertTrue(p1.getGamesJoined().contains("joinGame1"), "p1 correctly joined the game");
	}

	@Test
	void testGetSudoku() {
		assertNull(p0.getSudoku("getGame1"), "managed to get a non-existent game");	// Try to get a not yet created game.
		
		p0.generateNewSudoku("getGame1");
		assertNotNull(p1.getSudoku("getGame1"), "didn't get a created game as intended");
	}

	@Test
	void testPlaceNumber() {
		// Placing a number in a non existing game.
		assertEquals(-2, p0.placeNumber("placeGame1", 1, 1, 1));
		
		p0.generateNewSudoku("placeGame1");
		
		// Placing a number in a game not joined.
		assertEquals(-2, p1.placeNumber("placeGame1", 1, 1, 1));
		
		p0.join("placeGame1", "nickname0");
		p1.join("placeGame1", "nickname1");
		
		Integer[][] board = p0.getSudoku("placeGame1");
		int iFilled = 0, jFilled = 0;
		int iEmpty = 0, jEmpty = 0;
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				if(board[i][j] != 0) {
					iFilled = i;
					jFilled = j;
				}
				else {
					iEmpty = i;
					jEmpty = j;
				}
			}
		}
		
		// Placing a number in an already filled cell of the sudoku.
		assertEquals(0, p0.placeNumber("placeGame1", iFilled+1, jFilled+1, 1));
		
		// Placing a wrong number in an empty cell.
		int num = 1;
		for(num = 1; num < 10; num++) {
			if(!bManager.checkPosition(board, iEmpty, jEmpty, num))
				break;
		}
		assertEquals(-1, p1.placeNumber("placeGame1", iEmpty+1, jEmpty+1, num));
		
		// Placing the right number in an empty cell.
		for(num = 1; num < 10; num++) {
			if(bManager.checkPosition(board, iEmpty, jEmpty, num))
				break;
		}
		assertEquals(1, p0.placeNumber("placeGame1", iEmpty+1, jEmpty+1, num));
	}

	@Test
	void testLeaveGame() {
		assertFalse(p0.leaveGame("leaveGame1"), "p0 left a game not joined");
		
		p0.generateNewSudoku("leaveGame1");
		p0.join("leaveGame1", "nickname0");		// p0 joined 1 game
		p1.generateNewSudoku("leaveGame2");
		p1.join("leaveGame1", "nickname1");
		p1.join("leaveGame2", "nickname1");		// p1 joined 2 games
		
		int joinedP0 = p0.getGamesJoined().size();
		int joinedP1 = p1.getGamesJoined().size();
		
		assertTrue(p0.getGamesJoined().contains("leaveGame1"));
		assertTrue(p1.getGamesJoined().contains("leaveGame1"));
		assertTrue(p1.getGamesJoined().contains("leaveGame2"));
		
		assertTrue(p0.leaveGame("leaveGame1"), "p0 didn't leave game as intended");
		assertTrue(p1.leaveGame("leaveGame1"), "p1 didn't leave game as intended");
		
		assertFalse(p0.getGamesJoined().contains("leaveGame1"), "p0 didn't leave leaveGame1");
		assertFalse(p1.getGamesJoined().contains("leaveGame1"), "p1 didn't leave leaveGame1");
		
		assertEquals((joinedP0-1), p0.getGamesJoined().size());
		assertEquals((joinedP1-1), p1.getGamesJoined().size());
	}
	
	
	
	public void leaveGames(SudokuGameImpl peer) {
		ArrayList<String> games = new ArrayList<String>();
		peer.getGamesJoined();
		for(String game : games) {
			peer.leaveGame(game);
		}
	}
	
	@AfterEach
    void leaveAll() {
        leaveGames(p0);
        leaveGames(p1);
        leaveGames(p2);
        leaveGames(p3);
    }

	@AfterAll
	static void leaveNetwork() {
		assertTrue(p0.leaveNetwork(), "p0 didn't leave the network");
		assertTrue(p1.leaveNetwork(), "p0 didn't leave the network");
		assertTrue(p2.leaveNetwork(), "p0 didn't leave the network");
		assertTrue(p3.leaveNetwork(), "p0 didn't leave the network");
		
		assertEquals(0, p0.getGamesJoined().size(), "p0 still has some games joined");
		assertEquals(0, p1.getGamesJoined().size(), "p1 still has some games joined");
		assertEquals(0, p2.getGamesJoined().size(), "p2 still has some games joined");
		assertEquals(0, p3.getGamesJoined().size(), "p3 still has some games joined");
	}

}
