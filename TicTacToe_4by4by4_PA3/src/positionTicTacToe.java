/**
 * Class positionTicTacToe
 * @author TF in CS 640 at Boston University
 * Function: 
 * A helper class. Sort position information 
 * and its current state for all position in the cube for a particular game.
 * */
public class positionTicTacToe {
	
	int x;
	int y;
	int z;	
	int state;  
	// mark by player 1 or player 2 or null ('X', 'O', '_')
	// { 0 : unmarked, -1 : No meaning, 1 : 'X', 2: 'O' }
	
	/**
	 * Method: printPosition
	 * Function: print positions(x, y, z) and state.
	 * @author TF in CS 640 at Boston University
	 * */
	public void printPosition() {
		System.out.print("("+x+","+y+","+z+")");
		System.out.println("state: " + state);		
	}
	
	/**
	 * Constructor
	 * @author TF in CS 640 at Boston University
	 * */
	positionTicTacToe(int setX, int setY, int setZ, int setState) {
		x = setX;
		y = setY;
		z = setZ;
		state = setState;
	}
	
	/**
	 * Constructor
	 * @author TF in CS 640 at Boston University
	 * */
	positionTicTacToe(int setX, int setY, int setZ) {
		x = setX;
		y = setY;
		z = setZ;
		state = -1;
	}
}