package net.yeputons.spbau.practice.spring2016.interview1.game;

public class GameTurn {
    public final Player player;
    public final int row, column;

    public GameTurn(Player player, int row, int column) {
        this.player = player;
        this.row = row;
        this.column = column;

        if (player == null ) {
            throw new IllegalArgumentException("Illegal player");
        }
        if (!(0 <= row && row < GameState.HEIGHT && 0 <= column && column <= GameState.WIDTH)) {
            throw new IllegalArgumentException(
                    String.format("Invalid cell position: (%d, %d)", row, column));
        }
    }

    @Override
    public String toString() {
        return "GameTurn{" +
                "player=" + player +
                ", row=" + row +
                ", column=" + column +
                '}';
    }
}
