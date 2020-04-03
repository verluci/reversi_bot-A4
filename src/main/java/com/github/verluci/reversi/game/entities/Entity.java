package com.github.verluci.reversi.game.entities;

import com.github.verluci.reversi.game.Game;
import com.github.verluci.reversi.game.Game.*;

/**
 * Implement this class if you want to create a new entity that can play a game.
 */
public abstract class Entity {
    protected Game game;
    protected Player player;

    /**
     * Constructor for Entity
     */
    public Entity() { }

    // TODO: Change the abstraction a little bit so that the user chooses a tile instead.
    /**
     * Implement this method and use the move() method in this class.
     */
    public abstract void performNextMove();

    /**
     * Use this method if you want to give up.
     */
    protected void forfeit() {
        switch (player) {
            case PLAYER1:
                game.stopGame(Player.PLAYER2);
                break;
            case PLAYER2:
                game.stopGame(Player.PLAYER1);
                break;
        }
    }

    /**
     * This method tries to perform a move on the game.
     * @param x The horizontal position of the tile you want to make a move on.
     * @param y The vertical position of the tile you want to make a move on.
     */
    protected void move(int x, int y) {
        if(!game.tryMove(player, x, y))
            System.out.println(this.getClass().getSimpleName() + " " + player + " tried to perform a move but it somehow failed!");
    }

    /**
     * sets the game this entity is playing in.
     * @param game The game this entity should play in.
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Sets the Player the Entity is in a game.
     * @param player The player the entity plays in a game.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * @return The Player this Entity is playing.
     */
    public Player getPlayer() {
        return player;
    }
}
