package com.mygdx.game.Entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Character {
    /*
    protected BodyDef bDef;
    protected PolygonShape shape;
    protected FixtureDef fDef;
    protected Body body;

     */
    protected World world;
    protected float width, height;
    protected Vector2 position;
    protected boolean flipped;

    public Character(World world){
        this.world = world;
        /*
        bDef = new BodyDef();
        shape = new PolygonShape();
        fDef = new FixtureDef();

         */
        position = new Vector2();
    }

    public abstract void createChara();
    public abstract void update();
    public abstract void render(Batch batch);
}
