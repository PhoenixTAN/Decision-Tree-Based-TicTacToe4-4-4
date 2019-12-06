import java.util.List;
import java.util.Random;

/**
 * Class: aiTicTacToe
 * 
 * @author TF in CS 640 at Boston University
 * @author Ziqi Tan
 * 
 * Function: 
 * This file contains a function called myAIAlgorithm() 
 * to decide the next move your AI is going to take. This method will
 * be called by runTicTacToe.java every time it's your AI's turn.
 * 
 * Important: This is the only file we need to submit !!!
 * 
 * */

public class aiTicTacToeSelf {
	
	public int player;  // 1 for player 1 and 2 for player 2
	
	/**
	 * Private variables
	 * @author Ziqi Tan
	 * */	
	private positionTicTacToe myNextMove = new positionTicTacToe(0, 0, 0);
	private int[] playerSequenceNum = new int[4];
	private int[] opponentSequenceNum = new int[4];	
	private byte[] curBoard = new byte[64];
	private long time1 = 0;
	private boolean searchFinished = true;
				
	/**
	 * Private static/final variables
	 * @author Ziqi Tan
	 * */
	private static byte[][] winningLine = new byte[76][4];
	private static final int[] playerSequenceValue = new int[] {1, 15, 70, 100000};
	private static final int[] opponentSequenceValue = new int[] {-1, -10, -50, -100000};

	private static final int timeOutAlert = 9800;   // ms return next move in 9 second
	private static final int timeEnough = 8000;     // ms
	private static final int miniMaxDepth = 4;
	
	private static final int[] corePoints = {
		21, 22, 25, 26, 37, 38, 41, 42
	};
	
	@SuppressWarnings("unused")
	private static final int[] cornerPoints = {
		0, 3, 12, 16, 48, 51, 60, 63
	};
	
	@SuppressWarnings("unused")
	private static final int[] otherPoints = {
		1, 2, 4, 5, 6, 7, 8, 9, 
		10, 11, 13, 14, 15, 17, 18, 19, 
		20, 23, 24, 27, 28, 29, 
		30, 31, 32, 33, 34, 35, 36, 39, 
		40, 43, 44, 45, 46, 47, 49, 
		50, 52, 53, 54, 55, 56, 57, 58, 59, 
		61, 62
	};
	
	private static final int[] traverseOrder = {
		21, 22, 25, 26, 37, 38, 41, 42,			
		0, 3, 12, 16, 48, 51, 60, 63,			
		1, 2, 4, 5, 6, 7, 8, 9, 
		10, 11, 13, 14, 15, 17, 18, 19, 
		20, 23, 24, 27, 28, 29, 
		30, 31, 32, 33, 34, 35, 36, 39, 
		40, 43, 44, 45, 46, 47, 49, 
		50, 52, 53, 54, 55, 56, 57, 58, 59, 
		61, 62
	};

	/**
	 * Constructor
	 * @author TF in CS 640 at Boston University
	 * Modified by Ziqi Tan:
	 * 		Initialize winningLines.
	 * */
	public aiTicTacToeSelf( int setPlayer ) {		
		player = setPlayer;
		this.getWinningLines();
	}
		
