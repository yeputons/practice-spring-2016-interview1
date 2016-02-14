package net.yeputons.spbau.practice.spring2016.interview1;

import ch.qos.logback.classic.Level;
import net.yeputons.spbau.practice.spring2016.interview1.game.*;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "Shows help");
        options.addOption("v", "verbose", false, "Enables game-level information logging");
        options.addOption("d", "debug", false, "Enables debug-level logging, implies -v");
        options.addOption("g", "games-count", true, "Amount of games to play (default - 11)");
        Option portOption = new Option("p", "port", true, "Port to listen on");
        portOption.setRequired(true);
        options.addOption(portOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
        if (cmd == null || cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("GameServer", options, true);
            System.exit(1);
        }

        Level logLevel = Level.WARN;
        if (cmd.hasOption("d")) {
            logLevel = Level.DEBUG;
        } else if (cmd.hasOption("v")) {
            logLevel = Level.INFO;
        }
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(logLevel);

        log.info("Setting log level to " + logLevel);

        int port = Integer.parseInt(cmd.getOptionValue("p"));
        int gamesToPlay = Integer.parseInt(cmd.getOptionValue("g", "11"));
        try {
            new GameServer().run(port, gamesToPlay);
        } catch (Exception e) {
            log.error("Error caught during game", e);
        }
    }

    public void run(int port, int gamesToPlay) throws Exception {
        log.info("Starting server on " + port);
        try (ServerSocket listener = new ServerSocket(port)) {
            log.info("Waiting for client");
            try (Socket client = listener.accept()) {
                listener.close();
                Random random = new Random();

                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();

                int serverWins = 0, clientWins = 0, draws = 0;
                for (int game = 0; game < gamesToPlay; game++) {
                    switch (playGame(random.nextBoolean(), in, out)) {
                        case 0: draws++; break;
                        case 1: serverWins++; break;
                        case 2: clientWins++; break;
                    }
                }
                log.info(String.format("Server wins %d times, client wins %d times, %d draws", serverWins, clientWins, draws));
                if (serverWins > clientWins) {
                    System.out.println("Java");
                } else if (serverWins < clientWins) {
                    System.out.println("Python");
                } else {
                    System.out.println("Draw");
                }
            }
        }
    }

    private static int playGame(boolean serverFirst, InputStream in, OutputStream out) throws IOException {
        GameState state = new GameState();
        AbstractPlayer players[];
        log.info("Setting up game, serverFirst=" + serverFirst);
        if (serverFirst) {
            players = new AbstractPlayer[] {
                    new RandomBot(Player.X),
                    new IOPlayer(Player.O, in, out)
            };
        } else {
            players = new AbstractPlayer[] {
                    new IOPlayer(Player.X, in, out),
                    new RandomBot(Player.O)
            };
        }

        while (!state.isFinished()) {
            log.debug("State is:\n" + state);
            log.debug("Player " + state.whoseTurn() + " makes turn");
            GameTurn turn = players[state.whoseTurn().ordinal()].makeTurn();
            state.makeTurn(turn);
            log.debug("Broadcasting " + turn);
            for (AbstractPlayer p : players) {
                p.turnHappened(turn);
            }
        }

        log.info("Game is finished with result " + state.getGameResult() + ", state is:\n" + state);

        out.write(GameProtocol.STOP.ordinal());
        if (in.read() != GameProtocol.STOP.ordinal()) {
            throw new GameProtocolException();
        }

        GameResult result = state.getGameResult();
        switch (result) {
            case DRAW: return 0;
            case X_WINS: return serverFirst ? 1 : 2;
            case O_WINS: return serverFirst ? 2 : 1;
            default: throw new AssertionError();
        }
    }
}
