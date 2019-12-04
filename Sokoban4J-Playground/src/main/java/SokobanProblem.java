import java.util.*;

import cz.sokoban4j.simulation.actions.EDirection;
import cz.sokoban4j.simulation.actions.compact.CAction;
import cz.sokoban4j.simulation.actions.compact.CMove;
import cz.sokoban4j.simulation.actions.compact.CPush;
import cz.sokoban4j.simulation.board.compact.BoardCompact;
import cz.sokoban4j.simulation.board.compact.CTile;
import cz.sokoban4j.simulation.board.compressed.MTile;



public class SokobanProblem implements HeuristicProblem<BoardCompact, EDirection>{

	private boolean[][] final_deadSquares = null;
	private boolean[][] dead_lock = null;
	private BoardCompact mainBoard;;
	public SokobanProblem(BoardCompact board){
		mainBoard = board;
		//mainBoard.makeBoardCompressed();
		final_deadSquares = new boolean[mainBoard.width()][mainBoard.height()];
		final_deadSquares = DeadSquareDetector.detect(mainBoard);
		dead_lock = new boolean[mainBoard.width()][mainBoard.height()];
		for(int i = 0; i < mainBoard.width(); i++){
			for(int j = 0; j < mainBoard.height(); j++) {
				if(final_deadSquares[i][j]==true) {
					dead_lock[i][j] = false;
				}
				else if(is_deadlock(i,j,mainBoard)==true){
					dead_lock[i][j] = true;
				}
				else {

					dead_lock[i][j] = false;
				}

			}
		}
		//printdead_lock();
		//print_hashset();
	}

	public void print_hashset(){
		HashSet<EDirection> yes = new HashSet<EDirection>();
		yes = walkstowards(mainBoard);
		for(EDirection my:yes){
			System.out.println(my);
		}

	}

	public HashSet<EDirection> walkstowards (BoardCompact state){
		HashSet<EDirection> resultdirections = new HashSet<>();
		EDirection[] directions = EDirection.arrows();
		LinkedList<NodeBFS<EDirection>> queue = new LinkedList<>();
		HashSet<Pos> visited = new HashSet<>();
		NodeBFS<EDirection> initial = new NodeBFS<>(state.playerX,state.playerY,null,null);
		queue.add(initial);
		while (!queue.isEmpty()) {
			NodeBFS<EDirection> currentNode = queue.poll();
			Pos currentNode_xy = new Pos(currentNode.x,currentNode.y);
			if(visited.contains(currentNode_xy)) continue;
			visited.add(currentNode_xy);

			for (EDirection direction : directions) {
				NodeBFS<EDirection> forwardNode = new NodeBFS<>(
						currentNode.x + direction.dX,
						currentNode.y + direction.dY,
						currentNode,
						direction
				);
				Pos forwardNode_xy = new Pos(forwardNode.x,forwardNode.y);
				if(CTile.isWall(state.tile(forwardNode.x, forwardNode.y)) || visited.contains(forwardNode_xy)) continue;

				if(!CTile.isSomeBox(state.tile(forwardNode.x, forwardNode.y))){
					queue.add(forwardNode);
				}
				if (CTile.isSomeBox(state.tile(forwardNode.x, forwardNode.y))
						&& !CTile.isSomeBox(state.tile(forwardNode.x+direction.dX,forwardNode.y+direction.dY))
						&& final_deadSquares[forwardNode.x+direction.dX][forwardNode.y+direction.dY]==false
						&& results_deadlock(forwardNode.x+direction.dX,forwardNode.y+direction.dY,state,direction)==false
				)
				{
					//NodeBFS<EDirection> copycurrent = new NodeBFS<>(forwardNode);
					if (forwardNode.parent != null) {
						while (forwardNode.parent != null) {
							if (forwardNode.parent.parent == null) {
								resultdirections.add(forwardNode.action);
							}
							forwardNode = forwardNode.parent;
						}
					}
				}
			}
		}
		return resultdirections;
	}

	//!CTile.forSomeBox(board.tile(forwardNode.x, forwardNode.y)
	public boolean results_deadlock(int x, int y, BoardCompact board, EDirection dir) {
		EDirection[] directions = EDirection.arrows();
		for (EDirection direction : directions) {
			if(direction==dir.opposite()) {
				continue;
			}
			Pos currentNode = new Pos(x, y);
			Pos currentNode_CW = new Pos(currentNode.x + direction.cw().dX,currentNode.y + direction.cw().dY);
			Pos currentNode_CCW = new Pos(currentNode.x + direction.ccw().dX,currentNode.y + direction.ccw().dY);
			Pos forwardNode = new Pos(x + direction.dX, y + direction.dY);
			Pos forwardNode_CW = new Pos(forwardNode.x + direction.cw().dX,forwardNode.y + direction.cw().dY);
			Pos forwardNode_CCW = new Pos(forwardNode.x + direction.ccw().dX,forwardNode.y + direction.ccw().dY);
			if(dead_lock[currentNode.x][currentNode.y]==true && dead_lock[forwardNode.x][forwardNode.y]==true && CTile.isSomeBox(board.tile(forwardNode.x, forwardNode.y))      ) {
				if((CTile.isWall(board.tile(currentNode_CW.x, currentNode_CW.y))&&CTile.isWall(board.tile(forwardNode_CW.x, forwardNode_CW.y))&& !CTile.forSomeBox(board.tile(forwardNode.x, forwardNode.y)))
						||  (!CTile.forSomeBox(board.tile(forwardNode.x, forwardNode.y)) && CTile.isWall(board.tile(currentNode_CCW.x, currentNode_CCW.y))&&CTile.isWall(board.tile(forwardNode_CCW.x, forwardNode_CCW.y)))) {
					return true;
				}
			}

		}
		return false;
	}

