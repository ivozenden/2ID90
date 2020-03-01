package nl.tue.s2id90.contest;

import nl.tue.s2id90.game.Game;

/**
 *
 * @author huub
 */
public interface GameGuiListener<S,M> {
    void onHumanMove(M m);
    void onNewGameState(S s);
    void onStopGame(Game game);
}
