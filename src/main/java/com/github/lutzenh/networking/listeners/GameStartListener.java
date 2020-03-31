package com.github.lutzenh.networking.listeners;

import com.github.lutzenh.networking.types.GameStart;

/**
 * An interface that should be implemented when creating a listener for when the game starts.
 */
public interface GameStartListener {
    void onStartGame(GameStart gameStart);
}
