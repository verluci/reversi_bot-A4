package com.github.lutzenh.networking;

/**
 * This class is used when a match has started. (info about the starting conditions are contained in this class.
 */
public class GameStart {
    private Player startingPlayer;
    private Player opponent;
    private String gameType;

    /**
     * Constructor for Match
     * @param startingPlayer The player that can make the first move.
     * @param opponent The player that is played against.
     * @param gameType The type of game that is being played.
     */
    public GameStart(Player startingPlayer, Player opponent, String gameType) {
        this.startingPlayer = startingPlayer;
        this.opponent = opponent;
        this.gameType = gameType;
    }

    /**
     * @return The player that can start first.
     */
    public Player getStartingPlayer() {
        return startingPlayer;
    }

    /**
     * @return The player that is played against.
     */
    public Player getOpponent() {
        return opponent;
    }

    /**
     * @return The type of game being played.
     */
    public String getGameType() {
        return gameType;
    }

    @Override
    public String toString() {
        return "GameStart{" +
                "startingPlayer=" + startingPlayer +
                ", opponent=" + opponent +
                ", gameType='" + gameType + '\'' +
                '}';
    }
}
