#!/usr/bin/env python3
import argparse
import logging
import sys
import socket
from random import randrange

COMMAND_START=0
COMMAND_TURN_HAPPENED=1
COMMAND_MAKE_TURN=2
COMMAND_STOP=3

class GameBot:
    WIDTH=3
    HEIGHT=3

    def __init__(self, player):
        self.field = [[0] * self.WIDTH for _ in range(self.HEIGHT)]
        assert player in [0, 1]
        self.mine = player + 1
        self.others = 2 if player == 1 else 1

    def turnHappened(self, row, col):
        assert self.field[row][col] == 0
        self.field[row][col] = self.others

    def makeTurn(self):
        while True:
            row = randrange(self.WIDTH)
            col = randrange(self.HEIGHT)
            if not self.field[row][col]:
                self.field[row][col] = self.mine
                return row, col

class ConnectionAbortedError(Exception):
    pass

def recvInts(sock, count):
    result = b""
    while len(result) < count:
        current = sock.recv(count - len(result))
        if not current:
            raise ConnectionAbortedError
        result += current
    return list(result)

def main():
    parser = argparse.ArgumentParser(description="Client for tic-tac-toe network game")
    parser.add_argument("--host", type=str, help="Server hostname", default="localhost")
    parser.add_argument("port", type=int, help="Server port")
    parser.add_argument("--verbose", action="store_true", help="Enables game-level log")
    parser.add_argument("--debug", action="store_true", help="Enables detailed debugging game-level log, implies --verbose")

    args = parser.parse_args(sys.argv[1:])

    log_level = logging.WARN
    if args.debug:
        log_level = logging.DEBUG
    elif args.verbose:
        log_level = logging.INFO
    logging.getLogger().setLevel(log_level)

    host = args.host
    port = args.port
    logging.info("Connecting to %s:%d", host, port)
    with socket.create_connection((host, port)) as sock:
        bot = None
        while True:
            try:
                command, = recvInts(sock, 1)
            except ConnectionAbortedError:
                assert not bot, "Connection aborted during game"
                break

            if not bot:
                if command == COMMAND_START:
                    player, = recvInts(sock, 1)
                    bot = GameBot(player)
                    logging.info("Starting game as player %d", player)
                else:
                    assert False, "Unexpected command: " + command
            else:
                if command == COMMAND_STOP:
                    logging.info("Game ended")
                    sock.send(bytes([COMMAND_STOP]))
                    bot = None
                elif command == COMMAND_TURN_HAPPENED:
                    row, col = recvInts(sock, 2)
                    logging.debug("Turn happened: row=%d, col=%d", row, col)
                    bot.turnHappened(row, col)
                elif command == COMMAND_MAKE_TURN:
                    logging.debug("Making turn")
                    row, col = bot.makeTurn()
                    logging.debug("Made turn: row=%d, col=%d", row, col)
                    sock.send(bytes([COMMAND_TURN_HAPPENED, row, col]))
                else:
                    assert False, "Unexpected command: " + command

if __name__ == "__main__":
    main()
