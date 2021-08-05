package com.mygdx.game.Screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.mygdx.game.App;

public abstract class GameScreen implements Screen {
    public final GameScreenManager gsm;
    protected App app;
    //-----------------------------------//
    protected OrthographicCamera cam, foregroundCam, backgroundCam, B2DCam;
    protected Box2DDebugRenderer B2DR;
    protected SpriteBatch batch, foregroundBatch, backgroundBatch;

    public GameScreen(GameScreenManager gsm){
        this.gsm = gsm;
        this.app = gsm.app;
        cam = app.getCam();
        foregroundCam = app.getForegroundCam();
        backgroundCam = app.getBackGroundCam();
        B2DCam = app.getB2DCam();
        B2DR = app.getB2DR();

        batch = app.getBatch();
        foregroundBatch = app.getForegroundBatch();
        backgroundBatch = app.getBackGroundBatch();
    }

    public abstract void update(float dt);
    public abstract void handleInput();

}
