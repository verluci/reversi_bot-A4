package com.github.verluci.reversi.game;

import java.util.Objects;

/**
 * This class contains all information for a single-tile.
 */
public class Tile {
    private int xCoordinate;
    private int yCoordinate;
    private TileState state;

    //region Constructors

    /**
     * Default constructor for Tile
     * @param xCoordinate The horizontal coordinate of this tile.
     * @param yCoordinate The vertical coordinate of this tile.
     */
    public Tile(int xCoordinate, int yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.state = TileState.NONE;
    }

    /**
     * Constructor for Tile with a pre-specified TileState.
     * @param xCoordinate The horizontal coordinate of this tile.
     * @param yCoordinate The vertical coordinate of this tile.
     * @param state The state this tile tile should start in.
     */
    public Tile(int xCoordinate, int yCoordinate, TileState state) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.state = state;
    }

    //endregion

    //region Getters and Setters

    /**
     * @return The horizontal coordinate of this tile.
     */
    public int getXCoordinate() {
        return xCoordinate;
    }

    /**
     * @return The vertical coordinate of this tile.
     */
    public int getYCoordinate() {
        return yCoordinate;
    }

    /**
     * @return The current state of this tile.
     */
    public TileState getState() {
        return state;
    }

    /**
     * Use this method if you want to change the state that this tile is currently in.
     * @param state The state you want to change the tile into.
     */
    public void setState(TileState state) {
        this.state = state;
    }

    /**
     * @return True if there is not a player on this tile.
     */
    public boolean isEmpty() {
        return (state == TileState.NONE || state == TileState.POSSIBLE_MOVE);
    }

    //endregion

    /**
     * Simple function that converts a TileState into a character.
     * @param state The state you want to get the character from.
     * @return A character matching a TileState.
     */
    public static char tileStateToChar(TileState state) {
        switch (state) {
            case NONE:          return ' ';
            case POSSIBLE_MOVE: return '·';
            case PLAYER1:       return 'x';
            case PLAYER2:       return 'o';
            default:            return '#';
        }
    }

    // Equality

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return xCoordinate == tile.xCoordinate &&
                yCoordinate == tile.yCoordinate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xCoordinate, yCoordinate);
    }

    //endregion
}