	/**
	 * Method: myAIAlgorithm
	 * @author TF in CS 640 at Boston University
	 * @author Modified by Ziqi Tan
	 * 
	 * Function: 
	 * 	Decide the next move your AI is going to take. 
	 * 	This method will be called by runTicTacToe.java every time it's your AI's turn.
	 * 
	 * @param 
	 * 		List<positionTicTacToe> board: current board configuration;
	 * 		int player: current Player's turn.
	 * @return 
	 * 		An instance of class positionTicTacToe(x, y, z).
	 * */
	public positionTicTacToe myAIAlgorithm(List<positionTicTacToe> board, int player) {
									
		try {
			time1 = System.currentTimeMillis();
			System.out.println("Player" + player + "' turn:");
			
			// transform the List<> into byte[].
			this.ByteBoard(board);
			System.out.println("Evaluation " + this.evaluation(player));
			
			// Detect wining lines, which means that you will win.			
			byte winMove = getWinMove(player);
			if( winMove != -1 ) {
				int[] xyz = oneDToxyz(winMove);
				System.out.println("Win move");
				System.out.println("myNextMove: " + xyz[0] + " " + xyz[1] + " " + xyz[2]);
				return new positionTicTacToe(xyz[0], xyz[1], xyz[2]);
			}
			
			// Detect three sequences, which means that you have to block your opponent.
			byte forceMove = getForceMove(player);
			if( forceMove != -1 ) {
				int[] xyz = oneDToxyz(forceMove);
				System.out.println("Force move.");
				System.out.println("myNextMove: " + xyz[0] + " " + xyz[1] + " " + xyz[2]);
				return new positionTicTacToe(xyz[0], xyz[1], xyz[2]);
			}
			
			// First two steps should occupy the strongest point.
			int coreMove = this.getFirstTwoStep(player);
			if( coreMove != -1 ) {
				int[] xyz = oneDToxyz((byte)coreMove);
				System.out.println("Core move");
				System.out.println("myNextMove: " + xyz[0] + " " + xyz[1] + " " + xyz[2]);
				return new positionTicTacToe(xyz[0], xyz[1], xyz[2]);
			}
						
			int depth = miniMaxDepth;
			int maxValue = Integer.MIN_VALUE;
			// Progressive deepening
			do {		
				System.out.println("Progressive deepening: depth = " + depth);
				
				// traverse every next move
				for( int j = 0; j < traverseOrder.length; j++ ) {
					int i = j;
					if( curBoard[i] == 0 ) {					
						// make move					
						curBoard[i] = (byte)player;		
						this.searchFinished = true;
						int newValue = miniMax(depth, player, false, Integer.MIN_VALUE, Integer.MAX_VALUE);	
						if( newValue > maxValue && this.searchFinished ) {
							// update max value
							maxValue = newValue;						
							// update my next best move
							int[] xyz = oneDToxyz((byte) i);
							myNextMove = new positionTicTacToe(xyz[0], xyz[1], xyz[2]);							
						}
						// cancel move
						curBoard[i] = 0;  // backtracking

					}
					long time2 = System.currentTimeMillis();	
					if( time2 - time1 > timeOutAlert ) {
						System.out.println("Time out alert!");
						System.out.println("myNextMove: " + myNextMove.x + " " + myNextMove.y + " " + myNextMove.z + " Value: " + maxValue);
						return myNextMove;
					}
				}
				depth++;
				if( depth >= (64 - isDraw()) ) {
					System.out.println("Depth overflow. MyNextMove: " + myNextMove.x + " " + myNextMove.y + " " + myNextMove.z + " Value: " + maxValue);
					return myNextMove;
				}
				
			} while( System.currentTimeMillis() - time1 < timeEnough );   // if you have enough time
			
			System.out.println("myNextMove: " + myNextMove.x + " " + myNextMove.y + " " + myNextMove.z + " Value: " + maxValue);
		
		}
		catch( Exception error ) {
			error.printStackTrace();
		}	
		
		return myNextMove;										
	}
	
	/**
	 * Method: miniMax
	 * @author Ziqi Tan
	 * */
	private int miniMax(int depth, int player, boolean maximizingPlayer, int alpha, int beta) {
		
		// time control
		long time2 = System.currentTimeMillis();
		if( time2 - time1 > timeOutAlert ) {
			if( depth > 0 ) {
				this.searchFinished = false;
			}
			return evaluation(player);
		}
		
		// search finish
		if( depth == 0 ) {
			return evaluation(player);
		}
				
		// draw 
		if( isDraw() == 64 ) {
			return 0;
		}
		
		if( maximizingPlayer ) {	
			/* Maximizer player */
			int value = Integer.MIN_VALUE;
			// for each child do a miniMax recursion
			for( int i = 0; i < traverseOrder.length; i++ ) {
				if( curBoard[i] == 0 ) {					
					// winMove pruning
					byte winMove = getWinMove(player);
					if( winMove != -1 ) {
						curBoard[winMove] = (byte) player;
						value = evaluation(player);
						curBoard[winMove] = 0;
						break;
					}					
					// force move pruning
					byte forceMove = getForceMove(player);
					if( forceMove != -1 ) {
						curBoard[forceMove] = (byte)player;
						// DFS and deepening 
						value = Math.max(value, miniMax(depth, player, false, alpha, beta));
						curBoard[forceMove] = 0;  // backtracking						
						break;
					}
					else {
						// naive miniMax part
						curBoard[i] = (byte)player;						
						// DFS
						value = Math.max(value, miniMax(depth - 1, player, false, alpha, beta));
						curBoard[i] = 0;  // backtracking						
						// beta pruning
						alpha = Math.max(alpha, value);
						if( alpha >= beta ) {
							break;  
						}
					}									
				}
			}
			// return the maximum value			
			return value;
		}
		else {
			/* minimizing player */
			int value = Integer.MAX_VALUE;
			int opponent = (player == 1 ? 2 : 1);
			
			// for each child do a miniMax recursion
			for( int i = 0; i < traverseOrder.length; i++ ) {
				if( curBoard[i] == 0 ) {

					// winMove pruning
					byte winMove = getWinMove(opponent);
					if( winMove != -1 ) {
						curBoard[winMove] = (byte) opponent;
						value = evaluation(player);
						curBoard[winMove] = 0;
						break;
					}
					
					// force move pruning
					byte forceMove = getForceMove(opponent);
					if( forceMove != -1 ) {
						curBoard[forceMove] = (byte)opponent;
						value = Math.min(value, miniMax(depth, player, true, alpha, beta));
						curBoard[forceMove] = 0;  // backtracking
						break;
					}
					else {
						// naive miniMax part
						curBoard[i] = (byte)opponent;
						
						// DFS
						value = Math.min(value, miniMax(depth - 1, player, true, alpha, beta));
						curBoard[i] = 0;   // backtracking
						
						// alpha pruning
						beta = Math.min(beta, value);
						if( alpha >= beta ) {
							break;  
						}
					}										
				}
			}
			// return the minimum value			
			return value;			
		}
	}
	
