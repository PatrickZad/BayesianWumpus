package wumpusworld;

import java.util.*;
import java.lang.Math;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private World w;
    int rnd;

    /**
     * Record progress of the game
     */
    private List<Coordinate> frontier=new ArrayList<>();
    private List<Coordinate> known=new ArrayList<>();
    private Coordinate[][] coordinates = new Coordinate[4][4];
    private List<Coordinate> knownPits=new ArrayList<>();
    //private List<Coordinate> knownPits = new ArrayList<>();
    //private List<Coordinate> knownWumpus = new ArrayList<>();

    class Coordinate {
        final int x;
        final int y;
        Coordinate left;
        Coordinate right;
        Coordinate up;
        Coordinate down;
        List<Coordinate> neighbors;


        public Coordinate(int x, int y){
            this.x=x;
            this.y=y;


        }

        public void generateNeighbors(){
            neighbors=new ArrayList<>();
            if (x>1){
                left=coordinates[x-2][y-1];
                neighbors.add(left);
            }else {
                left=null;
            }

            if (x<4){
                right=coordinates[x][y-1];
                neighbors.add(right);
            }else {
                right=null;
            }

            if (y<4){
                up=coordinates[x-1][y];
                neighbors.add(up);
            }else {
                up=null;
            }

            if (y>1){
                down=coordinates[x-1][y-2];
                neighbors.add(down);
            }else {
                down=null;
            }
        }

        public Coordinate getLeft() {
            return left;
        }

        public Coordinate getRight() {
            return right;
        }

        public Coordinate getUp() {
            return up;
        }

        public Coordinate getDown() {
            return down;
        }

    }
    
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;
        //initialize
        for (int i=0; i<4; i++){
            for (int j=0; j<4; j++)
            coordinates[i][j]=new Coordinate(i+1, j+1);
        }
        for (int i=0; i<4; i++){
            for (int j=0; j<4; j++)
                coordinates[i][j].generateNeighbors();
        }
        known.add(coordinates[0][0]);
        frontier.add(coordinates[0][1]);
        frontier.add(coordinates[1][0]);
    }
   
            
    /**
     * Asks your solver agent to execute an action.
     */

    public void doAction()
    {
        //Location of the player
        int cX = w.getPlayerX();
        int cY = w.getPlayerY();
        
        
        //Basic action:
        //Grab Gold if we can.
        if (w.hasGlitter(cX, cY))
        {
            w.doAction(World.A_GRAB);
            return;
        }
        
        //Basic action:
        //We are in a pit. Climb up.
        if (w.isInPit())
        {
            knownPits.add(currentPosition());
            w.doAction(World.A_CLIMB);
            //knownPits.add(coordinates[w.getPlayerX()-1][w.getPlayerY()-1]);
            return;
        }

        //Take actions

        Coordinate destination = selectFrontier();
        moveToDestination(destination);

        /*
        //Test the environment
        if (w.hasBreeze(cX, cY))
        {
            System.out.println("I am in a Breeze");
        }
        if (w.hasStench(cX, cY))
        {
            System.out.println("I am in a Stench");
        }
        if (w.hasPit(cX, cY))
        {
            System.out.println("I am in a Pit");
        }
        if (w.getDirection() == World.DIR_RIGHT)
        {
            System.out.println("I am facing Right");
        }
        if (w.getDirection() == World.DIR_LEFT)
        {
            System.out.println("I am facing Left");
        }
        if (w.getDirection() == World.DIR_UP)
        {
            System.out.println("I am facing Up");
        }
        if (w.getDirection() == World.DIR_DOWN)
        {
            System.out.println("I am facing Down");
        }
        
        //decide next move
        rnd = decideRandomMove();
        if (rnd==0)
        {
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_MOVE);
        }
        
        if (rnd==1)
        {
            w.doAction(World.A_MOVE);
        }
                
        if (rnd==2)
        {
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_MOVE);
        }
                        
        if (rnd==3)
        {
            w.doAction(World.A_TURN_RIGHT);
            w.doAction(World.A_MOVE);
        }*/
                
    }    
    
     /**
     * Genertes a random instruction for the Agent.
     */
    public int decideRandomMove()
    {
      return (int)(Math.random() * 4);
    }

    /**
     * Decide which frontier to go
     * @return
     */
    private Coordinate selectFrontier(){
        Map<Coordinate, Double> pitProbabilities=frontierPitProbability();
        Map<Coordinate, Double> wumpusProbability=null;
        if (w.wumpusAlive()){
            wumpusProbability=frontierWumpusProbability();
            for (Coordinate room : wumpusProbability.keySet()){
                if (wumpusProbability.get(room)>0.9){
                    shootWumpus(room);
                    return room;
                }
            }
        }
        double factor=2;
        List<Coordinate> sameDestination=new ArrayList<>();
        for (Coordinate room : frontier){
            double prob=pitProbabilities.get(room);
            prob+= wumpusProbability!=null ? wumpusProbability.get(room) : 0;
            if (prob<factor){
                factor=prob;
                sameDestination.clear();
                sameDestination.add(room);
            }else
                if (prob==factor){
                    sameDestination.add(room);
                }
        }
        if (sameDestination.size()>1){
            Random rand=new Random();
            return sameDestination.get(rand.nextInt(sameDestination.size()));
        }
        return sameDestination.get(0);
    }

    /**
     * Calculate probabilities each frontier room has a pit
     * @return
     */

    private Map<Coordinate, Double> frontierPitProbability(){
        Map<Coordinate, Double> map=new HashMap<>();
        List<List<Coordinate>> allCombinations=findCombinations(Math.min(3-knownPits.size(), frontier.size()));
        for (Coordinate coordinate : frontier){
            double hasPit=0;
            double noPits=0;
            for (List<Coordinate> combination : allCombinations){
                if (matchKnownBreeze(combination)){
                    if (combination.contains(coordinate)){
                        hasPit+=Math.pow(0.2, combination.size())*Math.pow(0.8, frontier.size()-combination.size());
                    }else {
                        noPits+=Math.pow(0.2, combination.size())*Math.pow(0.8, frontier.size()-combination.size());
                    }
                }
            }
            hasPit/= (hasPit+noPits) >0 ? hasPit+noPits : 1;
            map.put(coordinate, hasPit);
        }
        return map;
    }

    /**
     * Calculate probabilities each frontier room has a pit
     * @return
     */

    private Map<Coordinate, Double> frontierWumpusProbability(){
        Map<Coordinate, Double> map=new HashMap<>();
        List<List<Coordinate>> allCombinations=findCombinations(1);
        for (Coordinate coordinate : frontier){
            double hasWumpus=0;
            double noWumpus=0;
            for (List<Coordinate> combination : allCombinations){
                if (matchKnownStench(combination)){
                    if (combination.contains(coordinate)){
                        hasWumpus+=Math.pow(1.0/15, combination.size())*Math.pow(1-1.0/15, frontier.size()-combination.size());
                    }else {
                        noWumpus+=Math.pow(1.0/15, combination.size())*Math.pow(1-1.0/15, frontier.size()-combination.size());
                    }
                }
            }
            hasWumpus/= (hasWumpus+noWumpus) >0 ? hasWumpus+noWumpus : 1;
            map.put(coordinate, hasWumpus);
        }
        return map;
    }

    /**
     * Find a path to destination room and move
     * @param destination
     */
    private void moveToDestination(Coordinate destination){
        if (destination==currentPosition()){
            return;
        }
        if (!currentPosition().neighbors.contains(destination)){
            //search tree method
            PathNode root=new PathNode(null, currentPosition());
            List<PathNode> leaves=new ArrayList<>();
            buildPath(root, destination, leaves);
            PathNode destNode=leaves.get(0);
            List<Coordinate> path=destNode.parentCoordinates;
            path.add(destNode.coordinate);
            if (leaves.size()>1){
                for (int i=1; i<leaves.size(); i++){
                    List<Coordinate> newPath=leaves.get(i).parentCoordinates;
                    newPath.add(leaves.get(i).coordinate);
                    if (pathLength(newPath)<pathLength(path)){
                        path=newPath;
                    }
                }
            }
            path.add(destNode.coordinate);
            for (int i=1; i<path.size();i++){
                moveToNeighbor(path.get(i));
            }
        }
        moveToNeighbor(destination);
        //move
        //Update frontier and known
        if (!known.contains(destination)){
            known.add(destination);
        }

        frontier.remove(destination);
        for (Coordinate neighbor : destination.neighbors){
            if (w.isUnknown(neighbor.x, neighbor.y) && !frontier.contains(neighbor)){
                frontier.add(neighbor);
            }
        }
    }

    private void shootWumpus(Coordinate wumpus){
        Coordinate destination=null;
        int distance=7;
        for (Coordinate coordinate : wumpus.neighbors){
            int newDistance=manhattan(coordinate, coordinates[w.getPlayerX()-1][w.getPlayerY()-1]);
            if (w.isVisited(coordinate.x, coordinate.y) && newDistance<distance){
                destination=coordinate;
                distance=newDistance;
            }
        }
        moveToDestination(destination);
        if (wumpus==destination.up){
            turnDir(World.DIR_UP);
            w.doAction(World.A_SHOOT);
            return;
        }
        if (wumpus==destination.down){
            turnDir(World.DIR_DOWN);
            w.doAction(World.A_SHOOT);
            return;
        }
        if (wumpus==destination.left){
            turnDir(World.DIR_LEFT);
            w.doAction(World.A_SHOOT);
            return;
        }
        if (wumpus==destination.right){
            turnDir(World.DIR_RIGHT);
            w.doAction(World.A_SHOOT);
            return;
        }
    }
    /**
     * Formalize combination tree to combination lists
     * @param max
     * @return
     */
    private List<List<Coordinate>> findCombinations(int max){
        List<List<Coordinate>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (int i=1 ; i<=max ; i++){
            for (Coordinate coordinate : frontier){
                TreeNode root=new TreeNode();
                root.coordinate=coordinate;
                root.depth=1;
                List<TreeNode> leaves=new ArrayList<>();
                buildCombinationTree(i, root, leaves);
                for (TreeNode node : leaves){
                    List<Coordinate> combination=new ArrayList<>();
                    TreeNode next=node;
                    while (next != null){
                        combination.add(next.coordinate);
                        next=next.parent;
                    }
                    result.add(combination);
                }
            }
        }
        return result;
    }

    /**
     * Check if a combination match known breeze info
     * @param pitsAssumption
     * @return
     */

    private boolean matchKnownBreeze(List<Coordinate> pitsAssumption){
        List<Coordinate> assumptBreezes=new ArrayList<>();
        for (Coordinate assumpt : pitsAssumption){
            for (Coordinate neighbor : assumpt.neighbors){
                if (known.contains(neighbor) && !assumptBreezes.contains(neighbor)){
                    assumptBreezes.add(neighbor);
                }
            }
        }
        for (Coordinate knownPit : knownPits){
            for (Coordinate neighbor : knownPit.neighbors){
                if (known.contains(neighbor) && !assumptBreezes.contains(neighbor)){
                    assumptBreezes.add(neighbor);
                }
            }
        }
        List<Coordinate> knownFrontier=new ArrayList<>();
        for (Coordinate front : frontier){
            for (Coordinate neighbor : front.neighbors){
                if (known.contains(neighbor) && !knownFrontier.contains(neighbor)){
                    knownFrontier.add(neighbor);
                }
            }
        }
        for (Coordinate knownFront : knownFrontier){
            if (assumptBreezes.contains(knownFront) && !w.hasBreeze(knownFront.x, knownFront.y)){
                return false;
            }
            if (!assumptBreezes.contains(knownFront) && w.hasBreeze(knownFront.x, knownFront.y)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a combination match unknown stench info
     * @param wumpusAssumption
     * @return
     */

    private boolean matchKnownStench(List<Coordinate> wumpusAssumption){
        List<Coordinate> assumptStenchs=new ArrayList<>();
        for (Coordinate assumpt : wumpusAssumption){
            for (Coordinate neighbor : assumpt.neighbors){
                if (known.contains(neighbor) && !assumptStenchs.contains(neighbor)){
                    assumptStenchs.add(neighbor);
                }
            }
        }
        List<Coordinate> knownFrontier=new ArrayList<>();
        for (Coordinate front : frontier){
            for (Coordinate neighbor : front.neighbors){
                if (known.contains(neighbor) && !knownFrontier.contains(neighbor)){
                    knownFrontier.add(neighbor);
                }
            }
        }
        for (Coordinate knownFront : knownFrontier){
            if (assumptStenchs.contains(knownFront) && !w.hasStench(knownFront.x, knownFront.y)){
                return false;
            }
            if (!assumptStenchs.contains(knownFront) && w.hasStench(knownFront.x, knownFront.y)){
                return false;
            }
        }
        return true;
    }

    /**
     * Manhattan distance
     * @param start
     * @param end
     * @return
     */
    private int manhattan(Coordinate start, Coordinate end){
        return Math.abs(start.x-end.x)+Math.abs(end.y-end.y);
    }

    /**
     * Do move and turn actions to move to a neighbor
     * @param neighbor
     */
    private void moveToNeighbor(Coordinate neighbor){
        int x=w.getPlayerX();
        int y=w.getPlayerY();
        Coordinate current=coordinates[x-1][y-1];
        if (neighbor==current.up){
            turnDir(World.DIR_UP);
            w.doAction(World.A_MOVE);
            return;
        }
        if (neighbor==current.down){
            turnDir(World.DIR_DOWN);
            w.doAction(World.A_MOVE);
            return;
        }
        if (neighbor==current.left){
            turnDir(World.DIR_LEFT);
            w.doAction(World.A_MOVE);
            return;
        }
        if (neighbor==current.right){
            turnDir(World.DIR_RIGHT);
            w.doAction(World.A_MOVE);
            return;
        }
    }

    /**
     * Adjust dir to move to a neighbor
     * @param dir
     */
    private void turnDir(int dir){
        if (dir != w.getDirection()){
            switch (dir-w.getDirection()){
                case 1:
                case -3:
                    w.doAction(World.A_TURN_RIGHT);
                    break;
                case 2:
                case -2:
                    w.doAction(World.A_TURN_RIGHT);
                    w.doAction(World.A_TURN_RIGHT);
                    break;
                case 3:
                case -1:
                    w.doAction(World.A_TURN_LEFT);
                    break;
            }
        }
    }

    /**
     * Build a tree to get frontier combinations for probability calculation
     * @param maxdepth
     * @param root
     * @param leaves
     */
    private void buildCombinationTree(int maxdepth, TreeNode root, List<TreeNode> leaves){
        if (maxdepth<2){
            leaves.add(root);
            return;
        }
        int index=frontier.indexOf(root.coordinate);
        for (int i=index+1; i<frontier.size(); i++){
            TreeNode node=new TreeNode();
            node.coordinate=frontier.get(i);
            node.parent=root;
            node.depth=root.depth+1;
            root.nexts.add(node);
            buildCombinationTree(maxdepth-1, node, leaves);
        }
    }

    private Coordinate currentPosition(){
        return coordinates[w.getPlayerX()-1][w.getPlayerY()-1];
    }

    private void buildPath(PathNode root, Coordinate destination, List<PathNode> validLeaves){
        if (root.coordinate.neighbors.contains(destination)){
            validLeaves.add(root);
            return;
        }
        for (Coordinate neighbor : root.coordinate.neighbors){
            if (known.contains(neighbor) && !root.parentCoordinates.contains(neighbor)){
                PathNode subNode=new PathNode(root, neighbor);
                root.nexts.add(subNode);
                buildPath(subNode, destination, validLeaves);
            }
        }
    }

    private int pathLength(List<Coordinate> path){
        int length=0;
        for (Coordinate room : path){
            if (knownPits.contains(room)){
                length+=17;
            }else {
                length++;
            }
        }
        return length;
    }
}

/**
 * Tree node for building combination tree
 */
class TreeNode{
    TreeNode parent=null;
    MyAgent.Coordinate coordinate;
    int depth;
    List<TreeNode> nexts=new ArrayList<>();
}

/**
 *Tree node for searching move path
 */
class PathNode{
    PathNode parent=null;
    MyAgent.Coordinate coordinate;
    List<PathNode> nexts=new ArrayList<>();
    List<MyAgent.Coordinate> parentCoordinates=new ArrayList<>();
    PathNode(PathNode parent, MyAgent.Coordinate coordinate){
        this.parent=parent;
        this.coordinate=coordinate;
        if (parent!=null){
            parentCoordinates.addAll(parent.parentCoordinates);
            parentCoordinates.add(parent.coordinate);
        }
    }
}