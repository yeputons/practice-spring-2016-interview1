package net.yeputons.spbau.practice.spring2016.interview1.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Bot which makes random valid turns
 */
public class RandomBot implements AbstractPlayer {
    private static final Logger log = LoggerFactory.getLogger(RandomBot.class);

    private final GameState state;
    private final Random random = new Random();
    private final Player player;

    public RandomBot(Player player) {
        state = new GameState();
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void turnHappened(GameTurn turn) {
        log.debug("Turn happened: " + turn);
        state.makeTurn(turn);
    }

    public GameTurn makeTurn() {
        while (true) {
            GameTurn turn = new GameTurn(player, random.nextInt(GameState.HEIGHT), random.nextInt(GameState.WIDTH));
            log.debug("Trying turn " + turn);
            if (state.getCell(turn.row, turn.column) == CellState.NONE) {
                log.debug("Turn is ok, returning");
                return turn;
            }
        }
    }
}
