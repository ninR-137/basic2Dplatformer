package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.App;
import com.mygdx.game.Controls.StartButton;

import java.util.Stack;

public class MENU_SCREEN extends GameScreen{
    private Stage stage;
    StartButton startButton;
    public MENU_SCREEN(GameScreenManager gsm) {
        super(gsm);
        stage = new Stage(new FitViewport(App.V_WIDTH, App.V_HEIGHT));
        startButton = new StartButton();
        stage.addActor(startButton);

        GameScreenManager.PLAY_SCREEN = new PLAY_SCREEN(gsm);
    }

    @Override
    public void update(float dt) {
        /*
        if(Gdx.input.isTouched()){
            if(GameScreenManager.PLAY_SCREEN == null) {
                GameScreenManager.PLAY_SCREEN = new PLAY_SCREEN(gsm);
                gsm.setGameScreen(GameScreenManager.PLAY_SCREEN);
            }
            else gsm.setGameScreen(GameScreenManager.LOADING_SCREEN);
        }

         */

        Gdx.input.setInputProcessor(stage);
        stage.act();


        if(startButton.isTouched){
            /*
            if(GameScreenManager.PLAY_SCREEN == null) {
                GameScreenManager.PLAY_SCREEN = new PLAY_SCREEN(gsm);
                gsm.setGameScreen(GameScreenManager.PLAY_SCREEN);
            }
            else gsm.setGameScreen(GameScreenManager.LOADING_SCREEN);
             */
            if(!startButton.getDisplayButtonIsTouched()){
                gsm.setGameScreen(GameScreenManager.PLAY_SCREEN);
                startButton.isTouched = false;
            }
        }

    }

    @Override
    public void handleInput() {

    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0,0,0,0);

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
        stage.getViewport().apply();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
