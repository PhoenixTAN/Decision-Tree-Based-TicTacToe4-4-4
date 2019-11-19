import java.util.*;
import java.util.concurrent.TimeUnit;

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

public class aiTicTacToe {
	
	public int player;  // 1 for player 1 and 2 for player 2
	
	/**
	 * Private static variables
	 * @author Ziqi Tan
	 * */
	private static List<positionTicTacToe> currentBoard;
	private static List<List<positionTicTacToe>> winningLines; 	
	private static positionTicTacToe myNextMove = new positionTicTacToe(0, 0, 0);
	private static int[] playerSequenceNum;
	private static int[] opponentSequenceNum;
	private static List<positionTicTacToe> avaliablePositions;
	private static List<Integer> nextMoveEvaluation;
	
	/**
	 * Private static final variables
	 * @author Ziqi Tan
	 * */
	private static final int[] playerSequenceValue = new int[] {1, 10, 100, 100000};
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

	private static final HashSet<positionTicTacToe> cornerPositions = new HashSet<positionTicTacToe>() { 
		{
			add( new positionTicTacToe(0, 0, 0) );
		}	
	};
	private static final HashSet<positionTicTacToe> edgePositions = new HashSet<positionTicTacToe>() { 
		{
			add( new positionTicTacToe(0, 1, 0) );
		}	
	};
	private static final HashSet<positionTicTacToe> facePositions = new HashSet<positionTicTacToe>() { 
		{
			add( new positionTicTacToe(1, 1, 0) );
		}	
	};
			
	/**
	 * Constructor
	 * @author TF in CS 640 at Boston University
	 * Modified by Ziqi Tan:
	 * 		Initialize winningLines.
	 * */
	public aiTicTacToe( int setPlayer ) {		
		player = setPlayer;
		winningLines = initializeWinningLines();
	}
	
