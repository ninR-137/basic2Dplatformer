package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.Screens.GameScreenManager;

import static com.mygdx.game.Handlers.B2DVars.PPM;

public class App extends Game {

	private OrthographicCamera cam, foregroundCam, backGroundCam, B2DCam;
	private SpriteBatch batch, foregroundBatch, backGroundBatch;
	private Box2DDebugRenderer B2DR;
	public static AssetManager assetManager;
	public static final int V_WIDTH = 1480, V_HEIGHT = 720;
	public static final String TITLE = "BambooRun";
	public static BitmapFont font; // for Debugging

	private GameScreenManager GSM;
	@Override
	public void create () {
		batch = new SpriteBatch();
		foregroundBatch = new SpriteBatch();
		backGroundBatch = new SpriteBatch();
		//---------------------------------//
		cam = new OrthographicCamera();
		foregroundCam = new OrthographicCamera();
		backGroundCam = new OrthographicCamera();
		B2DCam = new OrthographicCamera();

		B2DR = new Box2DDebugRenderer();

		cam.setToOrtho(false, V_WIDTH, V_HEIGHT);
		foregroundCam.setToOrtho(false, V_WIDTH, V_HEIGHT);
		backGroundCam.setToOrtho(false, V_WIDTH, V_HEIGHT);
		B2DCam.setToOrtho(false, (float) V_WIDTH/PPM, (float) V_HEIGHT/PPM);
		//---------------------------------//

		font = new BitmapFont();
		assetManager = new AssetManager();
		//---------------------------------//
		GSM = new GameScreenManager(this);
		setScreen(GameScreenManager.LOADING_SCREEN);
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void dispose () {
		batch.dispose();
		foregroundBatch.dispose();
		assetManager.dispose();
		font.dispose();
	}

	//-------------------------------//
	public SpriteBatch getBatch() {
		return batch;
	}
	public SpriteBatch getForegroundBatch() {
		return foregroundBatch;
	}
	public SpriteBatch getBackGroundBatch() {
		return backGroundBatch;
	}
	public Box2DDebugRenderer getB2DR() {
		return B2DR;
	}
	public OrthographicCamera getB2DCam() {
		return B2DCam;
	}
	public OrthographicCamera getCam() {
		return cam;
	}
	public OrthographicCamera getBackGroundCam() {
		return backGroundCam;
	}
	public OrthographicCamera getForegroundCam() {
		return foregroundCam;
	}
	//-------------------------------//
}
