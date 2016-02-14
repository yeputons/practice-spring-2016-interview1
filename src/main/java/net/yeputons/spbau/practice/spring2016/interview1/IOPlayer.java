package net.yeputons.spbau.practice.spring2016.interview1;

import net.yeputons.spbau.practice.spring2016.interview1.game.AbstractPlayer;
import net.yeputons.spbau.practice.spring2016.interview1.game.GameTurn;
import net.yeputons.spbau.practice.spring2016.interview1.game.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class which interacts with remote player via input/output streams
 */
class IOPlayer implements AbstractPlayer {
    private final InputStream in;
    private final OutputStream out;
    private final Player player;
    private static final Logger log = LoggerFactory.getLogger(IOPlayer.class);

    IOPlayer(Player player, InputStream in, OutputStream out) throws IOException {
        this.player = player;
        this.in = in;
        this.out = out;
        log.debug("Starting player");
        out.write(GameProtocol.START.ordinal());
        out.write(player.ordinal());
    }

    @Override
    public void turnHappened(GameTurn turn) {
        if (turn.player == player) {
            log.debug("Ignoring known turn " + turn);
            return;
        }
        log.debug("Sending turn " + turn);
        try {
            out.write(GameProtocol.TURN_HAPPENED.ordinal());
            out.write(turn.row);
            out.write(turn.column);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameTurn makeTurn() {
        try {
            log.debug("Requesting client to make turn");
            out.write(GameProtocol.MAKE_TURN.ordinal());
            int command = in.read();
            if (command != GameProtocol.TURN_HAPPENED.ordinal()) {
                throw new GameProtocolException();
            }
            int row = in.read(), column = in.read();
            GameTurn turn = new GameTurn(player, row, column);
            log.debug("Received " + turn);
            return turn;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
