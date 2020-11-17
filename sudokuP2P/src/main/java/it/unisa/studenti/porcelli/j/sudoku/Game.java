package it.unisa.studenti.porcelli.j.sudoku;

import java.io.IOException;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
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
		
		Game game = new Game();
		final CmdLineParser parser = new CmdLineParser(game);  
		
		try  
		{  
			parser.parseArgument(args);  
			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
			SudokuGameImpl peer = new SudokuGameImpl(id, master, new MessageListenerImpl(id));
			
			terminal.printf("\nStarting peer id: %d on master node: %s\n",
					id, master);
			
			while(true) {
				printMenu(terminal);
				
				int option = textIO.newIntInputReader()
						.withMaxVal(6)
						.withMinVal(0)
						.read("Option");
				switch (option) {
				case 1:
					terminal.printf("\nEnter a name for the Sudoku game\n");
					String name = textIO.newStringInputReader()
					        .withDefaultValue("default-sudoku")
					        .read("Name:");
					terminal.printf("\nEnter the difficulty\n0- 'Storymode'\n1- Very Easy\n2- Easy\n3- Normal\n4- Hard\n5- Very Hard\n6- Insane (no, really... are you insane? :o)\n\n");
					String difficultyString = textIO.newStringInputReader()
					        .withDefaultValue("1")
					        .read(" Difficulty:");
					int difficulty;
					try {
						difficulty = Integer.parseInt(difficultyString);
					}
					catch(NumberFormatException e) {
						terminal.printf("\nError in difficulty selection. Insert the corresponding number next time.\n");
						break;
					}
					
					if(difficulty < 0 || difficulty > 6)
						terminal.printf("\n- Error: choose an acceptable difficulty from the list\n", name);
					else if((peer.generateNewSudokuImproved(name, difficulty)) != null)
						terminal.printf("\n- Sudoku game %s successfully created\n", name);
					else
						terminal.printf("\n- Error in the creation of the Sudou game %d, it may already exist.\n", name);
					break;
				case 2:
					terminal.printf("\nEnter the name of the Sudoku game you wish to join\n");
					String sname = textIO.newStringInputReader()
					        .withDefaultValue("default-sudoku")
					        .read("Name:");
					terminal.printf("\nEnter your nickname\n");
					String nickname = textIO.newStringInputReader()
					        .withDefaultValue("default-nickname")
					        .read("Nickname:");
					
					if(peer.join(sname, nickname))
						terminal.printf("\n- Successfully joined game %s as %s\n", sname, nickname);
					else
						terminal.printf("\n- Error in joining the game %s\n", sname);
					break;
				case 3:
					terminal.printf("\nEnter the Sudoku game you want to see\n");
					String uname = textIO.newStringInputReader()
					        .withDefaultValue("default-sudoku")
					        .read("Name:");
					if(peer.getSudoku(uname) != null)
						terminal.printf("\n- Successfully retrieved game %s\n", uname);
					else
						terminal.printf("\n- Error in retrieving game %s\n", uname);
					break;
				case 4:
					terminal.printf("\nEnter the Sudoku name\n");
					String tname = textIO.newStringInputReader()
					        .withDefaultValue("default-sudoku")
					        .read(" Name:");
					terminal.printf("\nEnter the number to place\n");
					String numberString = textIO.newStringInputReader()
					        .withDefaultValue("1")
					        .read(" Number:");
					int number;
					try {
						number = Integer.parseInt(numberString);
					}
					catch(NumberFormatException e) {
						terminal.printf("\nError in number selected! Insert a number next time!\n");
						break;
					}
					
					int i, j;
					terminal.printf("\nEnter the position\n");
					String iString = textIO.newStringInputReader()
					        .withDefaultValue("1")
					        .read(" Row:");
					String jString = textIO.newStringInputReader()
					        .withDefaultValue("1")
					        .read(" Column:");
					try {
						i = Integer.parseInt(iString);
						j = Integer.parseInt(jString);
					}
					catch(NumberFormatException e) {
						terminal.printf("\nError in position selected! Insert an acceptable position!\n");
						break;
					}
					
					if(number < 1 || number > 9)
						terminal.printf("\n- Error: The number must be between 1 and 9.\n");
					else if(i < 1 || i > 9 || j < 1 || j > 9)
						terminal.printf("\n- Error: The position's coordinates must be between 1 and 9.\n");
					else {
						int score = peer.placeNumber(tname, i, j, number);
						
						if(score != -2) {	// Error flag is -2.
							switch(score) {
							case 1: 
								terminal.printf("\n- Successfully placed number in %s. You scored 1 point!\n", tname);
								break;
							case 0:
								terminal.printf("\n- Number already placed in %s. You scored 0 points.\n", tname);
								break;
							case -1:
								terminal.printf("\n- Oh no. This number is wrong in %s. You lost 1 point...\n", tname);
								break;
							default:
								terminal.printf("\n- You shoudn't be here...Who brought you to this forsaken place?\n");
								break;
							}
						}
						else
							terminal.printf("\n- Error in placing the number %d in (%d, %d)\n", number, i, j);
					}

					break;
				case 5:
					terminal.printf("\nEnter the name of the Sudoku game you wish to leave\n");
					String lname = textIO.newStringInputReader()
					        .withDefaultValue("default-sudoku")
					        .read("Name:");
					if(peer.leaveGame(lname))
						terminal.printf("\n- Successfully left game %s\n", lname);
					else
						terminal.printf("\n- Error in leaving the game %s\n", lname);
					break;
				case 6:
					terminal.printf("\nAre you sure you want to leave the Network?\n");
					boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
					if(exit) {
						peer.leaveNetwork();
						System.exit(0);
					}
					break;

				default:
					break;
				}
			}

		}  
		catch (CmdLineException clEx)  
		{  
			System.err.println("- ERROR: Unable to parse command-line options: " + clEx);  
		}
	}
	
	public static void printMenu(TextTerminal terminal) {
		terminal.printf("\n1 - Create a new Sudoku game\n");
		terminal.printf("\n2 - Join a Sudoku game\n");
		terminal.printf("\n3 - Get a Sudoku board\n");
		terminal.printf("\n4 - Place a number\n");
		terminal.printf("\n5 - Leave a game\n");
		terminal.printf("\n6 - EXIT\n");
	}
	
}
