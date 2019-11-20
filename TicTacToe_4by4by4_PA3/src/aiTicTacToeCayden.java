import java.util.*;

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

public class aiTicTacToeCayden {
	
	public int player;  // 1 for player 1 and 2 for player 2
	
	/**
	 * Private static variables
	 * @author Ziqi Tan
	 * */
	private static byte[][] winningLine = new byte[76][4];
	
	private positionTicTacToe myNextMove = new positionTicTacToe(0, 0, 0);
	private int[] playerSequenceNum = new int[4];
	private int[] opponentSequenceNum = new int[4];
	
	private byte[] curBoard = new byte[64];
		
	/**
	 * Private static final variables
	 * @author Ziqi Tan
	 * */
	private static final int[] playerSequenceValue = new int[] {1, 15, 150, 100000};
	private static final int[] opponentSequenceValue = new int[] {-1, -10, -100, -100000};
	private static final int miniMaxDepth = 3;
	
	private static final positionTicTacToe[] strongestPositions = {
			new positionTicTacToe(1, 1, 1),
			new positionTicTacToe(2, 2, 2),
			
			new positionTicTacToe(1, 2, 1),
			new positionTicTacToe(2, 1, 2),
			
			new positionTicTacToe(2, 1, 1),
			new positionTicTacToe(1, 2, 2),
			
			new positionTicTacToe(2, 2, 1),
			new positionTicTacToe(1, 1, 2)		
	};
	
	private static final int[] corePoints = {
		21, 22, 25, 26, 37, 38, 41, 42
	};
	
