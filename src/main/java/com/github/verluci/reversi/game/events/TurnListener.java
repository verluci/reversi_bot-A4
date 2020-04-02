package com.github.verluci.reversi.game.events;

import com.github.verluci.reversi.game.Game.*;

/**
 * An interface that should be implemented when it is the next player's turn.
 */
public interface TurnListener {
    void onNextPlayer(Player player);
}
