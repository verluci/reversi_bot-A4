package com.github.verluci.reversi.networking.listeners;

import com.github.verluci.reversi.networking.types.Move;

/**
 * An interface that should be implemented when creating a listener for when a move has been made.
 */
public interface MoveListener {
    void onPlayerMove(Move move);
}
