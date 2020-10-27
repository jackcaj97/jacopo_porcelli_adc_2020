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
	
	final private String players_game_name = "_players";	// String used for the players' list that joined a certain game.
	
	final private ArrayList<String> j_games=new ArrayList<String>();	// List of joined sudoku game boards.
	
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
	
	
	public BoardManager generateNewSudoku(String _game_name, int _difficulty) {
		
		BoardManager board = new BoardManager(_game_name, _difficulty);
		
		try {
			// Board creation in the DHT.
			FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty()) 
				_dht.put(Number160.createHash(_game_name)).data(new Data(board)).start().awaitUninterruptibly();
			
			// Players list creation in the DHT.
			futureGet = _dht.get(Number160.createHash(_game_name + players_game_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty())
				_dht.put(Number160.createHash(_game_name + players_game_name)).data(new Data(new HashSet<PeerAddress>())).start().awaitUninterruptibly();
			
			// TODO: remove
			board.printBoard();
			return board;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public boolean join(String _game_name, String _nickname) {
		
		return false;
	}
	
	
	
	public Integer[][] getSudoku(String _game_name) {
		return new Integer[1][1];
	}
	
	
	
	public Integer placeNumber(String _game_name, int _i, int _j, int _number) {
		return 0;
	}
	
	public boolean leaveNetwork() {
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
		
		return true;
	}
	
}
