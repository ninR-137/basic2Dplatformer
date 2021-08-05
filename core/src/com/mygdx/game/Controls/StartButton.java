package com.mygdx.game.Controls;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mygdx.game.App;

public class StartButton extends Actor {
    private float width, height, x, y;
    private TextureRegion StartButton, StartButtonPressed;
    public boolean isTouched;
    private boolean ButtonDisplayIsTouched;

    public  StartButton(){
        TextureAtlas textureAtlas = App.assetManager.get("UI/StartButton.atlas", TextureAtlas.class);
        StartButton = new TextureRegion(textureAtlas.findRegion("TouchToStart"));
        StartButtonPressed = new TextureRegion(textureAtlas.findRegion("TouchedToStart"));
        width = StartButton.getRegionWidth();
        height = StartButton.getRegionHeight();
        x = App.V_WIDTH/2f - width/2;
        y = App.V_HEIGHT/2f - height/2;
        setTouchable(Touchable.enabled);
        setBounds(x, y, width, height);

        addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                isTouched = true;
                ButtonDisplayIsTouched = true;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                ButtonDisplayIsTouched = false;
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if(ButtonDisplayIsTouched) batch.draw(StartButtonPressed, x, y, width, height);
        else batch.draw(StartButton, x, y, width, height);
    }

    public boolean getDisplayButtonIsTouched(){
        return ButtonDisplayIsTouched;
    }
}
