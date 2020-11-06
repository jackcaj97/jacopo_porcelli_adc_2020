package it.unisa.studenti.porcelli.j.sudoku;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

import it.unisa.studenti.porcelli.j.sudoku.MessageListener;
import it.unisa.studenti.porcelli.j.sudoku.board.BoardManager;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class SudokuGameImpl implements SudokuGame {
	
	final private Peer peer;
	final private PeerDHT _dht;
	final private int DEFAULT_MASTER_PORT=4000;
	
	final private int ROW_SUDOKU = 9;
	final private int COL_SUDOKU = 9;
	
	final private String players_game_name = "_players";	// String used for the players' list that joined a certain game.
	
	final private ArrayList<String> j_games=new ArrayList<String>();	// List of joined sudoku game boards.
	final private ArrayList<String> j_games_nick=new ArrayList<String>();	// List of joined sudoku game boards' nicknames used.
	
	private int difficultyToApply;
	
	public SudokuGameImpl( int _id, String _master_peer, final MessageListener _listener) throws Exception {
		peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
		_dht = new PeerBuilderDHT(peer).start();
		
		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		} else {
			throw new Exception("Error in master peer bootstrap.");
		}
		
		peer.objectDataReply(new ObjectDataReply() {
			public Object reply(PeerAddress sender, Object request) throws Exception {
				return _listener.parseMessage(request);
			}
		});
	}
	
	
	public Integer[][] generateNewSudoku(String _game_name) {
		
		BoardManager board = new BoardManager(_game_name);
		
		Integer[][] sudokuBoard = board.getAsMatrix();
		
		board.applyDifficulty(sudokuBoard, this.difficultyToApply);	// Applicazione della difficoltà sul sudoku.
		
		try {
			// Board creation in the DHT.
			FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty()) 
				_dht.put(Number160.createHash(_game_name)).data(new Data(sudokuBoard)).start().awaitUninterruptibly();
			
			// Players list creation in the DHT.
			futureGet = _dht.get(Number160.createHash(_game_name + players_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty())
				_dht.put(Number160.createHash(_game_name + players_game_name)).data(new Data(new HashSet<PeerAddress>())).start().awaitUninterruptibly();
			
			// TODO: remove
			//board.printBoard();
			board.printMatrix(sudokuBoard);
			
			return sudokuBoard;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Creates a new sudoku game with a selected difficulty.
	 * @param _game_name a String, the sudoku game name.
	 * @param _difficulty integer representing the difficulty of the sudoku board.
	 * @return BoardManager object containing the grid field of the sudoku game created.
	 */
	public Integer[][] generateNewSudokuImproved(String _game_name, int _difficulty) {
		
		this.difficultyToApply = _difficulty;
		
		Integer[][] sudokuBoard = generateNewSudoku(_game_name);
		
		return sudokuBoard;
	}
	
	
	
	public boolean join(String _game_name, String _nickname) {
		
		
		//TODO: Prelevare la board e la lista di players dalla dht
		//TODO: inserire il player nella lista della dht e nella lista della board.
		//TODO: aggiornare board e listaPlayers nella dht.
		
		return false;
	}
	
	
	
	public Integer[][] getSudoku(String _game_name) {
		return null;
	}
	
	
	
	public Integer placeNumber(String _game_name, int _i, int _j, int _number) {
		return 0;
	}
	
	/**
	 * Allows to leave a certain game, so that the peer doesn't receive notifications about it anymore.
	 * @param _game_name String of the name of the game to leave.
	 * @return true if the game is successfully left, false otherwise.
	 */
	public boolean leaveGame(String _game_name) {
		
		// TODO: Rimuoversi dalla lista di players associati ad un dato game_name (game_name_players)
		
		return false;
	}
	
	public boolean leaveNetwork() {
		for(String game: new ArrayList<String>(j_games)) 
			leaveGame(game);
		
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
		
		return true;
	}
	
}
