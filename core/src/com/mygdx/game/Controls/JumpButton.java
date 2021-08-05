package com.mygdx.game.Controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mygdx.game.App;

public class JumpButton extends Actor {
    public float CenterX, CenterY, Radius;
    private TextureRegion ButtonNotPressed, ButtonIsPressed;
    public boolean isTouched;
    private boolean ButtonDisplayIsTouched;
    public JumpButton(){
        setTouchable(Touchable.enabled);
        //--------------------------------------------------------------------------------------------------------//
        TextureAtlas textureAtlas = App.assetManager.get("UI/JumpButton.atlas", TextureAtlas.class);
        ButtonNotPressed = textureAtlas.findRegion("NJumpButton");
        ButtonIsPressed = textureAtlas.findRegion("NJumpButtonPressed");
        //--------------------------------------------------------------------------------------------------------//
        CenterX  = App.V_WIDTH * 0.9f;
        CenterY = 125;
        Radius = 100;
        setBounds(CenterX - Radius, CenterY - Radius, Radius*2, Radius*2);

        addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                isTouched = true;
                ButtonDisplayIsTouched = true;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                isTouched = false;
                ButtonDisplayIsTouched = false;
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if(ButtonDisplayIsTouched) batch.draw(ButtonIsPressed, CenterX - Radius, CenterY - Radius, Radius*2, Radius*2);
        else batch.draw(ButtonNotPressed, CenterX - Radius, CenterY - Radius, Radius*2, Radius*2);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}

