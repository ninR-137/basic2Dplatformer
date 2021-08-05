package com.mygdx.game;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.App;
import com.mygdx.game.Screens.PLAY_SCREEN;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PLAY_SCREEN.MobileControls = true;
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new App(), config);
	}
}
