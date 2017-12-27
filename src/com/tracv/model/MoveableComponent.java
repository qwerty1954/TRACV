package com.tracv.model;

import java.awt.*;

public abstract class MoveableComponent extends GameComponent {
    protected double speed;

    public MoveableComponent (double speed, double x, double y) {
        super(x, y);
        this.speed = speed;
    }
    public abstract void draw(Graphics g);

    public double getSpeed() {
        return speed;
    }
}
