
import java.nio.charset.StandardCharsets;
import java.util.*;
public class aiTicTacToeEric {

	private int mplayer; //1 for player 1 and 2 for player 2
	private qubicDictionary qd;
	private static List<List<positionTicTacToe>> winningLines;
	private HashMap<String, Integer> table;
	public static int [][] winningLinesArray;
	private static int [] searchOrder;
	private int [][] rewardMat;
	private int returnChoice;
	private int pnsTimeLimit;
	private int maxDepth;
	private int startDepth;
	private PnS pns;
	private boolean useQd;
	private boolean aborted;
	private long endTime;
	private long startTime;
	private double timeLimit = 10000;
	private int cntNodes;
	private int tableDepth = 3;
	private int [][] countEmpty = new int [3][3];
	private int [] countLine = new int [3];
	private positionTicTacToe fromIntToPosition(int x)
	{
		int i = x/16;
		int j = (x/4) % 4;
		int k = x % 4;
		return new positionTicTacToe(i, j, k);
	}
	private int heuristic(byte [] state){
		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++)
				countEmpty[i][j]=0;
		for(int i=0; i<76;i++){
			countLine[0]=0;
			countLine[1]=0;
			countLine[2]=0;
			for(int j=0;j<4;j++)
				countLine[state[winningLinesArray[i][j]]]++;
			if(countLine[1] == 0 && countLine[2] !=0)
				countEmpty[2][countLine[2]-1]++;
			else if(countLine[1] != 0 && countLine[2] ==0)
				countEmpty[1][countLine[1]-1]++;
		}
		int score = 0;
		for(int i=1;i<=2;i++)
			for(int j=0;j<3;j++)
				if(i==1)
					score += rewardMat[i-1][j] * countEmpty[i][j];
				else
					score -= rewardMat[i-1][j] * countEmpty[i][j];
		return score;
	}
	private int isTerminal(byte [] state){
		int instruction;

		// 1. Check if in pntt
		String s = pns.byte2string(state);
		if(pns.pntt.containsKey(s)){
			int winner = pns.pntt.get(s);
			if(winner == 1)
				return Node.inf;
			else
				return -Node.inf;
		}

		// 2. Check if one player win
		int prod;
		for(int i=0;i<76;i++){
			prod = 1;
			for(int j=0;j<4;j++)
				prod *= state[winningLinesArray[i][j]];
			if(prod == 1) { // player1 win
				return Node.inf;
			}else if(prod == 16) { // player2 win
				return -Node.inf;
			}
		}
		// 3. This means the state is not a terminal node
		return 0;


	}
	private int checkEasy(byte [] state){
		int instruction;
		// 1. Check if we can win directly
//		instruction = checkToWin(state, mplayer);
//		if(instruction != -1) {
//			return instruction;
//		}
//
//		// 2. Check if our opponent will win if we do not defend.
//		instruction = checkToWin(state, 3-mplayer);
//		if(instruction != -1)
//			return instruction;

		// 3. If we are first, use the strategy table.
		if(useQd) {
			instruction = qd.getInstruction(state);
			if (instruction != -1) {
				return instruction;
			}
		}

		// 4. Proof-number search Allis, L.V. 1991
		pns = new PnS(pnsTimeLimit, winningLinesArray);
		Node Root = new Node(state);
		Root.type = (byte) mplayer;
		pns.search(Root);
		return -1;
	}
	private int getScore(byte [] info)
	{
		String s;
//		s = pns.byte2string(info);
//		if (table.containsKey(s)) {
//				return table.get(s);
//			}
		byte [][] similar = new byte[192][64];
		for(int a=0; a<64; a++)
		{
			similar[0][a] = info[a];
		}
		for(int i=1;i<192;i++)
			for(int j=0;j<64;j++)
				similar[i][j] = similar[0][qd.automorphisms[i][j]];

		for(int i=0;i<192;i++) {
			s = pns.byte2string(similar[i]);
			if (table.containsKey(s)) {
				return table.get(s);
			}
		}
		return Node.inf*2;
	}
	private int alphaBetaSearch(byte [] state, int depth, int player, boolean needInstruction, int alpha, int beta){
		// 0. Check Time
		if(startDepth - depth <= 4) {
			endTime = System.currentTimeMillis();
			if (endTime + 100 > startTime + timeLimit) {
				aborted = true;
				return 0;
			}
		}
		int instruction, score;
		// 1. Check if it is terminal.
		if(startDepth - depth <= tableDepth) {
			score = getScore(state);
			if(score != Node.inf*2)
				return score;
		}
		cntNodes ++;
		if(needInstruction == false) {
			score = isTerminal(state);
			if (score != 0) {
				if(startDepth - depth <= tableDepth) table.put(pns.byte2string(state), score);
				return score;
			}
		}
		if(depth == 0) {
			score = heuristic(state);
			if(startDepth - depth <= tableDepth) table.put(pns.byte2string(state), score);
			return score;
		}

		// 2. Check whether the move is forced
		// 2.1 Check if we can win directly
		instruction = checkToWin(state, player);
		if(instruction != -1) {
			state[instruction] = (byte) player;
			score = alphaBetaSearch(state, depth-1, 3-player, false, alpha, beta);
			state[instruction] = 0;
			if(needInstruction) {
				returnChoice = instruction;
				//System.out.println(returnChoice);
			}
			if(startDepth - depth <= tableDepth) table.put(pns.byte2string(state), score);
			return score;
		}

		// 2.2. Check if our opponent will win if we do not defend
		instruction = checkToWin(state, 3-player);
		if(instruction != -1) {
			state[instruction] = (byte) player;
			score = alphaBetaSearch(state, depth-1, 3-player, false, alpha, beta);
			// do not decrease depth because we are forced
			state[instruction] = 0;
			if(needInstruction){
				returnChoice = instruction;
				//System.out.println(returnChoice);
			}
			if(startDepth - depth <= tableDepth) table.put(pns.byte2string(state), score);
			return score;
		}

		//3. minimax
		int si= 0 ;
		if(player == 1){ // max node
			score = -2*Node.inf;
			for(int i=0;i<64;i++){
				si = searchOrder[i];
				if(state[si] != 0)
					continue;
				state[si] = (byte) player;
				int tscore = alphaBetaSearch(state, depth-1, 3-player, false, alpha, beta);
				state[si] = 0;
				if(tscore > score){
					score = tscore;
					if(needInstruction == true) {
						returnChoice = si;
						//System.out.println(returnChoice);
					}
				}
				if(score > alpha)
					alpha = score;
				if(alpha >= beta){
					return score;
				}
				if(score == Node.inf){
					break;
				}
			}
		}else{ // min node
			score = 2*Node.inf;
			for(int i=0;i<64;i++) {
				si = searchOrder[i];
				if (state[si] != 0)
					continue;
				state[si] = (byte) player;
				int tscore = alphaBetaSearch(state, depth-1, 3-player, false, alpha, beta);
				state[si] = 0;
				if (tscore < score) {
					score = tscore;
					if(needInstruction == true) {
						//System.out.println(si);
						returnChoice = si;
					}
				}
				if(score < beta)
					beta = score;
				if(alpha >= beta)
					return score;
				if(score == -Node.inf){
					break;
				}
			}
		}
		if(startDepth - depth <= tableDepth) table.put(pns.byte2string(state), score);
		return score;
	}
	public positionTicTacToe myAIAlgorithm(List<positionTicTacToe> board, int player)
	{
		startTime = System.currentTimeMillis();
		int instruction;
		// 1. Copy state
		byte [] state = new byte[64];
		for (int a = 0; a < 64; a++) {
			state[a] = (byte) board.get(a).state;
		}

		// 2. Check whether it is easy
		instruction = checkEasy(state);
		if(instruction != -1){
			System.out.printf("Dict choice: %d\n",instruction);
			return fromIntToPosition(instruction);
		}
		double pnsTime = System.currentTimeMillis() - startTime;
		// 3. Alpha-beta search
		int i, score=0, finalChoice = -1, lastNodes=0;
		endTime=0;
		double lastTime = 0;
		for(i=0;i<64;i+=1) {
			aborted = false;
			cntNodes=0;
			table = new HashMap<>();
			startDepth = maxDepth+i;
			long miniStartTime = System.currentTimeMillis();
			int tscore = alphaBetaSearch(state, startDepth, player, true, -Node.inf, Node.inf);
			long miniEndTime = System.currentTimeMillis();
			double miniTotalTime = miniEndTime - miniStartTime;
			endTime = System.currentTimeMillis();
			if(aborted || endTime + 100 > startTime + timeLimit)
				break;
			//System.out.printf("rc:%d\n", returnChoice);
			lastTime = miniTotalTime;
			lastNodes = cntNodes;
			score = tscore;
			finalChoice = returnChoice;
			if(tscore == Node.inf || tscore == -Node.inf)
				break;
		}
		double TotalTime = endTime - startTime;
		if(aborted)
			startDepth--;
		System.out.printf("Player: %d, final score: %d, choice: %d, cntNodes: %d, sd: %d, time: %.4f, ptime:%.4f, ltime:%.4f\n",
				player, score, finalChoice, lastNodes, startDepth, TotalTime/1000.0, pnsTime/1000.0, lastTime/1000.0);
		return fromIntToPosition(finalChoice);

	}
	private int checkLineInState(byte [] state, int [] line)
	{
		int [] count={0,0,0};
		for(int i=0;i<4;i++)
			count[state[line[i]]] += 1;
		if(count[0]==1 && count[1] == 3)
			return 1;
		if(count[0]==1 && count[2] == 3)
			return 2;
		return 0;
	}
	private int checkToWin(byte [] state, int player)
	{
		for(int i=0;i<76;i++)
		{
			int [] curLine = winningLinesArray[i];
			if(checkLineInState(state, curLine) == player)
			{
				for(int j=0;j<4;j++)
					if(state[curLine[j]] == 0)
						return curLine[j];
			}
		}
		return -1;
	}
	private List<List<positionTicTacToe>> initializeWinningLines() {
		//create a list of winning line so that the game will "brute-force" check if a player satisfied any 	winning condition(s).
		List<List<positionTicTacToe>> winningLines = new ArrayList<List<positionTicTacToe>>();
		winningLinesArray = new int[76][4];
		//48 straight winning lines
		//z axis winning lines
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i, j, 0, -1));
				oneWinCondtion.add(new positionTicTacToe(i, j, 1, -1));
				oneWinCondtion.add(new positionTicTacToe(i, j, 2, -1));
				oneWinCondtion.add(new positionTicTacToe(i, j, 3, -1));
				winningLines.add(oneWinCondtion);
			}
		//y axis winning lines
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i, 0, j, -1));
				oneWinCondtion.add(new positionTicTacToe(i, 1, j, -1));
				oneWinCondtion.add(new positionTicTacToe(i, 2, j, -1));
				oneWinCondtion.add(new positionTicTacToe(i, 3, j, -1));
				winningLines.add(oneWinCondtion);
			}
		//x axis winning lines
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0, i, j, -1));
				oneWinCondtion.add(new positionTicTacToe(1, i, j, -1));
				oneWinCondtion.add(new positionTicTacToe(2, i, j, -1));
				oneWinCondtion.add(new positionTicTacToe(3, i, j, -1));
				winningLines.add(oneWinCondtion);
			}

		//12 main diagonal winning lines
		//xz plane-4
		for (int i = 0; i < 4; i++) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(0, i, 0, -1));
			oneWinCondtion.add(new positionTicTacToe(1, i, 1, -1));
			oneWinCondtion.add(new positionTicTacToe(2, i, 2, -1));
			oneWinCondtion.add(new positionTicTacToe(3, i, 3, -1));
			winningLines.add(oneWinCondtion);
		}
		//yz plane-4
		for (int i = 0; i < 4; i++) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(i, 0, 0, -1));
			oneWinCondtion.add(new positionTicTacToe(i, 1, 1, -1));
			oneWinCondtion.add(new positionTicTacToe(i, 2, 2, -1));
			oneWinCondtion.add(new positionTicTacToe(i, 3, 3, -1));
			winningLines.add(oneWinCondtion);
		}
		//xy plane-4
		for (int i = 0; i < 4; i++) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(0, 0, i, -1));
			oneWinCondtion.add(new positionTicTacToe(1, 1, i, -1));
			oneWinCondtion.add(new positionTicTacToe(2, 2, i, -1));
			oneWinCondtion.add(new positionTicTacToe(3, 3, i, -1));
			winningLines.add(oneWinCondtion);
		}

		//12 anti diagonal winning lines
		//xz plane-4
		for (int i = 0; i < 4; i++) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(0, i, 3, -1));
			oneWinCondtion.add(new positionTicTacToe(1, i, 2, -1));
			oneWinCondtion.add(new positionTicTacToe(2, i, 1, -1));
			oneWinCondtion.add(new positionTicTacToe(3, i, 0, -1));
			winningLines.add(oneWinCondtion);
		}
		//yz plane-4
		for (int i = 0; i < 4; i++) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(i, 0, 3, -1));
			oneWinCondtion.add(new positionTicTacToe(i, 1, 2, -1));
			oneWinCondtion.add(new positionTicTacToe(i, 2, 1, -1));
			oneWinCondtion.add(new positionTicTacToe(i, 3, 0, -1));
			winningLines.add(oneWinCondtion);
		}
		//xy plane-4
		for (int i = 0; i < 4; i++) {
			List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
			oneWinCondtion.add(new positionTicTacToe(0, 3, i, -1));
			oneWinCondtion.add(new positionTicTacToe(1, 2, i, -1));
			oneWinCondtion.add(new positionTicTacToe(2, 1, i, -1));
			oneWinCondtion.add(new positionTicTacToe(3, 0, i, -1));
			winningLines.add(oneWinCondtion);
		}

		//4 additional diagonal winning lines
		List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0, 0, 0, -1));
		oneWinCondtion.add(new positionTicTacToe(1, 1, 1, -1));
		oneWinCondtion.add(new positionTicTacToe(2, 2, 2, -1));
		oneWinCondtion.add(new positionTicTacToe(3, 3, 3, -1));
		winningLines.add(oneWinCondtion);

		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0, 0, 3, -1));
		oneWinCondtion.add(new positionTicTacToe(1, 1, 2, -1));
		oneWinCondtion.add(new positionTicTacToe(2, 2, 1, -1));
		oneWinCondtion.add(new positionTicTacToe(3, 3, 0, -1));
		winningLines.add(oneWinCondtion);

		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(3, 0, 0, -1));
		oneWinCondtion.add(new positionTicTacToe(2, 1, 1, -1));
		oneWinCondtion.add(new positionTicTacToe(1, 2, 2, -1));
		oneWinCondtion.add(new positionTicTacToe(0, 3, 3, -1));
		winningLines.add(oneWinCondtion);

		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0, 3, 0, -1));
		oneWinCondtion.add(new positionTicTacToe(1, 2, 1, -1));
		oneWinCondtion.add(new positionTicTacToe(2, 1, 2, -1));
		oneWinCondtion.add(new positionTicTacToe(3, 0, 3, -1));
		winningLines.add(oneWinCondtion);

		for(int i=0;i<76;i++)
			for(int j=0;j<4;j++) {
				positionTicTacToe p = winningLines.get(i).get(j);
				winningLinesArray[i][j] = 16*p.x + 4*p.y + p.z;
			}
		return winningLines;

	}
	public aiTicTacToeEric(int setPlayer, int d, int ptl, boolean uqd, int r1, int r2, int r3)
	{
		mplayer = setPlayer;
		qd = new qubicDictionary(setPlayer);
		winningLines = initializeWinningLines();
		useQd = uqd;
		maxDepth = d;
		pnsTimeLimit = ptl;
		rewardMat = new int [][] {{r1,r2,r3},{r1,r2,r3}};
		searchOrder = new int [] {0,3,12,15,21,22,25,26,37,38,41,42,48,51,60,63,
				1,2,4,5,6,7,8,9,10,11,13,14,
		16,17,18,19,20,23,24,27,28,29,30,31,
		32,33,34,35,36,39,40,43,44,45,46,47,
		49,50,52,53,54,55,56,57,58,59,61,62};
	}
	public aiTicTacToeEric(int setPlayer){
		mplayer = setPlayer;
		qd = new qubicDictionary(setPlayer);
		winningLines = initializeWinningLines();
		useQd = true;
		maxDepth = 4;
		pnsTimeLimit = (int) (timeLimit /2);
		rewardMat = new int [][] {{3,10,30},{3,10,30}};
		searchOrder = new int [] {0,3,12,15,21,22,25,26,37,38,41,42,48,51,60,63,
				1,2,4,5,6,7,8,9,10,11,13,14,
				16,17,18,19,20,23,24,27,28,29,30,31,
				32,33,34,35,36,39,40,43,44,45,46,47,
				49,50,52,53,54,55,56,57,58,59,61,62};
	}
}
class PnS{
	private int timeLimit;
	private int [][] winningLinesArray;
	public static HashMap<String, Byte> pntt;
	public PnS(int limit, int [][] wla)
	{
		timeLimit = limit;
		winningLinesArray = wla;
		if(pntt==null)
			pntt = new HashMap<String, Byte>();
		//System.out.println(pntt.size());
	}

	public String byte2string(byte [] a){
		return new String(a, StandardCharsets.US_ASCII);
	}

	public void search(Node Root){
		evaluate(Root);
		setProofAndDisproofNumbers(Root);
		Node current = Root;
		Node mostProving;
		long beginTime = System.currentTimeMillis();
		long endTime;
		int count = 0;
		while(Root.proof != 0 && Root.disproof != 0)
		{
			if(count % 1000 == 0){
				endTime = System.currentTimeMillis();
				if(endTime - beginTime > timeLimit)
					break;
			}
			count++;
			mostProving = selectMostProvingNode(current);
			expandNode(mostProving);
			current = updateAncestors(mostProving, Root);
		}
		System.out.printf("pc: %d, ps: %d, ", count, pntt.size());
	}

	private void evaluate(Node n){
		byte [] state = n.state;
		String s = byte2string(state);
		if(pntt.containsKey(s)){
			n.value = (byte) (3-pntt.get(s)*2);
			return;
		}
		int prod;
		for(int i=0;i<76;i++){
			prod = 1;
			for(int j=0;j<4;j++)
				prod *= state[winningLinesArray[i][j]];
			if(prod == 1) {
				n.value = 1;
				return;
			}else if(prod == 16) {
				n.value = -1;
				return;
			}
		}
		n.value = 0;
		return;
	}

	void setProofAndDisproofNumbers(Node n){
		if(n.expanded)
			if(n.type == 2) { // and
				n.proof = 0;
				n.disproof = n.inf;
				for(Node c:n.Children){
					n.proof += c.proof;
					if(c.disproof < n.disproof)
						n.disproof = c.disproof;
				}
			}else { // or
				n.proof = n.inf;
				n.disproof = 0;
				for(Node c:n.Children){
					n.disproof += c.disproof;
					if(c.proof<n.proof)
						n.proof = c.proof;
				}
			}
		else{
			if(n.value == 1) { // proof
				n.proof = 0;
				n.disproof = n.inf;
			}else if(n.value == -1){
				n.disproof = 0;
				n.proof = n.inf;
			}else{
				n.proof = 1;
				n.disproof = 1;
			}
		}
		if(n.proof == 0){ // proved, store it
			String s = byte2string(n.state);
			pntt.put(s, (byte)1);
		}else if(n.disproof == 0){
			String s = byte2string(n.state);
			pntt.put(s, (byte)2);
		}
	}

	private Node selectMostProvingNode(Node n){
		int value = Node.inf*10;
		Node best=n;
		while(n.expanded){
			if(n.type == 1){ // OR
				for(Node c:n.Children)
					if(value > c.proof){
						best = c;
						value = c.proof;
					}}
			else { // AND
				for(Node c:n.Children)
					if(value > c.disproof){
						best = c;
						value = c.disproof;
					}
			}
			n = best;
			value = Node.inf*10;
		}
		return n;
	}

	private void generateChildren(Node n){
		int step;
		step = checkToWin(n.state, n.type);
		if(step == -1)
			step = checkToWin(n.state, 3-n.type);
		if(step != -1){ // either close to win or lose
			Node child = new Node(n.state);
			child.lastStep = (byte) step;
			child.Parent = n;
			child.type = (byte) (3-n.type);
			child.state[step] = n.type;
			n.Children.add(child);
			return;
		}

		for(int i=0;i<64;i++){
			if(n.state[i] != 0)
				continue;
			Node child = new Node(n.state);
			child.lastStep = (byte)i;
			child.Parent = n;
			child.type = (byte) (3-n.type);
			child.state[i] = n.type;
			n.Children.add(child);
		}
		return;
	}

	private void expandNode(Node n){
		generateChildren(n);
		for(Node c:n.Children){
			evaluate(c);
			setProofAndDisproofNumbers(c);
			if((n.type == 1 && c.proof == 0) || (n.type == 2 && c.disproof == 0))
				break;
		}
		n.expanded = true;
	}

	private Node updateAncestors(Node n, Node Root){
		while(true){
			int oldProof = n.proof;
			int oldDisProof = n.disproof;
			setProofAndDisproofNumbers(n);
			if(n.proof == oldProof && n.disproof == oldDisProof)
				return n;
			if(n == Root)
				break;
			n = n.Parent;
		}
		return n;
	}

	private int checkLineInState(byte [] state, int [] line)
	{
		int [] count={0,0,0};
		for(int i=0;i<4;i++)
			count[state[line[i]]] += 1;
		if(count[0]==1 && count[1] == 3)
			return 1;
		if(count[0]==1 && count[2] == 3)
			return 2;
		return 0;
	}
	private int checkToWin(byte [] state, int player)
	{
		for(int i=0;i<76;i++)
		{
			int [] curLine = winningLinesArray[i];
			if(checkLineInState(state, curLine) == player)
			{
				for(int j=0;j<4;j++)
					if(state[curLine[j]] == 0)
						return curLine[j];
			}
		}
		return -1;
	}
}

class Node{
	static final int inf=1000000;
	boolean expanded=false; // For a non-terminal leaf, whether we have expanded it.
	List<Node> Children;
	Node Parent;
	int proof;
	int disproof;
	byte type; // 1 for OR, 2 for AND
	byte value; // 1 for first winning, -1 for second winning, 0 for unknown
	byte [] state; // chess board info
	byte lastStep; // the step to here
	public Node(byte [] s){
		state = new byte [64];
		for(int i=0;i<64;i++)
			state[i] = s[i];
		Children = new ArrayList<Node>();
	}
}

