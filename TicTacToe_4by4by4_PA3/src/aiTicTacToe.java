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

public class aiTicTacToe {
	
	public int player;  // 1 for player 1 and 2 for player 2
	
	/**
	 * Private static variables
	 * @author Ziqi Tan
	 * */
	private static List<List<positionTicTacToe>> winningLines;  
	private static positionTicTacToe myNextMove = new positionTicTacToe(0, 0, 0);
	private static int heuristicValue = 0;
	private static int alpha = Integer.MIN_VALUE;
	private static int beta = Integer.MAX_VALUE;
	private static int[] playerSequenceNum = new int[4];
	private static int[] opponentSequenceNum = new int[4];
	private static List<positionTicTacToe> avaliablePositions;
	private static HashMap<positionTicTacToe, Integer> nextMoveEvaluation = new HashMap<positionTicTacToe, Integer>();
	
	/**
	 * Private static final variables
	 * @author Ziqi Tan
	 * */
	private static final int[] playerSequenceValue = new int[] {1, 10, 16, 1000};
	private static final int[] opponentSequenceValue = new int[] {-2, -11, -17, -2000};
	private static final int miniMaxDepth = 3;
	
	private static final HashSet<positionTicTacToe> strongestPositions = new HashSet<positionTicTacToe>() {
		{
			add( new positionTicTacToe(1, 1, 1) );
		}	
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
					
		int x = 0;
		int y = 0;
		int z = 0;
						
		// write your new strategy here
		try {
					
			// The default is an AI randomly choose any available move.
			Random rand = new Random();
			do {			
				// We can also randomly choose a strong point.
				x = rand.nextInt(4);
				y = rand.nextInt(4);
				z = rand.nextInt(4);
				myNextMove = new positionTicTacToe(x,y,z);
			} while( getStateOfPositionFromBoard(myNextMove, board) != 0 );
			
			// if avaliablePositions <= 4
			// then hard code
			
			// TODO: detect three sequences, which means that you have to block your opponent
			
			// try only get the next depth
			getAvaliablePositions(board);
			initializeNextMoveValue(board, player);
			System.out.println(avaliablePositions.size());
			for( positionTicTacToe po: nextMoveEvaluation.keySet() ) {
				int value = nextMoveEvaluation.get(po);
				int newValue = miniMax(miniMaxDepth, board, player, false);
				if( newValue > value ) {
					value = newValue;
					// myNextMove = new positionTicTacToe(po.x, po.y, po.z);
				}
			}
		}
		catch( Exception error ) {
			System.out.println(error);
		}		
		return myNextMove;										
	}
	
	/**
	 * Method: miniMax
	 * @author Ziqi Tan
	 * */
	private int miniMax(int depth, List<positionTicTacToe> board, int player, boolean maximizingPlayer) {
		
		getAvaliablePositions(board);
		if( depth == 0 || avaliablePositions.size() == 0 /*|| winningLines.contains(board) */) {
			return this.heuristicValue;
		}
		if( maximizingPlayer ) {
			int value = Integer.MIN_VALUE;
			
			// generate children list
			List<List<positionTicTacToe>> children = generateChildren(board, player);
			System.out.println("children.size(): " + children.size());
			// for each child do a miniMax recursion
			for( int i = 0; i < children.size(); i++ ) {
				List<positionTicTacToe> child = children.get(i);
				value = Math.max(value, miniMax(depth - 1, child, player, false));
			}
			// return the maximum value
			
			return value;
		}
		else {
			// minimizing player
			int value = Integer.MAX_VALUE;
			// TODO:
			// generate children list
			List<List<positionTicTacToe>> children = generateChildren(board, player);
			// for each child do a miniMax recursion
			for( int i = 0; i < children.size(); i++ ) {
				List<positionTicTacToe> child = children.get(i);
				value = Math.min(value, miniMax(depth - 1, child, player, true));
			}
			// return the minimum value
			
			return value;
			
		}
	}
	
	/**
	 * Method: generateChildren
	 * @author Ziqi Tan
	 * */
	private List<List<positionTicTacToe>> generateChildren(List<positionTicTacToe> currentBoard, int player) {
		List<List<positionTicTacToe>> children = new ArrayList<List<positionTicTacToe>>();
		// (j, k, i) -> (x, y, z)
		
		
		return children;
	}
	
	/**
	 * Method: getAvaliablePositions
	 * @author Ziqi Tan
	 * @return the positions which have not been marked
	 * */
	private void getAvaliablePositions(List<positionTicTacToe> board) {
		avaliablePositions = new ArrayList<positionTicTacToe>();
		// (j, k, i) -> (x, y, z)
		for ( int i = 0; i < 4; i++ ) {
			for( int j = 0; j < 4; j++ ) {				
				for( int k = 0; k < 4; k++ ) {					
					if( getStateOfPositionFromBoard(new positionTicTacToe(j,k,i), board) == 0 ) {
						 // The position is not marked
						avaliablePositions.add( new positionTicTacToe(j,k,i) );
					}
				}
			}
		}
		
	}
	
	/**
	 * Method: initializeNextMoveValue
	 * @author Ziqi Tan
	 * */
	private void initializeNextMoveValue(List<positionTicTacToe> board, int player) {
		
		for( int i = 0; i < avaliablePositions.size(); i++ ) {
			List<positionTicTacToe> nextBoard = new ArrayList<positionTicTacToe>(board);
			positionTicTacToe position = avaliablePositions.get(i);
			nextBoard.add(new positionTicTacToe(position.x, position.y, position.z));
			nextMoveEvaluation.put(new positionTicTacToe(position.x, position.y, position.z), evaluator(nextBoard, player));
		}
	}
		
	/**
	 * Method: sequenceCounter
	 * @author Ziqi Tan
	 * 
	 * */
	private void sequenceCounter(int player) {
		boolean opponentFlag = false;
		
		
		
	}
	
	/**
	 * Method: evaluator
	 * @author Ziqi Tan
	 * Function:
	 * 		A helper function to evaluate the current board situation.
	 * 		1. calculate how many positions have been marked by both players.
	 * 		2. 
	 * @param
	 * 		List<positionTicTacToe> board: current board configuration;
	 * 		int player: current Player's turn.
	 * @return
	 * 		int value
	 * */
	private int evaluator(List<positionTicTacToe> board, int player) {
		
		int value = 0;
		
		
		
		return value;
	}
	
	/**
	 * Method: threeSequenceDetector
	 * @author Ziqi Tan
	 * @return Return the force position or null
	 * */
	private positionTicTacToe threeSequenceDetector() {
		positionTicTacToe forcePosition = null;
		
		return forcePosition;
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
	
}
