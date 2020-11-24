package it.unisa.studenti.porcelli.j.sudoku;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

import it.unisa.studenti.porcelli.j.sudoku.MessageListener;
import it.unisa.studenti.porcelli.j.sudoku.board.BoardManager;
import it.unisa.studenti.porcelli.j.sudoku.board.SudokuPanel;
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
	final private ArrayList<SudokuPanel> sudokuPanels = new ArrayList<SudokuPanel>();
	
	private int difficultyToApply;
	private BoardManager bManager;	// To handle everything related to the sudoku board.
	
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
				_dht.put(Number160.createHash(_game_name + scores_game_name)).data(new Data(new ArrayList<Integer>())).start().awaitUninterruptibly();
			
			// TODO: remove
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
			
			// Adding the nickname to the list of nicks for the sudoku game.
			FutureGet futureGet = _dht.get(Number160.createHash(_game_name + nicks_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
							
				ArrayList<String> nicknames_of_game;
				nicknames_of_game = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
				if(nicknames_of_game.contains(_nickname))
					return false;
				nicknames_of_game.add(_nickname);	// Adds the nickname chosen to the list of nicks for that game.
				_dht.put(Number160.createHash(_game_name + nicks_game_name)).data(new Data(nicknames_of_game)).start().awaitUninterruptibly();
			}
			
			// Fetching the sudoku game board.
			futureGet = _dht.get(Number160.createHash(_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
				
				// Fetch of the board from the dht.
				sudokuBoard = (Integer[][]) futureGet.dataMap().values().iterator().next().object();
				
				j_games_nick.add(_nickname);
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
				sudokuPanels.add(bManager.printMatrix(sudokuBoard));
			
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	
	public Integer[][] getSudoku(String _game_name) {
		
		try {
			Integer[][] sudokuBoard = null;
			
			// Fetching the sudoku game board.
			FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return null;
				
				// Fetch of the board from the dht.
				sudokuBoard = (Integer[][]) futureGet.dataMap().values().iterator().next().object();
				
				return sudokuBoard;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public Integer placeNumber(String _game_name, int _i, int _j, int _number) {
		
		_i -= 1;
		_j -= 1;
		
		// Check whether the game has been joined. Cannot place a number in a game that hasn't been joined yet.
		if(!j_games_names.contains(_game_name))
			return -2;
		
		try {
			Integer[][] sudokuBoard = getSudoku(_game_name);
			
			if(sudokuBoard != null) {
				int panelIndex = j_games_names.indexOf(_game_name);
				int score = bManager.placeNumInMatrix(sudokuBoard, _i, _j, _number, sudokuPanels.get(panelIndex));
				
				if(score == 1) {	// Update the matrix and the score for the leaderboard.
					
					// Fetch the position and the nickname used for this game.
					int localIndex = j_games_names.indexOf(_game_name);	// Index of local arraylists.
					String nicknameUsed = j_games_nick.get(localIndex);
					int globalIndex = -1;	// Index in arrays on the dht.
					
					// Fetching the nicknames' list.
					FutureGet futureGet = _dht.get(Number160.createHash(_game_name + nicks_game_name)).start();
					futureGet.awaitUninterruptibly();
					if (futureGet.isSuccess()) {
						if(futureGet.isEmpty()) 
							return -2;
									
						ArrayList<String> nicknames_of_game;
						nicknames_of_game = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
						globalIndex = nicknames_of_game.indexOf(nicknameUsed);
						
						if(globalIndex == -1)	// Something went wrong.
							return -2;
						
					}
					
					// Updating the sudoku game board on the dht.
					futureGet = _dht.get(Number160.createHash(_game_name)).start();
					futureGet.awaitUninterruptibly();
					if (futureGet.isSuccess()) {
						if(futureGet.isEmpty()) 
							return -2;
						
						_dht.put(Number160.createHash(_game_name)).data(new Data(sudokuBoard)).start().awaitUninterruptibly();
					}
					
					// Updating the personal score on the list for that sudoku game.
					futureGet = _dht.get(Number160.createHash(_game_name + scores_game_name)).start();
					futureGet.awaitUninterruptibly();
					if (futureGet.isSuccess()) {
						if(futureGet.isEmpty()) 
							return -2;
						
						ArrayList<Integer> scores_of_game;
						scores_of_game = (ArrayList<Integer>) futureGet.dataMap().values().iterator().next().object();
						scores_of_game.set(globalIndex, scores_of_game.get(globalIndex) + score);		// Adds the starting score to the list of scores for that game.
						_dht.put(Number160.createHash(_game_name + scores_game_name)).data(new Data(scores_of_game)).start().awaitUninterruptibly();
					}
					
					// Notify all the players for that Sudoku about the new score.
					futureGet = _dht.get(Number160.createHash(_game_name + players_game_name)).start();
					futureGet.awaitUninterruptibly();
					if (futureGet.isSuccess()) {
						
						String message = "";
						// If the board has just been completed.
						if(bManager.isCompleted(sudokuBoard))
							message = _game_name + " - " + nicknameUsed + " has just completed the sudoku!";
						else
							message = _game_name + " - " + nicknameUsed + " has just scored a point!";
						
						HashSet<PeerAddress> peers_on_topic;
						peers_on_topic = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
						for(PeerAddress peer:peers_on_topic)
						{
							FutureDirect futureDirect = _dht.peer().sendDirect(peer).object(message).start();
							futureDirect.awaitUninterruptibly();
						}
					}
				}
				return score;
			}
			else {
				return -2;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return -2;
	}
	
	/**
	 * Allows to leave a certain game, so that the peer doesn't receive notifications about it anymore.
	 * @param _game_name String of the name of the game to leave.
	 * @return true if the game is successfully left, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public boolean leaveGame(String _game_name) {
		
		try {
			
			// Removes itself from the list of peers playing this sudoku game.
			FutureGet futureGet = _dht.get(Number160.createHash(_game_name + players_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
				HashSet<PeerAddress> players_peers_of_game;
				players_peers_of_game = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
				players_peers_of_game.remove(_dht.peer().peerAddress());
				_dht.put(Number160.createHash(_game_name + players_game_name)).data(new Data(players_peers_of_game)).start().awaitUninterruptibly();
			}
			
			int indexToDeleteLocal = j_games_names.indexOf(_game_name);	// Index for the local lists.
			String nicknameToDelete = j_games_nick.get(indexToDeleteLocal);
			int indexToDelete = 0;	// Index for the global lists.
			
			// Removes itself from the list of nicknames of players playing this sudoku game.
			futureGet = _dht.get(Number160.createHash(_game_name + nicks_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
				ArrayList<String> nicks_of_game;
				nicks_of_game = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
				indexToDelete = nicks_of_game.indexOf(nicknameToDelete);	// Posizione del nickname nella lista sulla dht.
				nicks_of_game.remove(indexToDelete);
				_dht.put(Number160.createHash(_game_name + nicks_game_name)).data(new Data(nicks_of_game)).start().awaitUninterruptibly();
			}
			
			// Removes itself from the list of scores of players playing this sudoku game.
			futureGet = _dht.get(Number160.createHash(_game_name + scores_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if(futureGet.isEmpty()) 
					return false;
				ArrayList<Integer> scores_of_game;
				scores_of_game = (ArrayList<Integer>) futureGet.dataMap().values().iterator().next().object();
				scores_of_game.remove(indexToDelete);
				_dht.put(Number160.createHash(_game_name + scores_game_name)).data(new Data(scores_of_game)).start().awaitUninterruptibly();
			}
			
			// Removes the game's traces from the local lists.
			j_games.remove(indexToDeleteLocal);
			j_games_names.remove(indexToDeleteLocal);
			j_games_nick.remove(indexToDeleteLocal);
			
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean leaveNetwork() {
		for(String game: new ArrayList<String>(j_games_names)) 
			leaveGame(game);
		
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
		
		return true;
	}
	
}