class qubicDictionary {
	public static int [][] automorphisms;
	private static int [][] rotationAutomorphisms;
	private static int [][] otherAutomorphisms;
	private static HashMap<String, String> dict;
	private int player;
	private String order;
	private HashMap<String, Node> hm;
	private void setRotationAutomorphisms()
	{
		rotationAutomorphisms = new int [24][64];
		int [][][][] ra3d = new int [24][4][4][4];
		for(int a=0; a<64; a++)
		{
			int i = a / 16;
			int j = (a / 4) % 4;
			int k = a % 4;
			ra3d[0][i][j][k] = a;
		}
		rotateClockwise90(ra3d[1], ra3d[0]);
		rotateClockwise90(ra3d[2], ra3d[1]);
		rotateClockwise90(ra3d[3], ra3d[2]);
		rotateFront90(ra3d[4], ra3d[0]);
		rotateFront90(ra3d[8], ra3d[4]);
		rotateFront90(ra3d[12],ra3d[8]);
		rotateFront90(ra3d[16], ra3d[1]);
		rotateFront90(ra3d[20], ra3d[3]);
		for(int i=4; i<=20; i+=4)
			for(int j=1;j<4;j++)
				rotateClockwise90(ra3d[i+j], ra3d[i+j-1]);

		for(int b=0;b<24;b++)
			for(int a=0; a<64; a++)
			{
				int i = a / 16;
				int j = (a / 4) % 4;
				int k = a % 4;
				rotationAutomorphisms[b][a] = ra3d[b][i][j][k];
			}

	}
	private void rotateClockwise90(int [][][] tgt, int [][][] src)
	{
		for(int k=0;k<4;k++)
			for(int i=0;i<4;i++)
				for(int j=0;j<4;j++)
					tgt[k][j][3-i] = src[k][i][j];
	}
	private void rotateFront90(int [][][] tgt, int [][][] src)
	{
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++)
				for(int k=0;k<4;k++)
					tgt[3-j][i][k] = src[i][j][k];
	}
	private void setOtherAutomorphisms()
	{
		otherAutomorphisms = new int [8][64];
		int [][][][] oa3d = new int [8][4][4][4];
		int [][] op = {
				{0,1,2,3},
				{1,0,3,2},
				{0,2,1,3},
				{2,0,3,1},
				{1,3,0,2},
				{3,1,2,0},
				{2,3,0,1},
				{3,2,1,0}};
		for(int a=0; a<64; a++)
		{
			int i = a / 16;
			int j = (a / 4) % 4;
			int k = a % 4;
			oa3d[0][i][j][k] = a;
		}
		for(int a=1;a<8;a++)
			for(int i=0;i<4;i++)
				for(int j=0;j<4;j++)
					for(int k=0;k<4;k++)
						oa3d[a][i][j][k] = oa3d[0][op[a][i]][op[a][j]][op[a][k]];
		for(int b=0;b<8;b++)
			for(int a=0; a<64; a++)
			{
				int i = a / 16;
				int j = (a / 4) % 4;
				int k = a % 4;
				otherAutomorphisms[b][a] = oa3d[b][i][j][k];
			}
	}
	private void automorphismInitializer()
	{
		automorphisms = new int[192][64];
		setOtherAutomorphisms();
		setRotationAutomorphisms();
		for(int r=0;r<24;r++)
			for(int o=0;o<8;o++)
				for(int i=0;i<64;i++)
					automorphisms[r*8+o][i] = rotationAutomorphisms[r][otherAutomorphisms[o][i]];
	}
	private String arrayToString(int [] a)
	{
		String result = "";
		int distance=0;
		for(int i=0;i<64;i++) {
			if (a[i] == 0)
				distance++;
			else {
				if (distance > 0)
					result = result + String.valueOf(distance);
				distance = 0;
				if (a[i] == player)
					result = result + "x";
				else
					result = result + "o";
			}
		}
		return result;
	}
	public int getInstruction(byte[] info)
	{
		int [][] similar = new int[192][64];
		String s;
		for(int a=0; a<64; a++)
		{
			similar[0][a] = info[a];
		}
		//System.out.println(Arrays.toString(similar[0]));
		for(int i=1;i<192;i++)
			for(int j=0;j<64;j++)
				similar[i][j] = similar[0][automorphisms[i][j]];

		for(int i=0;i<192;i++) {
			s = arrayToString(similar[i]);
			if (dict.containsKey(s)) {
				System.out.printf("Player: %d, Find i : %d, %s ", player, i, s);
				order = s;
				return automorphisms[i][Integer.valueOf(dict.get(s))];
			}
		}

		return -1;

	}
	public qubicDictionary(int p)
	{
		player = p;
		//System.out.printf("Set player: %d\n", p);
		automorphismInitializer();
		dict = new HashMap<String, String>();
		for(int i=0;i<dict_array.length;i++)
			dict.put(dict_array[i][0], dict_array[i][1]);
//		preprocessForSecond();
//		for(int i=0;i<2929;i++){
//			int l1 = dict_array[i][0].length();
//			int l2 = dict_array[i][0].replace("o","").replace("x","").length();
//			if(l1 - l2>=16){
//				System.out.println(dict_array[i][0]);
//			}
//		}
	}
	public void preprocessForSecond(){
		String[] endPoints =
				new String[]{"xo1xo4x2o8xx2o9o2x1xoo8x8o",
						"xo1xo7x4x2ooox1o8o1oxx13x",
						"xo1xo7x4x3oox1o12x2xx1o6o11o",
						"xo1xoxoo4x7xoo15o1ox12xx",
						"xo1xoxoo4x7xoo15o2x12xx4o",
						"xo1xoxoo4x8oox14o2x12xx7o",
						"xo1xoxoo4x8oox14o2x1o10xx",
						"xx1o1o2o3x2x5ox2o2o8o4o10x5xx",
						"xx1o4o3x8ox2o2ox11ox11o2x2x2o",
						"xx1oo1x1oooxo8o3x15xxo19x",
						"xx3o2o3xo1x1o3o3o3x7o4x6x3x9o",
						"xx3o2o3xo1x5o3o4o2o3o4x2x3x3x",
						"xx3o2o3xo1x5o3o7o3o4x2x3x3x6o",
						"xxo5o3x8ox2o2ox11ox11o2x2x2o",
						"xxox1xo3o1x1xo5o6o12o18xo1x"};
		hm = new HashMap<>();
		byte [] state = new byte [64];
		Node root = new Node(state);
		hm.put("", root);
		pfsOne(root);
		System.out.println("Here");
		for(int i=0;i<15;i++){
			if(!hm.containsKey(endPoints[i])){
				System.out.println("Not contain");
				continue;}
			Node n = hm.get(endPoints[i]);
			while(n != root){
				int [] t = new int [64];
				for(int j=0;j<64;j++)
					t[j] = n.Parent.state[j];
				System.out.printf("%s %d\n", arrayToString(t), n.lastStep);
				n = n.Parent;
			}
			System.out.println("");
		}

	}
	private void pfsOne(Node n){
		int lastInstruction = getInstruction(n.state);
		n.state[lastInstruction] = 1;
		for(int i=0;i<64;i++){
			if(n.state[i] != 0)
				continue;
			n.state[i]=2;
			int instruction = getInstruction(n.state);
			if(instruction == -1 || hm.containsKey(order)) {
				n.state[i]=0;
				continue;
			}
			Node child = new Node(n.state);
			child.Parent = n;
			child.lastStep = (byte)i;
			hm.put(order, child);
			n.state[i]=0;
			pfsOne(child);
		}
	}
	private static final String [][] dict_array={
			{"ox1o12x3ox3o10x3o5o4o6x2x","56"},
			{"ox1o12x3ox3o10x3o6o4o6x","63"},
			{"ox1o12x3ox3o10x10o4o","60"},
			{"ox1o12x4x4o10x10o4o","22"},
			{"ox1o12x4x4o21o","37"},
			{"o2o12x4x26o","1"},
			{"o20x26o","16"},
			{"o","21"},
			{"", "0"},
			{"x14o", "12"},
			{"x20o", "48"},
			{"x21o", "3"},
			{"x22o", "3"},
			{"x26o", "3"},
			{"x2o", "12"},
			{"x2x16o10o", "48"},
			{"x2x16o19o", "15"},
			{"x2x16o1o", "15"},
			{"x2x16o20o", "15"},
			{"x2x16o21o", "12"},
			{"x2x16o22o", "12"},
			{"x2x16o23o", "12"},
			{"x2x16o24o", "12"},
			{"x2x16o25o", "12"},
			{"x2x16o26o", "15"},
			{"x2x16o2o", "12"},
			{"x2x16o39o", "48"},
			{"x2x16o3o", "15"},
			{"x2x16o40o", "15"},
			{"x2x16o41o", "15"},
			{"x2x16o42o", "12"},
			{"x2x16o4o", "15"},
			{"x2x16o5o", "15"},
			{"x2x16o6o", "15"},
			{"x2x16o7o", "12"},
			{"x2x16o8o", "15"},
			{"x2x16o9o", "12"},
			{"x2x16oo", "12"},
			{"x2x17o19o", "12"},
			{"x2x17o20o", "12"},
			{"x2x17o22o", "48"},
			{"x2x17o23o", "12"},
			{"x2x17o24o", "12"},
			{"x2x17o25o", "15"},
			{"x2x17o2o", "48"},
			{"x2x17o38o", "48"},
			{"x2x17o39o", "12"},
			{"x2x17o3o", "12"},
			{"x2x17o40o", "12"},
			{"x2x17o41o", "15"},
			{"x2x17o4o", "48"},
			{"x2x17o5o", "48"},
			{"x2x17o6o", "12"},
			{"x2x17o7o", "12"},
			{"x2x17o8o", "15"},
			{"x2x17o9o", "15"},
			{"x2x17oo", "12"},
			{"x2x1o14o", "51"},
			{"x2x1o14o27o2x", "63"},
			{"x2x1o14o30o8x", "48"},
			{"x2x1o15o", "48"},
			{"x2x1o15o26o14x", "51"},
			{"x2x1o15o26x2o", "60"},
			{"x2x1o15o29o8x", "48"},
			{"x2x1o16o", "51"},
			{"x2x1o16o25o14x", "51"},
			{"x2x1o16o25o2x", "63"},
			{"x2x1o16o28o8x", "48"},
			{"x2x1o17o", "48"},
			{"x2x1o17o24o14x", "51"},
			{"x2x1o17o24x2o", "60"},
			{"x2x1o18o", "51"},
			{"x2x1o18o23o2x", "63"},
			{"x2x1o21o", "48"},
			{"x2x1o21o20x2o", "60"},
			{"x2x1o30o", "51"},
			{"x2x1o30o11o2x", "63"},
			{"x2x1o33o", "48"},
			{"x2x1o33o8x2o", "60"},
			{"x2x1o34o", "51"},
			{"x2x1o34o10o8x", "48"},
			{"x2x1o34o7o2x", "63"},
			{"x2x1o35o", "51"},
			{"x2x1o35o6o14x", "51"},
			{"x2x1o35o6o2x", "63"},
			{"x2x1o35o9o8x", "48"},
			{"x2x1o36o", "48"},
			{"x2x1o36o5o14x", "51"},
			{"x2x1o36o5x2o", "60"},
			{"x2x1o36o8o8x", "48"},
			{"x2x1o37o", "48"},
			{"x2x1o37o4o14x", "51"},
			{"x2x1o37o4x2o", "60"},
			{"x2x1o42o", "63"},
			{"x2x1o42o11o2x", "51"},
			{"x2x1o42o2x10o", "63"},
			{"x2x1o42o2x1o", "63"},
			{"x2x1o42o2x2o", "63"},
			{"x2x1o42o2x6o", "63"},
			{"x2x1o42o2x8o", "63"},
			{"x2x1o42x2o11o", "60"},
			{"x2x1o42x2o1o", "60"},
			{"x2x1o42x2o6o", "60"},
			{"x2x1o42x2o9o", "60"},
			{"x2x1o45o", "60"},
			{"x2x1o45o8x2o", "48"},
			{"x2x1o4o4o1o30x2x", "60"},
			{"x2x1o4o4o2o29x2x", "60"},
			{"x2x1o54o", "51"},
			{"x2x1o55o", "48"},
			{"x2x1o56o", "51"},
			{"x2x1o57o", "48"},
			{"x2x1o6o", "48"},
			{"x2x1o6o20o14x", "60"},
			{"x2x1o6o20o7o6x11x", "63"},
			{"x2x1o6o28o6x2o8x", "63"},
			{"x2x1o6o2x10o21x2o", "63"},
			{"x2x1o6o2x5o29o11x", "48"},
			{"x2x1o6o2x5oo2x25o11x", "37"},
			{"x2x1o6o2x8o11x11o", "63"},
			{"x2x1o6o2x8x11o11o", "63"},
			{"x2x1o6o35x2o", "60"},
			{"x2x1o6o5o22o6x11x", "63"},
			{"x2x1o6o5o29x", "60"},
			{"x2x1o6o5o3o25x11x", "63"},
			{"x2x1o6o9o10o14x11x", "63"},
			{"x2x1o6o9o25x2o8x", "63"},
			{"x2x1o6x10o10o", "60"},
			{"x2x1o6x10o14o9o2x", "60"},
			{"x2x1o6x10o16o", "51"},
			{"x2x1o6x10o17o6o11x", "63"},
			{"x2x1o6x10o18o", "60"},
			{"x2x1o6x10o19o", "60"},
			{"x2x1o6x10o21o", "60"},
			{"x2x1o6x10o24o", "60"},
			{"x2x1o6x10o26o", "60"},
			{"x2x1o6x10o2o", "60"},
			{"x2x1o6x10o35o", "60"},
			{"x2x1o6x10o37o", "60"},
			{"x2x1o6x10o38o", "60"},
			{"x2x1o6x10o3o", "60"},
			{"x2x1o6x10o6o", "60"},
			{"x2x1o6x10o9o", "60"},
			{"x2x1o6x13o12o", "60"},
			{"x2x1o6x13o24o", "60"},
			{"x2x1o6x13o28o", "60"},
			{"x2x1o6x13o32o", "60"},
			{"x2x1o6x13o6o14x11o", "63"},
			{"x2x1o6x13oo", "60"},
			{"x2x1o6x14o10o9o2x", "22"},
			{"x2x1o6x14o11o", "51"},
			{"x2x1o6x14o13o6o11x", "63"},
			{"x2x1o6x14o14o", "60"},
			{"x2x1o6x14o18o", "60"},
			{"x2x1o6x14o20o", "60"},
			{"x2x1o6x14o23o", "60"},
			{"x2x1o6x14o28o", "51"},
			{"x2x1o6x14o33o", "51"},
			{"x2x1o6x14o35o", "60"},
			{"x2x1o6x14o5o", "60"},
			{"x2x1o6x14o6o", "60"},
			{"x2x1o6x14o8o", "51"},
			{"x2x1o6x14o9o", "60"},
			{"x2x1o6x18o19o", "60"},
			{"x2x1o6x20o11o", "60"},
			{"x2x1o6x20o12o", "60"},
			{"x2x1o6x20o28o", "60"},
			{"x2x1o6x20o29o", "60"},
			{"x2x1o6x20o3o10x11o", "63"},
			{"x2x1o6x20o4o9x11o", "63"},
			{"x2x1o6x20o5o", "60"},
			{"x2x1o6x20o7o6x11o", "63"},
			{"x2x1o6x20o8o", "60"},
			{"x2x1o6x20o9o", "60"},
			{"x2x1o6x21o11o", "60"},
			{"x2x1o6x21o13o", "60"},
			{"x2x1o6x21o16o", "60"},
			{"x2x1o6x21o27o", "60"},
			{"x2x1o6x21o28o", "60"},
			{"x2x1o6x21o2o10x11o", "63"},
			{"x2x1o6x21o3o3o3x", "60"},
			{"x2x1o6x21o4o", "60"},
			{"x2x1o6x21o6o6o11x", "63"},
			{"x2x1o6x21o7o", "60"},
			{"x2x1o6x21o8o", "60"},
			{"x2x1o6x24o13o", "60"},
			{"x2x1o6x24o17o", "51"},
			{"x2x1o6x24o1o", "60"},
			{"x2x1o6x24o21o", "60"},
			{"x2x1o6x24o5o", "60"},
			{"x2x1o6x25o12o", "60"},
			{"x2x1o6x25o21o", "48"},
			{"x2x1o6x25o3o", "48"},
			{"x2x1o6x25o4o", "60"},
			{"x2x1o6x25o4o4o2x", "60"},
			{"x2x1o6x25o6o2o2x", "63"},
			{"x2x1o6x25o7o1o2x", "63"},
			{"x2x1o6x25o9o2x10o", "63"},
			{"x2x1o6x25o9o2x11o", "60"},
			{"x2x1o6x25o9o2x4o", "63"},
			{"x2x1o6x25o9o2x8o", "63"},
			{"x2x1o6x25o9o2x9o", "63"},
			{"x2x1o6x25o9o2xo", "63"},
			{"x2x1o6x25o9o2xo3x3o", "63"},
			{"x2x1o6x25o9o2xx3o3o", "63"},
			{"x2x1o6x25oo8o2x", "37"},
			{"x2x1o6x26o11o", "60"},
			{"x2x1o6x26o12o", "51"},
			{"x2x1o6x26o22o", "51"},
			{"x2x1o6x26o23o", "60"},
			{"x2x1o6x26o3o", "60"},
			{"x2x1o6x26o6o", "51"},
			{"x2x1o6x26o8o", "60"},
			{"x2x1o6x29o12o", "48"},
			{"x2x1o6x29o16o", "60"},
			{"x2x1o6x29o6o", "60"},
			{"x2x1o6x29o7o", "60"},
			{"x2x1o6x29o8o", "60"},
			{"x2x1o6x29oo", "60"},
			{"x2x1o6x2o11o11x11o", "60"},
			{"x2x1o6x2o11x11o11o", "60"},
			{"x2x1o6x2o22o9o2x", "60"},
			{"x2x1o6x2o6o25o11x", "51"},
			{"x2x1o6x30o11o", "60"},
			{"x2x1o6x30o17o", "60"},
			{"x2x1o6x30o18o", "60"},
			{"x2x1o6x30o4o", "60"},
			{"x2x1o6x30o5o", "60"},
			{"x2x1o6x34o3o", "60"},
			{"x2x1o6x35o10o", "60"},
			{"x2x1o6x35o14o", "51"},
			{"x2x1o6x35o1o", "60"},
			{"x2x1o6x35o2o", "60"},
			{"x2x1o6x35o4o4x4o", "51"},
			{"x2x1o6x35o4x4o4o", "51"},
			{"x2x1o6x35o6o", "60"},
			{"x2x1o6x35oo", "60"},
			{"x2x1o6x35ooxo", "60"},
			{"x2x1o6x35oxoo", "60"},
			{"x2x1o6x36o11o", "60"},
			{"x2x1o6x36o12o", "60"},
			{"x2x1o6x36o13o", "60"},
			{"x2x1o6x36o3x3o3o", "60"},
			{"x2x1o6x37o10o", "60"},
			{"x2x1o6x37o11o", "60"},
			{"x2x1o6x37o12o", "60"},
			{"x2x1o6x37o3o3x3o", "60"},
			{"x2x1o6x38o11o", "60"},
			{"x2x1o6x38o1o", "60"},
			{"x2x1o6x38o2o", "60"},
			{"x2x1o6x38o2x2o2o", "48"},
			{"x2x1o6x38o5o", "60"},
			{"x2x1o6x38o6o", "60"},
			{"x2x1o6x40o1o", "51"},
			{"x2x1o6x42o3o", "51"},
			{"x2x1o6x42o7o", "51"},
			{"x2x1o6x45oo", "51"},
			{"x2x1o6x46o3o", "51"},
			{"x2x1o6x4o11o", "60"},
			{"x2x1o6x4o21o", "60"},
			{"x2x1o6x4o22ooox4x", "11"},
			{"x2x1o6x4o23o6o11x", "63"},
			{"x2x1o6x4o24o", "60"},
			{"x2x1o6x4o25o", "60"},
			{"x2x1o6x4o28o", "60"},
			{"x2x1o6x4o30o", "60"},
			{"x2x1o6x4o33o", "60"},
			{"x2x1o6x4o3o", "60"},
			{"x2x1o6x4o3o2o12o10x4x", "25"},
			{"x2x1o6x4o3o3o3x", "60"},
			{"x2x1o6x4o3o3o3x10o7x", "37"},
			{"x2x1o6x4o43o", "60"},
			{"x2x1o6x4o45o", "60"},
			{"x2x1o6x4o4o25o11x", "63"},
			{"x2x1o6x4o5o", "60"},
			{"x2x1o6x4o7o22x11o", "63"},
			{"x2x1o6x4o8o21x11o", "63"},
			{"x2x1o6x4o9o", "60"},
			{"x2x1o6x5o10o", "60"},
			{"x2x1o6x5o11o", "60"},
			{"x2x1o6x5o18o10x11o", "63"},
			{"x2x1o6x5o19o9x11o", "63"},
			{"x2x1o6x5o20o", "60"},
			{"x2x1o6x5o23o", "60"},
			{"x2x1o6x5o24o", "60"},
			{"x2x1o6x5o26o", "60"},
			{"x2x1o6x5o2o", "60"},
			{"x2x1o6x5o2o14oo10x4x", "38"},
			{"x2x1o6x5o2o15o2o7x4x", "25"},
			{"x2x1o6x5o2o2o12o10x4x", "25"},
			{"x2x1o6x5o3o25x11o", "63"},
			{"x2x1o6x5o42o", "60"},
			{"x2x1o6x5o44o", "60"},
			{"x2x1o6x5o4o", "60"},
			{"x2x1o6x5o6o22x11o", "63"},
			{"x2x1o6x5o7o21x11o", "63"},
			{"x2x1o6x5o8o", "60"},
			{"x2x1o6x8o11o", "41"},
			{"x2x1o6x8o11o3o2o7x4x", "41"},
			{"x2x1o6x8o12o", "60"},
			{"x2x1o6x8o16o", "4"},
			{"x2x1o6x8o17o", "60"},
			{"x2x1o6x8o1o", "60"},
			{"x2x1o6x8o20o", "48"},
			{"x2x1o6x8o21o", "60"},
			{"x2x1o6x8o27o", "60"},
			{"x2x1o6x8o28o", "60"},
			{"x2x1o6x8o29o", "60"},
			{"x2x1o6x8o33o", "48"},
			{"x2x1o6x8o37o", "48"},
			{"x2x1o6x8oo", "7"},
			{"x2x1o6x9o11o13o11x", "63"},
			{"x2x1o6x9o13o11x2o", "63"},
			{"x2x1o6x9o16o8o11x", "63"},
			{"x2x1o6x9o17o7x2o", "63"},
			{"x2x1o6x9o19o", "2"},
			{"x2x1o6x9o20o4o11x", "63"},
			{"x2x1o6x9o22o2o11x", "25"},
			{"x2x1o6x9o23o1o11x", "51"},
			{"x2x1o6x9o25o10ox", "63"},
			{"x2x1o6x9o25o11x2o", "51"},
			{"x2x1o6x9o25o1o9x", "63"},
			{"x2x1o6x9o25o2o8x", "63"},
			{"x2x1o6x9o25o6o4x", "63"},
			{"x2x1o6x9o25oo10x", "63"},
			{"x2x1o6x9o25ooxo8x", "63"},
			{"x2x1o6x9o25oxoo8x", "63"},
			{"x2x1o6x9o28o", "48"},
			{"x2x1o6x9o37o", "51"},
			{"x2x1o6x9o4o20o11x", "63"},
			{"x2x1o6x9o6o", "51"},
			{"x2x1o6x9o6o18o11x", "51"},
			{"x2x1o6x9o7o17o11x", "26"},
			{"x2x1o6x9oo24o11x", "63"},
			{"x2x1o9o", "48"},
			{"x2x1o9o26o5o2x11x", "26"},
			{"x2x1o9o32x2o", "60"},
			{"x2x1o9o5o26o2x11x", "37"},
			{"x2x1o9x10o13o7x2o", "63"},
			{"x2x1o9x10o21o", "63"},
			{"x2x1o9x10o21x2o10o", "63"},
			{"x2x1o9x10o21x2o11o", "60"},
			{"x2x1o9x10o21x2o3o", "60"},
			{"x2x1o9x10o21x2o3o3x3o", "60"},
			{"x2x1o9x10o21x2o3x3o3o", "60"},
			{"x2x1o9x10o21x2o8o", "63"},
			{"x2x1o9x10o21x2o9o", "60"},
			{"x2x1o9x10o2o18x2o", "60"},
			{"x2x1o9x10o36o", "51"},
			{"x2x1o9x10o3o17x2o", "60"},
			{"x2x1o9x10o9o11x2o", "21"},
			{"x2x1o9x11o1o", "48"},
			{"x2x1o9x11o24o", "48"},
			{"x2x1o9x11o2o", "48"},
			{"x2x1o9x11o32o", "48"},
			{"x2x1o9x11o8o", "48"},
			{"x2x1o9x12o19o", "63"},
			{"x2x1o9x12o34o", "48"},
			{"x2x1o9x13o10o", "63"},
			{"x2x1o9x13o19o", "63"},
			{"x2x1o9x13o21o", "48"},
			{"x2x1o9x13o22o", "63"},
			{"x2x1o9x13o30o", "48"},
			{"x2x1o9x13o33o", "48"},
			{"x2x1o9x13oo", "48"},
			{"x2x1o9x14o20o", "48"},
			{"x2x1o9x14o21o", "48"},
			{"x2x1o9x14o25o", "48"},
			{"x2x1o9x14o28o", "48"},
			{"x2x1o9x14o30o", "48"},
			{"x2x1o9x1o11o", "63"},
			{"x2x1o9x1o12o", "63"},
			{"x2x1o9x1o18o", "63"},
			{"x2x1o9x1o22o", "63"},
			{"x2x1o9x1o23o", "63"},
			{"x2x1o9x1o28o", "63"},
			{"x2x1o9x1o2o", "63"},
			{"x2x1o9x1o3o29x11o", "60"},
			{"x2x1o9x1o42o", "63"},
			{"x2x1o9x1o44o", "63"},
			{"x2x1o9x1o4o", "63"},
			{"x2x1o9x1o6o", "63"},
			{"x2x1o9x1o7o25x11o", "60"},
			{"x2x1o9x1o8o24x11o", "60"},
			{"x2x1o9x2o11o", "63"},
			{"x2x1o9x2o17o", "63"},
			{"x2x1o9x2o1o", "63"},
			{"x2x1o9x2o21o", "63"},
			{"x2x1o9x2o22o", "63"},
			{"x2x1o9x2o26o", "63"},
			{"x2x1o9x2o29o", "63"},
			{"x2x1o9x2o3o", "63"},
			{"x2x1o9x2o3o3o3x", "63"},
			{"x2x1o9x2o41o", "63"},
			{"x2x1o9x2o43o", "63"},
			{"x2x1o9x2o5o", "63"},
			{"x2x1o9x32o11o", "63"},
			{"x2x1o9x32o2o", "63"},
			{"x2x1o9x32o3x3o3o", "63"},
			{"x2x1o9x32o4o", "63"},
			{"x2x1o9x32o4o4x4o", "51"},
			{"x2x1o9x32o4x4o4o", "51"},
			{"x2x1o9x32o5o", "63"},
			{"x2x1o9x32o8o", "63"},
			{"x2x1o9x32o9o", "63"},
			{"x2x1o9x32ooxo", "63"},
			{"x2x1o9x32oxoo", "63"},
			{"x2x1o9x33o10o", "63"},
			{"x2x1o9x33o11o", "48"},
			{"x2x1o9x33o12o", "63"},
			{"x2x1o9x33o1o", "63"},
			{"x2x1o9x33o3o3x3o", "48"},
			{"x2x1o9x33o3x3o3o", "63"},
			{"x2x1o9x34o10o", "48"},
			{"x2x1o9x34o11o", "63"},
			{"x2x1o9x34o3o3x3o", "63"},
			{"x2x1o9x34o3x3o3o", "63"},
			{"x2x1o9x34o9o", "63"},
			{"x2x1o9x34oo", "48"},
			{"x2x1o9x35o2x2o2o", "48"},
			{"x2x1o9x35o4o", "63"},
			{"x2x1o9x35o8o", "48"},
			{"x2x1o9x35oo", "63"},
			{"x2x1o9x36o1o", "48"},
			{"x2x1o9x36o3o", "48"},
			{"x2x1o9x36o7o", "48"},
			{"x2x1o9x38o7o", "63"},
			{"x2x1o9x40o3o", "48"},
			{"x2x1o9x4o12o", "63"},
			{"x2x1o9x4o13o", "63"},
			{"x2x1o9x4o16o10x2o", "63"},
			{"x2x1o9x4o19o", "48"},
			{"x2x1o9x4o1o", "63"},
			{"x2x1o9x4o20o", "63"},
			{"x2x1o9x4o21o8o11x", "60"},
			{"x2x1o9x4o25o", "63"},
			{"x2x1o9x4o28o", "63"},
			{"x2x1o9x4o2o", "48"},
			{"x2x1o9x4o30o", "63"},
			{"x2x1o9x4o35o", "63"},
			{"x2x1o9x4o3o", "63"},
			{"x2x1o9x4o40o", "63"},
			{"x2x1o9x4o41o", "63"},
			{"x2x1o9x4o4o", "63"},
			{"x2x1o9x4o5o", "63"},
			{"x2x1o9x4o5o21x2o", "63"},
			{"x2x1o9x4o6o", "48"},
			{"x2x1o9x4o8o", "63"},
			{"x2x1o9x4oo29o11x", "60"},
			{"x2x1o9x4xooo", "37"},
			{"x2x1o9x5o12o16x11o", "60"},
			{"x2x1o9x5o14o14o11x", "37"},
			{"x2x1o9x5o18o10o11x", "60"},
			{"x2x1o9x5o19o", "1"},
			{"x2x1o9x5o24o4o11x", "37"},
			{"x2x1o9x5o26o", "51"},
			{"x2x1o9x5o26o2o11x", "37"},
			{"x2x1o9x5o26ooxo11x", "37"},
			{"x2x1o9x5o26oxoo11x", "37"},
			{"x2x1o9x5o27o1o11x", "37"},
			{"x2x1o9x5o29o4o6x", "48"},
			{"x2x1o9x5o29o8o2x", "48"},
			{"x2x1o9x5o29oo10x", "48"},
			{"x2x1o9x5o2o26o11x", "37"},
			{"x2x1o9x5o41o", "51"},
			{"x2x1o9x5o5o20o2x", "60"},
			{"x2x1o9x5o7o21o11x", "37"},
			{"x2x1o9x5o8o", "48"},
			{"x2x1o9x5o8o20o11x", "48"},
			{"x2x1o9x5oo", "39"},
			{"x2x1o9x6o10o", "63"},
			{"x2x1o9x6o11o", "63"},
			{"x2x1o9x6o13o", "63"},
			{"x2x1o9x6o17o", "63"},
			{"x2x1o9x6o18o", "51"},
			{"x2x1o9x6o19o", "2"},
			{"x2x1o9x6o23o", "48"},
			{"x2x1o9x6o25o", "63"},
			{"x2x1o9x6o26o", "63"},
			{"x2x1o9x6o27o", "63"},
			{"x2x1o9x6o29o", "51"},
			{"x2x1o9x6o32o", "48"},
			{"x2x1o9x6o33o", "51"},
			{"x2x1o9x6o36o", "48"},
			{"x2x1o9x6o38o", "51"},
			{"x2x1o9x6o39o", "51"},
			{"x2x1o9x6o3o", "51"},
			{"x2x1o9x6o40o", "48"},
			{"x2x1o9x6o4o", "48"},
			{"x2x1o9x6o7o", "48"},
			{"x2x1o9x6oo", "37"},
			{"x2x1o9x7o16o", "48"},
			{"x2x1o9x7o22o", "48"},
			{"x2x1o9x7o28o", "48"},
			{"x2x1o9x7o36o", "48"},
			{"x2x1o9x7o39o", "48"},
			{"x2x1o9x7o5o", "48"},
			{"x2x1o9x7o6o", "48"},
			{"x2x1o9x8o11o", "48"},
			{"x2x1o9x8o13o", "63"},
			{"x2x1o9x8o17o8o11x", "60"},
			{"x2x1o9x8o1o21x2o", "25"},
			{"x2x1o9x8o23o", "63"},
			{"x2x1o9x8o26o", "63"},
			{"x2x1o9x8o2o", "48"},
			{"x2x1o9x8o34o", "48"},
			{"x2x1o9x8o35o", "63"},
			{"x2x1o9x8o37o", "48"},
			{"x2x1o9x8o5o", "48"},
			{"x2x1o9x8o9o", "63"},
			{"x2x1o9x8oo", "63"},
			{"x2x1o9x9o10o", "63"},
			{"x2x1o9x9o22o", "63"},
			{"x2x1o9x9o26o", "63"},
			{"x2x1o9x9o30o", "63"},
			{"x2x1o9x9o35o", "48"},
			{"x2x1o9x9o36o", "48"},
			{"x2x1o9x9o37o", "48"},
			{"x2x1o9x9o3o", "51"},
			{"x2x1o9x9o4o", "48"},
			{"x2x1o9x9o8o16x11o", "60"},
			{"x2x1x3o5o1o3o3o3x11o9x5x", "12"},
			{"x2x1x3o5o1o7o15o9x5x", "21"},
			{"x2x1x3oo6o3o3o3x11o9x5x", "12"},
			{"x2x1x3oo6o7o15o9x5x", "21"},
			{"x2x1x9o1o7o15o9x", "21"},
			{"x2x20o11o", "15"},
			{"x2x20o12o", "51"},
			{"x2x20o14o", "12"},
			{"x2x20o19o", "12"},
			{"x2x20o20o", "15"},
			{"x2x20o21o", "12"},
			{"x2x20o22o", "48"},
			{"x2x20o2o", "12"},
			{"x2x20o35o", "15"},
			{"x2x20o36o", "51"},
			{"x2x20o38o", "12"},
			{"x2x20o3o", "12"},
			{"x2x20o4o", "12"},
			{"x2x20o5o", "15"},
			{"x2x20o6o", "51"},
			{"x2x20oo", "15"},
			{"x2x21o34o", "12"},
			{"x2x21o35o", "48"},
			{"x2x21o36o", "48"},
			{"x2x24o31o", "12"},
			{"x2x24o32o", "12"},
			{"x2x24o33o", "12"},
			{"x2x24o34o", "48"},
			{"x2x25o30o", "48"},
			{"x2x25o31o", "48"},
			{"x2x25o32o", "12"},
			{"x2x25o33o", "15"},
			{"x2x56o2o", "12"},
			{"x2x56oo", "15"},
			{"x2x57oo", "12"},
			{"x2x8o10o", "48"},
			{"x2x8o10o24o14x", "15"},
			{"x2x8o11o", "51"},
			{"x2x8o11x11o11o11o2x", "15"},
			{"x2x8o12o", "51"},
			{"x2x8o13o", "51"},
			{"x2x8o14o", "48"},
			{"x2x8o15o", "48"},
			{"x2x8o16o", "51"},
			{"x2x8o17o", "51"},
			{"x2x8o18o", "48"},
			{"x2x8o2o", "48"},
			{"x2x8o35o", "63"},
			{"x2x8o35o11o2x", "15"},
			{"x2x8o36o", "48"},
			{"x2x8o38o", "60"},
			{"x2x8o38o8x2o", "48"},
			{"x2x8o39o", "51"},
			{"x2x8o40o", "48"},
			{"x2x8o41o", "48"},
			{"x2x8o42o", "51"},
			{"x2x8o47o", "48"},
			{"x2x8o48o", "48"},
			{"x2x8o50o", "48"},
			{"x2x8o7o", "48"},
			{"x2x8o7o30o8x", "48"},
			{"x2x8o8o", "51"},
			{"x2x8o8o26o14x", "15"},
			{"x2x8o8o29o11x", "15"},
			{"x2x8o8o29o8x", "48"},
			{"x2x8o8o2x11o11o14x", "15"},
			{"x2x8o8o3o12x12o11x", "15"},
			{"x2x8o8o3o12x12o8x", "48"},
			{"x2x8o8o3x12o12o11x", "15"},
			{"x2x8o9o", "51"},
			{"x2x8o9o1x11o11o14x", "15"},
			{"x2x8o9o25o14x", "15"},
			{"x2x8oo", "48"},
			{"x2x8x2o10o12o", "51"},
			{"x2x8x2o10o24o", "60"},
			{"x2x8x2o10o28o", "60"},
			{"x2x8x2o10o32o", "60"},
			{"x2x8x2o10oo", "51"},
			{"x2x8x2o11o20o", "51"},
			{"x2x8x2o11o28o", "51"},
			{"x2x8x2o11o33o", "51"},
			{"x2x8x2o11o35o", "51"},
			{"x2x8x2o11x11o11o7o", "60"},
			{"x2x8x2o15o19o", "63"},
			{"x2x8x2o32o14o", "51"},
			{"x2x8x2o32o2o", "60"},
			{"x2x8x2o32o4x4o4o", "51"},
			{"x2x8x2o32o6o", "60"},
			{"x2x8x2o32oo", "51"},
			{"x2x8x2o33o11o", "60"},
			{"x2x8x2o33o12o", "60"},
			{"x2x8x2o33o13o", "60"},
			{"x2x8x2o35o11o", "60"},
			{"x2x8x2o35o1o", "60"},
			{"x2x8x2o35o2o", "60"},
			{"x2x8x2o37o1o", "51"},
			{"x2x8x2o39o3o", "51"},
			{"x2x8x2o39o7o", "51"},
			{"x2x8x2o5o16o", "4"},
			{"x2x8x2o5o16o9o2x", "63"},
			{"x2x8x2o5o17o", "51"},
			{"x2x8x2o5o1o", "60"},
			{"x2x8x2o5o20o", "48"},
			{"x2x8x2o5o21o", "60"},
			{"x2x8x2o5o27o", "60"},
			{"x2x8x2o5o28o", "60"},
			{"x2x8x2o5o29o", "60"},
			{"x2x8x2o5o33o", "60"},
			{"x2x8x2o5o37o", "48"},
			{"x2x8x2o5o5o11x11o", "60"},
			{"x2x8x2o5oo", "48"},
			{"x2x8x2o5oo25o11x", "26"},
			{"x2x8x2o6o16o8o11x", "26"},
			{"x2x8x2o6o18o6o11x", "26"},
			{"x2x8x2o6o20o4o11x", "37"},
			{"x2x8x2o6o24oo11x", "37"},
			{"x2x8x2o6o25o10ox", "37"},
			{"x2x8x2o6o25o2o8x", "37"},
			{"x2x8x2o6o25o6o4x", "37"},
			{"x2x8x2o6o28o", "48"},
			{"x2x8x2o6o37o", "51"},
			{"x2x8x2o6o4o20o11x", "37"},
			{"x2x8x2o6o6o", "51"},
			{"x2x8x2o6o8o16o11x", "37"},
			{"x2x8x2o6oo24o11x", "63"},
			{"x2x8x2o7o19o", "60"},
			{"x2x8x2o7o21o", "60"},
			{"x2x8x2o7o24o", "60"},
			{"x2x8x2o7o26o", "60"},
			{"x2x8x2o7o2o", "51"},
			{"x2x8x2o7o35o", "60"},
			{"x2x8x2o7o37o", "60"},
			{"x2x8x2o7o38o", "60"},
			{"x2x8x2o7o3o", "60"},
			{"x2x8x2o7o3o11x11o", "60"},
			{"x2x8x2o7o6o", "60"},
			{"x2x9o10o", "12"},
			{"x2x9o13o", "15"},
			{"x2x9o46o", "12"},
			{"x2x9o47o", "48"},
			{"x2x9o48o", "48"},
			{"x2x9o6o", "51"},
			{"x2x9o7o", "48"},
			{"x2x9o8o", "15"},
			{"x2x9o9o", "15"},
			{"x30o", "48"},
			{"x4o", "48"},
			{"x5o", "48"},
			{"x62o", "48"},
			{"x6o", "3"},
			{"xo", "12"},
			{"xo10x10o", "60"},
			{"xo10x10o12o23x", "63"},
			{"xo10x10o16o19x", "63"},
			{"xo10x10o24o11x", "63"},
			{"xo10x10oo35x", "63"},
			{"xo10x11o", "63"},
			{"xo10x11o12o22x", "63"},
			{"xo10x11o13o24x", "15"},
			{"xo10x11o14o20x", "63"},
			{"xo10x11o16o18x", "63"},
			{"xo10x11o17o17x", "15"},
			{"xo10x11o17o20x", "15"},
			{"xo10x11o18o16x", "63"},
			{"xo10x11o1o33x", "63"},
			{"xo10x11o20o2x", "51"},
			{"xo10x11o21o16x", "15"},
			{"xo10x11o21o1x", "51"},
			{"xo10x11o22ox", "51"},
			{"xo10x11o23x10o", "51"},
			{"xo10x11o23x13o", "51"},
			{"xo10x11o23x14o", "15"},
			{"xo10x11o23x4o", "51"},
			{"xo10x11o23x5o", "51"},
			{"xo10x11o23x6o", "51"},
			{"xo10x11o23x8o", "51"},
			{"xo10x11o23x9o", "51"},
			{"xo10x11o26o11x", "15"},
			{"xo10x11o29o5x", "63"},
			{"xo10x11o2o32x", "63"},
			{"xo10x11o35x2o", "3"},
			{"xo10x11o35xo", "15"},
			{"xo10x11o4o18x", "51"},
			{"xo10x11o4o33x", "15"},
			{"xo10x11o5o17x", "51"},
			{"xo10x11o6o16x", "51"},
			{"xo10x11oo34x", "15"},
			{"xo10x11oo37x", "15"},
			{"xo10x12o", "60"},
			{"xo10x12o10o23x", "15"},
			{"xo10x12o14o19x", "15"},
			{"xo10x12o22o11x", "63"},
			{"xo10x13o", "60"},
			{"xo10x13o13o19x", "63"},
			{"xo10x13o21o11x", "63"},
			{"xo10x13o9o23x", "63"},
			{"xo10x14o", "60"},
			{"xo10x14o12o19x", "63"},
			{"xo10x14o20o11x", "63"},
			{"xo10x14o8o23x", "63"},
			{"xo10x15o", "51"},
			{"xo10x15o22x11o", "48"},
			{"xo10x15o9o12x", "48"},
			{"xo10x15o9o24x", "15"},
			{"xo10x16o", "48"},
			{"xo10x16o10o7x", "51"},
			{"xo10x16o18x11o", "51"},
			{"xo10x17o", "48"},
			{"xo10x17o17x11o", "51"},
			{"xo10x17o9o7x", "51"},
			{"xo10x18o", "48"},
			{"xo10x18o16x11o", "51"},
			{"xo10x18o8o7x", "51"},
			{"xo10x1o", "48"},
			{"xo10x1o33x11o", "51"},
			{"xo10x1o5o27x", "51"},
			{"xo10x1o5o39x", "63"},
			{"xo10x1o9o23x", "51"},
			{"xo10x1o9o35x", "63"},
			{"xo10x23o", "15"},
			{"xo10x23o17o5x", "63"},
			{"xo10x23o23x2o", "3"},
			{"xo10x23o23xo", "15"},
			{"xo10x23o2o20x", "63"},
			{"xo10x23o4o18x", "63"},
			{"xo10x23o5o17x", "15"},
			{"xo10x23o6o16x", "63"},
			{"xo10x23oo22x", "63"},
			{"xo10x24o", "60"},
			{"xo10x24o10o11x", "63"},
			{"xo10x24o2o19x", "63"},
			{"xo10x25o", "15"},
			{"xo10x26o", "60"},
			{"xo10x26o8o11x", "63"},
			{"xo10x26oo19x", "63"},
			{"xo10x27o", "15"},
			{"xo10x27o13o5x", "63"},
			{"xo10x27o19x2o", "3"},
			{"xo10x27o1o17x", "15"},
			{"xo10x27o2o16x", "63"},
			{"xo10x27o4o2x", "51"},
			{"xo10x27o5o1x", "51"},
			{"xo10x27o6ox", "51"},
			{"xo10x27o7x13o", "15"},
			{"xo10x27o7x14o", "15"},
			{"xo10x27oo18x", "63"},
			{"xo10x27oo4o13x2x", "51"},
			{"xo10x28o", "60"},
			{"xo10x28o6o11x", "63"},
			{"xo10x29o", "60"},
			{"xo10x29o5o11x", "15"},
			{"xo10x2o", "48"},
			{"xo10x2o20o11x", "51"},
			{"xo10x2o22o1o10x6o1x2x", "48"},
			{"xo10x2o24o7x", "51"},
			{"xo10x2o26o5o11x2x", "26"},
			{"xo10x2o32x11o", "51"},
			{"xo10x2o4o17o12x1o6x2x", "3"},
			{"xo10x2o4o27x", "51"},
			{"xo10x2o4o4o25x1o6x2x", "3"},
			{"xo10x2o4oo38x2x", "51"},
			{"xo10x2o5o26o11x2x", "26"},
			{"xo10x2o8o23x", "51"},
			{"xo10x2o9o14o10x6o1x2x", "3"},
			{"xo10x2x20o15o", "63"},
			{"xo10x2x20oo10x4o", "63"},
			{"xo10x2x21o10x4o6o", "63"},
			{"xo10x2x21o2o7x4o", "63"},
			{"xo10x2x22o16o", "63"},
			{"xo10x2x4o15o", "63"},
			{"xo10x2x4o16o10x4o", "63"},
			{"xo10x2x4o31o", "63"},
			{"xo10x2x5o16o", "60"},
			{"xo10x2x5o7o25o7x", "25"},
			{"xo10x30o", "60"},
			{"xo10x30o4o11x", "63"},
			{"xo10x31o", "15"},
			{"xo10x32o", "48"},
			{"xo10x32o2x11o", "51"},
			{"xo10x33o", "48"},
			{"xo10x33o1x11o", "15"},
			{"xo10x34o", "48"},
			{"xo10x34ox11o", "51"},
			{"xo10x35o", "15"},
			{"xo10x35o11x2o", "3"},
			{"xo10x35o11xo", "15"},
			{"xo10x35o5o5x", "63"},
			{"xo10x35x11o1o", "15"},
			{"xo10x35x11o2o", "15"},
			{"xo10x36o", "48"},
			{"xo10x37o", "48"},
			{"xo10x38o", "48"},
			{"xo10x39o", "48"},
			{"xo10x40o", "48"},
			{"xo10x41o", "48"},
			{"xo10x42o", "48"},
			{"xo10x43o", "48"},
			{"xo10x44o", "48"},
			{"xo10x45o", "48"},
			{"xo10x46o", "48"},
			{"xo10x47o", "15"},
			{"xo10x48o", "60"},
			{"xo10x49o", "48"},
			{"xo10x50o", "48"},
			{"xo10x7o", "15"},
			{"xo10x7o10o16x", "51"},
			{"xo10x7o16o22x", "63"},
			{"xo10x7o17o12x8x2o", "15"},
			{"xo10x7o18o20x", "63"},
			{"xo10x7o1o28x8x2o", "15"},
			{"xo10x7o1o37x", "63"},
			{"xo10x7o1o6o30x2x", "25"},
			{"xo10x7o20o18x", "63"},
			{"xo10x7o21o17x", "15"},
			{"xo10x7o22o16x", "63"},
			{"xo10x7o24o2x", "51"},
			{"xo10x7o25o1x", "51"},
			{"xo10x7o26ox", "51"},
			{"xo10x7o27x13o", "15"},
			{"xo10x7o27x14o", "15"},
			{"xo10x7o2o36x", "63"},
			{"xo10x7o33o5x", "63"},
			{"xo10x7o39x2o", "51"},
			{"xo10x7o39xo", "15"},
			{"xo10x7o4o25x8x2o", "15"},
			{"xo10x7o4o34x", "15"},
			{"xo10x7o5o33x", "63"},
			{"xo10x7o6o32x", "63"},
			{"xo10x7o8o18x", "51"},
			{"xo10x7o9o17x", "51"},
			{"xo10x7oo23o14x2x", "22"},
			{"xo10x7oo24o13x2x", "22"},
			{"xo10x7oo38x", "63"},
			{"xo10x7oo7o30x2x", "25"},
			{"xo10x7oo8o29x2x", "22"},
			{"xo10x8o", "60"},
			{"xo10x8o14o23x", "15"},
			{"xo10x8o18o19x", "15"},
			{"xo10x8o26o11x", "15"},
			{"xo10x8o2o35x", "15"},
			{"xo10x8o2o38x", "15"},
			{"xo10x8o6o22x", "48"},
			{"xo10x8o6o34x", "15"},
			{"xo10x9o", "60"},
			{"xo10x9o13o23x", "63"},
			{"xo10x9o17o19x", "63"},
			{"xo10x9o1o35x", "63"},
			{"xo10x9o25o11x", "63"},
			{"xo10xo", "48"},
			{"xo10xo22o11x", "51"},
			{"xo10xo22o23x", "63"},
			{"xo10xo34x11o", "51"},
			{"xo10xo6o27x", "51"},
			{"xo10xo6o39x", "63"},
			{"xo1o18x2xx1o12xo13o", "38"},
			{"xo1o18x2xx1o12xo16o", "8"},
			{"xo1o18x2xx4o9xo13o", "16"},
			{"xo1o18x2xx4o9xo16o", "16"},
			{"xo1o2o12o1xx2x9x5x9o11o", "42"},
			{"xo1o2o2x2o35x2x", "63"},
			{"xo1o2x2o2o35x2x", "63"},
			{"xo1o3o3x3o5x15x3x21o", "42"},
			{"xo1o3x3o3o6x2xx1o8x3xo9o", "12"},
			{"xo1o3x3o3o6x2xx4o5x3xo9o", "12"},
			{"xo1o4o13x2xx1o12xo", "37"},
			{"xo1o4o13x2xx1o8x3xo5o", "38"},
			{"xo1o4o13x2xx4o5x3xo5o", "15"},
			{"xo1o4o13x2xx4o9xo", "37"},
			{"xo1o4o39x2x", "63"},
			{"xo1o4o6o6x2xx1o8x3xo", "38"},
			{"xo1o4o6o6x2xx4o5x3xo", "48"},
			{"xo1o7o10x2xx1o12xo", "37"},
			{"xo1o7o10x2xx1o8x3xo5o", "38"},
			{"xo1o7o10x2xx4o5x3xo5o", "12"},
			{"xo1o7o10x2xx4o9xo", "37"},
			{"xo1o8x", "60"},
			{"xo1o8x11o35x", "63"},
			{"xo1o8x23o23x", "63"},
			{"xo1o8x27o19x", "63"},
			{"xo1o8x2x10o21x11o", "63"},
			{"xo1o8x2x12o", "63"},
			{"xo1o8x2x20o", "63"},
			{"xo1o8x2x21o10x11o", "22"},
			{"xo1o8x2x22o", "63"},
			{"xo1o8x2x24o", "63"},
			{"xo1o8x2x26o17o2x", "51"},
			{"xo1o8x2x26o3o16x", "48"},
			{"xo1o8x2x28o", "63"},
			{"xo1o8x2x32o", "63"},
			{"xo1o8x2x44o", "63"},
			{"xo1o8x2x4o", "63"},
			{"xo1o8x2x5o22o18x", "41"},
			{"xo1o8x2x5o24o16x", "48"},
			{"xo1o8x2x5o38o2x", "41"},
			{"xo1o8x35o11x", "63"},
			{"xo1o8x7o39x", "63"},
			{"xo1oo14o1xx2x9x2x2xo1o6o", "26"},
			{"xo1oo17x12o1xx2xo", "5"},
			{"xo1oo17x14xx2xo1o", "5"},
			{"xo1oo17x14xx2xo4o", "5"},
			{"xo1oo17x9o4xx2xo", "5"},
			{"xo1oo43x2x", "63"},
			{"xo1x16o", "15"},
			{"xo1x17o", "12"},
			{"xo1x18o", "15"},
			{"xo1x19o", "12"},
			{"xo1x1o13o40x", "48"},
			{"xo1x1o14o30x", "63"},
			{"xo1x1o15o15o10x4x", "41"},
			{"xo1x1o15o19o6x12x", "49"},
			{"xo1x1o15o19o9x9x", "49"},
			{"xo1x1o15o26x", "41"},
			{"xo1x1o16o28x", "63"},
			{"xo1x1o17o24x", "60"},
			{"xo1x1o18o26x", "63"},
			{"xo1x1o1o40x", "60"},
			{"xo1x1o1o7x5x19o", "12"},
			{"xo1x1o21o20x", "60"},
			{"xo1x1o26o18x", "63"},
			{"xo1x1o2o3x2x5x19o1o", "60"},
			{"xo1x1o2o42x", "63"},
			{"xo1x1o30o14x", "63"},
			{"xo1x1o33o8x", "60"},
			{"xo1x1o34o10x", "63"},
			{"xo1x1o35o9x", "21"},
			{"xo1x1o36o5x", "60"},
			{"xo1x1o37o4x", "60"},
			{"xo1x1o3o2ox27o6x11x", "63"},
			{"xo1x1o3o3x7o29x7o3x", "60"},
			{"xo1x1o3o3x8o25x3o7x", "12"},
			{"xo1x1o42o14x", "51"},
			{"xo1x1o42x12o", "60"},
			{"xo1x1o42x13o", "60"},
			{"xo1x1o42x14o", "60"},
			{"xo1x1o42x4o", "60"},
			{"xo1x1o42x9o", "60"},
			{"xo1x1o45o8x", "48"},
			{"xo1x1o45x10o", "63"},
			{"xo1x1o45x1o", "63"},
			{"xo1x1o45x2o", "63"},
			{"xo1x1o45x6o", "63"},
			{"xo1x1o45x8o", "63"},
			{"xo1x1o45x9o", "21"},
			{"xo1x1o6o28o6x11x", "63"},
			{"xo1x1o6o35x", "60"},
			{"xo1x1o6o38x", "63"},
			{"xo1x1o6x10o", "60"},
			{"xo1x1o6x10o1o25x8o2x", "15"},
			{"xo1x1o6x14o", "60"},
			{"xo1x1o6x25o9x11o", "63"},
			{"xo1x1o6x29o", "60"},
			{"xo1x1o6x2x10o36o", "60"},
			{"xo1x1o6x2x11o13o", "21"},
			{"xo1x1o6x2x12o12o", "21"},
			{"xo1x1o6x2x21o25o", "60"},
			{"xo1x1o6x2x22o21o", "63"},
			{"xo1x1o6x2x25o18o", "21"},
			{"xo1x1o6x2x25o1o", "21"},
			{"xo1x1o6x2x25o21o", "25"},
			{"xo1x1o6x2x25o4o", "21"},
			{"xo1x1o6x2x26o1o", "51"},
			{"xo1x1o6x2x4oo", "41"},
			{"xo1x1o6x2x4oo6o8o15x6x", "63"},
			{"xo1x1o6x2x5o14o", "37"},
			{"xo1x1o6x2x5o25o", "41"},
			{"xo1x1o6x2x5o38o", "51"},
			{"xo1x1o6x2x5o41o", "60"},
			{"xo1x1o6x2x5o6o8oo14x6x", "63"},
			{"xo1x1o6x2x5o8o", "37"},
			{"xo1x1o6x2x6o8o", "48"},
			{"xo1x1o6x2x9o34o", "63"},
			{"xo1x1o6x30o", "60"},
			{"xo1x1o6x38o", "60"},
			{"xo1x1o6x48o", "41"},
			{"xo1x1o6x49o", "60"},
			{"xo1x1o6x50o", "60"},
			{"xo1x1o6x6o", "60"},
			{"xo1x1o6x8o", "41"},
			{"xo1x1o6x8o19o19x", "13"},
			{"xo1x1o9o32x", "60"},
			{"xo1x1o9x10o24x11o", "60"},
			{"xo1x1o9x16o", "63"},
			{"xo1x1o9x16o10o19x", "60"},
			{"xo1x1o9x16o15x11o", "63"},
			{"xo1x1o9x20o", "63"},
			{"xo1x1o9x21o9ox", "21"},
			{"xo1x1o9x24o", "63"},
			{"xo1x1o9x25o", "21"},
			{"xo1x1o9x2o1ox4o21x11x2o", "12"},
			{"xo1x1o9x2o3xo13o10x11o2x", "12"},
			{"xo1x1o9x32o", "63"},
			{"xo1x1o9x44o", "63"},
			{"xo1x1o9x45o", "41"},
			{"xo1x1o9x46o", "63"},
			{"xo1x1o9x4o", "63"},
			{"xo1x1o9x4o26o15x", "48"},
			{"xo1x1o9x5o19o19x", "13"},
			{"xo1x1o9x5o29x11o", "41"},
			{"xo1x1o9x5x10o4o4x4ox14o", "25"},
			{"xo1x1o9x5x15o9ox14o", "12"},
			{"xo1x1o9x5x19o1o", "12"},
			{"xo1x1o9x5x19o21o", "51"},
			{"xo1x1o9x5x19o5o", "12"},
			{"xo1x1o9x5x19oo", "25"},
			{"xo1x1o9x5x3x12o2oo", "35"},
			{"xo1x1o9x5x3x15oo3o", "38"},
			{"xo1x1o9x5x3x15oo8o", "22"},
			{"xo1x1o9x5x3x3o11oo", "48"},
			{"xo1x1o9x5x3x8o6oo", "38"},
			{"xo1x1o9x5x9o9o", "25"},
			{"xo1x1o9x5xoo", "48"},
			{"xo1x1o9x6o", "63"},
			{"xo1x1o9x6o22o2x", "12"},
			{"xo1x1o9x6o24o15x", "51"},
			{"xo1x1o9x6o8o31x", "51"},
			{"xo1x1o9x6oo13x", "60"},
			{"xo1x1o9x6oo13x10o11x", "12"},
			{"xo1x1o9x6oo2o10x22x", "12"},
			{"xo1x1o9x6oo39x", "60"},
			{"xo1x1o9x7o8o30x", "60"},
			{"xo1x1ox7ox6o24ox", "51"},
			{"xo1x1ox8x44ooxo", "48"},
			{"xo1x1ox8x44oxoo", "48"},
			{"xo1x1ox8x45o1o", "60"},
			{"xo1x1ox8x5o38o", "37"},
			{"xo1x1ox8x6o19o", "63"},
			{"xo1x1ox8x6o24o", "48"},
			{"xo1x1ox8x6o28o", "63"},
			{"xo1x1ox8x6o3o", "63"},
			{"xo1x1ox8x6o4o", "48"},
			{"xo1x1ox8x6o8o10o20x", "51"},
			{"xo1x1x2o3x8o16o", "41"},
			{"xo1x1x3o2xo1o22o12x", "60"},
			{"xo1x1x3o2xo1o9o25x", "41"},
			{"xo1x1x3o3o1o5o29x11x", "60"},
			{"xo1x1x3oo1xo1x5o19o19x", "25"},
			{"xo1x1x3oo1xo1x5o3x12o2o19x", "26"},
			{"xo1x1x3oo1xo1x5o3x15o13o5x", "26"},
			{"xo1x1x4o6o3o3o3x11o9x9x", "12"},
			{"xo1x1x9o1o3o3o3x11o9x9x", "12"},
			{"xo1x1x9o2o2oo3x10o3x6x", "52"},
			{"xo1x1xo5x19o9o", "60"},
			{"xo1x1xo5x8o19o11o7x", "60"},
			{"xo1x20o", "51"},
			{"xo1x2o12o28x", "60"},
			{"xo1x2o12o2o25x2x", "25"},
			{"xo1x2o13o30x", "63"},
			{"xo1x2o14o19o6x12x", "60"},
			{"xo1x2o14o19o9x9x", "63"},
			{"xo1x2o14o26x", "41"},
			{"xo1x2o15o28x", "63"},
			{"xo1x2o16o24x", "60"},
			{"xo1x2o17o26x", "63"},
			{"xo1x2o1o3x8o19x", "38"},
			{"xo1x2o1o42x", "63"},
			{"xo1x2o20o20x", "60"},
			{"xo1x2o25o18x", "63"},
			{"xo1x2o25o9o5x2x", "37"},
			{"xo1x2o29o14x", "63"},
			{"xo1x2o32o8x", "60"},
			{"xo1x2o33o10x", "63"},
			{"xo1x2o34o1o7x11x", "60"},
			{"xo1x2o34o9x", "63"},
			{"xo1x2o35o5x", "60"},
			{"xo1x2o36o4x", "60"},
			{"xo1x2o41o14x", "51"},
			{"xo1x2o41x12o", "60"},
			{"xo1x2o41x13o", "60"},
			{"xo1x2o41x14o", "60"},
			{"xo1x2o41x4o", "60"},
			{"xo1x2o41x5o", "60"},
			{"xo1x2o41x8o", "60"},
			{"xo1x2o44o8x", "48"},
			{"xo1x2o44x10o", "63"},
			{"xo1x2o44x2o", "63"},
			{"xo1x2o44x5o", "63"},
			{"xo1x2o44x8o", "63"},
			{"xo1x2o44x9o", "21"},
			{"xo1x2o5o38x", "63"},
			{"xo1x2o5x10o", "60"},
			{"xo1x2o5x10o4o31x", "63"},
			{"xo1x2o5x14o", "60"},
			{"xo1x2o5x15o13o17x", "48"},
			{"xo1x2o5x15o14o16x", "63"},
			{"xo1x2o5x25o2x19o", "48"},
			{"xo1x2o5x25o5o6x", "22"},
			{"xo1x2o5x25o9x11o", "63"},
			{"xo1x2o5x28x12o6o", "48"},
			{"xo1x2o5x29o", "60"},
			{"xo1x2o5x2x10o36o", "60"},
			{"xo1x2o5x2x11o13o", "21"},
			{"xo1x2o5x2x12o12o", "21"},
			{"xo1x2o5x2x21o25o", "60"},
			{"xo1x2o5x2x22o21o", "63"},
			{"xo1x2o5x2x25o18o", "63"},
			{"xo1x2o5x2x25o1o", "21"},
			{"xo1x2o5x2x25o21o", "48"},
			{"xo1x2o5x2x25o4o", "21"},
			{"xo1x2o5x2x26o1o", "51"},
			{"xo1x2o5x2x4oo", "63"},
			{"xo1x2o5x2x5o14o", "41"},
			{"xo1x2o5x2x5o25o", "41"},
			{"xo1x2o5x2x5o38o", "51"},
			{"xo1x2o5x2x5o41o", "60"},
			{"xo1x2o5x2x5o8o", "41"},
			{"xo1x2o5x2x6o8o", "51"},
			{"xo1x2o5x2x9o34o", "63"},
			{"xo1x2o5x30o", "60"},
			{"xo1x2o5x38o", "60"},
			{"xo1x2o5x48o", "60"},
			{"xo1x2o5x49o", "60"},
			{"xo1x2o5x50o", "60"},
			{"xo1x2o5x6o", "60"},
			{"xo1x2o5x6o20o19x", "63"},
			{"xo1x2o5x6o2x15o5o6x", "21"},
			{"xo1x2o5x6o31x11o", "60"},
			{"xo1x2o5x6oo39x", "63"},
			{"xo1x2o5x7oo16x", "63"},
			{"xo1x2o5x7oo16x12o11x", "60"},
			{"xo1x2o5x7oo19x", "38"},
			{"xo1x2o5x7oo3o12x24x", "60"},
			{"xo1x2o5x7xoo15o1ox12x", "48"},
			{"xo1x2o5x8o", "41"},
			{"xo1x2o5x8o16o2x", "20"},
			{"xo1x2o5x8o19o19x", "60"},
			{"xo1x2o5x8o19x12o", "48"},
			{"xo1x2o5x8o19x18o", "48"},
			{"xo1x2o5x8o19x2o", "48"},
			{"xo1x2o5x8o24o4x", "60"},
			{"xo1x2o5x8o2o26x", "41"},
			{"xo1x2o5x8o6o12x", "20"},
			{"xo1x2o5x8oo18x", "38"},
			{"xo1x2o5x8oo2o12x2x", "17"},
			{"xo1x2o5xo1x5o19o19x", "37"},
			{"xo1x2o5xo1x5x3o11o4o6x", "22"},
			{"xo1x2o8o32x", "60"},
			{"xo1x2o8o35x", "63"},
			{"xo1x2o8o5o29x11x", "60"},
			{"xo1x2o8x10o24x11o", "60"},
			{"xo1x2o8x16o", "63"},
			{"xo1x2o8x20o", "63"},
			{"xo1x2o8x24o", "63"},
			{"xo1x2o8x25o", "21"},
			{"xo1x2o8x32o", "63"},
			{"xo1x2o8x44o", "63"},
			{"xo1x2o8x45o", "21"},
			{"xo1x2o8x46o", "63"},
			{"xo1x2o8x4o", "63"},
			{"xo1x2o8x5o19o19x", "63"},
			{"xo1x2o8x6o", "63"},
			{"xo1x2oo40x", "60"},
			{"xo1x2x12o1oo2x12o12x", "15"},
			{"xo1x3o", "48"},
			{"xo1x3o12o1o25x11x", "21"},
			{"xo1x3o12o20o6x11x", "63"},
			{"xo1x3o12o27x", "60"},
			{"xo1x3o12o30x", "63"},
			{"xo1x3o12o5o21x11x", "63"},
			{"xo1x3o12oo26x11x", "63"},
			{"xo1x3o12oo29x11x", "60"},
			{"xo1x3o13o19o6x12x", "60"},
			{"xo1x3o13o19o9x9x", "63"},
			{"xo1x3o13o26x", "60"},
			{"xo1x3o13o26x11o2x", "12"},
			{"xo1x3o14o13o11x11x", "63"},
			{"xo1x3o14o17o7x11x", "63"},
			{"xo1x3o14o1o23x11x", "63"},
			{"xo1x3o14o25x", "60"},
			{"xo1x3o14o28x", "33"},
			{"xo1x3o14o5o19x11x", "63"},
			{"xo1x3o15o15o8x6x", "63"},
			{"xo1x3o15o15x11x3o", "12"},
			{"xo1x3o15o24x", "60"},
			{"xo1x3o16o16o6x11x", "63"},
			{"xo1x3o16o23x", "60"},
			{"xo1x3o16o26x", "63"},
			{"xo1x3o17o22x", "60"},
			{"xo1x3o18o21x", "60"},
			{"xo1x3o19o20x", "60"},
			{"xo1x3o20o19x", "60"},
			{"xo1x3o20o22x", "63"},
			{"xo1x3o21o18x", "60"},
			{"xo1x3o22o17x", "60"},
			{"xo1x3o23o16x", "60"},
			{"xo1x3o28o11x", "60"},
			{"xo1x3o28o14x", "63"},
			{"xo1x3o28o4o6x11x", "63"},
			{"xo1x3o29o10x", "60"},
			{"xo1x3o29o2o7x11x", "63"},
			{"xo1x3o30o9x", "60"},
			{"xo1x3o31o8x", "60"},
			{"xo1x3o32o10x", "63"},
			{"xo1x3o32o4o2x11x", "63"},
			{"xo1x3o32o7x", "60"},
			{"xo1x3o32oo6x11x", "63"},
			{"xo1x3o33o6x", "60"},
			{"xo1x3o33o6x3o7x", "21"},
			{"xo1x3o33o6x7o3x", "63"},
			{"xo1x3o33o9x", "21"},
			{"xo1x3o34o5x", "60"},
			{"xo1x3o34o5x11o2x", "12"},
			{"xo1x3o35o4x", "60"},
			{"xo1x3o36o3x", "60"},
			{"xo1x3o36o6x", "63"},
			{"xo1x3o37o2x", "60"},
			{"xo1x3o38o1x", "60"},
			{"xo1x3o39ox", "60"},
			{"xo1x3o40x10o", "60"},
			{"xo1x3o40x11o", "63"},
			{"xo1x3o40x12o", "60"},
			{"xo1x3o40x13o", "60"},
			{"xo1x3o40x14o", "60"},
			{"xo1x3o40x1o", "60"},
			{"xo1x3o40x2o", "60"},
			{"xo1x3o40x3o", "63"},
			{"xo1x3o40x4o", "60"},
			{"xo1x3o40x5o", "60"},
			{"xo1x3o40x6o", "60"},
			{"xo1x3o40x7o", "63"},
			{"xo1x3o40x8o", "60"},
			{"xo1x3o40x9o", "60"},
			{"xo1x3o40xo", "60"},
			{"xo1x3o41o1x", "63"},
			{"xo1x3o42ox", "63"},
			{"xo1x3o43x10o", "63"},
			{"xo1x3o43x11o", "12"},
			{"xo1x3o43x1o", "63"},
			{"xo1x3o43x2o", "63"},
			{"xo1x3o43x3o", "12"},
			{"xo1x3o43x4o", "63"},
			{"xo1x3o43x5o", "63"},
			{"xo1x3o43x6o", "63"},
			{"xo1x3o43x7o", "12"},
			{"xo1x3o43x8o", "63"},
			{"xo1x3o43x9o", "63"},
			{"xo1x3o4o28o6x11x", "63"},
			{"xo1x3o4o35x", "63"},
			{"xo1x3o4o38x", "63"},
			{"xo1x3o4x10o", "60"},
			{"xo1x3o4x10o14o9o2x11x", "60"},
			{"xo1x3o4x10o1o22o2x11x", "60"},
			{"xo1x3o4x10o1o24ox11x", "15"},
			{"xo1x3o4x12o17o6ox11x", "60"},
			{"xo1x3o4x12o22o2x", "63"},
			{"xo1x3o4x12o24ox", "63"},
			{"xo1x3o4x12o25x11o", "60"},
			{"xo1x3o4x12o25x3o", "60"},
			{"xo1x3o4x12o25x7o", "60"},
			{"xo1x3o4x14o", "60"},
			{"xo1x3o4x19o", "60"},
			{"xo1x3o4x19o12o2x", "60"},
			{"xo1x3o4x19o8o6x", "63"},
			{"xo1x3o4x25o11ox", "48"},
			{"xo1x3o4x25o12x11o", "60"},
			{"xo1x3o4x25o12x3o", "21"},
			{"xo1x3o4x25o12x7o", "60"},
			{"xo1x3o4x25o9o2x", "63"},
			{"xo1x3o4x29o", "60"},
			{"xo1x3o4x2x10o1o", "63"},
			{"xo1x3o4x2x10o36o", "60"},
			{"xo1x3o4x2x4oo", "63"},
			{"xo1x3o4x2x4oo19o19x", "63"},
			{"xo1x3o4x2x4oo22o18x", "51"},
			{"xo1x3o4x2x4oo23o17x", "22"},
			{"xo1x3o4x2x4oo24o16x", "22"},
			{"xo1x3o4x2x4oo7o33x", "51"},
			{"xo1x3o4x2x4oo8o32x", "51"},
			{"xo1x3o4x2x4oo9o31x", "60"},
			{"xo1x3o4x2x5o14o", "41"},
			{"xo1x3o4x2x5o26o", "60"},
			{"xo1x3o4x2x5o38o", "48"},
			{"xo1x3o4x2x5o41o", "60"},
			{"xo1x3o4x2x5o8o", "41"},
			{"xo1x3o4x2x6o1o", "37"},
			{"xo1x3o4x2x6o21o", "37"},
			{"xo1x3o4x2x6o25o", "63"},
			{"xo1x3o4x2x6o37o", "48"},
			{"xo1x3o4x2x6o8o", "48"},
			{"xo1x3o4x2x9o24ox7o", "63"},
			{"xo1x3o4x2x9o34o", "63"},
			{"xo1x3o4x30o", "60"},
			{"xo1x3o4x35o", "51"},
			{"xo1x3o4x38o", "60"},
			{"xo1x3o4x48o", "41"},
			{"xo1x3o4x49o", "60"},
			{"xo1x3o4x50o", "60"},
			{"xo1x3o4x6o", "60"},
			{"xo1x3o4x8o", "41"},
			{"xo1x3o4x8o19o19x", "60"},
			{"xo1x3o4x8x16o3o8x3o", "37"},
			{"xo1x3o4x9o9o15x", "37"},
			{"xo1x3o4x9o9o27x", "63"},
			{"xo1x3o5o34x", "60"},
			{"xo1x3o6o33x", "60"},
			{"xo1x3o7o32x", "60"},
			{"xo1x3o7x10o13o22x", "60"},
			{"xo1x3o7x10o21x13o", "60"},
			{"xo1x3o7x10o21x14o", "60"},
			{"xo1x3o7x10o2o18x", "60"},
			{"xo1x3o7x10o3o17x", "63"},
			{"xo1x3o7x10o4o16x", "60"},
			{"xo1x3o7x13o", "48"},
			{"xo1x3o7x13o21x3o", "63"},
			{"xo1x3o7x14o", "48"},
			{"xo1x3o7x15o", "48"},
			{"xo1x3o7x32o", "63"},
			{"xo1x3o7x36o", "37"},
			{"xo1x3o7x40o", "63"},
			{"xo1x3o7x44o", "42"},
			{"xo1x3o7x4o", "63"},
			{"xo1x3o7x4o10o31x", "60"},
			{"xo1x3o7x4o16o25x", "60"},
			{"xo1x3o7x4o21o20x", "60"},
			{"xo1x3o7x4o5o18o2x11x", "51"},
			{"xo1x3o7x4o5o36x", "60"},
			{"xo1x3o7x4o9o11o17x2x", "51"},
			{"xo1x3o7x4oo41x", "60"},
			{"xo1x3o7x5o10o30x", "48"},
			{"xo1x3o7x5o14o26x", "60"},
			{"xo1x3o7x5o18o22x", "60"},
			{"xo1x3o7x5o19o19x", "63"},
			{"xo1x3o7x5o19o9x7o1x", "48"},
			{"xo1x3o7x5o19oo8x9x", "63"},
			{"xo1x3o7x5o26o14x", "60"},
			{"xo1x3o7x5o29x1o", "37"},
			{"xo1x3o7x5o34o6x", "60"},
			{"xo1x3o7x5o8o17x", "60"},
			{"xo1x3o7x5o8o20o8x2x", "48"},
			{"xo1x3o7x5oo18o9x9x", "37"},
			{"xo1x3o7x5oo28x", "37"},
			{"xo1x3o7x5x15o3o", "51"},
			{"xo1x3o7x5x19oo", "51"},
			{"xo1x3o7x6o", "37"},
			{"xo1x3o7x6o25x4o", "60"},
			{"xo1x3o7x6o27ox", "63"},
			{"xo1x3o7x6o3o24x", "37"},
			{"xo1x3o7x8o25ox", "63"},
			{"xo1x3o7x9o23o1x", "37"},
			{"xo1x3o7x9o24ox", "63"},
			{"xo1x3o7x9o25x3o", "48"},
			{"xo1x3o7x9o25x7o", "48"},
			{"xo1x3o7x9o3o21x", "21"},
			{"xo1x3oo39x", "60"},
			{"xo1x3oo3x2x9o3o21x", "63"},
			{"xo1x56o", "15"},
			{"xo1x57o", "12"},
			{"xo1x58o", "12"},
			{"xo1x8o", "51"},
			{"xo1x8o35o14x", "15"},
			{"xo1x8o38o8x", "48"},
			{"xo1x8x10o12o", "60"},
			{"xo1x8x10o12o4o18x", "63"},
			{"xo1x8x10o13o", "60"},
			{"xo1x8x10o14o", "60"},
			{"xo1x8x10o14o12x3o", "60"},
			{"xo1x8x10o15o", "60"},
			{"xo1x8x10o16o", "60"},
			{"xo1x8x10o16oo18x", "63"},
			{"xo1x8x10o17o", "60"},
			{"xo1x8x10o17o2o15x", "63"},
			{"xo1x8x10o17o6o11x", "63"},
			{"xo1x8x10o18o", "60"},
			{"xo1x8x10o19o", "60"},
			{"xo1x8x10o1o", "60"},
			{"xo1x8x10o1o14o19x", "63"},
			{"xo1x8x10o1o25x5o", "41"},
			{"xo1x8x10o20o", "60"},
			{"xo1x8x10o21o", "60"},
			{"xo1x8x10o22o", "60"},
			{"xo1x8x10o23o", "60"},
			{"xo1x8x10o24o", "60"},
			{"xo1x8x10o25o", "60"},
			{"xo1x8x10o26o", "60"},
			{"xo1x8x10o27o", "60"},
			{"xo1x8x10o28o", "60"},
			{"xo1x8x10o29o", "60"},
			{"xo1x8x10o2o", "60"},
			{"xo1x8x10o30o", "60"},
			{"xo1x8x10o31o", "60"},
			{"xo1x8x10o32o", "60"},
			{"xo1x8x10o33o", "60"},
			{"xo1x8x10o34o", "60"},
			{"xo1x8x10o35o", "60"},
			{"xo1x8x10o36o", "51"},
			{"xo1x8x10o37o", "60"},
			{"xo1x8x10o38o", "60"},
			{"xo1x8x10o39o", "60"},
			{"xo1x8x10o3o", "60"},
			{"xo1x8x10o4o", "60"},
			{"xo1x8x10o5o", "60"},
			{"xo1x8x10o6o", "60"},
			{"xo1x8x10o7o", "60"},
			{"xo1x8x10oo", "60"},
			{"xo1x8x10oo16o18x", "63"},
			{"xo1x8x10ooo34x", "63"},
			{"xo1x8x11o12o7o2x", "41"},
			{"xo1x8x11o14o", "51"},
			{"xo1x8x11o16o18x2o", "51"},
			{"xo1x8x11o16o1o16x", "63"},
			{"xo1x8x11o16o6x8o", "21"},
			{"xo1x8x11o16o9o8x", "63"},
			{"xo1x8x11o17o", "51"},
			{"xo1x8x11o18o", "60"},
			{"xo1x8x11o19o", "51"},
			{"xo1x8x11o1o21x7o", "25"},
			{"xo1x8x11o20o", "51"},
			{"xo1x8x11o21o", "51"},
			{"xo1x8x11o26o", "60"},
			{"xo1x8x11o2o", "25"},
			{"xo1x8x11o30o", "51"},
			{"xo1x8x11o36o", "51"},
			{"xo1x8x11o37o", "51"},
			{"xo1x8x11o38o", "51"},
			{"xo1x8x11o3o", "51"},
			{"xo1x8x11o4o", "51"},
			{"xo1x8x11o5o", "51"},
			{"xo1x8x11oo17o16x", "63"},
			{"xo1x8x11oo22x8o", "41"},
			{"xo1x8x11oo3o21x", "21"},
			{"xo1x8x12o14o10o8x", "63"},
			{"xo1x8x12o14o11o7x", "15"},
			{"xo1x8x12o14o13o5x", "63"},
			{"xo1x8x12o14o16o2x", "63"},
			{"xo1x8x12o14o17o1x", "63"},
			{"xo1x8x12o14o19x1o", "15"},
			{"xo1x8x12o14o19x2o", "51"},
			{"xo1x8x12o14o2o16x", "63"},
			{"xo1x8x12o14o4o14x", "10"},
			{"xo1x8x12o14o5o13x", "15"},
			{"xo1x8x12o14o6o12x", "63"},
			{"xo1x8x12o14o7x8o", "41"},
			{"xo1x8x12o16o", "8"},
			{"xo1x8x12o17o", "60"},
			{"xo1x8x12o18o", "48"},
			{"xo1x8x12o1o", "60"},
			{"xo1x8x12o1o12o19x", "26"},
			{"xo1x8x12o25o", "60"},
			{"xo1x8x12o2o", "48"},
			{"xo1x8x12o2o22x5o", "15"},
			{"xo1x8x12o2o8o13x", "48"},
			{"xo1x8x12o2oo21x", "21"},
			{"xo1x8x12o34o", "15"},
			{"xo1x8x12o35o", "41"},
			{"xo1x8x12o36o", "60"},
			{"xo1x8x12o37o", "60"},
			{"xo1x8x12o3o10o19x", "21"},
			{"xo1x8x12o3o14o6x", "63"},
			{"xo1x8x12o3o21x11o", "60"},
			{"xo1x8x12o3o6o14x", "63"},
			{"xo1x8x12o4o9o19x", "7"},
			{"xo1x8x12o5o8o19x", "63"},
			{"xo1x8x12oo1o19x", "60"},
			{"xo1x8x12oo1o22x", "21"},
			{"xo1x8x13o11o12x8o", "63"},
			{"xo1x8x13o11o5o6x", "63"},
			{"xo1x8x13o12o", "60"},
			{"xo1x8x13o13o7x7o", "25"},
			{"xo1x8x13o15o", "60"},
			{"xo1x8x13o16o", "60"},
			{"xo1x8x13o17o", "51"},
			{"xo1x8x13o18o", "60"},
			{"xo1x8x13o1o", "51"},
			{"xo1x8x13o1o9o12x", "63"},
			{"xo1x8x13o24o", "60"},
			{"xo1x8x13o28o", "60"},
			{"xo1x8x13o2o", "60"},
			{"xo1x8x13o32o", "60"},
			{"xo1x8x13o33o", "51"},
			{"xo1x8x13o34o", "41"},
			{"xo1x8x13o35o", "60"},
			{"xo1x8x13o36o", "60"},
			{"xo1x8x13o3o", "60"},
			{"xo1x8x13oo", "60"},
			{"xo1x8x14o10o", "60"},
			{"xo1x8x14o11o", "51"},
			{"xo1x8x14o12o", "51"},
			{"xo1x8x14o13o", "21"},
			{"xo1x8x14o13o6o11x", "63"},
			{"xo1x8x14o14o", "60"},
			{"xo1x8x14o15o", "60"},
			{"xo1x8x14o16o", "51"},
			{"xo1x8x14o17o", "60"},
			{"xo1x8x14o18o", "60"},
			{"xo1x8x14o19o", "60"},
			{"xo1x8x14o1o", "60"},
			{"xo1x8x14o20o", "60"},
			{"xo1x8x14o21o", "60"},
			{"xo1x8x14o22o", "60"},
			{"xo1x8x14o23o", "60"},
			{"xo1x8x14o24o", "60"},
			{"xo1x8x14o25o", "60"},
			{"xo1x8x14o26o", "60"},
			{"xo1x8x14o27o", "60"},
			{"xo1x8x14o28o", "60"},
			{"xo1x8x14o29o", "60"},
			{"xo1x8x14o2o", "60"},
			{"xo1x8x14o30o", "60"},
			{"xo1x8x14o31o", "60"},
			{"xo1x8x14o32o", "51"},
			{"xo1x8x14o33o", "60"},
			{"xo1x8x14o34o", "60"},
			{"xo1x8x14o35o", "60"},
			{"xo1x8x14o3o", "60"},
			{"xo1x8x14o8o", "60"},
			{"xo1x8x14o8o4o18x", "63"},
			{"xo1x8x14o9o", "60"},
			{"xo1x8x14oo", "51"},
			{"xo1x8x15o11o", "51"},
			{"xo1x8x15o12o", "15"},
			{"xo1x8x15o13o", "10"},
			{"xo1x8x15o14o", "60"},
			{"xo1x8x15o16o", "51"},
			{"xo1x8x15o17o", "51"},
			{"xo1x8x15o1o", "51"},
			{"xo1x8x15o22o", "60"},
			{"xo1x8x15o23o", "51"},
			{"xo1x8x15o24o", "51"},
			{"xo1x8x15o25o", "48"},
			{"xo1x8x15o27o", "51"},
			{"xo1x8x15o28o", "51"},
			{"xo1x8x15o29o", "51"},
			{"xo1x8x15o31o", "51"},
			{"xo1x8x15o32o", "51"},
			{"xo1x8x15o33o", "51"},
			{"xo1x8x15o34o", "51"},
			{"xo1x8x15o7o", "51"},
			{"xo1x8x15o8o", "51"},
			{"xo1x8x15o9o", "48"},
			{"xo1x8x15oo", "51"},
			{"xo1x8x15ooox", "51"},
			{"xo1x8x15ooxo", "51"},
			{"xo1x8x16o10o", "51"},
			{"xo1x8x16o11o6o11x", "63"},
			{"xo1x8x16o12o", "60"},
			{"xo1x8x16o13o", "60"},
			{"xo1x8x16o14o", "51"},
			{"xo1x8x16o16o", "60"},
			{"xo1x8x16o18o", "60"},
			{"xo1x8x16o21o", "60"},
			{"xo1x8x16o25o", "60"},
			{"xo1x8x16o26o", "60"},
			{"xo1x8x16o29o", "60"},
			{"xo1x8x16o31o", "41"},
			{"xo1x8x16o32o", "60"},
			{"xo1x8x16o33o", "60"},
			{"xo1x8x16o6o", "51"},
			{"xo1x8x16o9o", "60"},
			{"xo1x8x16oo", "60"},
			{"xo1x8x17o10o6o11x", "21"},
			{"xo1x8x17o11o", "60"},
			{"xo1x8x17o12o", "60"},
			{"xo1x8x17o13o", "51"},
			{"xo1x8x17o14o", "60"},
			{"xo1x8x17o17o", "51"},
			{"xo1x8x17o19o", "60"},
			{"xo1x8x17o20o", "60"},
			{"xo1x8x17o24o", "60"},
			{"xo1x8x17o29o", "51"},
			{"xo1x8x17o30o", "41"},
			{"xo1x8x17o31o", "60"},
			{"xo1x8x17o32o", "60"},
			{"xo1x8x17o5o", "51"},
			{"xo1x8x17o6o", "60"},
			{"xo1x8x17o9o", "51"},
			{"xo1x8x18o10o", "60"},
			{"xo1x8x18o11o", "60"},
			{"xo1x8x18o19o", "60"},
			{"xo1x8x18o28o", "51"},
			{"xo1x8x18o29o", "41"},
			{"xo1x8x18o30o", "60"},
			{"xo1x8x18o31o", "60"},
			{"xo1x8x1o10o10o11x", "15"},
			{"xo1x8x1o10o10o14x", "63"},
			{"xo1x8x1o10o14o10x", "63"},
			{"xo1x8x1o10o14o7x", "15"},
			{"xo1x8x1o10o18o6x", "63"},
			{"xo1x8x1o10o22o2x", "63"},
			{"xo1x8x1o10o25x8o", "63"},
			{"xo1x8x1o10o2o22x", "63"},
			{"xo1x8x1o12o", "60"},
			{"xo1x8x1o13o", "51"},
			{"xo1x8x1o13o9o12x", "48"},
			{"xo1x8x1o21o", "51"},
			{"xo1x8x1o21o1o12x", "63"},
			{"xo1x8x1o21o5o5x", "51"},
			{"xo1x8x1o23o12x8o", "15"},
			{"xo1x8x1o23o1o10x", "63"},
			{"xo1x8x1o23o1o7x", "60"},
			{"xo1x8x1o23o5o6x", "63"},
			{"xo1x8x1o23o9o2x", "63"},
			{"xo1x8x1o23o9x11o", "21"},
			{"xo1x8x1o25o1o5x", "15"},
			{"xo1x8x1o25o7x1o", "15"},
			{"xo1x8x1o27o", "60"},
			{"xo1x8x1o28o", "60"},
			{"xo1x8x1o29o", "51"},
			{"xo1x8x1o33o", "51"},
			{"xo1x8x1o33x1o9o", "15"},
			{"xo1x8x1o36o", "60"},
			{"xo1x8x1o45o", "51"},
			{"xo1x8x1o46o", "41"},
			{"xo1x8x1o47o", "60"},
			{"xo1x8x1o48o", "60"},
			{"xo1x8x1o5o", "51"},
			{"xo1x8x1o5o17o12x", "63"},
			{"xo1x8x1o5o17o9x", "26"},
			{"xo1x8x1o5o21o5x", "51"},
			{"xo1x8x1o5o27x1o", "15"},
			{"xo1x8x1o5o4o22x", "15"},
			{"xo1x8x1o5o4o25x", "63"},
			{"xo1x8x1o5oo38x", "63"},
			{"xo1x8x1o6o", "60"},
			{"xo1x8x1o6o19o19x", "60"},
			{"xo1x8x1o7o15o1o7x11x", "51"},
			{"xo1x8x1o8o", "60"},
			{"xo1x8x1o9o", "51"},
			{"xo1x8x1o9o13o12x", "63"},
			{"xo1x8x1o9o13o9x", "63"},
			{"xo1x8x1o9o17o5x", "51"},
			{"xo1x8x1o9o23x1o", "15"},
			{"xo1x8x1o9oo22x", "15"},
			{"xo1x8x1o9oo25x", "15"},
			{"xo1x8x1o9ooox20x", "15"},
			{"xo1x8x1oo22o12x", "48"},
			{"xo1x8x1oo9o25x", "60"},
			{"xo1x8x23o14o", "60"},
			{"xo1x8x23o1o5o6x", "39"},
			{"xo1x8x23o22o", "51"},
			{"xo1x8x23o24o", "41"},
			{"xo1x8x23o25o", "51"},
			{"xo1x8x23o26o", "51"},
			{"xo1x8x23o2o", "51"},
			{"xo1x8x23o4o18x2o", "51"},
			{"xo1x8x23o4o1o16x", "63"},
			{"xo1x8x23o4o4o13x", "21"},
			{"xo1x8x23o4o9o8x", "63"},
			{"xo1x8x23o5o", "10"},
			{"xo1x8x23o6o", "60"},
			{"xo1x8x23o7o", "51"},
			{"xo1x8x23o8o", "51"},
			{"xo1x8x23o9o", "60"},
			{"xo1x8x24o13o", "63"},
			{"xo1x8x24o17o", "60"},
			{"xo1x8x24o1o", "60"},
			{"xo1x8x24o21o", "60"},
			{"xo1x8x24o22o", "51"},
			{"xo1x8x24o23o", "41"},
			{"xo1x8x24o24o", "60"},
			{"xo1x8x24o25o", "60"},
			{"xo1x8x24o4o", "60"},
			{"xo1x8x24o5o", "60"},
			{"xo1x8x24o6o", "51"},
			{"xo1x8x24o7o", "48"},
			{"xo1x8x24o8o", "60"},
			{"xo1x8x25o12o", "60"},
			{"xo1x8x25o1o15o3x", "63"},
			{"xo1x8x25o21o", "15"},
			{"xo1x8x25o22o", "41"},
			{"xo1x8x25o23o", "60"},
			{"xo1x8x25o24o", "60"},
			{"xo1x8x25o3o", "10"},
			{"xo1x8x25o4o", "60"},
			{"xo1x8x25o5o", "48"},
			{"xo1x8x26o11o", "60"},
			{"xo1x8x26o12o", "60"},
			{"xo1x8x26o1o6o11x", "63"},
			{"xo1x8x26o21o", "41"},
			{"xo1x8x26o22o", "60"},
			{"xo1x8x26o23o", "60"},
			{"xo1x8x26o2o", "60"},
			{"xo1x8x26o3o", "60"},
			{"xo1x8x26o6o", "60"},
			{"xo1x8x26o8o", "60"},
			{"xo1x8x27o10o", "60"},
			{"xo1x8x27o18o", "51"},
			{"xo1x8x27o19o", "51"},
			{"xo1x8x27o1o", "41"},
			{"xo1x8x27o20o", "41"},
			{"xo1x8x27o21o", "51"},
			{"xo1x8x27o22o", "51"},
			{"xo1x8x27o2o", "60"},
			{"xo1x8x27o3o", "51"},
			{"xo1x8x27o4o", "51"},
			{"xo1x8x27o5o", "51"},
			{"xo1x8x27o7o", "51"},
			{"xo1x8x27oo18x2o", "51"},
			{"xo1x8x27oo9o8x", "63"},
			{"xo1x8x28o18o", "15"},
			{"xo1x8x28o1o", "60"},
			{"xo1x8x28o1o12o3x", "63"},
			{"xo1x8x28o1o4o11x", "63"},
			{"xo1x8x28o1oo15x", "51"},
			{"xo1x8x28o20o", "21"},
			{"xo1x8x28o21o", "10"},
			{"xo1x8x28o2o", "21"},
			{"xo1x8x28o2o6o8x", "63"},
			{"xo1x8x28o3o2o11x", "37"},
			{"xo1x8x28o4o", "15"},
			{"xo1x8x28o4o1o11x", "63"},
			{"xo1x8x28o6o10ox", "63"},
			{"xo1x8x28o6o11x2o", "21"},
			{"xo1x8x28o6o2o8x", "63"},
			{"xo1x8x28o6o3o7x", "63"},
			{"xo1x8x28o6o6o4x", "63"},
			{"xo1x8x28o6oxoo8x", "63"},
			{"xo1x8x28o9o", "60"},
			{"xo1x8x28oo", "48"},
			{"xo1x8x29o10o", "60"},
			{"xo1x8x29o11o", "60"},
			{"xo1x8x29o12o", "60"},
			{"xo1x8x29o13o", "51"},
			{"xo1x8x29o14o", "60"},
			{"xo1x8x29o15o", "60"},
			{"xo1x8x29o16o", "60"},
			{"xo1x8x29o17o", "51"},
			{"xo1x8x29o18o", "41"},
			{"xo1x8x29o19o", "60"},
			{"xo1x8x29o1o", "15"},
			{"xo1x8x29o20o", "60"},
			{"xo1x8x29o2o", "60"},
			{"xo1x8x29o3o", "51"},
			{"xo1x8x29o4o", "60"},
			{"xo1x8x29o5o", "10"},
			{"xo1x8x29o6o", "60"},
			{"xo1x8x29o7o", "60"},
			{"xo1x8x29o8o", "60"},
			{"xo1x8x29o8o8o2x", "21"},
			{"xo1x8x29o9o", "60"},
			{"xo1x8x29oo", "60"},
			{"xo1x8x2o11o", "51"},
			{"xo1x8x2o11o10o12x", "60"},
			{"xo1x8x2o11o11x11o", "60"},
			{"xo1x8x2o14o14x14o", "51"},
			{"xo1x8x2o14x14o14o", "51"},
			{"xo1x8x2o15o6o12x", "60"},
			{"xo1x8x2o15o6o8x3x11o", "60"},
			{"xo1x8x2o20o1o12x", "37"},
			{"xo1x8x2o20o1o9x", "63"},
			{"xo1x8x2o20oo10x", "63"},
			{"xo1x8x2o22o12x10o", "60"},
			{"xo1x8x2o22o12x11o", "60"},
			{"xo1x8x2o22o12x6o", "60"},
			{"xo1x8x2o22o12x8o", "63"},
			{"xo1x8x2o22o1o10x", "63"},
			{"xo1x8x2o22o1o7x", "63"},
			{"xo1x8x2o22o4o7x", "60"},
			{"xo1x8x2o22o9o2x", "60"},
			{"xo1x8x2o25o6o11x", "63"},
			{"xo1x8x2o26o", "60"},
			{"xo1x8x2o27o", "60"},
			{"xo1x8x2o35o", "60"},
			{"xo1x8x2o45o", "41"},
			{"xo1x8x2o46o", "60"},
			{"xo1x8x2o47o", "60"},
			{"xo1x8x2o4o17o12x", "63"},
			{"xo1x8x2o4o17o9x", "63"},
			{"xo1x8x2o4o4o22x", "63"},
			{"xo1x8x2o4o4o25x", "63"},
			{"xo1x8x2o5o", "41"},
			{"xo1x8x2o5o19o19x", "60"},
			{"xo1x8x2o5o3o11x10o2x", "41"},
			{"xo1x8x2o5x3o11x3oo5o2x", "63"},
			{"xo1x8x2o6o25o11x", "63"},
			{"xo1x8x2o7o", "60"},
			{"xo1x8x2o7o14o12x", "60"},
			{"xo1x8x2o7o1o25x", "60"},
			{"xo1x8x2o8o13o12x", "21"},
			{"xo1x8x2o8o13o9x", "63"},
			{"xo1x8x2o8o1o21x", "63"},
			{"xo1x8x2o8oo22x", "63"},
			{"xo1x8x2o8oo25x", "63"},
			{"xo1x8x2o9o10o11x", "63"},
			{"xo1x8x2o9o10o14x", "21"},
			{"xo1x8x2o9o14o10x", "63"},
			{"xo1x8x2o9o14o7x", "63"},
			{"xo1x8x2o9o17o7x", "60"},
			{"xo1x8x2o9o1o23x", "48"},
			{"xo1x8x2o9o22o2x", "37"},
			{"xo1x8x2o9o25x10o", "63"},
			{"xo1x8x2o9o25x11o", "60"},
			{"xo1x8x2o9o25x6o", "60"},
			{"xo1x8x2o9o25x8o", "63"},
			{"xo1x8x2o9o5o15x3x11o", "60"},
			{"xo1x8x2o9o5o19x", "60"},
			{"xo1x8x30o10o", "60"},
			{"xo1x8x30o11o", "60"},
			{"xo1x8x30o12o", "60"},
			{"xo1x8x30o13o", "60"},
			{"xo1x8x30o14o", "60"},
			{"xo1x8x30o15o", "60"},
			{"xo1x8x30o16o", "51"},
			{"xo1x8x30o17o", "60"},
			{"xo1x8x30o18o", "60"},
			{"xo1x8x30o19o", "60"},
			{"xo1x8x30o1o", "60"},
			{"xo1x8x30o2o", "60"},
			{"xo1x8x30o3o", "60"},
			{"xo1x8x30o4o", "60"},
			{"xo1x8x30o5o", "60"},
			{"xo1x8x30o6o", "60"},
			{"xo1x8x30o7o", "60"},
			{"xo1x8x30o8o", "60"},
			{"xo1x8x30o9o", "60"},
			{"xo1x8x30oo", "60"},
			{"xo1x8x31o11o", "51"},
			{"xo1x8x31o12o", "48"},
			{"xo1x8x31o13o", "51"},
			{"xo1x8x31o15o", "51"},
			{"xo1x8x31o16o", "51"},
			{"xo1x8x31o17o", "51"},
			{"xo1x8x31o18o", "51"},
			{"xo1x8x31o1o", "51"},
			{"xo1x8x31o6o", "60"},
			{"xo1x8x31o7o", "51"},
			{"xo1x8x31o8o", "51"},
			{"xo1x8x31o9o", "51"},
			{"xo1x8x31oo", "51"},
			{"xo1x8x31ooox", "51"},
			{"xo1x8x31oxoo", "51"},
			{"xo1x8x32o13o", "60"},
			{"xo1x8x32o14o", "51"},
			{"xo1x8x32o15o", "41"},
			{"xo1x8x32o16o", "60"},
			{"xo1x8x32o17o", "60"},
			{"xo1x8x32o2o", "60"},
			{"xo1x8x32o3o", "60"},
			{"xo1x8x32o5o", "60"},
			{"xo1x8x32oo", "60"},
			{"xo1x8x33o12o", "60"},
			{"xo1x8x33o14o", "41"},
			{"xo1x8x33o15o", "60"},
			{"xo1x8x33o16o", "60"},
			{"xo1x8x33o1o", "60"},
			{"xo1x8x33o4o", "60"},
			{"xo1x8x33o5o", "60"},
			{"xo1x8x33o8o", "60"},
			{"xo1x8x34o12o", "51"},
			{"xo1x8x34o13o", "41"},
			{"xo1x8x34o14o", "60"},
			{"xo1x8x34o15o", "60"},
			{"xo1x8x34o3o", "60"},
			{"xo1x8x35o10o", "60"},
			{"xo1x8x35o11o", "51"},
			{"xo1x8x35o12o", "60"},
			{"xo1x8x35o13o", "51"},
			{"xo1x8x35o14o", "60"},
			{"xo1x8x35o1o", "51"},
			{"xo1x8x35o2o", "60"},
			{"xo1x8x35o3o", "60"},
			{"xo1x8x35o3x3o3o", "51"},
			{"xo1x8x35o6o", "60"},
			{"xo1x8x35o7o", "51"},
			{"xo1x8x35oo", "51"},
			{"xo1x8x35oxoo", "60"},
			{"xo1x8x36o11o", "41"},
			{"xo1x8x36o12o", "60"},
			{"xo1x8x36o13o", "60"},
			{"xo1x8x36o1o", "60"},
			{"xo1x8x36x3o3o3o", "41"},
			{"xo1x8x37o10o", "41"},
			{"xo1x8x37o11o", "60"},
			{"xo1x8x37o12o", "60"},
			{"xo1x8x37oo", "60"},
			{"xo1x8x38o10o", "60"},
			{"xo1x8x38o11o", "60"},
			{"xo1x8x38o1o", "60"},
			{"xo1x8x38o2o", "60"},
			{"xo1x8x38o3o", "60"},
			{"xo1x8x38o4o", "60"},
			{"xo1x8x38o5o", "60"},
			{"xo1x8x38o6o", "60"},
			{"xo1x8x38o7o", "60"},
			{"xo1x8x38o8o", "63"},
			{"xo1x8x38o9o", "60"},
			{"xo1x8x38oo", "60"},
			{"xo1x8x39o10o", "60"},
			{"xo1x8x39o2o", "60"},
			{"xo1x8x39o6o", "60"},
			{"xo1x8x39o8o", "41"},
			{"xo1x8x39o9o", "60"},
			{"xo1x8x40o1o", "60"},
			{"xo1x8x40o6o", "51"},
			{"xo1x8x40o7o", "41"},
			{"xo1x8x40o8o", "60"},
			{"xo1x8x40o9o", "60"},
			{"xo1x8x41o5o", "51"},
			{"xo1x8x41o6o", "41"},
			{"xo1x8x41o7o", "60"},
			{"xo1x8x41o8o", "60"},
			{"xo1x8x42o3o", "51"},
			{"xo1x8x42o5o", "41"},
			{"xo1x8x42o6o", "60"},
			{"xo1x8x42o7o", "51"},
			{"xo1x8x42oo", "60"},
			{"xo1x8x43o2o", "60"},
			{"xo1x8x43o4o", "41"},
			{"xo1x8x43o5o", "60"},
			{"xo1x8x43o6o", "60"},
			{"xo1x8x44o2o", "51"},
			{"xo1x8x44o3o", "41"},
			{"xo1x8x44o4o", "60"},
			{"xo1x8x44o5o", "60"},
			{"xo1x8x45o1o", "51"},
			{"xo1x8x45o2o", "41"},
			{"xo1x8x45o3o", "60"},
			{"xo1x8x45o4o", "60"},
			{"xo1x8x45oo", "60"},
			{"xo1x8x46o1o", "41"},
			{"xo1x8x46o2o", "60"},
			{"xo1x8x46o3o", "51"},
			{"xo1x8x47o1o", "51"},
			{"xo1x8x47o2o", "51"},
			{"xo1x8x47oo", "10"},
			{"xo1x8x47ooxo", "10"},
			{"xo1x8x47oxoo", "10"},
			{"xo1x8x48o1o", "60"},
			{"xo1x8x48oo", "41"},
			{"xo1x8x49oo", "60"},
			{"xo1x8x7o17o7o13x", "42"},
			{"xo1x8x7o1o20o16x", "63"},
			{"xo1x8x7o1o28o8x", "63"},
			{"xo1x8x7o1o37x1o", "63"},
			{"xo1x8x7o1o4o32x", "63"},
			{"xo1x8x7o20o1o16x", "63"},
			{"xo1x8x7o20o9o8x", "63"},
			{"xo1x8x7o21o", "51"},
			{"xo1x8x7o22o", "60"},
			{"xo1x8x7o23o", "51"},
			{"xo1x8x7o24o", "51"},
			{"xo1x8x7o25o", "51"},
			{"xo1x8x7o27o", "51"},
			{"xo1x8x7o2o", "60"},
			{"xo1x8x7o2o17o18x", "21"},
			{"xo1x8x7o2o1o34x", "63"},
			{"xo1x8x7o2o36xo", "21"},
			{"xo1x8x7o30o", "60"},
			{"xo1x8x7o34o", "51"},
			{"xo1x8x7o39o", "51"},
			{"xo1x8x7o40o", "41"},
			{"xo1x8x7o41o", "51"},
			{"xo1x8x7o42o", "51"},
			{"xo1x8x7o4o22x8o", "41"},
			{"xo1x8x7o4o26o7x", "63"},
			{"xo1x8x7o4o28o5x", "63"},
			{"xo1x8x7o4o32o1x", "63"},
			{"xo1x8x7o4o3o21x", "63"},
			{"xo1x8x7o4o4o29x", "11"},
			{"xo1x8x7o6o", "60"},
			{"xo1x8x7o6o13o18x", "63"},
			{"xo1x8x7o7o", "51"},
			{"xo1x8x7o8o", "51"},
			{"xo1x8x7o9o", "51"},
			{"xo1x8x7oo", "15"},
			{"xo1x8x7oo16o7o13x2x", "51"},
			{"xo1x8x7oo17o20x", "63"},
			{"xo1x8x7oo18o19x", "63"},
			{"xo1x8x7oo19o19x", "60"},
			{"xo1x8x7oo21o16x", "63"},
			{"xo1x8x7oo23o14x", "63"},
			{"xo1x8x7oo24o13x", "63"},
			{"xo1x8x7oo28o9x", "63"},
			{"xo1x8x7oo29o8x", "63"},
			{"xo1x8x7oo34o3x", "63"},
			{"xo1x8x7oo35o2x", "63"},
			{"xo1x8x7oo38x1o", "15"},
			{"xo1x8x7oo5o32x", "63"},
			{"xo1x8x7oo7o30x", "25"},
			{"xo1x8x7ooox1x3o8o21x", "55"},
			{"xo1x8x7xoo2o14ox", "48"},
			{"xo1x8x8o14o", "15"},
			{"xo1x8x8o14o4o19x", "60"},
			{"xo1x8x8o15o", "41"},
			{"xo1x8x8o15o3o19x", "13"},
			{"xo1x8x8o16o", "7"},
			{"xo1x8x8o16o1o19x", "63"},
			{"xo1x8x8o16o1o5o13x2x", "51"},
			{"xo1x8x8o16o2o19x", "60"},
			{"xo1x8x8o17o", "60"},
			{"xo1x8x8o17o1o19x", "60"},
			{"xo1x8x8o18o", "60"},
			{"xo1x8x8o18oo19x", "60"},
			{"xo1x8x8o19o10o8x", "60"},
			{"xo1x8x8o19o11o7x", "5"},
			{"xo1x8x8o19o12o6x", "60"},
			{"xo1x8x8o19o13o5x", "60"},
			{"xo1x8x8o19o14o4x", "60"},
			{"xo1x8x8o19o15o3x", "13"},
			{"xo1x8x8o19o16o2x", "60"},
			{"xo1x8x8o19o17o1x", "60"},
			{"xo1x8x8o19o18ox", "10"},
			{"xo1x8x8o19o19x1o", "10"},
			{"xo1x8x8o19o19xo", "60"},
			{"xo1x8x8o19o1o17x", "60"},
			{"xo1x8x8o19o2o16x", "60"},
			{"xo1x8x8o19o3o15x", "60"},
			{"xo1x8x8o19o4o14x", "60"},
			{"xo1x8x8o19o5o13x", "60"},
			{"xo1x8x8o19o6o12x", "60"},
			{"xo1x8x8o19o7o11x", "13"},
			{"xo1x8x8o19o8o10x", "60"},
			{"xo1x8x8o19o9o9x", "60"},
			{"xo1x8x8o19oo18x", "60"},
			{"xo1x8x8o19x15o2o", "51"},
			{"xo1x8x8o1o", "60"},
			{"xo1x8x8o1o16o19x", "63"},
			{"xo1x8x8o1o17o19x", "60"},
			{"xo1x8x8o20o", "41"},
			{"xo1x8x8o21o", "60"},
			{"xo1x8x8o22o", "41"},
			{"xo1x8x8o23o", "60"},
			{"xo1x8x8o24o", "60"},
			{"xo1x8x8o25o", "41"},
			{"xo1x8x8o26o", "41"},
			{"xo1x8x8o27o", "41"},
			{"xo1x8x8o28o", "60"},
			{"xo1x8x8o29o", "60"},
			{"xo1x8x8o29o8o2x", "41"},
			{"xo1x8x8o2o", "41"},
			{"xo1x8x8o2o13o2x", "48"},
			{"xo1x8x8o2o16o19x", "60"},
			{"xo1x8x8o30o", "41"},
			{"xo1x8x8o31o", "41"},
			{"xo1x8x8o32o", "41"},
			{"xo1x8x8o33o", "41"},
			{"xo1x8x8o34o", "60"},
			{"xo1x8x8o35o", "41"},
			{"xo1x8x8o36o", "41"},
			{"xo1x8x8o37o", "41"},
			{"xo1x8x8o38o", "15"},
			{"xo1x8x8o3o", "41"},
			{"xo1x8x8o3o14o19x", "63"},
			{"xo1x8x8o3o15o19x", "13"},
			{"xo1x8x8o3o15x18o", "48"},
			{"xo1x8x8o40o", "41"},
			{"xo1x8x8o41o", "41"},
			{"xo1x8x8o4o", "41"},
			{"xo1x8x8o4o14o19x", "60"},
			{"xo1x8x8o5o", "60"},
			{"xo1x8x8o5o13o19x", "60"},
			{"xo1x8x8o6o", "48"},
			{"xo1x8x8o6o12o19x", "60"},
			{"xo1x8x8o7o", "60"},
			{"xo1x8x8o7o11o19x", "13"},
			{"xo1x8x8o7o8o1o19x2x", "25"},
			{"xo1x8x8o8o", "41"},
			{"xo1x8x8o8o10o19x", "60"},
			{"xo1x8x8o8o7o1o19x2x", "51"},
			{"xo1x8x8o9o", "41"},
			{"xo1x8x8o9o9o19x", "60"},
			{"xo1x8x8oo", "48"},
			{"xo1x8x8oo14x10o3o7x", "63"},
			{"xo1x8x8oo17o19x", "63"},
			{"xo1x8x8oo18o19x", "60"},
			{"xo1x8x8oo18x12o", "48"},
			{"xo1x8x8oo2o15x", "20"},
			{"xo1x8x8oo2x3o8o1o19x2x", "51"},
			{"xo1x8x8x3oo1o13o8x", "63"},
			{"xo1x8x9o13o14o8x", "63"},
			{"xo1x8x9o13o23x2o", "51"},
			{"xo1x8x9o13o6o16x", "63"},
			{"xo1x8x9o13o9o13x", "51"},
			{"xo1x8x9o15o2x19o", "48"},
			{"xo1x8x9o17o10o8x", "63"},
			{"xo1x8x9o17o19x2o", "51"},
			{"xo1x8x9o17o2o16x", "63"},
			{"xo1x8x9o18x12o6o", "48"},
			{"xo1x8x9o19o", "10"},
			{"xo1x8x9o1o18o16x", "63"},
			{"xo1x8x9o1o26o8x", "63"},
			{"xo1x8x9o1o35x2o", "51"},
			{"xo1x8x9o20o", "60"},
			{"xo1x8x9o21o", "51"},
			{"xo1x8x9o25o3o7x", "37"},
			{"xo1x8x9o28o", "60"},
			{"xo1x8x9o2o15x19o", "48"},
			{"xo1x8x9o2o3o21x", "63"},
			{"xo1x8x9o37o", "51"},
			{"xo1x8x9o38o", "51"},
			{"xo1x8x9o39o", "51"},
			{"xo1x8x9o40o", "51"},
			{"xo1x8x9o4o", "51"},
			{"xo1x8x9o4o8o23x", "63"},
			{"xo1x8x9o5o", "51"},
			{"xo1x8x9o5o14o16x", "51"},
			{"xo1x8x9o5o22o8x", "63"},
			{"xo1x8x9o5o25o5x", "63"},
			{"xo1x8x9o5o28o2x", "63"},
			{"xo1x8x9o5o31x2o", "51"},
			{"xo1x8x9o5o9o12x", "48"},
			{"xo1x8x9o6o", "51"},
			{"xo1x8x9o7o17o11x", "26"},
			{"xo1x8x9oo", "60"},
			{"xo1x8x9oo12o23x", "63"},
			{"xo1x8x9oo16o19x", "63"},
			{"xo1x8x9oo20o15x", "63"},
			{"xo1x8x9oo24o11x", "63"},
			{"xo1x8x9oo28o7x", "63"},
			{"xo1x8x9oo4o31x", "63"},
			{"xo1x8x9ooo35x", "63"},
			{"xo1x8xo10o", "51"},
			{"xo1x8xo13o", "60"},
			{"xo1x8xo14o", "51"},
			{"xo1x8xo1o22o12x", "60"},
			{"xo1x8xo1o9o25x", "41"},
			{"xo1x8xo22o", "51"},
			{"xo1x8xo26o", "51"},
			{"xo1x8xo27o6o11x", "25"},
			{"xo1x8xo28o", "60"},
			{"xo1x8xo29o", "60"},
			{"xo1x8xo30o", "51"},
			{"xo1x8xo34o", "60"},
			{"xo1x8xo37o", "60"},
			{"xo1x8xo46o", "51"},
			{"xo1x8xo47o", "41"},
			{"xo1x8xo48o", "60"},
			{"xo1x8xo49o", "60"},
			{"xo1x8xo7o", "41"},
			{"xo1x8xo7o19o19x", "15"},
			{"xo1x8xo9o", "60"},
			{"xo1xo", "51"},
			{"xo1xo10o32x", "60"},
			{"xo1xo10o35x", "63"},
			{"xo1xo10o5o29x11x", "60"},
			{"xo1xo10x10o21x11o", "63"},
			{"xo1xo10x10o21x3o", "63"},
			{"xo1xo10x10o21x7o", "63"},
			{"xo1xo10x10o21xo", "63"},
			{"xo1xo10x16o", "63"},
			{"xo1xo10x17o", "63"},
			{"xo1xo10x18o", "63"},
			{"xo1xo10x19o", "63"},
			{"xo1xo10x1o", "63"},
			{"xo1xo10x20o", "63"},
			{"xo1xo10x24o", "63"},
			{"xo1xo10x25o", "63"},
			{"xo1xo10x25o1o19x", "60"},
			{"xo1xo10x26o8o11x", "60"},
			{"xo1xo10x32o", "63"},
			{"xo1xo10x35o", "63"},
			{"xo1xo10x3o", "63"},
			{"xo1xo10x3o1o16x16o7x", "12"},
			{"xo1xo10x3o1o19o9x9x", "63"},
			{"xo1xo10x3o1o29x", "60"},
			{"xo1xo10x3o22o8x", "60"},
			{"xo1xo10x3o26o4x", "63"},
			{"xo1xo10x3o2o28x", "63"},
			{"xo1xo10x3o2o2o2x22x", "63"},
			{"xo1xo10x3o6o11x9x1o", "60"},
			{"xo1xo10x44o", "63"},
			{"xo1xo10x45o", "21"},
			{"xo1xo10x46o", "63"},
			{"xo1xo10x4o", "63"},
			{"xo1xo10x5o13o27x", "38"},
			{"xo1xo10x5o13o2x16o7x", "60"},
			{"xo1xo10x5o13o2x20o3x", "60"},
			{"xo1xo10x5o13o2x4o19x", "60"},
			{"xo1xo10x5o13o2xo23x", "60"},
			{"xo1xo10x5o19o19x", "63"},
			{"xo1xo10x5o1o11o2x24x", "60"},
			{"xo1xo10x5o29o11x", "60"},
			{"xo1xo10x5o5o7o2x24x", "60"},
			{"xo1xo10x6o", "63"},
			{"xo1xo10xo", "63"},
			{"xo1xo12o25o7x11x", "60"},
			{"xo1xo12o33x", "63"},
			{"xo1xo12o5o27x11x", "60"},
			{"xo1xo13o32x", "63"},
			{"xo1xo13o44x", "51"},
			{"xo1xo14o1o29x11x", "60"},
			{"xo1xo14o22o8x11x", "60"},
			{"xo1xo14o28x", "60"},
			{"xo1xo14o2o2x2o19x11x", "63"},
			{"xo1xo14o31x", "63"},
			{"xo1xo14o40x", "48"},
			{"xo1xo14o43x", "15"},
			{"xo1xo14o5o22x2x", "22"},
			{"xo1xo14o5o25x11x", "60"},
			{"xo1xo15o15o14xx", "48"},
			{"xo1xo15o15x14xo", "63"},
			{"xo1xo15o30x", "63"},
			{"xo1xo16o17o11x11x", "60"},
			{"xo1xo16o19o6x12x", "60"},
			{"xo1xo16o19o9x9x", "63"},
			{"xo1xo16o1o27x11x", "22"},
			{"xo1xo16o21o7x11x", "60"},
			{"xo1xo16o26x", "41"},
			{"xo1xo16o29x", "63"},
			{"xo1xo16o29x7o3x", "60"},
			{"xo1xo16o5o23x11x", "60"},
			{"xo1xo16o9o19x11x", "60"},
			{"xo1xo17o28x", "63"},
			{"xo1xo17o28x8x2o", "15"},
			{"xo1xo17oo24x11x", "21"},
			{"xo1xo18o10o16x11x", "60"},
			{"xo1xo18o14o12x11x", "60"},
			{"xo1xo18o18o8x11x", "60"},
			{"xo1xo18o1o25x11x", "60"},
			{"xo1xo18o24x", "60"},
			{"xo1xo18o27x", "63"},
			{"xo1xo18o5o21x11x", "60"},
			{"xo1xo19o26x", "63"},
			{"xo1xo1o2x11x3o9o2x1xoo8x", "37"},
			{"xo1xo1o44x", "63"},
			{"xo1xo1o5x7xo16o1ox13x", "48"},
			{"xo1xo1o5x7xo16o2x13x4o", "48"},
			{"xo1xo1o5x7xoo15o1ox12xx", "48"},
			{"xo1xo1o5x7xoo15o2x12xx4o", "48"},
			{"xo1xo1o5x8o16o2x13x", "7"},
			{"xo1xo1o5x8o16x", "41"},
			{"xo1xo1o5x8o16x2o19x", "15"},
			{"xo1xo1o5x8o19x", "38"},
			{"xo1xo1o5x8oox14o2x12xx7o", "48"},
			{"xo1xo1o5x8oox14o2x1o10xx", "48"},
			{"xo1xo20o25x", "63"},
			{"xo1xo21o24x", "63"},
			{"xo1xo22o14o8x11x", "60"},
			{"xo1xo22o20x", "60"},
			{"xo1xo22o23x", "63"},
			{"xo1xo23o22x", "63"},
			{"xo1xo24o21x", "63"},
			{"xo1xo25o20x", "63"},
			{"xo1xo26o16x", "60"},
			{"xo1xo26o19x", "63"},
			{"xo1xo27o18x", "63"},
			{"xo1xo28o17x", "63"},
			{"xo1xo29o16x", "63"},
			{"xo1xo29o8o7x11x", "60"},
			{"xo1xo2o2o1x2x5ox15o16x", "26"},
			{"xo1xo2o40x", "60"},
			{"xo1xo2o43x", "63"},
			{"xo1xo2o4x2x5o16o16x", "63"},
			{"xo1xo2o4x2x5o16o2x13x4o", "37"},
			{"xo1xo2o4x2x6o14o14x", "60"},
			{"xo1xo2o4x8o11o3xo2x13x", "15"},
			{"xo1xo2o4x8o15xo2x3o9x", "15"},
			{"xo1xo2o4x8o16o2x13x", "37"},
			{"xo1xo2o7x5o13o2x24x", "12"},
			{"xo1xo2o7x5o19o9x9x", "37"},
			{"xo1xo31o14x", "63"},
			{"xo1xo32o13x", "63"},
			{"xo1xo33o12x", "63"},
			{"xo1xo33o4o7x11x", "60"},
			{"xo1xo34o11x", "63"},
			{"xo1xo34o2o8x11x", "60"},
			{"xo1xo34o8x", "60"},
			{"xo1xo35o10x", "63"},
			{"xo1xo36o1o7x11x", "60"},
			{"xo1xo36o9x", "63"},
			{"xo1xo37o5x", "60"},
			{"xo1xo37o8x", "63"},
			{"xo1xo37o8x7o3x", "60"},
			{"xo1xo37oo7x11x", "60"},
			{"xo1xo38o4x", "60"},
			{"xo1xo38o7x", "63"},
			{"xo1xo39o6x", "63"},
			{"xo1xo40o5x", "63"},
			{"xo1xo41o4x", "63"},
			{"xo1xo42o3x", "63"},
			{"xo1xo42ox", "60"},
			{"xo1xo43o2x", "63"},
			{"xo1xo43x10o", "60"},
			{"xo1xo43x11o", "15"},
			{"xo1xo43x12o", "60"},
			{"xo1xo43x13o", "60"},
			{"xo1xo43x14o", "60"},
			{"xo1xo43x1o", "60"},
			{"xo1xo43x3o", "15"},
			{"xo1xo43x4o", "60"},
			{"xo1xo43x5o", "60"},
			{"xo1xo43x7o", "15"},
			{"xo1xo43x8o", "60"},
			{"xo1xo43x9o", "60"},
			{"xo1xo43xo", "60"},
			{"xo1xo44o1x", "63"},
			{"xo1xo45ox", "63"},
			{"xo1xo46x10o", "63"},
			{"xo1xo46x11o", "60"},
			{"xo1xo46x1o", "63"},
			{"xo1xo46x2o", "63"},
			{"xo1xo46x3o", "63"},
			{"xo1xo46x4o", "63"},
			{"xo1xo46x5o", "63"},
			{"xo1xo46x6o", "63"},
			{"xo1xo46x7o", "63"},
			{"xo1xo46x8o", "63"},
			{"xo1xo46x9o", "63"},
			{"xo1xo46xo", "63"},
			{"xo1xo4o41x", "63"},
			{"xo1xo4x10ox3o9o2x1xoo8x", "15"},
			{"xo1xo4x2o8x3o9o2x1xoo8x", "15"},
			{"xo1xo4x2o8xx2o9o2x1xoo8x8o", "15"},
			{"xo1xo5o1x2x5x16o7o", "51"},
			{"xo1xo5o40x", "63"},
			{"xo1xo6o36x", "60"},
			{"xo1xo6ox2x5o38o2x", "51"},
			{"xo1xo6ox3o9x14o18x", "63"},
			{"xo1xo6ox8o19o6x12x", "60"},
			{"xo1xo7o38x", "63"},
			{"xo1xo7x10o", "60"},
			{"xo1xo7x10o14o21x", "63"},
			{"xo1xo7x10o14o7o13x2x", "51"},
			{"xo1xo7x10o14o9o2x11x", "60"},
			{"xo1xo7x10o17o18x", "63"},
			{"xo1xo7x10o17o4o13x2x", "51"},
			{"xo1xo7x10o5o8o12x11x", "60"},
			{"xo1xo7x13o21x1o", "38"},
			{"xo1xo7x14o", "60"},
			{"xo1xo7x14o13o18x", "63"},
			{"xo1xo7x21o16x8o", "63"},
			{"xo1xo7x22o", "60"},
			{"xo1xo7x22o10o4x", "63"},
			{"xo1xo7x22o12x5o", "63"},
			{"xo1xo7x22o5o18x", "63"},
			{"xo1xo7x22o5o6x", "21"},
			{"xo1xo7x24o10x1o", "60"},
			{"xo1xo7x25o12x10o", "63"},
			{"xo1xo7x25o12x8o", "63"},
			{"xo1xo7x25o3o5x", "21"},
			{"xo1xo7x25o3o8x", "63"},
			{"xo1xo7x25o4o16x", "63"},
			{"xo1xo7x25o4o2o13x2x", "51"},
			{"xo1xo7x25o7o4x", "63"},
			{"xo1xo7x25oo2o8x11x", "48"},
			{"xo1xo7x26o8x1o", "63"},
			{"xo1xo7x29o", "51"},
			{"xo1xo7x2x10o36o", "51"},
			{"xo1xo7x2x11o13o", "21"},
			{"xo1xo7x2x19o6o", "60"},
			{"xo1xo7x2x1o3o", "41"},
			{"xo1xo7x2x1o3o16o16x", "41"},
			{"xo1xo7x2x21o25o", "60"},
			{"xo1xo7x2x22o12o", "63"},
			{"xo1xo7x2x22o21o", "63"},
			{"xo1xo7x2x22o24o", "60"},
			{"xo1xo7x2x22o4o", "21"},
			{"xo1xo7x2x22o7o", "21"},
			{"xo1xo7x2x3o18o", "63"},
			{"xo1xo7x2x5o11o", "41"},
			{"xo1xo7x2x5o13o24o2x", "38"},
			{"xo1xo7x2x5o16o16x7o", "60"},
			{"xo1xo7x2x5o16o4o11x", "41"},
			{"xo1xo7x2x5o16o7o8x", "41"},
			{"xo1xo7x2x5o16o8o7x", "41"},
			{"xo1xo7x2x5o17o20o2x", "51"},
			{"xo1xo7x2x5o1o36o2x", "51"},
			{"xo1xo7x2x5o21o16o2x", "51"},
			{"xo1xo7x2x5o25o", "38"},
			{"xo1xo7x2x5o25o3o11x", "60"},
			{"xo1xo7x2x5o29o", "63"},
			{"xo1xo7x2x5o29o8o2x", "48"},
			{"xo1xo7x2x5o41o", "60"},
			{"xo1xo7x2x5o4o11x2x9o8o", "37"},
			{"xo1xo7x2x5o5o", "41"},
			{"xo1xo7x2x5o5o13o19x", "38"},
			{"xo1xo7x2x5o5o32o2x", "51"},
			{"xo1xo7x2x5o9o19o11x", "60"},
			{"xo1xo7x2x5oo37x2o", "38"},
			{"xo1xo7x2x6o28o", "63"},
			{"xo1xo7x2x6o8o", "51"},
			{"xo1xo7x30o", "60"},
			{"xo1xo7x38o", "60"},
			{"xo1xo7x3o4x4o15o5x", "37"},
			{"xo1xo7x42o", "38"},
			{"xo1xo7x46o", "60"},
			{"xo1xo7x4o33x8o", "63"},
			{"xo1xo7x4o3o6o19x2x", "63"},
			{"xo1xo7x4o3x3o25x7o", "22"},
			{"xo1xo7x4o3xoo27x", "48"},
			{"xo1xo7x4x2ooox1o8o1oxx13x", "19"},
			{"xo1xo7x4x3oo2o12x", "37"},
			{"xo1xo7x4x3oox1o12x2x1o7o", "28"},
			{"xo1xo7x4x3oox1o12x2x2o6o", "48"},
			{"xo1xo7x4x3oox1o12x2x9o11o", "42"},
			{"xo1xo7x4x3oox1o12x2xx1o6o11o", "48"},
			{"xo1xo7x4x3oox1o15x1o", "38"},
			{"xo1xo7x4x3oox1o15x9o", "38"},
			{"xo1xo7x4x3oox1o8o3x2x1o", "19"},
			{"xo1xo7x4x3oox1o8o3x2x21o", "19"},
			{"xo1xo7x4x3oox1o8o3x2x2o", "19"},
			{"xo1xo7x4x3oox1o8o6x", "38"},
			{"xo1xo7x50o", "38"},
			{"xo1xo7x6o", "60"},
			{"xo1xo7x6o18o12x8x2o", "48"},
			{"xo1xo7x6o18o3o8x11x", "60"},
			{"xo1xo7x6o1o16o2x13x", "15"},
			{"xo1xo7x6o1o16o9x6x", "60"},
			{"xo1xo7x6o1o16x2o19x", "15"},
			{"xo1xo7x6o1o26x", "60"},
			{"xo1xo7x6o21o18x", "63"},
			{"xo1xo7x6o21o6x", "63"},
			{"xo1xo7x6o28x8o", "63"},
			{"xo1xo7x6o2o25x", "63"},
			{"xo1xo7x6o2o37x", "63"},
			{"xo1xo7x6o31x2o", "60"},
			{"xo1xo7x6o5o22x2x5o", "41"},
			{"xo1xo7x6o5o25x8x2o", "15"},
			{"xo1xo7x6o9o21x", "63"},
			{"xo1xo7x8o", "38"},
			{"xo1xo7x8o13o5o6x12x", "38"},
			{"xo1xo7x8o16o16x", "41"},
			{"xo1xo7x8o16o3o5x6x", "41"},
			{"xo1xo7x8o16o9xo5x", "41"},
			{"xo1xo7x8o16x12o", "41"},
			{"xo1xo7x8o16x2o12o6x", "15"},
			{"xo1xo7x8o16x2o15o3x", "15"},
			{"xo1xo7x8o16x2o18ox", "15"},
			{"xo1xo7x8o16x2o9o9x", "15"},
			{"xo1xo7x8o16x2x9o8o", "15"},
			{"xo1xo7x8oo15o2x13x", "23"},
			{"xo1xo7x8oo15x", "36"},
			{"xo1xo7x8oo15x2o19x", "36"},
			{"xo1xo7x8oo15x2x9o", "43"},
			{"xo1xo7x8oo18o6x12x", "38"},
			{"xo1xo7x8oo18x", "23"},
			{"xo1xo7x8oo25x", "38"},
			{"xo1xo7x8oox14o2x13x7o", "48"},
			{"xo1xo7x8oox14o2x1o11x", "48"},
			{"xo1xo7x8oox1o15x", "17"},
			{"xo1xo7x8x10o1o3o3x5x2x3o7o", "15"},
			{"xo1xo7x8x10o5o3o5x", "37"},
			{"xo1xo7x8x12o3o3x8x3o7o", "41"},
			{"xo1xo7x9o12o12x", "63"},
			{"xo1xo7x9o12o15x", "63"},
			{"xo1xo7x9o12o24x", "63"},
			{"xo1xo7x9o19o17x", "63"},
			{"xo1xo7x9o19o2o14x2x", "51"},
			{"xo1xo7x9o19o3o13x2x", "51"},
			{"xo1xo7x9o19o8x", "63"},
			{"xo1xo7x9o20o16x", "63"},
			{"xo1xo7x9o21o6x", "63"},
			{"xo1xo7x9o23o4x", "63"},
			{"xo1xo7x9o28o8x", "63"},
			{"xo1xo7x9o36ox", "63"},
			{"xo1xo7x9o4o32x", "63"},
			{"xo1xo7x9o6o12o17x2x", "51"},
			{"xo1xo7x9o6o21x", "63"},
			{"xo1xo7x9o7o11o17x2x", "51"},
			{"xo1xo7x9o7o20x", "63"},
			{"xo1xo7x9o8o19x", "48"},
			{"xo1xo7x9oo36x", "63"},
			{"xo1xo8o1x3o5x11o10x", "12"},
			{"xo1xo8o37x", "63"},
			{"xo1xo9o36x", "63"},
			{"xo1xoo45x", "63"},
			{"xo1xoo6x2x5o16x2x9o8o", "37"},
			{"xo1xoo6x2x5x10o4o4x4ox14o", "25"},
			{"xo1xoo6x2x5x16o7o", "51"},
			{"xo1xoo6x8o16x2o19x", "37"},
			{"xo1xoo9o5x15xx24o", "42"},
			{"xo1xooxo40x", "60"},
			{"xo1xox6x19o9o", "51"},
			{"xo1xox6x2oo4o4x6o7o6x12x", "38"},
			{"xo1xoxoo2o1x2x5ox15o16x", "26"},
			{"xo1xoxoo43x", "63"},
			{"xo1xoxoo4x7xo16o1ox13x", "51"},
			{"xo1xoxoo4x7xo16o2x13x4o", "48"},
			{"xo1xoxoo4x7xoo15o1ox12xx", "51"},
			{"xo1xoxoo4x7xoo15o2x12xx4o", "48"},
			{"xo1xoxoo4x8oox14o2x12xx7o", "48"},
			{"xo1xoxoo4x8oox14o2x1o10xx", "48"},
			{"xo2o12o30x2x", "63"},
			{"xo2o13o29x2x", "63"},
			{"xo2o14o1x3x12x2xo8o", "22"},
			{"xo2o14o1xx2x12x2xo8o8o", "26"},
			{"xo2o14o28x2x", "63"},
			{"xo2o28o14x2x", "63"},
			{"xo2o29o13x2x", "63"},
			{"xo2o30o12x2x", "63"},
			{"xo2o43x", "60"},
			{"xo3o14o27x11x", "63"},
			{"xo3o18o23x11x", "63"},
			{"xo3o19x8o2xx2x1o7o", "26"},
			{"xo3o30o11x11x", "63"},
			{"xo3o34o7x11x", "63"},
			{"xo3o3o2ox34x11x", "51"},
			{"xo3o3o3x10o23x11x", "63"},
			{"xo3o3o3x22o11x11x", "63"},
			{"xo3o3o3x26o7x11x", "63"},
			{"xo3o3o3x6o27x11x", "63"},
			{"xo3o3x3o10o23x11x", "63"},
			{"xo3o3x3o22o11x11x", "63"},
			{"xo3o3x3o26o7x11x", "63"},
			{"xo3o3x3o6o27x11x", "63"},
			{"xo3o42x", "60"},
			{"xo3o4o4o9xx10x3x1o", "21"},
			{"xo3o6o35x11x", "63"},
			{"xo3o6x", "60"},
			{"xo3o6x11o35x", "63"},
			{"xo3o6x23o23x", "63"},
			{"xo3o6x27o19x", "63"},
			{"xo3o6x2x10o1o12x6x7o", "63"},
			{"xo3o6x2x10o21x11o", "51"},
			{"xo3o6x2x12o", "63"},
			{"xo3o6x2x12o13o20x", "3"},
			{"xo3o6x2x16o", "63"},
			{"xo3o6x2x20o", "63"},
			{"xo3o6x2x21o10x11o", "21"},
			{"xo3o6x2x22o", "63"},
			{"xo3o6x2x24o", "63"},
			{"xo3o6x2x28o", "48"},
			{"xo3o6x2x32o", "63"},
			{"xo3o6x2x44o", "48"},
			{"xo3o6x2x4o", "63"},
			{"xo3o6x2x5o6o12x19o1x", "3"},
			{"xo3o6x2x5o6o34x", "37"},
			{"xo3o6x35o11x", "63"},
			{"xo3o6x7o39x", "63"},
			{"xo3o9x32x3o", "12"},
			{"xo3ox5x8o14ooox8x4x", "61"},
			{"xo3x3o2xo10o35x", "63"},
			{"xo3x3o2xo22o23x", "63"},
			{"xo3x3o2xo26o19x", "63"},
			{"xo3x3o2xo6o39x", "63"},
			{"xo4o13o27x11x", "63"},
			{"xo4o17o23x11x", "63"},
			{"xo4o29o11x11x", "63"},
			{"xo4o33o7x11x", "63"},
			{"xo4o41x", "60"},
			{"xo4o5o35x11x", "63"},
			{"xo4o5x", "60"},
			{"xo4o5x11o35x", "63"},
			{"xo4o5x23o23x", "63"},
			{"xo4o5x27o19x", "63"},
			{"xo4o5x2x10o1o12x6x7o", "63"},
			{"xo4o5x2x10o21x11o", "51"},
			{"xo4o5x2x12o", "63"},
			{"xo4o5x2x16o", "63"},
			{"xo4o5x2x20o", "63"},
			{"xo4o5x2x21o10x11o", "51"},
			{"xo4o5x2x22o", "63"},
			{"xo4o5x2x24o", "63"},
			{"xo4o5x2x28o", "48"},
			{"xo4o5x2x32o", "63"},
			{"xo4o5x2x44o", "48"},
			{"xo4o5x2x4o", "63"},
			{"xo4o5x2x5o6o12x19o1x", "3"},
			{"xo4o5x35o11x", "63"},
			{"xo4o5x7o39x", "63"},
			{"xo5o12o27x11x", "63"},
			{"xo5o16o23x11x", "63"},
			{"xo5o28o11x11x", "63"},
			{"xo5o32o7x11x", "63"},
			{"xo5o40x", "60"},
			{"xo5o4o35x11x", "63"},
			{"xo5o4x", "60"},
			{"xo5o4x11o35x", "63"},
			{"xo5o4x23o23x", "63"},
			{"xo5o4x27o19x", "63"},
			{"xo5o4x2x10o21x11o", "51"},
			{"xo5o4x2x12o", "48"},
			{"xo5o4x2x13o", "48"},
			{"xo5o4x2x14o", "48"},
			{"xo5o4x2x15o", "48"},
			{"xo5o4x2x32o", "63"},
			{"xo5o4x2x44o", "48"},
			{"xo5o4x2x4o", "63"},
			{"xo5o4x2x5o19x2o16o1x", "51"},
			{"xo5o4x2x5o19x2o17ox", "51"},
			{"xo5o4x2x5o19x4o14o1x", "51"},
			{"xo5o4x2x5o22o18x", "41"},
			{"xo5o4x2x5o38o2x", "51"},
			{"xo5o4x2x5o6o12x19o1x", "51"},
			{"xo5o4x2x5o7o11x2o18x", "51"},
			{"xo5o4x2x9o", "63"},
			{"xo5o4x35o11x", "63"},
			{"xo5o4x7o39x", "63"},
			{"xo5o7o5x15x3x21o", "43"},
			{"xo5o7o5x15x3xo", "26"},
			{"xo5o7o5x4x10x3xo17o", "25"},
			{"xo5o7o5x4x10x3xo20o", "22"},
			{"xo5o7o5x4x10x3xo4o", "22"},
			{"xo5o7o5x4x10x3xo5o", "22"},
			{"xo5o7o5x4x3o6x3xo", "60"},
			{"xo5o7o5x4x4o5x3xo", "38"},
			{"xo5o7o5x4x5o4x3xo", "22"},
			{"xo6o11o27x11x", "63"},
			{"xo6o15o23x11x", "63"},
			{"xo6o19o19x11x", "63"},
			{"xo6o39x", "51"},
			{"xo7o10o27x11x", "63"},
			{"xo7o14o23x11x", "63"},
			{"xo7o26o11x11x", "63"},
			{"xo7o2o35x11x", "63"},
			{"xo7o2x", "60"},
			{"xo7o2x11o35x", "63"},
			{"xo7o2x23o23x", "63"},
			{"xo7o2x27o19x", "63"},
			{"xo7o2x2x10o21x11o", "51"},
			{"xo7o2x2x12o", "63"},
			{"xo7o2x2x20o", "63"},
			{"xo7o2x2x21o10x11o", "51"},
			{"xo7o2x2x22o", "63"},
			{"xo7o2x2x24o", "63"},
			{"xo7o2x2x28o", "48"},
			{"xo7o2x2x32o", "63"},
			{"xo7o2x2x44o", "48"},
			{"xo7o2x2x4o", "63"},
			{"xo7o2x35o11x", "63"},
			{"xo7o2x7o39x", "63"},
			{"xo7o30o7x11x", "63"},
			{"xo7o38x", "60"},
			{"xo8o13o23x11x", "63"},
			{"xo8o1o35x11x", "63"},
			{"xo8o1x", "60"},
			{"xo8o1x11o35x", "63"},
			{"xo8o1x23o23x", "63"},
			{"xo8o1x27o19x", "63"},
			{"xo8o1x2x12o", "63"},
			{"xo8o1x2x20o", "63"},
			{"xo8o1x2x22o", "63"},
			{"xo8o1x2x22o12x8o", "63"},
			{"xo8o1x2x24o", "63"},
			{"xo8o1x2x28o", "48"},
			{"xo8o1x2x32o", "63"},
			{"xo8o1x2x44o", "51"},
			{"xo8o1x2x4o", "63"},
			{"xo8o1x2x9o25x8o", "63"},
			{"xo8o1x35o11x", "63"},
			{"xo8o1x7o39x", "63"},
			{"xo8o25o11x11x", "63"},
			{"xo8o29o7x11x", "63"},
			{"xo8o37x", "60"},
			{"xo8o4x32x3o", "51"},
			{"xo8o9o27x11x", "63"},
			{"xo9o12o23x11x", "63"},
			{"xo9o24o11x11x", "63"},
			{"xo9o28o7x11x", "63"},
			{"xo9o36x", "60"},
			{"xo9o8o27x11x", "63"},
			{"xo9oo35x11x", "63"},
			{"xo9ox", "60"},
			{"xo9ox11o35x", "63"},
			{"xo9ox23o23x", "63"},
			{"xo9ox27o19x", "63"},
			{"xo9ox2x12o", "63"},
			{"xo9ox2x20o", "63"},
			{"xo9ox2x22o", "63"},
			{"xo9ox2x22o12x8o", "63"},
			{"xo9ox2x24o", "63"},
			{"xo9ox2x28o", "48"},
			{"xo9ox2x32o", "63"},
			{"xo9ox2x44o", "51"},
			{"xo9ox2x4o", "63"},
			{"xo9ox2x5o22o18x", "48"},
			{"xo9ox2x9o25x8o", "63"},
			{"xo9ox35o11x", "63"},
			{"xo9ox7o39x", "63"},
			{"xoo1o43x2x", "63"},
			{"xoo1o7x47x", "63"},
			{"xoo9x", "60"},
			{"xoo9x11o35x", "63"},
			{"xoo9x15o31x", "51"},
			{"xoo9x2x12o", "63"},
			{"xoo9x2x32o", "63"},
			{"xoo9x2x44o", "63"},
			{"xoo9x2x4o", "63"},
			{"xoo9x2x5o22o18x", "51"},
			{"xoo9x2x5o38o2x", "3"},
			{"xoo9x2x8o", "63"},
			{"xoo9x2x9o", "63"},
			{"xoo9x35o11x", "63"},
			{"xoo9x7o39x", "63"},
			{"xooo18x2xx1o", "41"},
			{"xooo18x2xx1o12x14o", "15"},
			{"xooo18x2xx1o12x17o", "8"},
			{"xooo18x2xx4o", "41"},
			{"xooo18x2xx4o9x17o", "8"},
			{"xooo3o13xx3x4o6x2xo12o7x", "48"},
			{"xooo3o14x12o1xx2x", "51"},
			{"xooo3o14x14xx", "41"},
			{"xooo3o14x14xx2x2o", "15"},
			{"xooo3o14x14xx2x5o", "15"},
			{"xooo3x3o3o3o2x2xx14x", "51"},
			{"xooo3x3o3o5ox2xx4o9x21x", "60"},
			{"xooo3x3o3o6x2xx1o12x", "51"},
			{"xooo3x3o3o6x2xx4o9x", "63"},
			{"xooo3x3o3o6x2xx4o9xo20x", "12"},
			{"xoooo14x2x12o1xx2x9o", "15"},
			{"xoooo17x12o1xx2x", "51"},
			{"xoooo17x14xx", "41"},
			{"xoooo17x14xx2x2o", "51"},
			{"xoooo17x14xx2x5o", "32"},
			{"xoooo17x9o4xx2x", "51"},
			{"xoox1xo5x8o19o19x", "60"},
			{"xoox8x10o", "60"},
			{"xoox8x14o", "60"},
			{"xoox8x38o", "60"},
			{"xoox8x48o", "41"},
			{"xoox8x50o", "60"},
			{"xoox8x8o", "41"},
			{"xoox8x8o19o19x", "60"},
			{"xooxo46x", "63"},
			{"xx1o1o2o3x2x5ox2o2o8o15x6x", "42"},
			{"xx1o1o2o3x2x5ox2o2o8o4o10x5xx", "63"},
			{"xx1o1o6x2x4o1x2o3o11o18x", "21"},
			{"xx1o1o7x11o11o10xo1x", "21"},
			{"xx1o1o7x7o3o11o10xo1x1x", "12"},
			{"xx1o1o7x7o3o6o4o4x5xo1x1x", "60"},
			{"xx1o1o7x7x3o11o4o5xo1x", "12"},
			{"xx1o4o3x8o19o18xo1x", "51"},
			{"xx1o4o3x8ox2o2ox11ox11o2x2x2o", "15"},
			{"xx1o4o3xo1x5o3o11o4x6x", "60"},
			{"xx1o4o3xo1x9o11o11x", "42"},
			{"xx1o4o3xo1x9o11o4x5ox", "60"},
			{"xx1o4o3xo1x9o11o4x6x13o", "60"},
			{"xx1o4ox2x7o1x2o3o11o18x", "42"},
			{"xx1o4ox2x7o1x2o3o11oo16xx", "63"},
			{"xx1o5o2x2x5o3o", "60"},
			{"xx1o5o2x2x5o3o15o15x", "60"},
			{"xx1o5o2x2x5o3o15x15o", "60"},
			{"xx1o5o2xo1x9o11o3o7x7x", "60"},
			{"xx1o6o1x2x4o1x2o3o11o18x", "42"},
			{"xx1o7x9o4o14o6x7o3xo1x", "51"},
			{"xx1o8x7o1x2o3o11o18x", "15"},
			{"xx1o8x7oo19o18xo1x", "15"},
			{"xx1o8x7oo19o6o2x8xo1x", "15"},
			{"xx1o8x7ooox17o18xo1x", "15"},
			{"xx1o8x8o14o4o18xo1x", "15"},
			{"xx1o8x8o18oo18xo1x", "15"},
			{"xx1o8x8o18ooox16xo1x", "15"},
			{"xx1o8x8o19o6o11xo1x", "15"},
			{"xx1o8x8o2o16o18xo1x", "15"},
			{"xx1o8x8o2o16o6o2x8xo1x", "15"},
			{"xx1o8x8o6o12o18xo1x", "51"},
			{"xx1o8x8ox2o2ox10oox17x2o", "15"},
			{"xx1o8xo1x9o11o3o7x", "60"},
			{"xx1o8xo1x9o11o7o3x", "48"},
			{"xx1o8xo1x9o3x7o7o3x11o", "48"},
			{"xx1oo1x1oo2o8o3x15xxo19x", "37"},
			{"xx1oo1x1oooxo8o3x15xxo19x", "38"},
			{"xx1oo7x8o19o18xo1x", "15"},
			{"xx1oo7x8ox2o10o1x2o6x", "51"},
			{"xx2o7o2o5x15xx2o21o", "13"},
			{"xx2o7x1o6o19o18xo1x", "48"},
			{"xx2o7x7o1x2o3o11o18x", "48"},
			{"xx2o7x8o7o11o18xo1x", "13"},
			{"xx2o7xo1x1o7o11o11x", "21"},
			{"xx2o8x11o7o3o3o3x2xo1x", "60"},
			{"xx3o2o3x2x5o3o11o6o8x6x", "42"},
			{"xx3o2o3x2x5o3o2o8o15x6x", "22"},
			{"xx3o2o3x2x9o11o", "60"},
			{"xx3o2o3xo1x1o3o3o3x7o4x6x3x9o", "60"},
			{"xx3o2o3xo1x5o3o11o3ox6x3x", "51"},
			{"xx3o2o3xo1x5o3o11o4x2o3x3x", "29"},
			{"xx3o2o3xo1x5o3o11o4x6x1o1x", "60"},
			{"xx3o2o3xo1x5o3o4o2o3o4x2x3x3x", "51"},
			{"xx3o2o3xo1x5o3o7o3o4x2x3x3x6o", "63"},
			{"xx3o2o3xo1x5o3o7o3o4x6x3x", "45"},
			{"xx3o3o1oxo1x9o11x3o15x", "42"},
			{"xx3o3o2x2x5o3o", "60"},
			{"xx3o3o2x2x5o3o15o15x", "60"},
			{"xx3o3o2xo18o15x", "60"},
			{"xx3o3o2xo1x9o11x10o", "42"},
			{"xx3o3o2xo1x9o11x3o6o8x", "42"},
			{"xx3o3o2xo1x9oo10x3o15x", "42"},
			{"xx3o6x8o19o2o15xo1x", "48"},
			{"xx3o6x8o6o12o18xo1x", "48"},
			{"xx3o6xo1x1o3o3o3x7o11x3x9o", "60"},
			{"xx3o6xo1x1o3o3o3x7o8o2x3x", "60"},
			{"xx3o6xo1x1o7o11o11x", "21"},
			{"xx3o6xo1x5o3o11o11x1o1x", "48"},
			{"xx3o6xo1x5o3o11o3o7x3x", "60"},
			{"xx3o6xo1x5o3o11o7o3x3x", "29"},
			{"xx3o6xo1x5o3o4o2o3o7x3x3x", "63"},
			{"xx3o6xo1x5o3o4o3o2o7x3x3x", "63"},
			{"xx3o6xo1x5o3o7o3o11x3x", "45"},
			{"xx3o6xo1x5o3o7o3o7x3x3x6o", "63"},
			{"xx3o6xo1x5o3o8o2o11x3x", "45"},
			{"xx3o6xo1x5o3o8o2o7x3x3x6o", "63"},
			{"xx3o6xo1x5o3o9o1o11x3x", "48"},
			{"xx3o6xo1x9o11o11xo", "21"},
			{"xx3o6xo1x9o4o2o3o7x3x", "63"},
			{"xx3o6xo1x9o7o3o11x", "45"},
			{"xx3o6xo1x9o7o3o7x3x10o", "63"},
			{"xx3o7x11o6o4o10xo1x", "42"},
			{"xx3o7x11o7o3o10xo1x", "57"},
			{"xx3o7x11o7o3o10xo1x2o2x", "63"},
			{"xx3o7x11o7o3o10xo1x5x2o", "63"},
			{"xx3o7x11o7o3o3o3x2xo1x", "57"},
			{"xx3o7x11o7o3o3o3x2xo1x2o2x", "63"},
			{"xx3o7x11o7o3o3o3x2xo1x5x2o", "63"},
			{"xx3o7x11o8o2o10xo1x", "63"},
			{"xx3o7x11o9o1o10xo1x", "21"},
			{"xx3o7x4o6o11o10xo1x", "21"},
			{"xx3o7x5o5o11o10xo1x", "21"},
			{"xx3o7x7o3o6o4o4x5xo1x1x4o", "60"},
			{"xx3o7x7o3o6o4o4x5xo1x1x9o", "60"},
			{"xx3ox2o2xo1x10o5o5o9x", "51"},
			{"xx3x4o10o11o3o10xo1x", "60"},
			{"xx3x4o10o11o3o3o3x2xo1x", "60"},
			{"xx3x5o9o11o3o10xo1x", "60"},
			{"xx3x8o6o11o3o10xo1x", "60"},
			{"xx3x9o5o11o3o10xo1x", "60"},
			{"xx3x9o5o11o3o3o3x2xo1x", "60"},
			{"xx4o2o2x2x5o3o", "60"},
			{"xx4o2o2x2x5o3o15o15x", "60"},
			{"xx4o5x8o19o2o15xo1x", "51"},
			{"xx4o5x8o6o12o18xo1x", "48"},
			{"xx4o6x11o7o3o3o3x2xo1x", "57"},
			{"xx5oo3xo1x9o11o11x", "22"},
			{"xx6o2oxo1x9o11o11x", "42"},
			{"xx6o3x1o6o19o18xo1x", "51"},
			{"xx6o3x1ox5o3o11o4x", "60"},
			{"xx6o3x1ox6o2o11o4o16xx", "63"},
			{"xx6o3x1ox9o11o", "42"},
			{"xx6o3x1ox9o11o4o16x", "60"},
			{"xx6o3x7o1x2o3o11o18x", "9"},
			{"xx6o3x8o19o18xo", "15"},
			{"xx6o3x8o19o2o15xo1x", "51"},
			{"xx6o3x8o19o3o14xo1x", "13"},
			{"xx6o3x8o19o4o13xo1x", "51"},
			{"xx6o3x8o19o5o12xo1x", "51"},
			{"xx6o3x8o6o12o18xo1x", "51"},
			{"xx6o3x8o7o11o18xo1x", "51"},
			{"xx6o3x8o8o10o18xo1x", "51"},
			{"xx6o3x8o9o9o18xo1x", "48"},
			{"xx6o3xo1x5o3o11o3ox6x", "45"},
			{"xx6o3xo1x5o3o11o4x2o3x", "29"},
			{"xx6o3xo1x5o3o11o4x6x1o", "60"},
			{"xx6o3xo1x5o3o7o3o4x6x", "41"},
			{"xx6o3xo1x5o3o8o2o4x6x", "60"},
			{"xx6o3xo1x8oo11o11x", "42"},
			{"xx6o3xo1x9o11o11x1o", "42"},
			{"xx6o3xo1x9o11o11xo", "42"},
			{"xx6o3xo1x9o11o3o7x", "42"},
			{"xx6o3xo1x9o11o7o3x", "42"},
			{"xx6o3xo1x9o1o9o11x", "42"},
			{"xx6o3xo1x9o7o3o11x", "42"},
			{"xx6o3xo1x9o8o2o11x", "42"},
			{"xx6o3xo1x9o9o1o11x", "42"},
			{"xx7o2x1ox4oo3o34x", "22"},
			{"xx7o2x1ox5o3o", "60"},
			{"xx7o2x1ox5o3o15o15x", "60"},
			{"xx7o2x2x5o3o11o", "17"},
			{"xx7o2x2x5o3o11o3o15x", "17"},
			{"xx7o2x7o1x2o3o11o9o5x2x", "15"},
			{"xx7o2x8o19o2o15xo1x", "51"},
			{"xx7o2x8o6o12o18xo1x", "51"},
			{"xx7o2xo1x5o3o", "17"},
			{"xx7o2xo1x5o3o11o3o7x7x", "60"},
			{"xx7o2xo1x5o3o15o15x", "17"},
			{"xx7o2xo1x9o11o11xo", "41"},
			{"xx7o2xo1x9o11o3o6ox7x", "60"},
			{"xx7o2xo1x9o11o3o7xo6x", "60"},
			{"xx7o2xo1x9o3o7o3o7x7x", "45"},
			{"xx7o2xo1x9o4o2o3o3o3x3x7x", "60"},
			{"xx7o2xo1x9o7o3o11x", "41"},
			{"xx7o2xo1x9o7o3o3o3x3x7x2o", "63"},
			{"xx7oo1x2x5o3o", "17"},
			{"xx7oo1x2x5o3o15o15x", "17"},
			{"xx8o1x8o19o2o15xo1x", "51"},
			{"xx8o1x8o6o12o18xo1x", "51"},
			{"xx9ox8o6o12o18xo1x", "48"},
			{"xxo1o7x8o19o18xo1x", "15"},
			{"xxo1o7x8ox2o10o1x2o6x", "17"},
			{"xxo2o6x2x4o1x2o3o11o18x", "21"},
			{"xxo2o6xo1x9o11o11x", "51"},
			{"xxo2o7x11o11o10xo1x", "21"},
			{"xxo2o7x7o3o11o10xo1x1x", "12"},
			{"xxo2o7x7o3o6o4o4x5xo1x1x", "60"},
			{"xxo2x15o11o3o10xo1x", "60"},
			{"xxo4oo3xo1x6x2o11o11x", "60"},
			{"xxo5o3x8o19o18xo1x", "51"},
			{"xxo5o3x8ox2o2ox11ox11o2x2x2o", "15"},
			{"xxo5o3xo1x5o3o11o4x6x", "60"},
			{"xxo5o3xo1x9o11o11x", "42"},
			{"xxo6o2x2x5o3o", "60"},
			{"xxo6o2x2x5o3o15o15x", "60"},
			{"xxo6o2x2x5o3o15x15o", "60"},
			{"xxo6o2xo1x9o11o11x", "48"},
			{"xxo6o2xo1x9o11o3o7x7x", "60"},
			{"xxo8x9o4o14o6x7o3xo1x", "51"},
			{"xxo9x7o1x2o3o11o18x", "15"},
			{"xxo9x7oo19o18xo1x", "15"},
			{"xxo9x8o14o4o18xo1x", "15"},
			{"xxo9x8o18oo18xo1x", "15"},
			{"xxo9x8o19o2o15xo1x", "15"},
			{"xxo9x8o19o6o11xo1x", "15"},
			{"xxo9x8o2o16o18xo1x", "15"},
			{"xxo9x8o6o12o18xo1x", "51"},
			{"xxo9x8ox2o2ox10oox17x2o", "15"},
			{"xxo9xo1x5o3o11o11x", "48"},
			{"xxo9xo1x9o11o11x3o", "48"},
			{"xxo9xo1x9o11o3o7x", "48"},
			{"xxo9xo1x9o11o7o3x", "48"},
			{"xxo9xo1x9o3o7o11x", "51"},
			{"xxo9xo1x9o3x7o7o3x11o", "48"},
			{"xxox1o6x6o1o19o9x", "60"},
			{"xxox1o6x8o10o8o6x", "37"},
			{"xxox1o6x8o19o", "48"},
			{"xxox1o9x3o1o15o3o9x1x", "63"},
			{"xxox1o9x3o1o19o9x", "37"},
			{"xxox1o9x5o10o8o6x", "63"},
			{"xxox1o9x5o19o", "51"},
			{"xxox1o9x5o4o10x3o1o16xo1x", "48"},
			{"xxox1xo3o1x1xo5o6o12o18xo1x", "51"},
			{"xxox2o5x6o1o19o9x", "60"},
			{"xxox2o5x8o10o8o6x", "37"},
			{"xxox2o5x8o19o", "48"},
			{"xxox2o8x3o1o19o9x", "48"},
			{"xxox2o8x5o19o", "51"},
			{"xxox3o6ox5o19o18xo1x", "12"},
			{"xxox3o7x5o19o", "48"},
			{"xxox3o7x5o19o19o1x", "12"},
			{"xxox3o7x5o19o2o15xo1x", "51"},
			{"xxox3o7x5o19o3o14xo1x", "48"},
			{"xxox3o7x5o19o4o13xo1x", "48"},
			{"xxox3o7x5o19o5o12xo1x", "48"},
			{"xxox3o7x5o19o6o2x", "12"},
			{"xxox3o7x5o19o6xo", "60"},
			{"xxox3o7x5o6o12o18xo1x", "48"},
			{"xxox3o7x5o7o11o18xo1x", "48"},
			{"xxox3o7x5o8o10o18xo1x", "48"},
			{"xxox3o7x5o9o9o18xo1x", "48"},
			{"xxox3o7x5oxo17o19o1x", "51"},
			{"xxox8x2o5o19o", "48"}};
}