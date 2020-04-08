package nl.tue.s2id90.group93;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Collections;
import java.util.List;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.Move;


// Done: rename this class (and hence this file) to have a distinct name
//       for your player during the tournament
// Implementation made by Tommie Kerssies and Ivo Zenden
public class TI_v2 extends DraughtsPlayer{
    private int bestValue=0;
    int maxSearchDepth;
    
    /** boolean that indicates that the GUI asked the player to stop thinking. */
    private boolean stopped;

    public TI_v2(int maxSearchDepth) {
        super("checkers-king.png"); // Done: replace with your own icon
        this.maxSearchDepth = maxSearchDepth;
    }
    
    @Override public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        DraughtsNode node = new DraughtsNode(s.clone());    // the root of the search tree
        try {
            //We moved the iterative deepening here, since at the previous location
            //the full deepening had to end before the best move was set
            //meaning no moves were set if the deepening wasn't finished
            
            int depth = 1;
            //in an announcement it was mentioned that we shouldn't include a max
            //limit on the depth, so therefore no guard on the while statement
            while (true) {
                // compute bestMove and bestValue in a call to alphabeta
                int val = alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);
                
                //needed since bestmove would be set to null
                if (node.getBestMove() != null) {
                    // store the value corresponding to the best move
                    bestValue = val;
                    
                    // store the bestMove found uptill now
                    bestMove  = node.getBestMove();

                    // print the results for debugging reasons
                    System.err.format(
                        "%s: depth= %2d, best move = %5s, value=%d\n", 
                        this.getClass().getSimpleName(), depth, bestMove, bestValue
                    );
                }
                
                // increase depth
                depth++;
            }
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
        //removed iterative deepening here, moved to "getMove" function
        int value = 0;
        
