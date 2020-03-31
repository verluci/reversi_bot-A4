package com.github.lutzenh.networking.listeners;

import com.github.lutzenh.networking.types.GameEnd;

/**
 * An interface that should be implemented when creating a listener for when the game-ends.
 */
public interface GameEndListener {
    void onGameEnded(GameEnd ending);
}
