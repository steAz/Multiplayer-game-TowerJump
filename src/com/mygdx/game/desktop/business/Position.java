package com.mygdx.game.desktop.business;

import java.io.Serializable;

public class Position implements Serializable{

    public float x;
    public float y;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public Position(Position pos) {
        this.x = pos.x;
        this.y = pos.y;
    }
}
