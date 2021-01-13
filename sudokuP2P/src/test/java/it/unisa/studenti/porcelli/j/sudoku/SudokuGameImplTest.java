package it.unisa.studenti.porcelli.j.sudoku;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class SudokuGameImplTest {
	
	private static SudokuGameImpl p0, p1, p2, p3;
	
	public SudokuGameImplTest() {
		
	}
	
	 @BeforeAll
	 static void createPeers() throws Exception {
		 
		 class MessageListenerImpl implements MessageListener{
			 int peerid;

			 public MessageListenerImpl(int peerid) {
				 this.peerid = peerid;
			 }

			 public Object parseMessage(Object obj) {
				 System.out.println("\n["+peerid+"] (Direct Message Received) "+obj+"\n\n");
				 return "success";
			 }
		 }

		 p0 = new SudokuGameImpl(0, "127.0.0.1", new MessageListenerImpl(0));
		 p1 = new SudokuGameImpl(1, "127.0.0.1", new MessageListenerImpl(1));
		 p2 = new SudokuGameImpl(2, "127.0.0.1", new MessageListenerImpl(2));
		 p3 = new SudokuGameImpl(3, "127.0.0.1", new MessageListenerImpl(3));
	 }

	@Test
	void testGenerateNewSudoku() {
		assertNotNull(p1.generateNewSudoku("generateGame1"), "game1 not created");
        assertNull(p2.generateNewSudoku("generateGame1"), "created a game that already existed [game1]");
        assertNotNull(p3.generateNewSudoku("generateGame2"), "game2 not created");
	}

	@Test
	void testJoin() {
		//fail("Not yet implemented");
	}

	@Test
	void testGetSudoku() {
		//fail("Not yet implemented");
	}

	@Test
	void testPlaceNumber() {
		//fail("Not yet implemented");
	}

	@Test
	void testLeaveGame() {
		//fail("Not yet implemented");
	}
	
	public void leaveGames(SudokuGameImpl peer) {
		ArrayList<String> games = new ArrayList<String>();
		peer.getGamesJoined();
		for(String game : games) {
			peer.leaveGame(game);
		}
	}
	
	@AfterEach
    public void leaveAll() {
        leaveGames(p0);
        leaveGames(p1);
        leaveGames(p2);
        leaveGames(p3);
    }

	@AfterAll
	void leaveNetwork() {
		fail("Not yet implemented");
	}

}
