package com.github.lutzenh.networking.listeners;

import com.github.lutzenh.networking.types.Challenge;

/**
 * An interface that should be implemented when creating a listener for when a challenge has been received.
 */
public interface ReceiveChallengeListener {
    void onReceiveChallenge(Challenge challenge);
}
