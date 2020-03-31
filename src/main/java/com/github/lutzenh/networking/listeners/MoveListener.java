package com.github.lutzenh.networking.listeners;

import com.github.lutzenh.networking.Move;

/**
 * An interface that should be implemented when creating a listener for when a move has been made.
 */
public interface MoveListener {
    void onPlayerMove(Move move);
}
