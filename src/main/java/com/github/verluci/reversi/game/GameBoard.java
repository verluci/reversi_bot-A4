package com.github.verluci.reversi.game;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains information about the current state of a game-board.
 */
public class GameBoard {
    private int xSize;
    private int ySize;

    private Tile[][] tiles;

    /**
     * Constructor for GameBoard
     * @param xSize The horizontal size of this board.
     * @param ySize The vertical size of this board.
     */
    public GameBoard(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;

        tiles = new Tile[xSize][ySize];

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                tiles[x][y] = new Tile(x, y);
            }
        }
    }

    /**
     * Use this method if you want to clear/empty the board.
     */
    public void empty() {
        tiles = new Tile[xSize][ySize];

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                tiles[x][y] = new Tile(x, y);
            }
        }
    }

    //region Getters and Setters

    /**
     * @return The horizontal size of this board.
     */
    public int getXSize() {
        return xSize;
    }

    /**
     * @return The vertical size of this board.
     */
    public int getYSize() {
        return ySize;
    }

    /**
     * @param xPosition The horizontal position of the tile you want to retrieve.
     * @param yPosition The vertical position of the tile you want to retrieve.
     * @return The tile at the given position.
     */
    public Tile getTile(int xPosition, int yPosition) {
        return tiles[xPosition][yPosition];
    }

    /**
     * @return All tiles as a two-dimensional array.
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
     * @param state The type of tiles you want to get as a list.
     * @return A list of tiles filtered based on the given state.
     */
    public List<Tile> getTilesWithState(TileState state) {
        List<Tile> tileList = new ArrayList<>();

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                if(tiles[x][y].getState() == state)
                    tileList.add(tiles[x][y]);
            }
        }

        return tileList;
    }

    /**
     * @param state The type of tile you want to get the amount from.
     * @return The amount of this type of tile on the board.
     */
    public int countTilesWithState(TileState state) {
        int counter = 0;

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                if(tiles[x][y].getState() == state)
                    counter++;
            }
        }

        return counter;
    }

    /**
     * Change a list of tiles on the board with the specified tiles.
     * @param tileList A list of tiles that should replace tiles with the same position on the board.
     */
    public void setTiles(List<Tile> tileList) {
        for (Tile tile : tileList) {
            tiles[tile.getXCoordinate()][tile.getYCoordinate()] = tile;
        }
    }

    //endregion

    //region GPGPU Conversion

    /**
     * This method creates a GameBoard using two long values.
     * @param player1 The player defined as player1
     * @param player2 The player defined as player2
     * @return A GameBoard with the given long values as tiles.
     */
    public static GameBoard createGameBoardUsingLongValues(long player1, long player2) {
        GameBoard board = new GameBoard(8, 8);

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (isBitSet(player1, (y * 8) + x))
                    board.getTile(x, y).setState(TileState.PLAYER1);
                else if (isBitSet(player2, (y * 8) + x))
                    board.getTile(x, y).setState(TileState.PLAYER2);
            }
        }

        return board;
    }

    /**
     * Check if a certain bit is set to 1
     * @param value The value you want to check if a bit has been set on.
     * @param index The index of the bit you want to check.
     * @return If the given index on the value's bit is set to 1
     */
    private static boolean isBitSet(long value, int index) {
        return ((value & (1L << index)) != 0);
    }

    /**
     * Returns a long value of the given player's TileStates.
     * @param player The player you want to retrieve the long value from.
     * @return A long value of the given player's TileStates.
     */
    public long getPlayerTilesLongValue(TileState player) {
        var playerTiles = new BigInteger("0");
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                int index = (y * xSize) + x;

                if(tiles[x][y].getState().equals(player))
                    playerTiles = playerTiles.setBit(index);
            }
        }

        return playerTiles.longValue();
    }

    //endregion

    @Override
    public String toString() {
        StringBuilder boardBuilder = new StringBuilder();
        for (int y = -1; y < ySize; y++) {
            for (int x = -1; x < xSize; x++) {
                if(y < 0 && x < 0) {
                    boardBuilder.append("  ");
                }
                else if (y < 0) {
                    boardBuilder.append(" ").append(x).append("  ");
                }
                else if (x < 0) {
                    boardBuilder.append(y).append(" ");
                }
                else {
                    char tileStateChar = Tile.tileStateToChar(getTile(x, y).getState());
                    boardBuilder.append("[").append(tileStateChar).append("] ");
                }
            }
            boardBuilder.append("\n");
        }

        return boardBuilder.toString();
    }
}
