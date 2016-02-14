package net.yeputons.spbau.practice.spring2016.interview1.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Incapsulates full information about game state.
 * Contains helper methods which determine winner and sequence of turns.
 */
public class GameState {
    public static final int WIDTH = 3;
    public static final int HEIGHT = 3;
    public static final int NEED_IN_ROW = 3;

    private static final Logger log = LoggerFactory.getLogger(GameState.class);

    private final CellState[][] field;
    private GameResult cachedGameResult;

    /**
     * Creates initial game state
     */
    public GameState() {
        field = new CellState[HEIGHT][WIDTH];
        for (int row = 0; row < HEIGHT; row++)
            for (int col = 0; col < WIDTH; col++)
                field[row][col] = CellState.NONE;
    }

    public CellState getCell(int row, int column) {
        return field[row][column];
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int row = 0; row < HEIGHT; row++) {
            if (row > 0) {
                result.append('\n');
            }
            for (CellState cell : field[row])
                result.append(cell);
        }
        return result.toString();
    }

    public void makeTurn(GameTurn turn) {
        if (isFinished()) {
            throw new IllegalStateException("Game is finished");
        }
        Player expectedPlayer = whoseTurn();
        if (turn.player != expectedPlayer) {
            throw new IllegalArgumentException(
                    String.format("Trying to make turn of player %s, expected %s", turn.player, expectedPlayer));
        }
        if (field[turn.row][turn.column] != CellState.NONE) {
            throw new IllegalArgumentException("Cell is occupied");
        }
        field[turn.row][turn.column] = turn.player == Player.X ? CellState.X : CellState.O;
    }

    /**
     * Returns player who should make turn right now.
     * Checks for parity only, does not check for turn's existence.
     */
    public Player whoseTurn() {
        int countX = 0, countO = 0;
        for (CellState[] row : field) {
            for (CellState cell : row) {
                if (cell == CellState.X) {
                    countX++;
                } else if (cell == CellState.O) {
                    countO++;
                }
            }
        }
        assert countX == countO || countX == countO + 1;
        if (countX == countO) {
            return Player.X;
        } else if (countX == countO + 1) {
            return Player.O;
        } else {
            throw new AssertionError("Illegal GameState");
        }
    }

    private boolean hasFreeCells() {
        for (CellState[] row : field)
            for (CellState cell : row)
                if (cell == CellState.NONE) {
                    return true;
                }
        return false;
    }

    public boolean isFinished() {
        return getGameResult() != null;
    }

    private boolean segmentIsSame(int row0, int col0, int drow, int dcol) {
        for (int step = 0; step < NEED_IN_ROW; step++) {
            int row = row0 + drow * step;
            int col = col0 + dcol * step;
            if (!(0 <= row && row < HEIGHT && 0 <= col && col < WIDTH)) {
                return false;
            }
            if (field[row][col] != field[row0][col0]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Either <code>null</code> (if game is not finished yet) or <code>GameResult</code>
     */
    public GameResult getGameResult() {
        if (cachedGameResult != null) {
            return cachedGameResult;
        }

        for (int row0 = 0; row0 < HEIGHT; row0++) {
            for (int col0 = 0; col0 < WIDTH; col0++) {
                if (field[row0][col0] == CellState.NONE) {
                    continue;
                }
                // Four directions in which we check for consecutive cells
                final int drow[] = { 1, 1, 0, -1 };
                final int dcol[] = { 0, 1, 1, 1 };
                for (int dir = 0; dir < 4; dir++) {
                    if (field[row0][col0] != CellState.NONE) {
                        if (segmentIsSame(row0, col0, drow[dir], dcol[dir])) {
                            if (field[row0][col0] == CellState.X) {
                                cachedGameResult = GameResult.X_WINS;
                            } else {
                                cachedGameResult = GameResult.O_WINS;
                            }
                            log.debug(String.format("Found winning player %s: line starts at (%d, %d) and goes in direction (%d, %d)",
                                    cachedGameResult,
                                    row0, col0,
                                    drow[dir], dcol[dir]));
                            return cachedGameResult;
                        }
                    }
                }
            }
        }
        if (!hasFreeCells()) {
            log.debug("Game ended in draw");
            return cachedGameResult = GameResult.DRAW;
        }
        return null;
    }
}
