package com.github.verluci.reversi.networking.types;

import java.util.Objects;

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

    /**
     * @param o The object you want to compare to this Player.
     * @return If the player has the same identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return name.equals(player.name);
    }

    /**
     * @return This object's hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
