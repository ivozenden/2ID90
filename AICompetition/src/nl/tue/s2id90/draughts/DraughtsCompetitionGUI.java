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
    int[] bestWeights = new int[] {1,1,1,1,1,1};
    int weightIndex = 0;
    int newWeight;
    boolean firstGameWon = false;
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
        startLearningGame();
    }
    private void startLearningGame() {
        int[] newWeights = Arrays.copyOf(bestWeights, bestWeights.length);
        generateNewWeight();
        newWeights[weightIndex] = newWeight;
        List<DraughtsPlayer> players = this.getPlugins(pluginFolders).get(0).getPlayers();
        int i = 0;
        if (firstGameWon) i++;
        players.get(i % 2).setWeights(bestWeights);
        players.get((i + 1) % 2).setWeights(newWeights);
        competition = new Competition(players);
        fillTable(competition.getSchedule());
        updateRanking();
        updateGUI();
        Game game = competition.getSchedule().get(0);
        startGame(game);
    }
    @Override public void onStopGame(Game game) {
        System.out.println(game.getResult());
        System.out.println(Arrays.toString(bestWeights));
        if (!firstGameWon && game.getResult() == Game.Result.BLACK_WINS) {
            firstGameWon = true;
            startLearningGame();
        } else if (firstGameWon && game.getResult() == Game.Result.WHITE_WINS) {
            bestWeights[weightIndex]=newWeight;
            learnNextWeight();
        } else {
            learnNextWeight();
        }
    }
    private void learnNextWeight() {
        firstGameWon = false;
        weightIndex = (weightIndex + 1) % bestWeights.length;
        startLearningGame();
    }
    public void generateNewWeight() {
        int delta = getMax(bestWeights)-getMin(bestWeights);
        System.out.println("delta: " + delta);
        newWeight = (int) Math.round(
            bestWeights[weightIndex] + 
            Math.random()*delta
        );
        if (newWeight == bestWeights[weightIndex]) newWeight++;
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
