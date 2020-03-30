package com.github.lutzenh.networking;

public class Challenge {
    private int number;
    private Player player;
    private String gameType;

    public Challenge(int number, Player player, String gameType) {
        this.number = number;
        this.player = player;
        this.gameType = gameType;
    }

    public int getNumber() {
        return number;
    }

    public Player getPlayer() {
        return player;
    }

    public String getGameType() {
        return gameType;
    }
}