	private static final int[] cornerPoints = {
		0, 3, 12, 16, 48, 51, 60, 63
	};
	
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
			
	};

	/**
	 * Constructor
	 * @author TF in CS 640 at Boston University
	 * Modified by Ziqi Tan:
	 * 		Initialize winningLines.
	 * */
	public aiTicTacToeCayden( int setPlayer ) {		
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
		
		// TODO: this is where you are going to implement your AI algorithm to win the game. 
							
		try {
			System.out.println("Player" + player + "' turn:");
			
			this.ByteBoard(board);
			this.printBoardTicTacToe(board);
			this.printByteBoard();
			// Detect three sequences, which means that you have to block your opponent.
			byte forceMove = getForceMove(player);
			if( forceMove != -1 ) {
				int[] xyz = oneDToxyz(forceMove);
				System.out.println("Force move.");
				return new positionTicTacToe(xyz[0], xyz[1], xyz[2]);
			}
						
			// Detect wining lines, which means that you will win.			
			byte winMove = getWinMove(player);
			if( winMove != -1 ) {
				int[] xyz = oneDToxyz(winMove);
				System.out.println("Win move");
				return new positionTicTacToe(xyz[0], xyz[1], xyz[2]);
			}
							
			// if avaliablePositions <= 4
			// occupy the strongest points
			// then hard code
			/*getAvaliablePositions(currentBoard);
			if( avaliablePositions.size() > 60 ) {						
				for( int i = 0; i < strongestPositions.length; i++ ) {	
					positionTicTacToe po = strongestPositions[i];
					myNextMove = new positionTicTacToe(po.x, po.y, po.z);
					if( getStateOfPositionFromBoard(myNextMove, board) == 0 ) {
						System.out.println("occupy the strongest points");
						System.out.println(po.x + " " +  po.y + " " + po.z);
						return myNextMove;
					}
				}							
			}*/
			
			int maxValue = Integer.MIN_VALUE;
			for( int i = 0; i < curBoard.length; i++ ) {
				
				if( curBoard[i] == 0 ) {					
					// make move
					System.out.println("Move: " + Arrays.toString(oneDToxyz((byte) i)) + evaluation(player));
					curBoard[i] = (byte)player;
					
					int newValue = miniMax(miniMaxDepth, player, false, Integer.MIN_VALUE, Integer.MAX_VALUE);					
					System.out.println("New value: " + newValue);
					if( newValue > maxValue ) {
						// update max value
						maxValue = newValue;						
						// update my next best move
						int[] xyz = oneDToxyz((byte) i);
						myNextMove = new positionTicTacToe(xyz[0], xyz[1], xyz[2]);
					}
					// cancel move
					curBoard[i] = 0;  // backtracking

				}
						
			}
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

		if( depth == 0 || isWin() ) {
			return evaluation(player);
		}
		if( maximizingPlayer ) {
			
			int value = Integer.MIN_VALUE;
			// for each child do a miniMax recursion
			for( int i = 0; i < curBoard.length; i++ ) {				
				if( curBoard[i] == 0 ) {
					// System.out.println("Maximizer move" + player);
					curBoard[i] = (byte)player;
					// System.out.println("Move:" + curBoard[i]);
					value = Math.max(value, miniMax(depth - 1, player, false, alpha, beta));
					curBoard[i] = 0; // backtracking
					alpha = Math.max(alpha, value);
					if( alpha >= beta ) {
						break;  // beta pruning
					}
				}
			}
			// return the maximum value			
			return value;
		}
		else {
			// minimizing player
			int value = Integer.MAX_VALUE;
			int opponent = (player == 1 ? 2 : 1);
			
			// for each child do a miniMax recursion
			for( int i = 0; i < curBoard.length; i++ ) {
				if( curBoard[i] == 0 ) {
					// System.out.println("Minimizer move" + opponent);
					curBoard[i] = (byte)opponent;
					// System.out.println("Move:" + curBoard[i]);
					value = Math.min(value, miniMax(depth - 1, player, true, alpha, beta));
					curBoard[i] = 0;   // backtracking
					beta = Math.min(beta, value);
					if( alpha >= beta ) {
						break;  // alpha pruning
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

			if( playerCounter > 0 ) {
				playerSequenceNum[playerCounter-1]++;
			}
			if( opponentCounter > 0 ) {
				opponentSequenceNum[opponentCounter-1]++;
			}
					
		}
		
		// System.out.println(Arrays.toString(playerSequenceNum));
		// System.out.println(Arrays.toString(opponentSequenceNum));
		
		int value = 0;
		for( int i = 0; i < 4; i++ ) {
			value += playerSequenceNum[i] * playerSequenceValue[i];
			value += opponentSequenceNum[i] * opponentSequenceValue[i];
		}	
		return value;	
	}
	
	/**
	 * Method: isWin
	 * @author Ziqi Tan
	 * Function: check whether the game is end.
	 * */	
	private boolean isWin() {

		for(int i = 0; i < winningLine.length; i++) {
			byte p0 = winningLine[i][0];
			byte p1 = winningLine[i][1];
			byte p2 = winningLine[i][2];
			byte p3 = winningLine[i][3];
						
			byte state0 = curBoard[p0];
			byte state1 = curBoard[p1];
			byte state2 = curBoard[p2];
			byte state3 = curBoard[p3];

			// if they have the same state (marked by same player) and they are not all marked.
			if ( state0 != 0 && state0 == state1 && state1 == state2 && state2 == state3) {
				return true;
			}
		}
		return false;
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
	 * 
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
				winningLine[count][1] = xyzTo1d(0, i, j);
				winningLine[count][2] = xyzTo1d(0, i, j);
				winningLine[count][3] = xyzTo1d(0, i, j);
				count++;
			}
		
		// 12 main diagonal winning lines
		// xz plane-4
		for( int i = 0; i < 4; i++ ) {
			winningLine[count][0] = xyzTo1d(0, i, 0);
			winningLine[count][1] = xyzTo1d(0, i, 1);
			winningLine[count][2] = xyzTo1d(0, i, 2);
			winningLine[count][3] = xyzTo1d(0, i, 3);
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
			
			int playerCounter = 0;
			if(p0 == player) {
				playerCounter++;
			}
			if(p1 == player) {
				playerCounter++;
			}
			if(p2 == player) {
				playerCounter++;
			}
			if(p3 == player) {
				playerCounter++;
			}
			
			if(playerCounter == 3) {
				if( p3 == 0 ) {
					winMove = p3;				
					return winMove;
				}
				//return blockMove = p3;
				else if (p2 == 0) {
					winMove = p2;				
					return winMove;
				}
				//return blockMove = p2;
				else if (p1 == 0) {
					winMove = p1;				
					return winMove;
				}
				//return blockMove = p1;
				else if (p0 == 0) {
					winMove = p0;				
					return winMove;
				}
			}
		}
		return winMove;
	}
	
	/**
	 * Method: getStateOfPositionFromBoard
	 * @author TF in CS 640 at Boston University
	 * Function: 
	 * 	A helper function to get state of a certain position 
	 * 	in the Tic-Tac-Toe board by given an instance of class positionTicTacToe.
	 * */
	private int getStateOfPositionFromBoard( positionTicTacToe position, List<positionTicTacToe> board ) {
		int index = position.x*16 + position.y*4 + position.z;
		return board.get(index).state;
	}
	
	/**
	 * A Test.
	 * */
	public positionTicTacToe myAIAlgorithm2(List<positionTicTacToe> board, int player) {
				
		// Detect three sequences, which means that you have to block your opponent.
		byte forceMove = getForceMove(player);
		if( forceMove != -1 ) {
			int[] xyz = oneDToxyz(forceMove);
			return new positionTicTacToe(xyz[0], xyz[1], xyz[2]);
		}
					
		// Detect wining lines, which means that you will win.			
		byte winMove = getWinMove(player);
		if( winMove != -1 ) {
			int[] xyz = oneDToxyz(winMove);
			return new positionTicTacToe(xyz[0], xyz[1], xyz[2]);
		}
		
		Random rand = new Random();
		do {			
			// We can also randomly choose a strong point.
			int x = rand.nextInt(4);
			int y = rand.nextInt(4);
			int z = rand.nextInt(4);
			myNextMove = new positionTicTacToe(x,y,z);
		} while( getStateOfPositionFromBoard(myNextMove, board) != 0 );
		
		return myNextMove;
	
	}
	
	public void printBoardTicTacToe(List<positionTicTacToe> targetBoard) {

		for ( int i = 0; i < 4; i++ ) {
			System.out.println("level(z) "+i);
			for( int j = 0; j < 4; j++ ) {
				System.out.print("["); // boundary
				for( int k = 0; k < 4; k++ ) {
					if ( getStateOfPositionFromBoard(new positionTicTacToe(j,k,i), targetBoard) == 1 ) {
						System.out.print("X"); //print cross "X" for position marked by player 1
					}
					else if( getStateOfPositionFromBoard(new positionTicTacToe(j,k,i), targetBoard) == 2 ) {
						System.out.print("O"); //print cross "O" for position marked by player 2
					}
					else if( getStateOfPositionFromBoard(new positionTicTacToe(j,k,i), targetBoard) == 0 ) {
						System.out.print("_"); //print "_" if the position is not marked
					}
					if( k == 3 ) {
						System.out.print("]"); // boundary
						System.out.println();
					}					
				}
			}
			System.out.println();
		}
	}
	
	private void printByteBoard() {
		for ( int i = 0; i < 4; i++ ) {
			System.out.println("level(z) "+i);
			for( int j = 0; j < 4; j++ ) {
				System.out.print("["); // boundary
				for( int k = 0; k < 4; k++ ) {
					if ( curBoard[(int)this.xyzTo1d(j, k, i)] == 1 ) {
						System.out.print("X"); //print cross "X" for position marked by player 1
					}
					else if( curBoard[(int)this.xyzTo1d(j, k, i)] == 2  ) {
						System.out.print("O"); //print cross "O" for position marked by player 2
					}
					else if( curBoard[(int)this.xyzTo1d(j, k, i)] == 0 ) {
						System.out.print("_"); //print "_" if the position is not marked
					}
					if( k == 3 ) {
						System.out.print("]"); // boundary
						System.out.println();
					}					
				}
			}
			System.out.println();
		}
		
		
	}
}
