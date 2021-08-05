package com.mygdx.game.Entities;

import com.badlogic.gdx.math.Vector2;

public abstract class State {
    public enum movementState{
        IDLE, RUNNING, JUMPING, FALLING,
        FLIPPED_IDLE, FLIPPED_RUNNING, FLIPPED_JUMPING, FLIPPED_FALLING,
        DEATH;
    }

    public abstract movementState handleInput(Vector2 input);
    public abstract void update(Character character);
}
