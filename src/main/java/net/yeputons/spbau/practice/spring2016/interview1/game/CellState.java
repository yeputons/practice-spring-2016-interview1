package net.yeputons.spbau.practice.spring2016.interview1.game;

public enum CellState {
    NONE {
        @Override
        public String toString() {
            return ".";
        }
    },
    X {
        @Override
        public String toString() {
            return "X";
        }
    },
    O {
        @Override
        public String toString() {
            return "O";
        }
    }
}
