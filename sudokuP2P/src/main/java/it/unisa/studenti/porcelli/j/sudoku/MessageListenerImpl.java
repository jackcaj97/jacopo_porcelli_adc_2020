package it.unisa.studenti.porcelli.j.sudoku;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

public class MessageListenerImpl implements MessageListener {

	int peerid;
	
	public MessageListenerImpl(int peerid)
	{
		this.peerid=peerid;

	}
	
	public Object parseMessage(Object obj) {
		
		TextIO textIO = TextIoFactory.getTextIO();
		TextTerminal terminal = textIO.getTextTerminal();
		terminal.printf("\n"+peerid+"] (Direct Message Received) "+obj+"\n\n");
		return "success";
	}

}