	/**
	 * Method: evaluation
	 * @author Ziqi Tan
	 * Function:
	 * 		A helper function to evaluate the current board situation.
	 * 		Use wining lines to count.
	 * @param
	 * 		List<positionTicTacToe> board: current board configuration;
	 * 		int player: current Player's turn.
	 * @return
	 * 		int value
	 * */
	private int evaluation(int player) {
		player = (byte)player;
		// initialize
		int opponent = (player == 1 ? 2 : 1);
		opponent = (byte)opponent;
		for( int i = 0; i < playerSequenceNum.length; i++ ) {
			playerSequenceNum[i] = 0;
		}
		for( int i = 0; i < opponentSequenceNum.length; i++ ) {
			opponentSequenceNum[i] = 0;
		}
		
		for(int i = 0; i < winningLine.length; i++) {
			
			byte p0 = winningLine[i][0];
			byte p1 = winningLine[i][1];
			byte p2 = winningLine[i][2];
			byte p3 = winningLine[i][3];
			
			byte state0 = curBoard[p0];
			byte state1 = curBoard[p1];
			byte state2 = curBoard[p2];
			byte state3 = curBoard[p3];

			int playerCounter = 0;
			int opponentCounter = 0;
			
			if(state0 == player) { 
				playerCounter++; 
			}
			else if( state0 == opponent ) {
				opponentCounter++;
			}
			
			if(state1 == player) { 
				playerCounter++; 
			}
			else if( state1 == opponent ) {
				opponentCounter++;
			}
			
			if(state2 == player) { 
				playerCounter++; 
			}
			else if( state2 == opponent ) {
				opponentCounter++;
			}
			
			if(state3 == player) { 
				playerCounter++; 
			}
			else if( state3 == opponent ) {
				opponentCounter++;
			}
			
			if( playerCounter > 0 && opponentCounter > 0 ) {
				continue;
			}
			
			if( playerCounter > 0 ) {
				playerSequenceNum[playerCounter-1]++;
			}
			if( opponentCounter > 0 ) {
				opponentSequenceNum[opponentCounter-1]++;
			}
					
		}
				
		int value = 0;
		for( int i = 0; i < 4; i++ ) {
			value += playerSequenceNum[i] * playerSequenceValue[i];
			value += opponentSequenceNum[i] * opponentSequenceValue[i];
		}	
		return value;	
	}
		
	/**
	 * Method: isDraw
	 * @author Ziqi Tan
	 * @ return the number of moves that have made
	 * */
	private int isDraw() {
		
		int steps = 0;
		for( int i = 0; i < curBoard.length; i++ ) {
			if( curBoard[i] != 0 ) {
				steps++;
			}
		}		
		return steps;		
	}
						
	/**
	 * Method: getByteBoard
	 * @author Kaijia You
	 * */
	private byte[] ByteBoard(List<positionTicTacToe> board) {
		for(int i = 0;i < board.size();i++) {
			curBoard[i] = (byte)board.get(i).state;
		}	
		return curBoard;
	}
	
	/**
	 * Method: xyzTo1d
	 * @author Kaijia You
	 * Function: Transform 3 dimension coordination to 1 dimension.
	 * @param int x, y, z
	 * @return byte index
	 * */
	private byte xyzTo1d(int i, int j, int k) {
		return (byte)(i * 16 + j * 4 + k);
	}
	
