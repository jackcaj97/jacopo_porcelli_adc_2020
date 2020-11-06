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
	
	final private String players_game_name = "_players";	// String used for the players list of peers that joined a certain game.
	final private String nicks_game_name = "_nicknames";	// String used for the nicknames list.
	final private String scores_game_name = "_scores";		// String used for the scores list.
	
	final private ArrayList<Integer[][]> j_games=new ArrayList<Integer[][]>();	// List of joined sudoku game boards.
	final private ArrayList<String> j_games_names=new ArrayList<String>();	// List of joined sudoku game boards' names.
	final private ArrayList<String> j_games_nick=new ArrayList<String>();	// List of joined sudoku game boards' nicknames used.
	
	private int difficultyToApply;
	private BoardManager bManager;
	
	public SudokuGameImpl( int _id, String _master_peer, final MessageListener _listener) throws Exception {
		bManager = new BoardManager();
		
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
			else	// Sudoku game with _game_name already exists eventually.
				return null;
			
			// Players peers list creation in the DHT.
			futureGet = _dht.get(Number160.createHash(_game_name + players_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty())
				_dht.put(Number160.createHash(_game_name + players_game_name)).data(new Data(new HashSet<PeerAddress>())).start().awaitUninterruptibly();
			
			// Players nicknames list creation in the DHT.
			futureGet = _dht.get(Number160.createHash(_game_name + nicks_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty())
				_dht.put(Number160.createHash(_game_name + nicks_game_name)).data(new Data(new ArrayList<String>())).start().awaitUninterruptibly();
			
			// Players scores list creation in the DHT.
			futureGet = _dht.get(Number160.createHash(_game_name + scores_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty())
				_dht.put(Number160.createHash(_game_name + scores_game_name)).data(new Data(new ArrayList<PeerAddress>())).start().awaitUninterruptibly();
			
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
	
	
	
	@SuppressWarnings("unchecked")
	public boolean join(String _game_name, String _nickname) {
		
		// Check whether the game is already joined.
		if(j_games_names.contains(_game_name))
			return false;
		
		try {
			Integer[][] sudokuBoard = null;
			
			// Fetching the sudoku game board.
			FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
				
				// Fetch of the board and the associated players' list from the dht.
				sudokuBoard = (Integer[][]) futureGet.dataMap().values().iterator().next().object();
				
				j_games.add(sudokuBoard);	// Appends the fetched game to the list of joined games.
				j_games_names.add(_game_name);	// Appends the game's name to the list of joined games.
			}
			
			// Adding the peer to the list for that sudoku game, to be notified eventually.
			futureGet = _dht.get(Number160.createHash(_game_name + players_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
				
				HashSet<PeerAddress> players_peers_of_game;
				players_peers_of_game = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
				players_peers_of_game.add(_dht.peer().peerAddress());	// Adds itself to the peers list for that game.
				_dht.put(Number160.createHash(_game_name + players_game_name)).data(new Data(players_peers_of_game)).start().awaitUninterruptibly();
			}
			
			// Adding the nickname to the list of nicks for the sudoku game.
			futureGet = _dht.get(Number160.createHash(_game_name + nicks_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
				
				ArrayList<String> nicknames_of_game;
				nicknames_of_game = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
				nicknames_of_game.add(_nickname);	// Adds the nickname chosen to the list of nicks for that game.
				_dht.put(Number160.createHash(_game_name + nicks_game_name)).data(new Data(nicknames_of_game)).start().awaitUninterruptibly();
			}
			
			// Adding a new personal score on the list for that sudoku game.
			futureGet = _dht.get(Number160.createHash(_game_name + scores_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
				
				ArrayList<Integer> scores_of_game;
				scores_of_game = (ArrayList<Integer>) futureGet.dataMap().values().iterator().next().object();
				scores_of_game.add(0);	// Adds the starting score to the list of scores for that game.
				_dht.put(Number160.createHash(_game_name + scores_game_name)).data(new Data(scores_of_game)).start().awaitUninterruptibly();
			}
			
			if(sudokuBoard != null)
				bManager.printMatrix(sudokuBoard);
			
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
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
		for(String game: new ArrayList<String>(j_games_names)) 
			leaveGame(game);
		
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
		
		return true;
	}
	
}
