package com.github.lutzenh.networking;

/**
 * This class holds information about a player.
 */
public class Player {

    private String name;

    /**
     * The constructor for Player
     * @param name The name of the player.
     */
    public Player(String name) {
        this.name = name;
    }

    /**
     * @return The name of the player.
     */
    public String getName() {
        return name;
    }
}
