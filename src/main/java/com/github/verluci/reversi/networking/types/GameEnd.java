package com.github.verluci.reversi.networking.types;

/**
 * This class can be used when receiving a listener on a game's ending.
 */
public class GameEnd {
    private GameResult result;
    private int playerOneScore;
    private int playerTwoScore;
    String comment;

    /**
     * Constructor for GameEnd.
     * @param result The result of the match.
     * @param playerOneScore The total-score of player-one.
     * @param playerTwoScore The total-score of player-two.
     * @param comment A message on how the game has ended.
     */
    public GameEnd(GameResult result, int playerOneScore, int playerTwoScore, String comment) {
        this.result = result;
        this.playerOneScore = playerOneScore;
        this.playerTwoScore = playerTwoScore;
        this.comment = comment;
    }

    /**
     * @return The result of the match.
     */
    public GameResult getResult() {
        return result;
    }

    /**
     * @return The ending-score of player one.
     */
    public int getPlayerOneScore() {
        return playerOneScore;
    }

    /**
     * @return The ending-score of player two.
     */
    public int getPlayerTwoScore() {
        return playerTwoScore;
    }

    /**
     * @return A message on how the game has ended.
     */
    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "GameEnd{" +
                "result=" + result +
                ", playerOneScore=" + playerOneScore +
                ", playerTwoScore=" + playerTwoScore +
                ", comment='" + comment + '\'' +
                '}';
    }
}
