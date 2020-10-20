package it.unisa.studenti.porcelli.j.sudoku;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

import it.unisa.studenti.porcelli.j.sudoku.MessageListener;
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
	
	final private ArrayList<Integer[][]> s_games=new ArrayList<Integer[][]>();
	
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
		
		return new Integer[1][1];
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
	
}
