import cz.sokoban4j.simulation.actions.EDirection;
import cz.sokoban4j.simulation.actions.compact.CAction;
import cz.sokoban4j.simulation.actions.compact.CPush;


import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class AStar<S,A> {
  public static <S, A> Solution<S, A> search(HeuristicProblem<S, A> prob) {

    Node<S, A> startnode = new Node<S, A>(
            prob.initialState(),
            null,
            null,
            0
    );
    startnode.estimate = prob.estimate(startnode.state);
    startnode.fcost = startnode.cost + startnode.estimate;
    HashSet<EDirection> resultdirections = new HashSet<>();

    PriorityQueue<Node<S,A>> frontierPriorityQueue = new PriorityQueue<Node<S, A>> ();

    HashMap<S, Node<S,A>> frontierMap = new HashMap<S, Node<S, A>>();
    HashSet<S> explored = new HashSet<>();


    frontierPriorityQueue.add(startnode);
    List<A> mylist = new ArrayList<A>();
    while (!frontierPriorityQueue.isEmpty()) {

      Node<S, A> firstNode = frontierPriorityQueue.poll();
      if (explored.contains(firstNode.state)) continue;
      explored.add(firstNode.state);
      if (prob.isGoal(firstNode.state)) {
        Node<S,A> copycurrent = new Node<S,A>(firstNode);
        if (copycurrent.parent != null) {
          while(copycurrent.parent != null) {
            mylist.add(copycurrent.action);
            copycurrent = copycurrent.parent;
          }
        }
        Collections.reverse(mylist);
        Solution<S,A> mysolution = new Solution<S,A>(mylist,firstNode.state,firstNode.cost);
        return mysolution;
      }

      for (A i : prob.actions(firstNode.state)) {
        // System.out.println(Arrays.toString(prob.actions(firstNode.state).toArray()));

        S nextstate = prob.result(firstNode.state, i);
        if (explored.contains(nextstate)) continue;
        Node<S,A> child = new Node<S,A>(prob.result(firstNode.state, i), firstNode, i,firstNode.cost + prob.cost(firstNode.state, i));
        child.estimate = prob.estimate(child.state);
       //  if(child.estimate == 1000){
        //System.out.println("yo");
       //  child.estimate = firstNode.estimate;
       //  }
        //else{
        // System.out.println(child.estimate);
        //}
        child.fcost = child.cost + child.estimate;
        frontierPriorityQueue.add(child);
      }
    }

    return null;
  }
}