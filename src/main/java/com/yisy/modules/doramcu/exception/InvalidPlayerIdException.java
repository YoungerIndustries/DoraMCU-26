package com.yisy.modules.doramcu.exception;
import com.yisy.modules.doramcu.model.players.Player;

public class InvalidPlayerIdException extends IllegalArgumentException {
    public InvalidPlayerIdException(String message) {
        super(message);
    }
}
