public class NodeBFS<EDirection>{
    public int x;
    public int y;
    public NodeBFS<EDirection> parent;
    public EDirection action; //action we took to get here



    public NodeBFS(int x, int y, NodeBFS<EDirection> nodeparent, EDirection nodeaction) {
        this.x = x;
        this.y = y;
        this.parent = nodeparent;
        this.action = nodeaction;

    }

    public NodeBFS(NodeBFS<EDirection> another) {
        this.x = another.x;
        this.y = another.y;
        this.parent = another.parent;
        this.action = another.action;
    }


}