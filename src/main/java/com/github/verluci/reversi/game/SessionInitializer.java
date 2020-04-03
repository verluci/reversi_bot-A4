package com.github.verluci.reversi.game;

import com.github.verluci.reversi.game.Game.*;
import com.github.verluci.reversi.game.entities.Entity;
import com.github.verluci.reversi.game.entities.LocalPlayerEntity;
import com.github.verluci.reversi.game.entities.TicTacToeAIEntity;

import java.security.InvalidParameterException;

public class SessionInitializer {
    private Game game;
    private Entity player1;
    private Entity player2;

    /**
     * Constructor for SessionInitializer
     * @param player1 An Entity that is player1 in this game.
     * @param player2 An Entity that is player2 in this game.
     * @param gameType The class-definition of the game that is going to be played.
     */
    public SessionInitializer(Entity player1, Entity player2, Class<?> gameType) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = GameFactory.createGame((Class<Game>) gameType);

        player1.setGame(game);
        player2.setGame(game);

        player1.setPlayer(Player.PLAYER1);
        player2.setPlayer(Player.PLAYER2);
    }

    /**
     * Start the session in which players are playing a Game.
     * @param startingPlayer The player that is allowed to make the first move.
     */
    public void start(Entity startingPlayer) {
        if(startingPlayer == player1 || startingPlayer == player2)
            game.startGame(startingPlayer.getPlayer());
        else
            throw new InvalidParameterException("The given player is not in this session!");

        while (game.getCurrentGameState() == Game.GameState.RUNNING) {
            switch (game.getCurrentPlayer()) {
                case PLAYER1:
                    player1.performNextMove();
                    break;
                case PLAYER2:
                    player2.performNextMove();
                    break;
            }
        }
    }

    /**
     * @return The game that is being played.
     */
    public Game getGame() {
        return game;
    }

    //TODO: Remove this entry-point when all session-code has been implemented.
    /**
     * A temporary entry-point to test SessionInitializer using TicTacToe.
     * @param args Unused.
     */
    public static void main(String[] args) {
        Entity player1 = new LocalPlayerEntity();
        Entity player2 = new TicTacToeAIEntity();

        SessionInitializer newSession = new SessionInitializer(
                player1,
                player2,
                TicTacToeGame.class);

        Game game = newSession.getGame();

        game.onGameEnd((winner, playerOneScore, playerTwoScore) -> {
            System.out.println("Game has ended: p1=" + playerOneScore + ", p2=" + playerTwoScore + ", winner:" + winner);
        });

        game.onGameStart(startingPlayer -> {
            System.out.println("Game has started: startingPlayer=" + startingPlayer);

            System.out.println("\n" + game.getBoard().toString() + "\n");
            System.out.println("Next Player=" + game.getCurrentPlayer() + "\n");
        });

        game.onMove((mover, xPosition, yPosition) -> {
            System.out.println("Move=" + mover + " - " + xPosition + ", " + yPosition);
        });

        game.onInvalidMove((mover, xPosition, yPosition) -> {
            System.out.println("Invalid Move=" + mover + " - " + xPosition + ", " + yPosition);
        });

        game.onNextPlayer(player -> {
            System.out.println("\n" + game.getBoard().toString() + "\n");
            System.out.println("Next Player=" + player + "\n");
        });

        newSession.start(player1);
    }
}
