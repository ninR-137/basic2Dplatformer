package com.mygdx.game.Controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mygdx.game.App;

public class JoyStick extends Actor {
    private TextureAtlas JoyStickAtlas;
    private TextureRegion InnerJoyStick, OuterJoyStick;

    public boolean released = true;

    //Positions

    private float InnerX, InnerY;
    private final float OuterX, OuterY, OuterRad, InnerRad;
    private double actuatorX = 0, actuatorY = 0;
    private int pointerID;
    private double distance = 0;

    public JoyStick(){
        setTouchable(Touchable.enabled);

        //Sample

        OuterX = App.V_WIDTH * 0.1f;
        OuterY = 125;
        OuterRad = 100;

        InnerX = OuterX;
        InnerY = OuterY;
        InnerRad = OuterRad*0.75f;

        setBounds(OuterX - OuterRad, OuterY - OuterRad, OuterRad*2, OuterRad * 2);
        JoyStickAtlas = App.assetManager.get("UI/JoyStick.atlas", TextureAtlas.class);
        InnerJoyStick = JoyStickAtlas.findRegion("InnerJoystick");
        OuterJoyStick = JoyStickAtlas.findRegion("OuterJoyStick");
        addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pointerID = pointer;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                setActuator(x,y);
                released = false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                resetActuator();

                released = true;
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateInnerJoystickPosition();
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(OuterJoyStick, OuterX - OuterRad, OuterY - OuterRad, OuterRad * 2, OuterRad * 2);
        batch.draw(InnerJoyStick, InnerX - InnerRad, InnerY - InnerRad, InnerRad * 2, InnerRad * 2);

    }



    public void setActuator(float x, float y){

        //What the Actual FUCK!!!!!
        float dx = x  - OuterRad;
        float dy = y - OuterRad;
        distance = Math.sqrt(dx*dx + dy*dy);

        if(distance <= OuterRad){
            actuatorX = dx / OuterRad;
            actuatorY = dy / OuterRad;
        }else {
            actuatorX = dx / distance;
            actuatorY = dy / distance;
        }
    }

    public void resetActuator(){
        actuatorX = 0;
        actuatorY = 0;
    }

    public void updateInnerJoystickPosition(){
        InnerX = (float) (OuterX + actuatorX * OuterRad);
        InnerY = (float) (OuterY + actuatorY * OuterRad);
    }

    public float getOuterRad() {
        return OuterRad;
    }

    public double getDistance() {
        return distance;
    }

    public double getActuatorX() {
        return actuatorX;
    }

    public double getActuatorY() {
        return actuatorY;
    }

}
