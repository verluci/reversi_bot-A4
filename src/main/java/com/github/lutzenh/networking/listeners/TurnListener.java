package com.github.lutzenh.networking.listeners;

/**
 * An interface that should be implemented when creating a listener for when it is the players turn.
 */
public interface TurnListener {
    void onReceiveTurn(String message);
}
