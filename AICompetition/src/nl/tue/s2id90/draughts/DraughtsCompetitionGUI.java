package nl.tue.s2id90.draughts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.tue.s2id90.contest.Competition;
import nl.tue.s2id90.contest.CompetitionGUI;
import nl.tue.s2id90.contest.SelectionPanel;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import nl.tue.s2id90.game.Game;
import nl.tue.s2id90.game.Player;
import org10x10.dam.game.Move;
public class DraughtsCompetitionGUI extends CompetitionGUI<DraughtsPlayer,DraughtsPlayerProvider, Move, DraughtsState> {
    final static int gamesPerWeight = 10; // must be an even number such that same amount of white/black games are tried
    int[] weights = new int[] {1,1,1,1,1,1};
    int[] newWeights;
    int weightIndex = 0;
    int gameNumber = 0;
    int gamesWon = 0;
    DraughtsCompetitionGUI(String[] pluginFolders) {
        super(p->(p instanceof DraughtsPlugin)&& (p instanceof DraughtsPlayerProvider), pluginFolders);
        DraughtsGUI gui = new DraughtsGUI();
        initComponents(gui);
        // install move board listener; no other place to put this code :-)
        gui.installMoveBoardListener();
        // listen to eachother's events
        this.add(gui);
        gui.add(this);
        this.setVisible(true);
        learnNextWeight();
    }
    private void startGame() {
        List<DraughtsPlayer> players = this.getPlugins(pluginFolders).get(0).getPlayers();
        players.get(gameNumber % 2).setWeights(weights);
        players.get(gameNumber % 2).setWeights(newWeights);
        competition = new Competition(players);
        List<Game> singleGameList = new ArrayList<>();
        singleGameList.add(competition.getSchedule().get(0));
        fillTable(singleGameList);
        updateRanking();
        updateGUI();
        startGame(singleGameList.get(0));
    }
    @Override public void onStopGame(Game game) {
        System.out.println("game "+gameNumber+" for weights "+Arrays.toString(weights)+": "+game.getResult());
        if ((gameNumber % 2 == 0 && game.getResult() == Game.Result.BLACK_WINS)
            || (gameNumber % 2 == 1 && game.getResult() == Game.Result.WHITE_WINS)) {
            gamesWon++;
        }
        if (gameNumber+1-gamesWon >= gamesPerWeight/2) {
            
        } else if (gameNumber != gamesPerWeight-1) {
            gameNumber++;
            startGame();
        } else if (gamesWon > gamesPerWeight/2)
            return;
        }
    }
    private void learnNextWeight() {
        gameNumber = 0;
        weightIndex = (weightIndex + 1) % weights.length;
        newWeights = Arrays.copyOf(weights, weights.length);
        newWeights[weightIndex] = getNewWeight();
        startGame();
    }
    public int getNewWeight() {
        int delta = getMax(weights)-getMin(weights);
        System.out.println("delta: " + delta);
        int newWeight = (int) Math.round(weights[weightIndex] + 
            Math.random()*delta
        );
        if (newWeight == weights[weightIndex]) return newWeight + 1; 
        else return newWeight;
    }
    public static int getMax(int[] inputArray){ 
      int maxValue = inputArray[0]; 
      for(int i=1;i < inputArray.length;i++){ 
        if(inputArray[i] > maxValue) maxValue = inputArray[i]; 
      } 
      return maxValue; 
    }
    public static int getMin(int[] inputArray){ 
      int minValue = inputArray[0]; 
      for(int i=1;i<inputArray.length;i++){ 
        if(inputArray[i] < minValue) minValue = inputArray[i]; 
      } 
      return minValue; 
    } 
   /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(CompetitionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(CompetitionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(CompetitionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(CompetitionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new DraughtsCompetitionGUI(args);
        });
    } 
}
