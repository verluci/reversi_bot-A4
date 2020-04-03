package com.github.verluci.reversi.networking.types;

/**
 * This class contains information about a move that has been performed by a player.
 */
public class Move {
    private Player player;
    private int move;
    private String details;

    /**
     * Constructor for Move
     * @param player The player that made the move.
     * @param move The move that has been made.
     * @param details Information about the move that has been made.
     */
    public Move(Player player, int move, String details) {
        this.player = player;
        this.move = move;
        this.details = details;
    }

    /**
     * @return The player that made the move.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The move that has been made.
     */
    public int getMove() {
        return move;
    }

    /**
     * @return Information about the move that has been made.
     */
    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "Move{" +
                "player=" + player +
                ", move=" + move +
                ", details='" + details + '\'' +
                '}';
    }
}
