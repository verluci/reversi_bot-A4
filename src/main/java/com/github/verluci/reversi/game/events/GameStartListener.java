package com.github.verluci.reversi.game.events;

import com.github.verluci.reversi.game.Game.*;

/**
 * An interface that should be implemented when the game starts.
 */
public interface GameStartListener {
    void onGameStart(Player startingPlayer);
}
