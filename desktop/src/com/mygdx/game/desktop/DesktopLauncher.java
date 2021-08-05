package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.App;
import com.mygdx.game.Screens.PLAY_SCREEN;

public class DesktopLauncher {
	public static void main (String[] arg) {
		PLAY_SCREEN.MobileControls = false;
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new App(), config);
		config.title = App.TITLE;
		config.width = 1080;
		config.height = 540;
		config.backgroundFPS = 60;
		config.foregroundFPS = 60;
	}
}