        if (node.getState().isWhiteToMove()) {
            value = alphaBetaMax(node, alpha, beta, depth);
        } else  {
            value = alphaBetaMin(node, alpha, beta, depth);
        }
        return value;
    }
    
    /** Does a minimize alphabeta computation with the given alpha and beta
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
        
        //added: if no possible moves also stop
        if (depth == 0 || state.getMoves().isEmpty()) {
            int value = evaluate(state);
            return value;
        }
        
        List<Move> moves = state.getMoves();
        
        //set first move as best, in case of timeout
        node.setBestMove(moves.get(0));
        
        //found that repeatedly removing the first item is quite slow (since the
        //position of the other items also has to be changed), so we decided to 
        //replace this with an for each loop (which results in cleaner code as well)
        for (Move move: moves) {
            state.doMove(move); //try move
            DraughtsNode newNode = new DraughtsNode(state);
            int result = alphaBetaMax(newNode, alpha, beta, depth-1); //recurse
            state.undoMove(move); //undo move
            
            if (result < beta) {   //not <= in case of the pruning
                node.setBestMove(move);
                beta = result;
            }
            if (beta <= alpha) {
                return alpha;
            }
        }
        return beta;
     }
    
    /** Does a maximize alphabeta computation with the given alpha and beta
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
     int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) { stopped = false; throw new AIStoppedException(); }
        DraughtsState state = node.getState();
        
        //added: if no possible moves, also stop
        if (depth == 0 || state.getMoves().isEmpty()) {
            int value = evaluate(state);
            return value;
        }
        
        List<Move> moves = state.getMoves();
        
        //set first move as best, in case of timeout
        node.setBestMove(moves.get(0));
        
        //found that repeatedly removing the first item is quite slow (since the
        //position of the other items also has to be changed), so we decided to 
        //replace this with an for each loop (which results in cleaner code as well)
        for (Move move: moves) {
            state.doMove(move); //try move
            DraughtsNode newNode = new DraughtsNode(state);
            int result = alphaBetaMin(newNode, alpha, beta, depth-1); //recurse
            state.undoMove(move); //undo move
            
            if (result > alpha) {   //not >= in case of the pruning
                node.setBestMove(move);
                alpha = result;
            }

            if (alpha >= beta) {
                return beta;
            }
        }

        return alpha;
    }


    /** A method that evaluates the given state. */
    /**
     * Evaluates the state using various evaluation factors
     * @param state the state to be evaluated
     * @return the value of the state
     */
    int evaluate(DraughtsState state) {            
        //added: if end state give minimal value for white and max for black
        //since game winning moves always have to be taken
        if (state.isEndState()) {
            if (state.isWhiteToMove()) {
                return MIN_VALUE;
            } else {
                return MAX_VALUE;
            }
        }
        
        int[] pieces = state.getPieces();
        int[] p = new int[pieces.length];

        //set value of white piece to 1, white king to 3 (holds automatically)
        //set value of black piece to -1, black king to -3
        for (int i = 1; i < pieces.length; i++) {
            switch(pieces[i]){
            case DraughtsState.EMPTY:         //empty field
                p[i] = 0;
                break;
            case DraughtsState.WHITEPIECE:         //white piece
                p[i] = 1;
                break;
            case DraughtsState.BLACKPIECE:         //black piece
                p[i] = -1;
                break;
            case DraughtsState.WHITEKING:         //white king
                p[i] = 3;
                break;
            case DraughtsState.BLACKKING:         //black king
                p[i] = -3;
                break;
            }
        }

        
        //Goals: 
        //done: Add value for pieces on back row
        //done: replace positional value with center value (pieces in center)
        //done: add balance value (i.e. left and right side have equal amount of pieces)
        //done: formations i.e. three diagonal pieces or pyramids (use neighbours)
        //Figure out balance between new evaluation factors
        
        //a good thing means players earn points doing it, and a bad thing means
        //players lose points for doing it

        //material value (good thing)
        int material_value = materialValue(p);
        
        //balance value (good thing)
        int balance_value = balanceValue(pieces);
        
        //defender value (good thing)
        int defender_value = defenderValue(p);
        
        //center value (good thing)
        int center_value = centerValue(p);
        
        //formation value (good thing)
        int formation_value = formationValue(p);

        //tempi value (good thing)
        int tempi_value = tempiVal(p);

        //safe value (good thing)
        //int safe_value = safePiecesValue(p);

        //loner value (bad thing)
        //int loner_value = lonerPiecesValue(p);

        //holes value (bad thing)
        //int holes_value = holesValue(p);
        
        //total value 
        //10 4 3 4 5 4 finishes tests
        int total_value = 10 * material_value + 7 * center_value + 4 * tempi_value
                        + 4 * balance_value + 5 * defender_value + 6 * formation_value;
//        int total_value = 6 * material_value + 3 * center_value + 3 * tempi_value
//                        + 2 * safe_value + 1 * loner_value + 2 * holes_value;
        return total_value;
    }

    /** Computes the material value
     * By counting the pieces
     * @param p contains list with the pieces of this state
     * @return the material value of this state
     */
    int materialValue(int[] p) {
        int material_value = 0;
        for (int i = 1; i < p.length; i++) {
            material_value = material_value + p[i];
        }
        return material_value;
    }
    
    /** Computes the balance value
     * By checking the balance of the white and the black pieces
     * Players want to maximize their own balance and minimize the other
     * player's balance
     * @param pieces contains list with the pieces of this state
     * @return the balance value of this state
     */
    int balanceValue(int[] pieces) {
        int balance_value = 0;
        int white_balance = 0; //white's balance (when balanced this is 0)
        int black_balance = 0; //black's balance (when balanced this is 0)
        
        for (int i = 1; i < pieces.length; i++) {
            //get column
            int column = i % 10; //have columns 0 - 9 (where 0 is column 10)
            if (column == 0) {column = 10;} //columns 1-10
            
            if (column == 1 || column == 2 || column == 6 ||
                    column == 7 || column == 8) { //left pieces
                if (pieces[i] == DraughtsState.WHITEPIECE ||
                        pieces[i] == DraughtsState.WHITEKING) {
                    white_balance = white_balance - 1; //remove 1 for left pieces
                } else if (pieces[i] == DraughtsState.BLACKPIECE ||
                        pieces[i] == DraughtsState.BLACKKING) {
                    black_balance = black_balance - 1;
                }
            } else { //right pieces
                if (pieces[i] == DraughtsState.WHITEPIECE ||
                        pieces[i] == DraughtsState.WHITEKING) {
                    white_balance = white_balance + 1; //add 1 for right pieces
                } else if (pieces[i] == DraughtsState.BLACKPIECE ||
                        pieces[i] == DraughtsState.BLACKKING) {
                    black_balance = black_balance + 1;
                }
            }
        }
        //take absolute values of balance, since the players would otherwise
        //prefer right pieces
        white_balance = Math.abs(white_balance);
        black_balance = Math.abs(black_balance);
        
        balance_value = white_balance - black_balance;
        
        return balance_value;
    }

    /** Computes the defender value
     * Counts the pieces on the player's backrow
     * @param p contains list with the pieces of this state
     * @return the defender value of this state
     */
    int defenderValue(int[] p) {
        int defender_value = 0;
        
        for (int i = 1; i < p.length; i++) {
            if (i < 6) { //black's back row
                if (p[i] == -1 || p[i] == -3) { //black piece or king
                    defender_value = defender_value + p[i];
                }
            } else if (i > 45) { //white's back row
                if (p[i] == 1 || p[i] == 3) { //white piece or king
                    defender_value = defender_value + p[i];
                }
            }
        }
        
        return defender_value;
    }
    
    /** Computes the center value value
     * Pieces in the middle get a higher score.
     * @param p contains list with the pieces of this state
     * @return the center value of this state
     */
    int centerValue(int[] p) {
        int center_value = 0;
        //center means row between 2 and 7 (exclusive)
        //and columns 2, 3, 8 and 9
        for (int i = 1; i < p.length; i++) {
            if (i > 15 && i < 36) { //rows 3, 4, 5, 6
                if (i % 10 == 2 || i % 10 == 3 ||
                        i % 10 == 8 || i % 10 == 9) { //columns 2, 3, 8, 9
                    center_value = center_value + p[i];
                }
            }
        }
        return center_value;
    }
    
    /** Computes the formation value value
     * 3 adjacent pieces in a diagonal or 3 pieces in a pyramid shape (^ or v)
     * @param p contains list with the pieces of this state
     * @return the formation value of this state
     */
    int formationValue(int[] p) {
        int formation_value = 0;
        
        for (int i = 0; i < p.length; i++) {
            if (p[i] != 0) { //don't care about empty spaces
                int[] neighbours = neighborSquares(i);
                
                //piece can have 1, 2 or 4 neighbours
                //can only have formation if piece has 4 neighbours
                if (neighbours.length == 4) { 
                    //order of neighbours is always topleft, topright
                    //bottomleft bottomright
                    int topleft = neighbours[0];
                    int topright = neighbours[1];
                    int bottomleft = neighbours[2];
                    int bottomright = neighbours[3];

                    //for formations we want topleft, this piece, bottomright
                    //or topright, this piece, bottomleft
                    if (p[i] == 1 || p[i] == 3) { //white piece
                        //diagonal from topleft to bottomright)
                        if ((p[topleft] == 1 || p[topleft] == 3) && 
                                (p[bottomright] == 1 || p[bottomright] == 3)) {
                            formation_value = formation_value + 1;
                        }
                        //diagonal from topright to bottomleft
                        if ((p[topright] == 1 || p[topright] == 3) && 
                                (p[bottomleft] == 1 || p[bottomleft] == 3)) {
                            formation_value = formation_value + 1;
                        }
                        //pyramid with current piece as top
                        if ((p[bottomright] == 1 || p[bottomright] == 3) && 
                                (p[bottomleft] == 1 || p[bottomleft] == 3)) {
                            formation_value = formation_value + 1;
                        }
                        //upside down pyramid with current piece as bottom
                        if ((p[topright] == 1 || p[topright] == 3) && 
                                (p[topleft] == 1 || p[topleft] == 3)) {
                            formation_value = formation_value + 1;
                        }
                        
                    } else { //black piece (already filtered out p[i] == 0 earlier)
                        //diagonal from topleft to bottomright)
                        if ((p[topleft] == -1 || p[topleft] == -3) && 
                                (p[bottomright] == -1 || p[bottomright] == -3)) {
                            formation_value = formation_value - 1;
                        }
                        //diagonal from topright to bottomleft
                        if ((p[topright] == -1 || p[topright] == -3) && 
                                (p[bottomleft] == -1 || p[bottomleft] == -3)) {
                            formation_value = formation_value - 1;
                        }
                        //pyramid with current piece as top
                        if ((p[bottomright] == -1 || p[bottomright] == -3) && 
                                (p[bottomleft] == -1 || p[bottomleft] == -3)) {
                            formation_value = formation_value - 1;
                        }
                        //upside down pyramid with current piece as bottom
                        if ((p[topright] == -1 || p[topright] == -3) && 
                                (p[topleft] == -1 || p[topleft] == -3)) {
                            formation_value = formation_value - 1;
                        }
                    }
                }
            }
        }
        
        return formation_value;
    }
    
    /** Computes the tempi value
     * By multiplying the value of the pieces by how close they are to the
     * advance row (i.e. the row where they become kings)
     * @param p contains list with the pieces of this state
     * @return the tempi value of this state
     */
    int tempiVal(int[] p) {
        //tempi value
        //white - black
        int white_tempi = 0;
        int black_tempi = 0;
        int row = 0;

        for (int i = 1; i < p.length; i++) {
            if (p[i] == 1 || p[i] == 3) { //white piece
                row = (((50-i)/5)+1); //bottom row is 1
                white_tempi = white_tempi + p[i] * row;
            } else if (p[i] == -1 || p[i] == -3) { //black piece
                row = (((i-1)/5)+1); //top row is 1
                black_tempi = black_tempi + p[i] * row;
            }
        }

        int tempi_value = white_tempi + black_tempi;
        return tempi_value;
    }

    /** Computes the safe value
     * By counting the pieces that are safe, i.e. that are adjacent to an edge
     * @param p contains list with the pieces of this state
     * @return the safe value of this state
     */
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
    
    /** Computes the loner value
     * By counting the pieces that have no neighbors of the same color
     * @param p contains list with the pieces of this state
     * @return the loner value of this state
     */
    int lonerPiecesValue (int[] p) {
        int lonerValue = 0;
        for(int i = 1; i < p.length; i ++) {
            boolean loner = true;
            boolean white = p[i] == 1 || p[i] == 3;
            int[] neighbors = neighborSquares(i); //get neighbor squares of piece
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

    /** Computes the holes value
     * By counting the empty spaces with at least 3 neighbors of the same color
     * The player corresponding to the color gets negative points
     * @param p contains list with the pieces of this state
     * @return the holes value of this state
     */
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

    /** Computes the neighbor squares of a given piece
     * This is a support method for loner and holes value
     * @param i contains the location of the piece whose neighbors we want to know
     * @return the list of indices of the neighboring squares
     */
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

        if (num_neighbors == 1) { //corner cases, only 2 possibilities
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
}

