package com.github.verluci.reversi.networking.types;

/**
 * This class holds information about a challenge against the connected client.
 */
public class Challenge {
    private int number;
    private Player player;
    private String gameType;

    /**
     * Constructor for Challenge
     * @param number The identifier for this challenge.
     * @param player The player that called for the challenge.
     * @param gameType The game the challenger wants to play.
     */
    public Challenge(int number, Player player, String gameType) {
        this.number = number;
        this.player = player;
        this.gameType = gameType;
    }

    /**
     * @return The identifier for this challenge.
     */
    public int getNumber() {
        return number;
    }

    /**
     * @return The player that called for the challenge.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The game the challenger wants to play.
     */
    public String getGameType() {
        return gameType;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "number=" + number +
                ", player=" + player +
                ", gameType='" + gameType + '\'' +
                '}';
    }
}
