package com.github.verluci.reversi.networking.listeners;

import com.github.verluci.reversi.networking.types.Challenge;

/**
 * An interface that should be implemented when creating a listener for when a challenge has been cancelled.
 */
public interface CancelChallengeListener {
    void onCancelChallenge(Challenge challenge);
}
