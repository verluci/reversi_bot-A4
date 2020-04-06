package com.github.verluci.reversi.game;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an implementation of the game Othello / Reversi
 */
public class OthelloGame extends Game {
    // The size of an othello board.
    private static final int BOARD_SIZE = 8;

    private enum Direction { NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST };

    /**
     * Constructor for Othello Game
     */
    public OthelloGame() {
        super("Reversi", new GameBoard(BOARD_SIZE, BOARD_SIZE));

        setPlayerScore(Player.PLAYER1, 2);
        setPlayerScore(Player.PLAYER2, 2);
    }

    /**
     * Sets all valid moves on the board for the given player.
     * @param player The player for which the moves are possible.
     */
    @Override
    protected void findValidMoves(Player player) {
        Tile[][] tiles = board.getTiles();

        for (int y = 0; y < board.getYSize(); y++) {
            for (int x = 0; x < board.getXSize(); x++) {
                if(tiles[x][y].isEmpty()) {
                    tiles[x][y].setState(TileState.NONE);
                    if(checkValidMove(player, x, y, tiles))
                        tiles[x][y].setState(TileState.POSSIBLE_MOVE);
                }
            }
        }
    }

    /**
     * @param player The player the move should be checked for.
     * @param xPosition The horizontal position of the move that should be checked.
     * @param yPosition The vertical position of the move that should be checked.
     * @param boardTiles The representation of the board.
     * @return If the given move is valid for the given player.
     */
    private boolean checkValidMove(Player player, int xPosition, int yPosition, Tile[][] boardTiles) {
        boolean isValidMove = false;
        for (Direction direction : Direction.values()) {
            if(checkDirectionIsValid(direction, xPosition, yPosition, player, boardTiles))
                isValidMove = true;
        }

        return isValidMove;
    }

    /**
     * This method checks if a valid move is possible based on the tiles in a given direction.
     * @param direction The direction you want to check
     * @param xPosition The origin's horizontal position
     * @param yPosition The origin's vertical position
     * @param player The player that the validity should be checked for.
     * @param boardTiles The representation of the board.
     * @return if a valid move is possible based on the tiles in a given direction.
     */
    private static boolean checkDirectionIsValid(Direction direction, int xPosition, int yPosition, Player player, Tile[][] boardTiles) {
        if(!boardTiles[xPosition][yPosition].isEmpty())
            return false;

        var offsetVector = getOffsetUsingDirection(direction);
        TileState playerTile = getTileStateUsingPlayer(player);
        TileState opponent = getInvertedTileStateUsingPlayer(player);

        int directionX = xPosition + offsetVector[0];
        int directionY = yPosition + offsetVector[1];
        int counter = 0;

        while (directionX >= 0 && directionX < boardTiles.length
            && directionY >= 0 && directionY < boardTiles[0].length
            && boardTiles[directionX][directionY].getState().equals(opponent)
        ) {
            directionX += offsetVector[0];
            directionY += offsetVector[1];
            counter++;
        }

        return directionX >= 0 && directionX < boardTiles.length
                && directionY >= 0 && directionY < boardTiles[0].length
                && boardTiles[directionX][directionY].getState().equals(playerTile)
                && counter > 0;
    }

    /**
     * Check if the given move is valid.
     * @param player The player that wants to make the move.
     * @param x The horizontal position of the move.
     * @param y The vertical position of the move.
     * @return True if the move is valid.
     */
    @Override
    public boolean isValidMove(Player player, int x, int y) {
        if(player == Player.UNDEFINED)
            return false;

        return checkValidMove(player, x, y, board.getTiles());
    }

    /**
     * Sets the given tile to the player's color and checks if their are any lines connecting in a given direction.
     * @param player The player that performs the move
     * @param x The horizontal position of the move.
     * @param y The vertical position of the move.
     */
    @Override
    protected void performMove(Player player, int x, int y) {
        Tile tile = board.getTile(x, y);
        tile.setState(getTileStateUsingPlayer(player));

        for (Direction direction : Direction.values()) {
            flipTilesInDirection(direction, x, y, player, board.getTiles());
        }

        calculatePlayerScores();
    }

    /**
     * This method updates each players score by counting the amount of tiles in their possession.
     */
    private void calculatePlayerScores() {
        setPlayerScore(Player.PLAYER1, board.countTilesWithState(TileState.PLAYER1));
        setPlayerScore(Player.PLAYER2, board.countTilesWithState(TileState.PLAYER2));
    }

