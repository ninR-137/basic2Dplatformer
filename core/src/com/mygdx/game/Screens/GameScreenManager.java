package com.mygdx.game.Screens;

import com.mygdx.game.App;

public class GameScreenManager {
    public final App app;
    public static GameScreen LOADING_SCREEN, PLAY_SCREEN, MENU_SCREEN;

    public GameScreenManager(App app){
        this.app = app;
        LOADING_SCREEN = new LOADING_SCREEN(this);
    }

    public void setGameScreen(GameScreen screen){app.setScreen(screen);}
}
