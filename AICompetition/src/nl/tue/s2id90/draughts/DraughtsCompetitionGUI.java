package nl.tue.s2id90.draughts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.s2id90.contest.Competition;
import nl.tue.s2id90.contest.CompetitionGUI;
import nl.tue.s2id90.contest.SelectionPanel;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import nl.tue.s2id90.game.Game;
import nl.tue.s2id90.game.Player;
import org10x10.dam.game.Move;
public class DraughtsCompetitionGUI extends CompetitionGUI<DraughtsPlayer,DraughtsPlayerProvider, Move, DraughtsState> {
    final static int gamesPerWeight = 100; // must be an even number such that same amount of white/black games are tried
    int[] weights = new int[] {1,1,1,1,1,1}; // best weigths found after running the whole night are: {6, 3, 3, 2, 1, 2}
    int[] newWeights;
    int weightIndex = 0;
    int gamesPlayed = 0;
    float points = 0; // one point for win, half for draw
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
        players.get(gamesPlayed % 2).setWeights(weights);
        players.get((gamesPlayed + 1) % 2).setWeights(newWeights);
        competition = new Competition(players);
        List<Game> singleGameList = new ArrayList<>();
        singleGameList.add(competition.getSchedule().get(0));
        fillTable(singleGameList);
        updateRanking();
        updateGUI();
        startGame(singleGameList.get(0));
    }
    @Override public void onStopGame(Game game) {
        gamesPlayed++;
        if ((gamesPlayed % 2 == 0 && game.getResult() == Game.Result.BLACK_WINS)
            || (gamesPlayed % 2 == 1 && game.getResult() == Game.Result.WHITE_WINS)) {
            points = points + 1;
        } else if (game.getResult() == Game.Result.DRAW) {
            points = points + 0.5f;
        }
        System.out.println("game "+gamesPlayed+" (new points is "+points+") for old weights "+Arrays.toString(weights)+" to new weights "+Arrays.toString(newWeights)+": "+game.getResult());

        if (points > gamesPerWeight/2) {
            weights = Arrays.copyOf(newWeights, newWeights.length);
            learnNextWeight();
        } else if (gamesPlayed-points >= gamesPerWeight/2) {
            learnNextWeight();
        } else if (gamesPlayed != gamesPerWeight) {
            startGame();
        }
    }
    private void learnNextWeight() {
        points = 0;
        gamesPlayed = 0;
        weightIndex = (weightIndex + 1) % weights.length;
        newWeights = Arrays.copyOf(weights, weights.length);
        newWeights[weightIndex] = getNewWeight();
        startGame();
    }
    public int getNewWeight() {
        int sign = 1;
        if(Math.random() > 0.5) sign = -1; // also try weight reductions as the optimal weight might be below a previous addition
        int newWeight = (int) Math.round(weights[weightIndex] + 
            Math.random()*weights[weightIndex]*sign
        );
        if (newWeight == weights[weightIndex] || newWeight < 1) return getNewWeight(); 
        else return newWeight;
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
