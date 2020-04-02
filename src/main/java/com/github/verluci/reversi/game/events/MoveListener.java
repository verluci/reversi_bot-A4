package com.github.verluci.reversi.game.events;

import com.github.verluci.reversi.game.Game.*;

/**
 * An interface that should be implemented when a player performs a move.
 */
public interface MoveListener {
    void OnMove(Player mover, int xPosition, int yPosition);
}