	/**
	 * Method: oneDToxyz
	 * @author Kaijia You
	 * Function: Transform 1 dimension coordination to 3 dimension.
	 * @return int[]{x, y, z} 
	 * */
	private int[] oneDToxyz(byte index) {
		
		int i = (int)index;
		
		int x = i/16;
		i -= x*16;
		
		int y = i/4;
		i -= y*4;
		
		int z = i/1;
		
		return new int[]{x, y, z};
	}
	
	/**
	 * Method: initializeWinningLines
	 * @author Kaijia You
	 * Function: Store all 76 winning lines in a byte[76][4] array.
	 * */
	private void getWinningLines() {
		// this.winningLine
		int count = 0;
		// create a list of winning line so that the game will "brute-force" check if a player satisfied any winning condition(s).
		
		// 48 straight winning lines
		// z axis winning lines
		for( int i = 0; i < 4; i++ )
			for( int j = 0; j < 4; j++ ) {		
				winningLine[count][0] = xyzTo1d(i, j, 0);
				winningLine[count][1] = xyzTo1d(i, j, 1);
				winningLine[count][2] = xyzTo1d(i, j, 2);
				winningLine[count][3] = xyzTo1d(i, j, 3);
				count++;
			}
		
		// y axis winning lines
		for( int i = 0; i < 4; i++ )
			for( int j = 0; j < 4; j++ ) {
				winningLine[count][0] = xyzTo1d(i, 0, j);
				winningLine[count][1] = xyzTo1d(i, 1, j);
				winningLine[count][2] = xyzTo1d(i, 2, j);
				winningLine[count][3] = xyzTo1d(i, 3, j);
				count++;
			}
		
		// x axis winning lines
		for( int i = 0; i < 4; i++ )
			for( int j = 0; j <4 ; j++ ){
				winningLine[count][0] = xyzTo1d(0, i, j);
				winningLine[count][1] = xyzTo1d(1, i, j);
				winningLine[count][2] = xyzTo1d(2, i, j);
				winningLine[count][3] = xyzTo1d(3, i, j);
				count++;
			}
		
		// 12 main diagonal winning lines
		// xz plane-4
		for( int i = 0; i < 4; i++ ) {
			winningLine[count][0] = xyzTo1d(0, i, 0);
			winningLine[count][1] = xyzTo1d(1, i, 1);
			winningLine[count][2] = xyzTo1d(2, i, 2);
			winningLine[count][3] = xyzTo1d(3, i, 3);
			count++;
		}
		//yz plane-4
		for( int i = 0; i < 4; i++ ) {
			winningLine[count][0] = xyzTo1d(i, 0, 0);
			winningLine[count][1] = xyzTo1d(i, 1, 1);
			winningLine[count][2] = xyzTo1d(i, 2, 2);
			winningLine[count][3] = xyzTo1d(i, 3, 3);
			count++;
		}
		// xy plane-4
		for( int i = 0; i < 4; i++ ) {
			winningLine[count][0] = xyzTo1d(0, 0, i);
			winningLine[count][1] = xyzTo1d(1, 1, i);
			winningLine[count][2] = xyzTo1d(2, 2, i);
			winningLine[count][3] = xyzTo1d(3, 3, i);
			count++;
		}
		
		//12 anti diagonal winning lines
		//xz plane-4
		for( int i = 0; i < 4; i++ ) {
			winningLine[count][0] = xyzTo1d(0, i, 3);
			winningLine[count][1] = xyzTo1d(1, i, 2);
			winningLine[count][2] = xyzTo1d(2, i, 1);
			winningLine[count][3] = xyzTo1d(3, i, 0);
			count++;
		}
		// yz plane-4
		for( int i = 0; i < 4; i++ ) {
			winningLine[count][0] = xyzTo1d(i, 0, 3);
			winningLine[count][1] = xyzTo1d(i, 1, 2);
			winningLine[count][2] = xyzTo1d(i, 2, 1);
			winningLine[count][3] = xyzTo1d(i, 3, 0);
			count++;
		}
		//xy plane-4
		for(int i = 0; i<4; i++) {
			winningLine[count][0] = xyzTo1d(0, 3, i);
			winningLine[count][1] = xyzTo1d(1, 2, i);
			winningLine[count][2] = xyzTo1d(2, 1, i);
			winningLine[count][3] = xyzTo1d(3, 0, i);
			count++;
		}
		
		//4 additional diagonal winning lines
		winningLine[count][0] = xyzTo1d(0, 0, 0);
		winningLine[count][1] = xyzTo1d(1, 1, 1);
		winningLine[count][2] = xyzTo1d(2, 2, 2);
		winningLine[count][3] = xyzTo1d(3, 3, 3);
		count++;
		
		winningLine[count][0] = xyzTo1d(0, 0, 3);
		winningLine[count][1] = xyzTo1d(1, 1, 2);
		winningLine[count][2] = xyzTo1d(2, 2, 1);
		winningLine[count][3] = xyzTo1d(3, 3, 0);
		count++;
		
		winningLine[count][0] = xyzTo1d(3, 0, 0);
		winningLine[count][1] = xyzTo1d(2, 1, 1);
		winningLine[count][2] = xyzTo1d(1, 2, 2);
		winningLine[count][3] = xyzTo1d(0, 3, 3);
		count++;
		
		winningLine[count][0] = xyzTo1d(0, 3, 0);
		winningLine[count][1] = xyzTo1d(1, 2, 1);
		winningLine[count][2] = xyzTo1d(2, 1, 2);
		winningLine[count][3] = xyzTo1d(3, 0, 3);
		count++;
		
	}
	
