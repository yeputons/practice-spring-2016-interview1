package net.yeputons.spbau.practice.spring2016.interview1.game;

/**
 * Common interface for classes which interact with players (either local bot or remote client)
 */
public interface AbstractPlayer {
    /**
     * Should be called whenever a turn is made in the game (including turn by this player)
     * @param turn Turn that was made
     */
    void turnHappened(GameTurn turn);

    /**
     * Should be called whenever this player should make a turn
     * @return Turn that player makes
     */
    GameTurn makeTurn();
}