    /**
     * This method flips all tiles in a given direction.
     * @param direction The direction of tiles that should be flipped.
     * @param xPosition The horizontal starting position
     * @param yPosition The vertical starting position
     * @param player The owner of the tile at the starting position.
     * @param boardTiles The representation of the board.
     */
    private static void flipTilesInDirection(Direction direction, int xPosition, int yPosition, Player player, Tile[][] boardTiles) {
        var offsetVector = getOffsetUsingDirection(direction);
        TileState playerTile = getTileStateUsingPlayer(player);
        TileState opponentTile = getInvertedTileStateUsingPlayer(player);

        int directionX = xPosition + offsetVector[0];
        int directionY = yPosition + offsetVector[1];

        List<Tile> flippingTiles = new ArrayList<>();
        while (directionX >= 0 && directionX < boardTiles.length
                && directionY >= 0 && directionY < boardTiles[0].length
                && boardTiles[directionX][directionY].getState().equals(opponentTile)
        ) {
            flippingTiles.add(boardTiles[directionX][directionY]);
            directionX += offsetVector[0];
            directionY += offsetVector[1];
        }

        if(directionX >= 0 && directionX < boardTiles.length
            && directionY >= 0 && directionY < boardTiles[0].length
            && boardTiles[directionX][directionY].getState().equals(playerTile)
            && !flippingTiles.isEmpty() )
        {
            for (Tile tile : flippingTiles) {
                tile.setState(playerTile);
            }
        }
    }

    /**
     * @return A list of four tiles, where Othello starts with.
     */
    @Override
    protected List<Tile> getStartingTiles() {
        List<Tile> tiles = new ArrayList<>();

        tiles.add(new Tile(3, 3, TileState.PLAYER2));
        tiles.add(new Tile(4, 3, TileState.PLAYER1));
        tiles.add(new Tile(3, 4, TileState.PLAYER1));
        tiles.add(new Tile(4, 4, TileState.PLAYER2));

        return tiles;
    }

    /**
     * The defined ending condition is when no tiles are left of a certain player or the board is full.
     * There is another ending condition where no moves are longer possible, this condition is handled in
     * calculateNextPlayer() by returning an UNDEFINED player when no moves are possible.
     * @return If the game has ended.
     */
    @Override
    protected boolean hasGameEnded() {
        // Check if there are any player tiles of a player left.
        if(board.countTilesWithState(TileState.PLAYER1) < 1 || board.countTilesWithState(TileState.PLAYER2) < 1)
            return true;

        // Check if there are any open tiles left.
        if(board.countTilesWithState(TileState.NONE) < 1 && board.countTilesWithState(TileState.POSSIBLE_MOVE) < 1)
            return true;

        // If so the game has not ended yet.
        return false;
    }

    /**
     * Calculates the next player, If no next-player can be chosen this method will return UNDEFINED
     * @param currentPlayer The current player that has played before the next player.
     * @return The player that is allowed to make the next move.
     */
    @Override
    protected Player calculateNextPlayer(Player currentPlayer) {
        Player opponent = getOppositePlayer(currentPlayer);
        findValidMoves(opponent);

        if(board.countTilesWithState(TileState.POSSIBLE_MOVE) > 0)
            return opponent;
        else {
            findValidMoves(currentPlayer);
            if(board.countTilesWithState(TileState.POSSIBLE_MOVE) > 0)
                return currentPlayer;
            else
                return Player.UNDEFINED;
        }

    }

    /**
     * @return The player with the highest score.
     */
    @Override
    protected Player checkLeadingPlayer() {
        if(getPlayerScore(Player.PLAYER1) == getPlayerScore(Player.PLAYER2))
            return Player.UNDEFINED;
        else
            return getPlayerScore(Player.PLAYER1) > getPlayerScore(Player.PLAYER2) ? Player.PLAYER1 : Player.PLAYER2;
    }

    /**
     * This method converts a direction into a 2 value array of the x and y direction
     * @param direction The direction the offset should be retrieved from
     * @return An offset as a 2 value (x,y) int array
     */
    private static int[] getOffsetUsingDirection(Direction direction) {
        switch (direction) {
            case NORTH:
                return new int[] { 0, -1 };
            case NORTH_EAST:
                return new int[] { 1, -1 };
            case EAST:
                return new int[] { 1, 0 };
            case SOUTH_EAST:
                return new int[] { 1, 1 };
            case SOUTH:
                return new int[] { 0, 1 };
            case SOUTH_WEST:
                return new int[] { -1, 1 };
            case WEST:
                return new int[] { -1, 0 };
            case NORTH_WEST:
                return new int[] { -1, -1 };
            default:
                throw new IllegalArgumentException("The given direction does not exist!");
        }
    }
}