	/**
	 * Method: getForceMove
	 * @author Tian Ding
	 * Function: get a force move if your opponent has a three-in-a-row
	 * @return Return the force position or null
	 * */
	private byte getForceMove(int player) {
		int opponent = (player == 1 ? 2 : 1);
		opponent = (byte)opponent;
		byte forceMove = -1;
		
		for(int i = 0; i < winningLine.length; i++) {
			byte p0 = winningLine[i][0];
			byte p1 = winningLine[i][1];
			byte p2 = winningLine[i][2];
			byte p3 = winningLine[i][3];
				
			int state0 = curBoard[p0];
			int state1 = curBoard[p1];
			int state2 = curBoard[p2];
			int state3 = curBoard[p3];
				
			if( state0 == opponent && state1 == opponent && state2 == opponent && state3 == 0 ) {
				forceMove = p3;
			}
			// return blockMove = p3;
			else if( state0 == opponent && state1 == opponent && state2 == 0 && state3 == opponent ) {
				forceMove = p2;
			}
			// return blockMove = p2;
			else if( state0 == opponent && state1 == 0 && state2 == opponent && state3 == opponent ) {
				forceMove = p1;
			}
			// return blockMove = p1;
			else if( state0 == 0 && state1 == opponent && state2 == opponent && state3 == opponent ) {
				forceMove = p0;
			}
		}
		return forceMove;
	}
	
	/**
	 * Method: getWinMoveFromByte
	 * @author Kaijia You
	 * Function: 
	 * 		Get the winning move if there is one. 
	 * 		Traverse all the winningLines.
	 * @param current player
	 * @return a tic tac toe position
	 * */
	private byte getWinMove(int player) {
		byte winMove = -1;
		player = (byte)player;
		
		for(int i = 0;i < winningLine.length;i++) {
			byte p0 = winningLine[i][0];
			byte p1 = winningLine[i][1];
			byte p2 = winningLine[i][2];
			byte p3 = winningLine[i][3];
			
			byte state0 = curBoard[p0];
			byte state1 = curBoard[p1];
			byte state2 = curBoard[p2];
			byte state3 = curBoard[p3];
			
			int playerCounter = 0;
			if(state0 == player) {
				playerCounter++;
			}
			if(state1 == player) {
				playerCounter++;
			}
			if(state2 == player) {
				playerCounter++;
			}
			if(state3 == player) {
				playerCounter++;
			}
			
			if(playerCounter == 3) {
				if( state3 == 0 ) {
					winMove = p3;				
					return winMove;
				}
				else if (state2 == 0) {
					winMove = p2;				
					return winMove;
				}
				else if (state1 == 0) {
					winMove = p1;				
					return winMove;
				}
				else if (state0 == 0) {
					winMove = p0;				
					return winMove;
				}
			}
		}
		return winMove;
	}
	
	/**
	 * Method: getFirstFourStep
	 * @author Ziqi Tan
	 * */
	private int getFirstTwoStep(int player) {

		int move = -1;
		// occupy the strongest points
		// then hard code
		int steps = 0;
		
		for( int i = 0; i < curBoard.length; i++ ) {
			if( curBoard[i] != 0 ) {
				steps++;
			}
		}
		
		if( steps < 2 ) {
			Random rand = new Random();					
			do {			
				int index = rand.nextInt(corePoints.length);				
				move = corePoints[index];
			} while( curBoard[move] != 0 );			
			return move;
		}
					
		return move;
	}
}