	public positionTicTacToe myAIAlgorithm2(List<positionTicTacToe> board, int player) {
		
		currentBoard = new ArrayList<positionTicTacToe>(board);
		
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
			currentBoard = new ArrayList<positionTicTacToe>(board);
			
			// The default is an AI randomly choose any available move.
			Random rand = new Random();
			do {			
				// We can also randomly choose a strong point.
				int x = rand.nextInt(4);
				int y = rand.nextInt(4);
				int z = rand.nextInt(4);
				myNextMove = new positionTicTacToe(x,y,z);
			} while( getStateOfPositionFromBoard(myNextMove, board) != 0 );
		
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
			System.out.println(avaliablePositions.size());
			initializeNextMoveValue(currentBoard, player);
			TimeUnit.SECONDS.sleep(200);
			// if avaliablePositions <= 4
			// occupy the strongest points
			// then hard code
			/*if( avaliablePositions.size() > 60 ) {						
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
			/*			
			for( int i = 0; i < avaliablePositions.size(); i++ ) {
				positionTicTacToe po = avaliablePositions.get(i);
				int value = nextMoveEvaluation.get(i);	
				//System.out.println(value);
				List<positionTicTacToe> newBoard = new ArrayList<positionTicTacToe>(currentBoard);
				newBoard.add(new positionTicTacToe(po.x, po.y, po.z, player));
				int beta = evaluator(newBoard, player);
				int newValue = miniMax(miniMaxDepth, newBoard, player, false, value, Integer.MIN_VALUE, beta);  // DFS
				// int newValue = evaluator(newBoard, player);
				if( newValue > value ) {
					value = newValue;
					myNextMove = new positionTicTacToe(po.x, po.y, po.z);
					// System.out.println(value);
				}				
			}*/
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
	private int miniMax(int depth, List<positionTicTacToe> board, int player, boolean maximizingPlayer, int value, int alpha, int beta) {
		
		if( depth == 0 || isWin(board)/**/ ) {
			//System.out.println(value);
			return value;
		}
		if( maximizingPlayer ) {
			value = Integer.MIN_VALUE;
			
			// generate children list
			getAvaliablePositions(board);
			List<List<positionTicTacToe>> children = generateChildren(board, player);
			// for each child do a miniMax recursion
			for( int i = 0; i < children.size(); i++ ) {
				List<positionTicTacToe> child = children.get(i);
				value = Math.max(value, evaluator(child, player));
				//System.out.println(value);
				value = Math.max(value, miniMax(depth - 1, child, player, false, value, alpha, beta));
			}
			// return the maximum value
			
			return value;
		}
		else {
			// minimizing player
			value = Integer.MAX_VALUE;
			// generate children list
			getAvaliablePositions(board);
			int opponent = (player == 1 ? 2 : 1);
			List<List<positionTicTacToe>> children = generateChildren(board, opponent);
			// for each child do a miniMax recursion
			for( int i = 0; i < children.size(); i++ ) {
				List<positionTicTacToe> child = children.get(i);
				value = Math.min(value, evaluator(child, player));
				value = Math.min(value, miniMax(depth - 1, child, player, true, value, alpha, beta));
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
			
			List<positionTicTacToe> child = new ArrayList<positionTicTacToe>(board);
			child.add( new positionTicTacToe(po.x, po.y, po.z, player));
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
		
		/*System.out.println("avaliablePositions");
		for( positionTicTacToe po : avaliablePositions ) {
			System.out.println(po.x + " " +  po.y + " " + po.z);
		}*/
		
	}
	
	/**
	 * Method: initializeNextMoveValue
	 * @author Ziqi Tan
	 * */
	private void initializeNextMoveValue(List<positionTicTacToe> board, int player) {
		nextMoveEvaluation = new ArrayList<Integer>();
				
		for( int i = 0; i < avaliablePositions.size(); i++ ) {
		 	List<positionTicTacToe> nextBoard = deepCopyATicTacToeBoard(board);
			// List<positionTicTacToe> nextBoard = new ArrayList<positionTicTacToe>(board);
			positionTicTacToe po = avaliablePositions.get(i);
			System.out.println(po.x + " " +  po.y + " " + po.z);
			// nextBoard.add(new positionTicTacToe(position.x, position.y, position.z, player));
			
			for( int j = 0; j < nextBoard.size(); j++ ) {
				if( nextBoard.get(j).x == po.x && nextBoard.get(j).y == po.y && nextBoard.get(j).z == po.z ) {
					nextBoard.get(j).state = player;
					System.out.println(nextBoard.get(j).x + " " + nextBoard.get(j).y + " " + nextBoard.get(j).z );
					System.out.println(po.x + " " +  po.y + " " + po.z);
					//this.printBoardTicTacToe(nextBoard);
					System.out.println(evaluator(nextBoard, player));
					nextMoveEvaluation.add(evaluator(nextBoard, player));
					break;
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
		playerSequenceNum = new int[4];
		opponentSequenceNum = new int[4];
		
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

		for(int i = 0; i < winningLines.size(); i++)
		{
			positionTicTacToe p0 = winningLines.get(i).get(0);
			positionTicTacToe p1 = winningLines.get(i).get(1);
			positionTicTacToe p2 = winningLines.get(i).get(2);
			positionTicTacToe p3 = winningLines.get(i).get(3);

			int state0 = getStateOfPositionFromBoard(p0, board);
			int state1 = getStateOfPositionFromBoard(p1, board);
			int state2 = getStateOfPositionFromBoard(p2, board);
			int state3 = getStateOfPositionFromBoard(p3, board);
			//if they have the same state (marked by same player) and they are not all marked.
			if ( state0 != 0 && state0 == state1 && state1 == state2 && state2 == state3)
			{
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
		// print each position on the board, uncomment this for debugging if necessary
		/*
		System.out.println("board:");
		System.out.println("board slots: "+board.size());
		for (int i=0;i<board.size();i++)
		{
			board.get(i).printPosition();
		}
		*/
		
		// print in "graphical" display
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
	
}
