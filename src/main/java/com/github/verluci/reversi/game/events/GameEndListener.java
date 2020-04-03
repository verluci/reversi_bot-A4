package com.github.verluci.reversi.game.events;

import com.github.verluci.reversi.game.Game.*;

/**
 * An interface that should be implemented when a game ends.
 */
public interface GameEndListener {
    void onGameEnd(Player winner, int playerOneScore, int playerTwoScore);
}
