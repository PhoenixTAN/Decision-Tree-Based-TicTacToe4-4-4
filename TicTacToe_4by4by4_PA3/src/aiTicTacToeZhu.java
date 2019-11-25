import java.util.*;
public class aiTicTacToeZhu {

	public int player; //1 for player 1 and 2 for player 2
	private List<List<positionTicTacToe>> winningLines;
	private int index;
	private int getStateOfPositionFromBoard(positionTicTacToe position, List<positionTicTacToe> board)
	{
		//a helper function to get state of a certain position in the Tic-Tac-Toe board by given position TicTacToe
		int index = position.x*16+position.y*4+position.z;
		return board.get(index).state;
	}
	public positionTicTacToe myAIAlgorithmZhu(List<positionTicTacToe> board, int player)
	{
		//TODO: this is where you are going to implement your AI algorithm to win the game. The default is an AI randomly choose any available move.
		//positionTicTacToe myNextMove = new positionTicTacToe(0,0,0);
		index = 0;
		alphabeta(board, 4, Integer.MIN_VALUE, Integer.MAX_VALUE, true, player);
		positionTicTacToe myNextMove = indexToPosition(index);


		return myNextMove;


	}

	public positionTicTacToe myAIAlgorithmRandom(List<positionTicTacToe> board, int player) {
		positionTicTacToe myNextMove = new positionTicTacToe(0,0,0);
		do
			{
				Random rand = new Random();
				int x = rand.nextInt(4);
				int y = rand.nextInt(4);
				int z = rand.nextInt(4);
				myNextMove = new positionTicTacToe(x,y,z);
			}while(getStateOfPositionFromBoard(myNextMove,board)!=0);
		return myNextMove;
	}


