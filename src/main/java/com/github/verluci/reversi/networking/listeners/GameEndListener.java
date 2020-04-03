package com.github.verluci.reversi.networking.listeners;

import com.github.verluci.reversi.networking.types.GameEnd;

/**
 * An interface that should be implemented when creating a listener for when the game-ends.
 */
public interface GameEndListener {
    void onGameEnded(GameEnd ending);
}