	public void printdead_lock(){
		if(dead_lock != null){
			System.out.println("dead squares: ");
			for (int y = 0 ; y < mainBoard.height() ; ++y) {
				for (int x = 0 ; x < mainBoard.width() ; ++x)
					System.out.print(CTile.isWall(mainBoard.tile(x, y)) ? '#' : (dead_lock[x][y] ? 'X' : '_'));
				System.out.println();
			}
		}
	}

	public boolean is_deadlock(int x, int y, BoardCompact board) {
		EDirection[] directions = EDirection.arrows();
		for (EDirection direction : directions) {
			Pos forwardNode = new Pos(x + direction.dX, y + direction.dY);
			Pos forwardCW = new Pos(x + direction.cw().dX, y + direction.cw().dY);
			Pos forwardCCW = new Pos(x + direction.ccw().dX, y + direction.ccw().dY);
			Pos wallCW = new Pos(forwardCW.x + direction.dX, forwardCW.y + direction.dY);
			Pos wallCCW = new Pos(forwardCCW.x + direction.dX, forwardCCW.y + direction.dY);
			if(CTile.isWall(board.tile(forwardNode.x , forwardNode.y)) && (      (CTile.isWall(board.tile(wallCW.x, wallCW.y))&&!final_deadSquares[forwardCW.x][forwardCW.y])       ||       (CTile.isWall(board.tile(wallCCW.x, wallCCW.y))&&!final_deadSquares[forwardCCW.x][forwardCCW.y])     )) {
				return true;
			}
			else {
				continue;
			}
		}
		return false;
	}


	@Override
	public BoardCompact initialState() {
		return mainBoard;

	}
	public List<EDirection> actions(BoardCompact state) {
		List<EDirection> last = new ArrayList<>();
		HashSet<EDirection> results;
		results = walkstowards(state);
		for(EDirection mydir :results) {
			last.add(mydir);
		}
		return last;
	}

	@Override
	public BoardCompact result(BoardCompact state, EDirection action) {
		BoardCompact bclone = state.clone();
		//bclone.MYBOARD = false;
		CPush mypush = CPush.getAction(action);
		CMove mymove = CMove.getAction(action);
		if(mymove.isPossible(bclone)) {
			mymove.perform(bclone);
			return bclone;
		}
		else{
			mypush.perform(bclone);
			//bclone.MYBOARD = true;
			return bclone;
		}
	}



	@Override
	public boolean isGoal(BoardCompact state) {
		if(state.isVictory())
		{
			return true;
		}
		return false;
	}


	@Override
	public double cost(BoardCompact state, EDirection action) {
		return 1;
	}



	@Override
	public int estimate(BoardCompact state) {
		//if (state.MYBOARD == false) {
			//return 1000;
		//} else {
			List<int[]> myboxlist = new ArrayList<>();
			List<int[]> mytargetlist = new ArrayList<>();
			for (int y = 0; y < state.height(); ++y) {
				for (int x = 0; x < state.width(); ++x) {
					if (CTile.isSomeBox(state.tile(x, y))) {
						int[] a = new int[2];
						a[0] = x;
						a[1] = y;
						myboxlist.add(a);
					}
					if (CTile.forSomeBox(state.tile(x, y))) {
						int[] b = new int[2];
						b[0] = x;
						b[1] = y;
						mytargetlist.add(b);
					}
				}
			}
			int sokobantobox = 1000;
			int manhattansokoban = 0;
			int totaldistanceboxtarget = 0;
			for (int i = 0; i < myboxlist.size(); i++) {
				int a[] = myboxlist.get(i);
				manhattansokoban = Math.abs(a[0] - state.playerX) + Math.abs(a[1] - state.playerY);
				if (manhattansokoban < sokobantobox) {
					sokobantobox = manhattansokoban;
				}

				int minimum = 1000;
				if (mytargetlist.size() == 0) {
					break;
				}
				for (int j = 0; j < mytargetlist.size(); j++) {
					int b[] = mytargetlist.get(j);
					int manhattandistance = Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
					if (manhattandistance == 0) {
						minimum = 0;
						break;
					}
					if (manhattandistance < minimum) {
						minimum = manhattandistance;

					}
				}
				totaldistanceboxtarget = totaldistanceboxtarget + minimum;
			}
			return totaldistanceboxtarget + sokobantobox - 1;
		}
	//}





}