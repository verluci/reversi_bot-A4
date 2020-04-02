package com.github.verluci.reversi.networking.listeners;

import com.github.verluci.reversi.networking.types.GameStart;

/**
 * An interface that should be implemented when creating a listener for when the game starts.
 */
public interface GameStartListener {
    void onStartGame(GameStart gameStart);
}
