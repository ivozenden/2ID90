package nl.tue.s2id90.group93;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.Move;

/**
 * Implementation of the DraughtsPlayer interface.
 * @author huub
 */
// Done: rename this class (and hence this file) to have a distinct name
//       for your player during the tournament
public class DraughtsPlayerTI  extends DraughtsPlayer{
    private int bestValue=0;
    int maxSearchDepth = 5;
    int[] weights;
    float randomness = 0.2f;
    
    /** boolean that indicates that the GUI asked the player to stop thinking. */
    private boolean stopped;

    public DraughtsPlayerTI(int[] weights) {
        super("checkers-king.png"); // Done: replace with your own icon
        setWeights(weights);
    }
    
    public void setWeights(int[] weights) {
        this.weights = weights;
    }
    
    @Override public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        DraughtsNode node = new DraughtsNode(s.clone());    // the root of the search tree
        if (Math.random() < randomness) {
            return getRandomValidMove(s);
        }
        try {
            // compute bestMove and bestValue in a call to alphabeta
            bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, maxSearchDepth);
            
            // store the bestMove found uptill now
            // NB this is not done in case of an AIStoppedException in alphaBeat()
            bestMove  = node.getBestMove();
            
            // print the results for debugging reasons
//            System.err.format(
//                "%s: depth= %2d, best move = %5s, value=%d\n", 
//                this.getClass().getSimpleName(),maxSearchDepth, bestMove, bestValue
//            );
        } catch (AIStoppedException ex) {  /* nothing to do */  }
        
        if (bestMove==null) {
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
            return bestMove;
        }
    } 

    /** This method's return value is displayed in the AICompetition GUI.
     * 
     * @return the value for the draughts state s as it is computed in a call to getMove(s). 
     */
    @Override public Integer getValue() { 
       return bestValue;
    }

    /** Tries to make alphabeta search stop. Search should be implemented such that it
     * throws an AIStoppedException when boolean stopped is set to true;
    **/
    @Override public void stop() {
       stopped = true; 
    }
    
    /** returns random valid move in state s, or null if no moves exist. */
    Move getRandomValidMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Collections.shuffle(moves);
        return moves.isEmpty()? null : moves.get(0);
    }
    
    /** Implementation of alphabeta that automatically chooses the white player
     *  as maximizing player and the black player as minimizing player.
     * @param node contains DraughtsState and has field to which the best move can be assigned.
     * @param alpha
     * @param beta
     * @param depth maximum recursion Depth
     * @return the computed value of this node
     * @throws AIStoppedException
     **/
    int alphaBeta(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException
    {
        //iterative deepening (to revert back remove while loop and change
        //"value = function" to "return function")
        int curr_depth = 1;
        int value = 0;
        while (curr_depth <= depth) {
            if (node.getState().isWhiteToMove()) {
                value = alphaBetaMax(node, alpha, beta, curr_depth);
            } else  {
                value = alphaBetaMin(node, alpha, beta, curr_depth);
            }
            curr_depth++;
        }
        return value;
    }
    
    /** Does an alphabeta computation with the given alpha and beta
     * where the player that is to move in node is the minimizing player.
     * 
     * <p>Typical pieces of code used in this method are:
     *     <ul> <li><code>DraughtsState state = node.getState()</code>.</li>
     *          <li><code> state.doMove(move); .... ; state.undoMove(move);</code></li>
     *          <li><code>node.setBestMove(bestMove);</code></li>
     *          <li><code>if(stopped) { stopped=false; throw new AIStoppedException(); }</code></li>
     *     </ul>
     * </p>
     * @param node contains DraughtsState and has field to which the best move can be assigned.
     * @param alpha
     * @param beta
     * @param depth  maximum recursion Depth
     * @return the compute value of this node
     * @throws AIStoppedException thrown whenever the boolean stopped has been set to true.
     */
     int alphaBetaMin(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) { stopped = false; throw new AIStoppedException(); }
        DraughtsState state = node.getState();
        if (depth == 0) {
            int val = evaluate(state);
            return val;
        }
        List<Move> moves = state.getMoves();
        if (!moves.isEmpty()) {
            node.setBestMove(moves.get(0)); //set first move as best, for timeout
        }
        while (!moves.isEmpty()) {
            Move bestMove = moves.remove(0); //get first move
            state.doMove(bestMove);
            DraughtsNode newNode = new DraughtsNode(state);
            int x = alphaBetaMax(newNode, alpha, beta, depth-1);
            state.undoMove(bestMove);
            if (x < beta) {   //not <= in case of the pruning
                //System.err.println("set move at depth: " + depth);
                node.setBestMove(bestMove);
                beta = x;
            }
            if (beta <= alpha) {
                return alpha;
            }
        }

        return beta;
     }
    
    int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) { stopped = false; throw new AIStoppedException(); }
        DraughtsState state = node.getState();
        if (depth == 0) {
            int val = evaluate(state);
            return val;
        }
        List<Move> moves = state.getMoves();
        if (!moves.isEmpty()) {
            node.setBestMove(moves.get(0)); //set first move as best, for timeout
        }
        while (!moves.isEmpty()) {
            Move bestMove = moves.remove(0); //get first move
            state.doMove(bestMove);
            DraughtsNode newNode = new DraughtsNode(state);
            int x = alphaBetaMin(newNode, alpha, beta, depth-1);
            state.undoMove(bestMove);
            if (x > alpha) {   //not >= in case of the pruning
                //System.err.println("set move at depth: " + depth);
                node.setBestMove(bestMove);
                alpha = x;
            }

            if (alpha >= beta) {
                return beta;
            }
        }


        return alpha;
    }

    /** A method that evaluates the given state. */
    // Evaluates a state using multiple methods
    int evaluate(DraughtsState state) { 
        int[] pieces = state.getPieces();
        int[] p = new int[pieces.length];

        //set value of white piece to 1, white king to 3 (holds automatically)
        //set value of black piece to -1, black king to -3
        for (int i = 1; i < pieces.length; i++) {
            switch(pieces[i]){
            case 0:         //empty field
                p[i] = 0;
                break;
            case 1:         //white piece
                p[i] = 1;
                break;
            case 2:         //black piece
                p[i] = -1;
                break;
            case 3:         //white king
                p[i] = 3;
                break;
            case 4:         //black king
                p[i] = -3;
                break;
            }
        }

        int[] values = new int[6];
        //material value (good thing)
        values[0] = materialValue(p);
        //positional value (good thing)
        values[1] = positionalValue(p);
        //tempi value (good thing)
        values[2] = tempiVal(p);
        //safe value (i.e. pieces adjacent to an edge) (good thing)
        values[3] = safePiecesValue(p);
        //loner value (i.e. pieces with no other adjacent pieces) (bad thing)
        values[4] = lonerPiecesValue(p);
        //holes value, i.e. empty spaces with at least 3 neighbors of same colour (bad thing)
        values[5] = holesValue(p);
        
        int total_value = 0;
        for (int i = 0; i < values.length; i++) {
            total_value += values[i] * weights[i];
        }
        return total_value;
    }

    //material value
    int materialValue(int[] p) {
        int material_value = 0;
        for (int i = 1; i < p.length; i++) {
            material_value = material_value + p[i];
        }
        return material_value;
    }

    //positional value
    int positionalValue(int[] p) {        
        //implement modifier of score, pieces closer to an edge get a lower
        //score, pieces closer to the middle get an higher score
        int positional_value = 0;
        //row 0, 1, 8, 9 get a multiplier of 1
        //row 2, 3, 6, 7 get a multiplier of 2
        //row 4, 5 get a multiplier of 3
        //same for columns
        for (int i = 1; i < p.length; i++) {
            if (i < 11 || i > 40) { //rows 0, 1, 8, 9
                if (i % 5 == 0 || i % 5 == 1) { //columns 0,1,8,9
                    positional_value = positional_value + p[i] * 1 * 1; //1 for row and column
                } else if (i % 5 == 2 || i % 5 == 4) { //columns 2,3,6,7
                    positional_value = positional_value + p[i] * 1 * 2; //1 for row, 2 for column
                } else { //columns 4,5
                    positional_value = positional_value + p[i] * 1 * 3; //1 for row, 3 for column
                }
            } else if (i < 21 || i > 30) { //rows 2,3,6,7
                if (i % 5 == 0 || i % 5 == 1) { //columns 0,1,8,9
                    positional_value = positional_value + p[i] * 2 * 1; //2 for row, 1 for column
                } else if (i % 5 == 2 || i % 5 == 4) { //columns 2,3,6,7
                    positional_value = positional_value + p[i] * 2 * 2; //2 for row and column
                } else { //columns 4,5
                    positional_value = positional_value + p[i] * 2 * 3; //2 for row, 3 for column
                }
            } else { //rows 4,5
                if (i % 5 == 0 || i % 5 == 1) { //columns 0,1,8,9
                    positional_value = positional_value + p[i] * 3 * 1; //3 for row, 1 for column
                } else if (i % 5 == 2 || i % 5 == 4) { //columns 2,3,6,7
                    positional_value = positional_value + p[i] * 3 * 2; //3 for row, 2 for column
                } else { //columns 4,5
                    positional_value = positional_value + p[i] * 3 * 3; //3 for row and column
                }
            }
        }
        return positional_value;
    }
    
    //tempi value
    int tempiVal(int[] p) {
        //tempi value
        //white - black
        int white_tempi = 0;
        int black_tempi = 0;

        for (int i = 1; i < p.length; i++) {
            if (p[i] == 1 || p[i] == 3) { //white piece
                white_tempi = white_tempi + p[i] * (((50-i)/5)+1);
            } else if (p[i] == -1 || p[i] == -3) { //black piece
                black_tempi = black_tempi + p[i] * (((i-1)/5)+1);
            }
        }

        int tempi_value = white_tempi + black_tempi;
        return tempi_value;
    }

    //safe pieces (i.e. adjacent to an edge)
    int safePiecesValue (int[] p) {
        int safeValue = 0;

        for (int i = 1; i < p.length; i++) {
            if (i <= 5) { //top edge
                safeValue = safeValue + p[i];
            } else if (i > 45) { //bottom edge
                safeValue = safeValue + p[i];
            } else if (i % 10 == 6) { //left edge
                safeValue = safeValue + p[i];
            } else if (i % 10 == 5) { //right edge
                safeValue = safeValue = p[i];
            }
        }
        return safeValue;
    }
    
    //number of pieces without neighbors of same colour
    int lonerPiecesValue (int[] p) {
        //need function to get neighbouring squares
        //check if neighbouring squares are empty (might want to change to no neighbours of same colour)
        int lonerValue = 0;
        for(int i = 1; i < p.length; i ++) {
            boolean loner = true;
            boolean white = p[i] == 1 || p[i] == 3;
            int[] neighbors = neighborSquares(i);
            for(int k = 0; k < neighbors.length; k++) {
                if (white) {
                    if (p[neighbors[k]] == 1 || p[neighbors[k]] == 3) { //check for white neighbors
                        loner = false;
                    }
                } else {
                    if (p[neighbors[k]] == -1 || p[neighbors[k]] == -3) { //check for black neighbors
                        loner = false;
                    }
                }
            }
            if (loner) {
                lonerValue = lonerValue + -1 * p[i]; //-1 since it is a bad thing
            }
        }
        return lonerValue;
    }

    //number of holes, i.e. empty space surrounded by at least 3 pieces of the same colour
    //colour gets minus points
    int holesValue(int[] p) {
        int holes_value = 0;
        for(int i = 1; i < p.length; i ++) {
            int white_neighbors = 0;
            int black_neighbors = 0;
            int[] neighbors = neighborSquares(i);
            
            if (p[i] == 0) {
                for(int k = 0; k < neighbors.length; k++) {
                    if (p[neighbors[k]] == 1 || p[neighbors[k]] == 3) { //check for white neighbors
                        white_neighbors = white_neighbors + 1;
                    }
                    if (p[neighbors[k]] == -1 || p[neighbors[k]] == -3) { //check for black neighbors
                        black_neighbors = black_neighbors + 1;
                    }
                }
            }
            if (white_neighbors >= 3) {
                holes_value = holes_value - 1;
            } else if (black_neighbors >= 3) {
                holes_value = holes_value + 1;
            }
        }
        return holes_value;
    }

    int[] neighborSquares(int i) {
        //find row
        int row = ((i-1)/5) + 1; //final +1 is to get rows from 1-10 for rows 1-10
        //find column
        int column = i % 10; //have columns 0 - 9
        if (column == 0) {column = 10;} //columns 1-10

        //determine number of neighbors, can have 1, 2, or 4
        int num_neighbors = 0;
        if (row == 1 || row == 10) {
            num_neighbors += 1; //has 1 neighbouring row
        } else {
            num_neighbors += 2; //has 2 neighbouring rows
        }
        if (column == 5 || column == 6) { //has 1 neighbouring column
            //stays the same
        } else { //has 2 neighbouring columns
            num_neighbors = num_neighbors * 2;
        }

        //create neighbors array
        int[] neighbors = new int[num_neighbors];

        if (num_neighbors == 1) { //corner cases, only 2
            if (i == 5) { //i = 5
                neighbors[0] = 10;
            } else { // i = 46
                neighbors[0] = 41;
            }
        } else if (num_neighbors == 2) { //edge cases, easier without corners
            if (column < 5) { //i = 1-4
                neighbors[0] = i + 5;
                neighbors[1] = i + 6;
            } else if (column > 6) { //i = 47-50
                neighbors[0] = i - 6;
                neighbors[1] = i - 5;
            } else { //i = 6, 16, 26, 36, 15, 25, 35, 45
                neighbors[0] = i - 5;
                neighbors[1] = i + 5;
            }
        } else { //center cases
            if (column > 6) {
                neighbors[0] = i - 6; //top left
                neighbors[1] = i - 5; //top right
                neighbors[2] = i + 4; //bottom left
                neighbors[3] = i + 5; //bottom right
            } else if (column < 5) {
                neighbors[0] = i - 5; //top left
                neighbors[1] = i - 4; //top right
                neighbors[2] = i + 5; //bottom left
                neighbors[3] = i + 6; //bottom right
            }
        }
        return neighbors;
    }
    
    @Override public String getName() {
        return Arrays.toString(this.weights);
    }
}
