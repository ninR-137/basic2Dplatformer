package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.App;
import com.mygdx.game.Handlers.MyContactListener;
import com.mygdx.game.Handlers.TiledObjectUtil;
import com.mygdx.game.Handlers.WorldManager;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.mygdx.game.App.V_HEIGHT;
import static com.mygdx.game.App.V_WIDTH;

public class PLAY_SCREEN extends GameScreen{

    private Stage stage;
    private Image blackImage;


    private FileHandle file = Gdx.files.local("bin/worldManager.json");
    public static WorldDescriptor worldDescriptor;
    public static boolean MobileControls = false;
    private WorldManager worldManager;
    private OrthographicCamera UICam;

    //-------------------------------------------------------------------------------------------------//
    public static class WorldDescriptor{
        public int Map_Id = 1;
        public int BamboosCollected = 0;
    }


    public void save(WorldDescriptor worldDescriptor){
        Json json = new Json();
        file.writeString(json.toJson(worldDescriptor), false);
    }


    public void load(){
        Json json = new Json();
        try{
            worldDescriptor = json.fromJson(WorldDescriptor.class, file);
        }catch (Exception e) {e.printStackTrace();}
        if(!worldManager.getMap(worldDescriptor.Map_Id).created)
            worldManager.getMap(worldDescriptor.Map_Id).create();
        Darken();
    }


    //-------------------------------------------------------------------------------------------------//

    public PLAY_SCREEN(GameScreenManager gsm) {
        super(gsm);
        UICam = new OrthographicCamera();
        UICam.setToOrtho(false, V_WIDTH, V_HEIGHT);
        //----------------------------------------------------------------------------------//
        stage = new Stage(new StretchViewport(V_WIDTH, V_HEIGHT, UICam));
        blackImage = new Image(App.assetManager.get("Textures/BlackBox32x32.png", Texture.class));
        stage.addActor(blackImage);
        //----------------------------------------------------------------------------------//
        worldManager = new WorldManager(this);
        worldDescriptor = new WorldDescriptor();
    }

    @Override
    public void update(float dt) {
        worldManager.update(dt);
        stage.act();
        if(MyContactListener.MapTransitionOccurred) Darken();
    }

    @Override
    public void handleInput() {
        worldManager.handleInput();
    }

    @Override
    public void show() {
        blackImage.setHeight(720);
        blackImage.setWidth(1480);
        blackImage.addAction(fadeOut(12f));
    }

    public void Darken(){
        blackImage.addAction(alpha(1));
        blackImage.addAction(fadeOut(15f));
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        update(delta);
        worldManager.render();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {stage.getViewport().update(width, height);}

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
        worldManager.dispose();
        TiledObjectUtil.dispose();
    }
}