	private List<List<positionTicTacToe>> initializeWinningLines()
	{
		//create a list of winning line so that the game will "brute-force" check if a player satisfied any 	winning condition(s).
		List<List<positionTicTacToe>> winningLines = new ArrayList<List<positionTicTacToe>>();
		
		//48 straight winning lines
		//z axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,j,0,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//y axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,j,-1));
				winningLines.add(oneWinCondtion);
			}
		//x axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,j,-1));
				winningLines.add(oneWinCondtion);
			}
		
		//12 main diagonal winning lines
		//xz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,0,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,1,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,2,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//yz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,0,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//xy plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,0,i,-1));
				oneWinCondtion.add(new positionTicTacToe(1,1,i,-1));
				oneWinCondtion.add(new positionTicTacToe(2,2,i,-1));
				oneWinCondtion.add(new positionTicTacToe(3,3,i,-1));
				winningLines.add(oneWinCondtion);
			}
		
		//12 anti diagonal winning lines
		//xz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,3,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,2,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,1,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,0,-1));
				winningLines.add(oneWinCondtion);
			}
		//yz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,3,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,0,-1));
				winningLines.add(oneWinCondtion);
			}
		//xy plane-4
		for(int i = 0; i<4; i++)
			{
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
	public aiTicTacToeZhu(int setPlayer)
	{
		winningLines = initializeWinningLines();
		player = setPlayer;
	}

	private int calculateWinningLines(List<positionTicTacToe> board, int player) {

		int opponent;

		if(player == 1) {
			opponent = 2;
		} else {
			opponent = 1;
		}

		int numberOfWinningLines = 0;

		for (int i = 0;i < winningLines.size();i++)
		{

			positionTicTacToe p0 = winningLines.get(i).get(0);
			positionTicTacToe p1 = winningLines.get(i).get(1);
			positionTicTacToe p2 = winningLines.get(i).get(2);
			positionTicTacToe p3 = winningLines.get(i).get(3);

			int state0 = getStateOfPositionFromBoard(p0,board);
			int state1 = getStateOfPositionFromBoard(p1,board);
			int state2 = getStateOfPositionFromBoard(p2,board);
			int state3 = getStateOfPositionFromBoard(p3,board);

			//if they have the same state (marked by same player) and they are not all marked.
			if (state0 == player || state1 == player || state2 == player || state3 == player ) {
				if(state0 != opponent && state1 != opponent && state2 != opponent && state3 != opponent )
				{
					numberOfWinningLines++;
				}
			}

		}
		return numberOfWinningLines;
	}

	private int isTerminalNode(List<positionTicTacToe> board)
	{
		//test whether the current game is ended

		//brute-force
		for(int i = 0;i < winningLines.size();i++)
		{

			positionTicTacToe p0 = winningLines.get(i).get(0);
			positionTicTacToe p1 = winningLines.get(i).get(1);
			positionTicTacToe p2 = winningLines.get(i).get(2);
			positionTicTacToe p3 = winningLines.get(i).get(3);

			int state0 = getStateOfPositionFromBoard(p0,board);
			int state1 = getStateOfPositionFromBoard(p1,board);
			int state2 = getStateOfPositionFromBoard(p2,board);
			int state3 = getStateOfPositionFromBoard(p3,board);

			//if they have the same state (marked by same player) and they are not all marked.
			if(state0 == state1 && state1 == state2 && state2 == state3 && state0!=0)
			{
				return 1;
			}
		}
		for(int i = 0;i < board.size();i++)
		{
			if(board.get(i).state == 0)
			{
				//game is not ended, continue
				return 0;
			}
		}
		return -1; //call it a draw
	}

	private List<positionTicTacToe> deepCopyATicTacToeBoard(List<positionTicTacToe> board)
	{
		//deep copy of game boards
		List<positionTicTacToe> copiedBoard = new ArrayList<positionTicTacToe>();
		for(int i=0;i<board.size();i++)
		{
			copiedBoard.add(new positionTicTacToe(board.get(i).x,board.get(i).y,board.get(i).z,board.get(i).state));
		}
		return copiedBoard;
	}

	private int alphabeta (List<positionTicTacToe> node, int depth, int a, int b, boolean maximizingPlayer, int player) {
		int value;

		int opponent;
		if(player == 1) {
			opponent = 2;
		} else {
			opponent = 1;
		}

		/*int flag = isTerminalNode(node);
		if(flag != 0){
			int result = calculateWinningLines(node, player);
			if(flag == 1){
				int temp = calculateTerminalValue(node, player);
				result += temp;
				return result;
			}
		}*/
		int temp = isTerminalNode(node);
		if (temp == 1 && maximizingPlayer == false) {
			return 99999;
		} else if (temp == 1 && maximizingPlayer == true) {
			return -99999;
		} else if (temp == -1) {
			return 0;
		}
		if(depth == 0) { // results are unknown. value needs to be set.

			int result = calculateWinningLines(node, player);
			int temp1 = calculateHeuristicValue(node, player);
			result += temp1;

			return result;
		}

		if(maximizingPlayer) {
			value = Integer.MIN_VALUE;
			for (int i = 0; i < node.size(); i++) {
				if (node.get(i).state == 0) {
					List<positionTicTacToe> child = deepCopyATicTacToeBoard(node);
					child.get(i).state = player;
					int newValue = alphabeta(child, depth-1, a, b, false, opponent);
					if (newValue > value) {
						value = newValue;
						index = i;

					}
					a = Math.max(a, value);
					if (a >= b) {
						break;
					}
				}

			}
			return value;
		}
		else {
			value = Integer.MAX_VALUE;
			for (int i = 0; i < node.size(); i++) {
				if (node.get(i).state == 0) {
					List<positionTicTacToe> child = deepCopyATicTacToeBoard(node);
					child.get(i).state = player;
					value = Math.min(value, alphabeta(child, depth-1, a, b, true, opponent));
					b = Math.min(b, value);
					if (a >= b) {
						break;
					}
				}

			}
			return value;

		}
	}
	public int calculateHeuristicValue(List<positionTicTacToe> board, int player){
		int opponent = player == 1 ? 2 : 1;
		int result = 0;
		for(positionTicTacToe pos : board){
			if(pos.state == player){
				int myCount = countMaxConsecutive(board, pos, player);
				int enemyCount = countMaxConsecutive(board, pos, opponent);

				if(myCount >= 1){
					if(myCount >= 2){
						if(myCount >= 3){
							if(myCount == 4){
								result += 100;
							}
							result += 50;
						}
						result += 30;
					}
					result += 5;
				}
				if(enemyCount >= 1){
					if(enemyCount >= 2){
						if(enemyCount >= 3){
							result += 80;
						}
						result += 12;
					}
					result += 3;
				}
			}
		}
		return result;
	}

	public int calculateTerminalValue(List<positionTicTacToe> board, int player){
		int opponent = player == 1 ? 2 : 1;
		int result = 0;
		for(positionTicTacToe pos : board){
			if(pos.state == player){
				int myCount = countMaxConsecutive(board, pos, player);
				if(myCount >= 4){
					result += 200;
					break;
				}
			}
		}
		return result;
	}


	public int countMaxConsecutive(List<positionTicTacToe> board, positionTicTacToe pos, int player){
		int max = Integer.MIN_VALUE;
		int count = 0;
		for(int x = 0; x < 4; x++) {
			if (x == pos.x) {
				continue;
			} else {
				positionTicTacToe newPos = new positionTicTacToe(x, pos.y, pos.z);
				int index = positionToIndex(newPos);
				if (board.get(index).state == player) {
					count++;
				}
			}
		}
		max = Math.max(max, count);
		count = 0;
		for(int y = 0; y < 4; y++){
			if(y == pos.y){
				count++;
			}
			else {
				positionTicTacToe newPos = new positionTicTacToe(pos.x, y, pos.z);
				int index = positionToIndex(newPos);
				if(board.get(index).state == player){
					count++;
				}
			}
		}
		max = Math.max(max, count);
		count = 0;
		for(int z = 0; z < 4; z++){
			if(z == pos.z){
				count++;
			}
			else {
				positionTicTacToe newPos = new positionTicTacToe(pos.x, pos.y, z);
				int index = positionToIndex(newPos);
				if(board.get(index).state == player){
					count++;
				}
			}
		}
		max = Math.max(max, count);

		if(pos.x == pos.y){
			count = 0;
			for(int i = 0; i < 4; i++) {
				if (i == pos.x) {
					count++;
				} else {
					positionTicTacToe newPos = new positionTicTacToe(i, i, pos.z);
					int index = positionToIndex(newPos);
					if (board.get(index).state == player) {
						count++;
					}
				}
			}
			max = Math.max(max, count);
		}
		if(pos.x == pos.z){
			count = 0;
			for(int i = 0; i < 4; i++) {
				if (i == pos.x) {
					count++;
				} else {
					positionTicTacToe newPos = new positionTicTacToe(i, pos.y, i);
					int index = positionToIndex(newPos);
					if (board.get(index).state == player) {
						count++;
					}
				}
			}
			max = Math.max(max, count);
		}
		if(pos.y == pos.z){
			count = 0;
			for(int i = 0; i < 4; i++) {
				if (i == pos.y) {
					count++;
				} else {
					positionTicTacToe newPos = new positionTicTacToe(pos.x, i, i);
					int index = positionToIndex(newPos);
					if (board.get(index).state == player) {
						count++;
					}
				}
			}
			max = Math.max(max, count);
		}
		if(pos.x == pos.y && pos.x == pos.z){
			count = 0;
			for(int i = 0; i < 4; i++) {
				if (i == pos.y) {
					count++;
				} else {
					positionTicTacToe newPos = new positionTicTacToe(i, i, i);
					int index = positionToIndex(newPos);
					if (board.get(index).state == player) {
						count++;
					}
				}
			}
			max = Math.max(max, count);
		}
		return max;
	}


	private positionTicTacToe indexToPosition (int index) {
			int x = index / 16;
			int y = (index % 16) / 4;
			int z = (index % 16) % 4;

			return new positionTicTacToe(x, y, z);
	}
	private int positionToIndex(positionTicTacToe pos){
		int result = 0;
		result += pos.x * 16;
		result += pos.y * 4;
		result += pos.z;
		return result;
	}


}
