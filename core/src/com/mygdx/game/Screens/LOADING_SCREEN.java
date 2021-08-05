package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.App;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

public class LOADING_SCREEN extends GameScreen {
    private float progress;
    private final ShapeRenderer shapeRenderer;
    private float x, y, width, height;
    private Stage stage;
    private Image clock;
    private Image wait;

    public LOADING_SCREEN(GameScreenManager gsm) {
        super(gsm);

        shapeRenderer = new ShapeRenderer();
        progress = 0f;
        x = 100;
        y = App.V_HEIGHT/4f - height;
        width = App.V_WIDTH - (2 * x);
        height = 20;
        QueAssets();
        stage = new Stage(new StretchViewport(App.V_WIDTH, App.V_HEIGHT));
        clock = new Image(new Texture("UI/Clock1.png"));
        wait = new Image(new Texture("UI/Wait.png"));
        stage.addActor(clock);
        stage.addActor(wait);
    }

    public void QueAssets(){
        //Maps
            //Map 1
        App.assetManager.load("Maps/Map1/ForegroundMap.png", Texture.class);
        App.assetManager.load("Maps/Map1/Backgrounds/MainBackground.png",Texture.class);
        App.assetManager.load("Maps/Map1/Backgrounds/Background1.png",Texture.class);
        App.assetManager.load("Maps/Map1/Backgrounds/Background2.png",Texture.class);
        App.assetManager.load("Maps/Map1/Backgrounds/Background3.png",Texture.class);
        App.assetManager.load("Maps/Map1/Backgrounds/Background4.png",Texture.class);
        App.assetManager.load("Maps/Map1/Backgrounds/Background5.png",Texture.class);
        //Map 2
        App.assetManager.load("Maps/Map2/ForeGroundMap.png", Texture.class);
        App.assetManager.load("Maps/Map2/Backgrounds/Mainbackground.png", Texture.class);
        App.assetManager.load("Maps/Map2/Backgrounds/Background1.png", Texture.class);
        App.assetManager.load("Maps/Map2/Backgrounds/Background2.png", Texture.class);
        App.assetManager.load("Maps/Map2/Backgrounds/Background3.png", Texture.class);
        App.assetManager.load("Maps/Map2/Backgrounds/Background4.png", Texture.class);
        //Map3
        App.assetManager.load("Maps/Map3/Parallax/1.png",Texture.class);
        App.assetManager.load("Maps/Map3/Parallax/2.png",Texture.class);
        App.assetManager.load("Maps/Map3/Parallax/3.png",Texture.class);
        App.assetManager.load("Maps/Map3/Parallax/4.png",Texture.class);
        App.assetManager.load("Maps/Map3/Parallax/5.png",Texture.class);
        App.assetManager.load("Maps/Map3/Parallax/12.png",Texture.class);
        App.assetManager.load("Maps/Map3/Parallax/345.png",Texture.class);
        App.assetManager.load("Maps/Map3/Parallax/Background.png",Texture.class);
        App.assetManager.load("Maps/Map3/ForegroundMap.png", Texture.class);
        //Map4
        App.assetManager.load("Maps/Map4/Parallax/Background.png", Texture.class);
        App.assetManager.load("Maps/Map4/Parallax/1.png", Texture.class);
        App.assetManager.load("Maps/Map4/Parallax/2.png", Texture.class);
        App.assetManager.load("Maps/Map4/Parallax/3.png", Texture.class);
        App.assetManager.load("Maps/Map4/Parallax/Mist.png", Texture.class);
        //Map5
        App.assetManager.load("Maps/Map5/Backgrounds/Background.png", Texture.class);
        App.assetManager.load("Maps/Map5/Backgrounds/Parallax1.png", Texture.class);
        App.assetManager.load("Maps/Map5/Backgrounds/Parallax2.png", Texture.class);
        App.assetManager.load("Maps/Map5/Backgrounds/TransparantBackground2.png", Texture.class);
        App.assetManager.load("Maps/Map5/Backgrounds/TransparentBackground1.png", Texture.class);
        //Animations
        App.assetManager.load("CharaAnim/ZebraIdle.png", Texture.class);
        App.assetManager.load("CharaAnim/ZebraJumpingAnim.png", Texture.class);
        App.assetManager.load("CharaAnim/ZebraFallingAnim.png", Texture.class);
        App.assetManager.load("CharaAnim/ZebraRunningAnim.png", Texture.class);
        App.assetManager.load("CharaAnim/BambooSpin.png", Texture.class);
        //UI components
        App.assetManager.load("UI/JoyStick.atlas", TextureAtlas.class);
        App.assetManager.load("UI/JumpButton.atlas", TextureAtlas.class);
        App.assetManager.load("UI/StartButton.atlas",TextureAtlas.class);
        //Textures
        App.assetManager.load("Textures/9ca4a0.png", Texture.class);
        App.assetManager.load("Textures/BackGroundTest32x32.png", Texture.class);
        App.assetManager.load("Textures/BlackBox32x32.png", Texture.class);
    }

    @Override
    public void update(float dt) {
        handleInput();
        progress = MathUtils.lerp(progress, App.assetManager.getProgress(), 0.1f);
        if(App.assetManager.update() && progress >= 0.99999976) {
            if(GameScreenManager.MENU_SCREEN == null) GameScreenManager.MENU_SCREEN = new MENU_SCREEN(gsm);
            else gsm.setGameScreen(GameScreenManager.MENU_SCREEN);
        }
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void show() {
        wait.setPosition(stage.getWidth()/2 - (float) 739/2, stage.getHeight()/2 - 100);
        wait.addAction(alpha(0f));
        wait.addAction(fadeIn(2f));

        clock.setWidth(200);
        clock.setHeight(200);
        clock.setPosition(stage.getWidth()/2 + 250, stage.getHeight()/2 - 150);
        clock.addAction(alpha(0f));
        clock.addAction(fadeIn(4f));
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0,0,0,0);
        shapeRenderer.setProjectionMatrix(cam.combined);
        stage.act();
        stage.draw();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.FIREBRICK);
        shapeRenderer.rect(x, y, progress * width, height);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
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
        shapeRenderer.dispose();
    }
}
