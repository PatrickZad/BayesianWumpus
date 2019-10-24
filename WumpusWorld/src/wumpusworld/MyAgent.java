package wumpusworld;

import java.nio.charset.CoderResult;
import java.util.*;

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
    private Set<Coordinate> frotier;
    private Set<Coordinate> known;
    private Coordinate[][] coordinates = new Coordinate[4][4];

    class Coordinate {
        final int x;
        final int y;
        final Coordinate left;
        final Coordinate right;
        final Coordinate up;
        final Coordinate down;
        List<Coordinate> neighbors;


        public Coordinate(int x, int y){
            this.x=x;
            this.y=y;
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
        known.add(coordinates[0][0]);
        frotier.add(coordinates[0][1]);
        frotier.add(coordinates[1][0]);
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
            w.doAction(World.A_CLIMB);
            return;
        }
        
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
        }
                
    }    
    
     /**
     * Genertes a random instruction for the Agent.
     */
    public int decideRandomMove()
    {
      return (int)(Math.random() * 4);
    }

    /**
     * find way to a frontier root
     */
    private void moveTo(Coordinate destination){
        //find path
        Coordinate current = coordinates[w.getPlayerX()-1][w.getPlayerY()-1];
        Stack<Coordinate> path = new Stack<>();
        path.push(destination);
        Coordinate next=null;
        for (int i=0; i<destination.neighbors.size(); i++){
            next=destination.neighbors.get(i);
            if (w.isVisited(next.x, next.y)){
                break;
            }
        }
        while (!current.equals(next)){

        }
    }

}



