package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.Game;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.GameClient;
import com.github.verluci.reversi.networking.types.GameResult;
import com.github.verluci.reversi.networking.types.Player;

/**
 * Use this Agent if you want to play against an enemy that has been fetched using a running GameClient
 */
public class NetworkAgent extends Agent {
    private GameClient gameClient;
    private Player localPlayer;

    /**
     * Constructor for NetworkAgent
     * @param gameClient The GameClient that initiates the connection.
     * @param localPlayer The player on the local side of the game.
     */
    public NetworkAgent(GameClient gameClient, Player localPlayer) {
        super();

        this.gameClient = gameClient;
        this.localPlayer = localPlayer;
    }

    @Override
    public void performNextMove() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setGame(Game game) {
        super.setGame(game);

        gameClient.onMove(listener -> {
            int position = listener.getMove();

            int xCoordinate = position % game.getBoard().getXSize();
            int yCoordinate = Math.floorDiv(position, game.getBoard().getXSize());

            synchronized (this) {
                if(!listener.getPlayer().equals(localPlayer)) {
                    move( xCoordinate, yCoordinate);
                    notify();
                }
            }
        });

        gameClient.onGameEnd(listener -> {
            GameResult result = listener.getResult();

            switch (result) {
                case WIN:
                    if(player.equals(Game.Player.PLAYER1))
                        game.stopGame(Game.Player.PLAYER2);
                    else
                        game.stopGame(Game.Player.PLAYER1);
                    break;
                case LOSS:
                    if(player.equals(Game.Player.PLAYER1))
                        game.stopGame(Game.Player.PLAYER1);
                    else
                        game.stopGame(Game.Player.PLAYER2);
                    break;
                case DRAW:
                    game.stopGame(Game.Player.UNDEFINED);
                    break;
            }

            synchronized (this) {
                notify();
            }
        });

        gameClient.onTurn(listener -> {
            synchronized (this) {
                notify();
            }
        });

        game.onMove((mover, xPosition, yPosition) -> {
            try {
                if(mover != player)
                    gameClient.performMove(xPosition + (yPosition * game.getBoard().getXSize()));

            } catch (GameClientExceptions.MoveException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void setPlayer(Game.Player player) {
        super.setPlayer(player);
    }
}
