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
	private static List<positionTicTacToe> currentBoard;
	private static List<List<positionTicTacToe>> winningLines; 	
	private static positionTicTacToe myNextMove = new positionTicTacToe(0, 0, 0);
	private static int[] playerSequenceNum = new int[4];
	private static int[] opponentSequenceNum = new int[4];
	
	private static byte[] curBoard = new byte[64];
	private static byte[][] winningLine = new byte[76][4];
	
	private static List<positionTicTacToe> avaliablePositions;
	
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

	/**
	 * Constructor
	 * @author TF in CS 640 at Boston University
	 * Modified by Ziqi Tan:
	 * 		Initialize winningLines.
	 * */
	public aiTicTacToeCayden( int setPlayer ) {		
		player = setPlayer;
		winningLines = initializeWinningLines();
	}
	
	public positionTicTacToe myAIAlgorithm2(List<positionTicTacToe> board, int player) {
		
		currentBoard = deepCopyATicTacToeBoard(board);
		
		// Detect three sequences, which means that you have to block your opponent.
		positionTicTacToe forceMove = getForceMove(player);
		if( forceMove != null ) {
			return forceMove;
		}
		
		// Detect wining lines, which means that you will win.
		positionTicTacToe winMove = getWinMove(player);
		if( winMove != null ) {
			return winMove;
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
			
			currentBoard = deepCopyATicTacToeBoard(board);
			this.ByteBoard(board);
					
			// Detect three sequences, which means that you have to block your opponent.
			positionTicTacToe forceMove = getForceMove(player);
			if( forceMove != null ) {
				return forceMove;
			}
			
			// Detect wining lines, which means that you will win.
			positionTicTacToe winMove = getWinMove(player);
			if( winMove != null ) {
				return winMove;
			}
			
			getAvaliablePositions(currentBoard);

			// if avaliablePositions <= 4
			// occupy the strongest points
			// then hard code
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
			}
			int maxValue = Integer.MIN_VALUE;		
			for( int i = 0; i < avaliablePositions.size(); i++ ) {
				
				positionTicTacToe po = avaliablePositions.get(i);
				
				// TODO: backtracking
				List<positionTicTacToe> newBoard = deepCopyATicTacToeBoard(currentBoard);
				this.makeMove(po, player, newBoard);
				
				int newValue = miniMax(miniMaxDepth, newBoard, player, false, Integer.MIN_VALUE, Integer.MAX_VALUE);  // DFS
				// System.out.println("New Value: " + newValue);
				
				// update max value
				if( newValue > maxValue ) {
					maxValue = newValue;
					myNextMove = new positionTicTacToe(po.x, po.y, po.z);
					// System.out.println("New move: " + po.x + " " + po.y + " " + po.z + " Value: " + maxValue);
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
	private int miniMax(int depth, List<positionTicTacToe> board, int player, boolean maximizingPlayer, /*int value,*/ int alpha, int beta) {
		
		if( depth == 0 || isWin(board) ) {
			// return value;
			return evaluator(board, player);
		}
		if( maximizingPlayer ) {
			
			int value = Integer.MIN_VALUE;
			// generate children list
			getAvaliablePositions(board);
			// TODO: backtracking
			List<List<positionTicTacToe>> children = generateChildren(board, player);
			// for each child do a miniMax recursion
			for( int i = 0; i < children.size(); i++ ) {
				List<positionTicTacToe> child = children.get(i);
				// TODO: force move pruning
				value = Math.max(value, miniMax(depth - 1, child, player, false, alpha, beta));		
				alpha = Math.max(alpha, value);
				if( alpha >= beta ) {
					break;  // beta pruning
				}
			}
			// return the maximum value
			
			return value;
		}
		else {
			// minimizing player
			int value = Integer.MAX_VALUE;
			// generate children list
			getAvaliablePositions(board);
			int opponent = (player == 1 ? 2 : 1);
			List<List<positionTicTacToe>> children = generateChildren(board, opponent);
			// for each child do a miniMax recursion
			for( int i = 0; i < children.size(); i++ ) {
				List<positionTicTacToe> child = children.get(i);
				value = Math.min(value, miniMax(depth - 1, child, player, true, alpha, beta));
				beta = Math.min(beta, value);
				if( alpha >= beta ) {
					break;  // alpha pruning
				}
			}
			// return the minimum value
			
			return value;			
		}
	}
	
	/**
	 * Method: generateChildren
	 * @author Ziqi Tan
	 * */
	private List<List<positionTicTacToe>> generateChildren(List<positionTicTacToe> board, int player) {
		List<List<positionTicTacToe>> children = new ArrayList<List<positionTicTacToe>>();
		
		for( positionTicTacToe po : avaliablePositions ) {
			
			List<positionTicTacToe> child = this.deepCopyATicTacToeBoard(board);
			this.makeMove(po, player, child);
			children.add(child);
		}
		
		return children;
	}
	
	/**
	 * Method: getAvaliablePositions
	 * @author Ziqi Tan
	 * @return the positions which have not been marked
	 * */
	private void getAvaliablePositions(List<positionTicTacToe> board) {
		avaliablePositions = new ArrayList<positionTicTacToe>();
		for ( int i = 0; i < 4; i++ ) {
			for( int j = 0; j < 4; j++ ) {				
				for( int k = 0; k < 4; k++ ) {					
					if( getStateOfPositionFromBoard(new positionTicTacToe(i,j,k), board) == 0 ) {
						 // The position is not marked
						avaliablePositions.add( new positionTicTacToe(i,j,k) );
					}
				}
			}
		}
	}
		
	/**
	 * Method: evaluator
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
	private int evaluator(List<positionTicTacToe> board, int player) {
		
		// initialize
		int opponent = (player == 1 ? 2 : 1);
		for( int i = 0; i < playerSequenceNum.length; i++ ) {
			playerSequenceNum[i] = 0;
		}
		for( int i = 0; i < opponentSequenceNum.length; i++ ) {
			opponentSequenceNum[i] = 0;
		}
		
		for(int i = 0; i < winningLines.size(); i++) {
			positionTicTacToe p0 = winningLines.get(i).get(0);
			positionTicTacToe p1 = winningLines.get(i).get(1);
			positionTicTacToe p2 = winningLines.get(i).get(2);
			positionTicTacToe p3 = winningLines.get(i).get(3);

			int state0 = getStateOfPositionFromBoard(p0, board);
			int state1 = getStateOfPositionFromBoard(p1, board);
			int state2 = getStateOfPositionFromBoard(p2, board);
			int state3 = getStateOfPositionFromBoard(p3, board);

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
		
		int value = 0;
		for( int i = 0; i < 4; i++ ) {
			value += playerSequenceNum[i] * playerSequenceValue[i];
			value += opponentSequenceNum[i] * opponentSequenceValue[i];
		}	
		return value;	
	}
	
	/**
	 * Method: getForceMove
	 * @author Ziqi Tan
	 * @param current player
	 * @return Return the force position or null
	 * */
	private positionTicTacToe getForceMove(int player) {
		
		int opponent = (player == 1 ? 2 : 1);
		positionTicTacToe forceMove = null;
		
		for(int i = 0; i < winningLines.size(); i++) {
			
			positionTicTacToe p0 = winningLines.get(i).get(0);
			positionTicTacToe p1 = winningLines.get(i).get(1);
			positionTicTacToe p2 = winningLines.get(i).get(2);
			positionTicTacToe p3 = winningLines.get(i).get(3);

			int state0 = getStateOfPositionFromBoard(p0, currentBoard);
			int state1 = getStateOfPositionFromBoard(p1, currentBoard);
			int state2 = getStateOfPositionFromBoard(p2, currentBoard);
			int state3 = getStateOfPositionFromBoard(p3, currentBoard);
			
			if( state0 == opponent && state1 == opponent && state2 == opponent && state3 == 0 ) {
				forceMove = new positionTicTacToe(p3.x, p3.y, p3.z);
			}
			// return blockMove = p3;
			else if( state0 == opponent && state1 == opponent && state2 == 0 && state3 == opponent ) {
				forceMove = new positionTicTacToe(p2.x, p2.y, p2.z);
			}
			// return blockMove = p2;
			else if( state0 == opponent && state1 == 0 && state2 == opponent && state3 == opponent ) {
				forceMove = new positionTicTacToe(p1.x, p1.y, p1.z);
			}
			// return blockMove = p1;
			else if( state0 == 0 && state1 == opponent && state2 == opponent && state3 == opponent ) {
				forceMove = new positionTicTacToe(p0.x, p0.y, p0.z);
			}
		}
		return forceMove;
	}
	
	/**
	 * Method: getWinMove
	 * @author Ziqi Tan
	 * Function: 
	 * 		Get the winning move if there is one. 
	 * 		Traverse all the winningLines.
	 * @param current player
	 * @return a tic tac toe position
	 * */
	private positionTicTacToe getWinMove(int player) {
		positionTicTacToe winMove = null;
		for(int i = 0; i < winningLines.size(); i++) {
			positionTicTacToe p0 = winningLines.get(i).get(0);
			positionTicTacToe p1 = winningLines.get(i).get(1);
			positionTicTacToe p2 = winningLines.get(i).get(2);
			positionTicTacToe p3 = winningLines.get(i).get(3);

			int state0 = getStateOfPositionFromBoard(p0, currentBoard);
			int state1 = getStateOfPositionFromBoard(p1, currentBoard);
			int state2 = getStateOfPositionFromBoard(p2, currentBoard);
			int state3 = getStateOfPositionFromBoard(p3, currentBoard);

			int playerCounter = 0;
			if(state0 == player) { playerCounter++; }
			if(state1 == player) { playerCounter++; }
			if(state2 == player) { playerCounter++; }
			if(state3 == player) { playerCounter++; }
			
			if( playerCounter == 3 ) {
				if( state3 == 0 ) {
					winMove = new positionTicTacToe(p3.x, p3.y, p3.z);				
					return winMove;
				}
				//return blockMove = p3;
				else if (state2 == 0) {
					winMove = new positionTicTacToe(p2.x, p2.y, p2.z);				
					return winMove;
				}
				//return blockMove = p2;
				else if (state1 == 0) {
					winMove = new positionTicTacToe(p1.x, p1.y, p1.z);				
					return winMove;
				}
				//return blockMove = p1;
				else if (state0 == 0) {
					winMove = new positionTicTacToe(p0.x, p0.y, p0.z);				
					return winMove;
				}
			}
		}
		return winMove;
	}
	
	public boolean isWin(List<positionTicTacToe> board) {

		for(int i = 0; i < winningLines.size(); i++) {
			positionTicTacToe p0 = winningLines.get(i).get(0);
			positionTicTacToe p1 = winningLines.get(i).get(1);
			positionTicTacToe p2 = winningLines.get(i).get(2);
			positionTicTacToe p3 = winningLines.get(i).get(3);

			int state0 = getStateOfPositionFromBoard(p0, board);
			int state1 = getStateOfPositionFromBoard(p1, board);
			int state2 = getStateOfPositionFromBoard(p2, board);
			int state3 = getStateOfPositionFromBoard(p3, board);
			// if they have the same state (marked by same player) and they are not all marked.
			if ( state0 != 0 && state0 == state1 && state1 == state2 && state2 == state3) {
				return true;
			}
		}
		return false;
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
	 * @author TF in CS 640 at Boston University
	 * */
	private List<List<positionTicTacToe>> initializeWinningLines() {
		// create a list of winning line so that the game will "brute-force" check if a player satisfied any winning condition(s).
		List<List<positionTicTacToe>> winningLines = new ArrayList<List<positionTicTacToe>>();
		
		// 48 straight winning lines
		// z axis winning lines
		for( int i = 0; i < 4; i++ )
			for( int j = 0; j < 4; j++ ) {
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,j,0,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,3,-1));
				winningLines.add(oneWinCondtion);
			}
		
		// y axis winning lines
		for( int i = 0; i < 4; i++ )
			for( int j = 0; j < 4; j++ ) {
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,j,-1));
				winningLines.add(oneWinCondtion);
			}
		
		// x axis winning lines
		for( int i = 0; i < 4; i++ )
			for( int j = 0; j <4 ; j++ ){
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,j,-1));
				winningLines.add(oneWinCondtion);
			}
		
		// 12 main diagonal winning lines
		// xz plane-4
		for( int i = 0; i < 4; i++ ) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(0,i,0,-1));
			oneWinCondtion.add(new positionTicTacToe(1,i,1,-1));
			oneWinCondtion.add(new positionTicTacToe(2,i,2,-1));
			oneWinCondtion.add(new positionTicTacToe(3,i,3,-1));
			winningLines.add(oneWinCondtion);
		}
		//yz plane-4
		for( int i = 0; i < 4; i++ ) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(i,0,0,-1));
			oneWinCondtion.add(new positionTicTacToe(i,1,1,-1));
			oneWinCondtion.add(new positionTicTacToe(i,2,2,-1));
			oneWinCondtion.add(new positionTicTacToe(i,3,3,-1));
			winningLines.add(oneWinCondtion);
		}
		// xy plane-4
		for( int i = 0; i < 4; i++ ) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(0,0,i,-1));
			oneWinCondtion.add(new positionTicTacToe(1,1,i,-1));
			oneWinCondtion.add(new positionTicTacToe(2,2,i,-1));
			oneWinCondtion.add(new positionTicTacToe(3,3,i,-1));
			winningLines.add(oneWinCondtion);
		}
		
		//12 anti diagonal winning lines
		//xz plane-4
		for( int i = 0; i < 4; i++ ) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(0,i,3,-1));
			oneWinCondtion.add(new positionTicTacToe(1,i,2,-1));
			oneWinCondtion.add(new positionTicTacToe(2,i,1,-1));
			oneWinCondtion.add(new positionTicTacToe(3,i,0,-1));
			winningLines.add(oneWinCondtion);
		}
		// yz plane-4
		for( int i = 0; i < 4; i++ ) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(i,0,3,-1));
			oneWinCondtion.add(new positionTicTacToe(i,1,2,-1));
			oneWinCondtion.add(new positionTicTacToe(i,2,1,-1));
			oneWinCondtion.add(new positionTicTacToe(i,3,0,-1));
			winningLines.add(oneWinCondtion);
		}
		//xy plane-4
		for(int i = 0; i<4; i++) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(0,3,i,-1));
			oneWinCondtion.add(new positionTicTacToe(1,2,i,-1));
			oneWinCondtion.add(new positionTicTacToe(2,1,i,-1));
			oneWinCondtion.add(new positionTicTacToe(3,0,i,-1));
			winningLines.add(oneWinCondtion);
		}
		
		//4 additional diagonal winning lines
		List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,0,0,-1));
		oneWinCondtion.add(new positionTicTacToe(1,1,1,-1));
		oneWinCondtion.add(new positionTicTacToe(2,2,2,-1));
		oneWinCondtion.add(new positionTicTacToe(3,3,3,-1));
		winningLines.add(oneWinCondtion);
		
		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,0,3,-1));
		oneWinCondtion.add(new positionTicTacToe(1,1,2,-1));
		oneWinCondtion.add(new positionTicTacToe(2,2,1,-1));
		oneWinCondtion.add(new positionTicTacToe(3,3,0,-1));
		winningLines.add(oneWinCondtion);
		
		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(3,0,0,-1));
		oneWinCondtion.add(new positionTicTacToe(2,1,1,-1));
		oneWinCondtion.add(new positionTicTacToe(1,2,2,-1));
		oneWinCondtion.add(new positionTicTacToe(0,3,3,-1));
		winningLines.add(oneWinCondtion);
		
		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,3,0,-1));
		oneWinCondtion.add(new positionTicTacToe(1,2,1,-1));
		oneWinCondtion.add(new positionTicTacToe(2,1,2,-1));
		oneWinCondtion.add(new positionTicTacToe(3,0,3,-1));
		winningLines.add(oneWinCondtion);	
		
		return winningLines;
		
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
	
	/**
	 * Method: deepCopyATicTacToeBoard
	 * Function: Deep copy a game board.
	 * @author TF in CS 640 at Boston University
	 * @param List<positionTicTacToe>
	 * @return List<positionTicTacToe>
	 * */
	private List<positionTicTacToe> deepCopyATicTacToeBoard(List<positionTicTacToe> board) {
		// deep copy of game boards
		List<positionTicTacToe> copiedBoard = new ArrayList<positionTicTacToe>();
		for( int i = 0; i < board.size(); i++ ) {
			copiedBoard.add(new positionTicTacToe(board.get(i).x, board.get(i).y, board.get(i).z, board.get(i).state));
		}
		return copiedBoard;
	}
	
	/**
	 * Method: makeMove
	 * Function: make a move on a board.
	 * @author Ziqi Tan
	 * @return 
	 * */
	private boolean makeMove(positionTicTacToe position, int player, List<positionTicTacToe> targetBoard) {
		// make move on Tic-Tac-Toe board, given position and player 
		// player 1 = 1, player 2 = 2
		
		// brute force (obviously not a wise way though)
		for( int i = 0; i < targetBoard.size(); i++ ) {
			// if this is the position
			if( targetBoard.get(i).x == position.x && targetBoard.get(i).y == position.y && targetBoard.get(i).z == position.z )  {
				if( targetBoard.get(i).state == 0 ) {
					targetBoard.get(i).state = player;
					return true;
				}
				else {
					System.out.println("Error: this is not a valid move.");
				}
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
	 * */
	private byte xyzTo1d(int i, int j, int k) {
		return (byte)(i * 16 + j * 4 + k);
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
		List<List<positionTicTacToe>> winningLines = new ArrayList<List<positionTicTacToe>>();
		
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
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
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
	 * Method: getForceMoveFromByte
	 * @author Tian Ding
	 * Function: get a force move if your opponent has a three-in-a-row
	 * @return Return the force position or null
	 * */
	private positionTicTacToe getForceMoveFromByte(int player) {
		
		
		return null;
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
	private byte getWinMoveFromByte(byte player) {
		byte winMove = (Byte) null;
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
}
