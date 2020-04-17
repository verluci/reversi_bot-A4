package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.Game;
import com.github.verluci.reversi.game.Game.Player;

/**
 * Implement this class if you want to create a new agent that can play a game.
 */
public abstract class Agent {
    protected Game game;
    protected Player player;

    /**
     * Constructor for Agent
     */
    public Agent() {
    }

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
     *
     * @param x The horizontal position of the tile you want to make a move on.
     * @param y The vertical position of the tile you want to make a move on.
     */
    protected void move(int x, int y) {
        game.tryMove(player, x, y);
    }

    /**
     * sets the game this agent is playing in.
     *
     * @param game The game this agent should play in.
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * @return The Player this agent is playing.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the Player the agent is in a game.
     *
     * @param player The player the agent plays in a game.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }
}
