package com.mygdx.game.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.App;
import com.mygdx.game.Controls.JoyStick;
import com.mygdx.game.Controls.JumpButton;
import com.mygdx.game.Handlers.MyContactListener;
import com.mygdx.game.Screens.PLAY_SCREEN;

import static com.mygdx.game.Handlers.B2DVars.BIT_BAMBOOS;
import static com.mygdx.game.Handlers.B2DVars.BIT_GROUND;
import static com.mygdx.game.Handlers.B2DVars.BIT_PLAYER;
import static com.mygdx.game.Handlers.B2DVars.PPM;

public class Player extends Character{
    private final StateMachine stateMachine;
    public float spawnX, spawnY;
    protected BodyDef bDef;
    protected PolygonShape shape;
    protected FixtureDef fDef;
    protected Body body;
    private JoyStick joyStick;
    private JumpButton jumpButton;
    private float jumpForce = 160f;
    //User Controls
    //......

    public Player(World world, JoyStick joyStick, JumpButton jumpButton) {
        super(world);
        stateMachine = new StateMachine();
        this.joyStick = joyStick;
        this.jumpButton = jumpButton;
        width = 60;
        height = 60;
        /*
        position.x = spawnX;
        position.y = spawnY;
        createChara();

         */
    }

    //this has to be seperate to give space to set up spawn
    @Override
    public void createChara() {
        position.x = spawnX;
        position.y = spawnY;
        bDef = new BodyDef();
        shape = new PolygonShape();
        fDef = new FixtureDef();

        bDef.type = BodyDef.BodyType.DynamicBody;
        bDef.position.set(position);

        body = world.createBody(bDef);

        shape.setAsBox(width * 0.45f /PPM, height* 0.75f /PPM, new Vector2(0, -height * 0.25f /PPM), 0);
        fDef.shape = shape;
        fDef.isSensor = true;
        body.createFixture(fDef).setUserData("Foot");

        shape.setAsBox(width/2/PPM,height/2/PPM);
        fDef.shape = shape;
        //fDef.restitution = 0.5f;
        fDef.friction = 0.1f;
        fDef.isSensor = false;
        fDef.filter.categoryBits = BIT_PLAYER;
        fDef.filter.maskBits = (short) (BIT_GROUND | BIT_BAMBOOS);
        body.createFixture(fDef).setUserData("Player");

        //Foot Sensor Test


        shape.dispose();
    }

    @Override
    public void update() {
        position.x = body.getPosition().x;
        position.y = body.getPosition().y;
        playerControls();
        stateMachine.update(this);
    }

    private void playerControls(){
        if(!PLAY_SCREEN.MobileControls) {
            int velocityX = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                velocityX += 1;
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                velocityX -= 1;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && (MyContactListener.playerOnLand > 0)) {
                body.applyForceToCenter(0, jumpForce*2, true);
            }

            body.setLinearVelocity(velocityX * 2f, body.getLinearVelocity().y);
        } else {
            double velocityX = joyStick.getActuatorX();

            body.setLinearVelocity((float) velocityX * 2.5f, body.getLinearVelocity().y);
            if (jumpButton.isTouched && MyContactListener.playerOnLand > 0) {
                body.applyForceToCenter(0, jumpForce, true);
                jumpButton.isTouched = false;
            }
        }

    }

    @Override
    public void render(Batch batch) {
        if(stateMachine.state != null){
            switch (stateMachine.state){
                case IDLE:
                    batch.draw(stateMachine.currentIdleFrames, body.getPosition().x * PPM - width / 2, body.getPosition().y * PPM - height / 2, width, height);
                    break;
                case JUMPING:
                    batch.draw(stateMachine.currentJumpingFrames, body.getPosition().x * PPM - width / 2, body.getPosition().y * PPM - height / 2, width, height);
                    break;
                case FALLING:
                    batch.draw(stateMachine.currentFallingFrames, body.getPosition().x * PPM - width / 2, body.getPosition().y * PPM - height / 2, width, height);
                    break;
                case RUNNING:
                    batch.draw(stateMachine.currentRunningAnim, body.getPosition().x * PPM - width / 2, body.getPosition().y * PPM - height / 2, width, height);
                    break;
                case FLIPPED_IDLE:
                    batch.draw(stateMachine.currentFlippedIdleFrames, body.getPosition().x * PPM - width / 2, body.getPosition().y * PPM - height / 2, width, height);
                    break;
                case FLIPPED_JUMPING:
                    batch.draw(stateMachine.currentFlippedJumpingFrames, body.getPosition().x * PPM - width / 2, body.getPosition().y * PPM - height / 2, width, height);
                    break;
                case FLIPPED_FALLING:
                    batch.draw(stateMachine.currentFlippedFallingFrames, body.getPosition().x * PPM - width / 2, body.getPosition().y * PPM - height / 2, width, height);
                    break;
                case FLIPPED_RUNNING:
                    batch.draw(stateMachine.currentFlippedRunningAnim, body.getPosition().x * PPM - width / 2, body.getPosition().y * PPM - height / 2, width, height);
                    break;
            }
        }
    }

    //------------------------------------------------------//
    public float getHorizontalVelocity() {return body.getLinearVelocity().x;}
    public float getVerticalVelocity() {return body.getLinearVelocity().y;}
    public float getPositionX(){return body.getPosition().x * PPM;}
    public float getPositionY(){return body.getPosition().y * PPM;}
    public boolean isPlayerDead(){return stateMachine.state == State.movementState.DEATH;}
    public void createNewPlayer(){
        position.x = spawnX;
        position.y = spawnY;
        createChara();
    }
    public void setSpawnX(float spawnX){this.spawnX = spawnX;}
    public void setSpawnY(float spawnY){this.spawnY = spawnY;}
    public void setJumpForce(float jumpForce) {this.jumpForce = jumpForce;}
    //-------------------------------------------------------//

    private class StateMachine extends State{
        private final Animation<TextureRegion> IdleAnim, FlippedIdleAnim;
        private final Animation<TextureRegion> JumpingPoseAnim, FlippedJumpingPoseAnim;
        private final Animation<TextureRegion> ZebraFallingPoseAnim, ZebraFlippedFallingPoseAnim;
        private final Animation<TextureRegion> RunningAnim, FlippedRunningAnim;
        public TextureRegion currentIdleFrames, currentFlippedIdleFrames;
        public TextureRegion currentJumpingFrames, currentFlippedJumpingFrames;
        public TextureRegion currentFallingFrames, currentFlippedFallingFrames;
        public TextureRegion currentRunningAnim, currentFlippedRunningAnim;
        public movementState state;
        public float stateTime;

        public StateMachine(){
            state = movementState.IDLE;
            IdleAnim = new Animation<TextureRegion>(1/20f, createFlippedAnimFrames(App.assetManager.get("CharaAnim/ZebraIdle.png", Texture.class), 2, 6));
            FlippedIdleAnim =  new Animation<TextureRegion>(1/20f, createAnimFrames(App.assetManager.get("CharaAnim/ZebraIdle.png", Texture.class), 2, 6));
            JumpingPoseAnim = new Animation<TextureRegion>(1/20f, createFlippedAnimFrames(App.assetManager.get("CharaAnim/ZebraJumpingAnim.png",Texture.class), 2, 6));
            FlippedJumpingPoseAnim = new Animation<TextureRegion>(1/20f, createAnimFrames(App.assetManager.get("CharaAnim/ZebraJumpingAnim.png", Texture.class),2, 6));
            ZebraFallingPoseAnim = new Animation<TextureRegion>(1/20f, createFlippedAnimFrames(App.assetManager.get("CharaAnim/ZebraFallingAnim.png", Texture.class), 2, 6));
            ZebraFlippedFallingPoseAnim = new Animation<TextureRegion>(1/20f, createAnimFrames(App.assetManager.get("CharaAnim/ZebraFallingAnim.png", Texture.class), 2, 6));
            RunningAnim = new Animation<TextureRegion>(1/46f, createFlippedAnimFrames(App.assetManager.get("CharaAnim/ZebraRunningAnim.png", Texture.class),2, 6));
            FlippedRunningAnim = new Animation<TextureRegion>(1/46f, createAnimFrames(App.assetManager.get("CharaAnim/ZebraRunningAnim.png", Texture.class),2,6));
        }

        @Override
        public movementState handleInput(Vector2 input) {
            if(input.x < 0) flipped = true;
            else if(input.x > 0) flipped = false;

            if(flipped){
                if (MyContactListener.playerOnLand == 0) {
                    if (input.y > 0) return movementState.FLIPPED_JUMPING;
                    if (input.y <= 0) return movementState.FLIPPED_FALLING;
                } else {
                    if (input.x >= -0.5f) return movementState.FLIPPED_IDLE;
                    if (input.x < -0.5f) return movementState.FLIPPED_RUNNING;
                }
            } else {
                if (MyContactListener.playerOnLand == 0) {
                    if (input.y > 0) return movementState.JUMPING;
                    if (input.y <= 0) return movementState.FALLING;
                } else {
                    if (input.x <= 0.5) return movementState.IDLE;
                    if (input.x >= 0.5) return movementState.RUNNING;
                }
            }

            return null;
        }

        @Override
        public void update(Character character) {
            if(!body.isActive()){
                state = movementState.DEATH;
            }
            else {
                state = handleInput(body.getLinearVelocity());
            }

            updateAnimations();
        }

        //-------------------------------------------Animations---------------------------------------------//

        private void updateAnimations(){
            stateTime += Gdx.graphics.getDeltaTime();

            //----------------------------//
            currentIdleFrames = IdleAnim.getKeyFrame(stateTime, true);
            currentFlippedIdleFrames = FlippedIdleAnim.getKeyFrame(stateTime, true);
            currentJumpingFrames = JumpingPoseAnim.getKeyFrame(stateTime, true);
            currentFlippedJumpingFrames = FlippedJumpingPoseAnim.getKeyFrame(stateTime, true);
            currentFallingFrames = ZebraFallingPoseAnim.getKeyFrame(stateTime, true);
            currentFlippedFallingFrames = ZebraFlippedFallingPoseAnim.getKeyFrame(stateTime, true);
            currentRunningAnim = RunningAnim.getKeyFrame(stateTime, true);
            currentFlippedRunningAnim = FlippedRunningAnim.getKeyFrame(stateTime, true);
        }

        public TextureRegion[] createAnimFrames(Texture animSheet, int FRAME_COL, int FRAME_ROW){

            TextureRegion[][] tmp = TextureRegion.split(animSheet, animSheet.getWidth()/FRAME_COL, animSheet.getHeight()/FRAME_ROW);

            int index = 0;

            TextureRegion[] animFrames = new TextureRegion[FRAME_COL * FRAME_ROW];
            for (int i = 0; i < FRAME_ROW; i++){
                for(int j = 0; j < FRAME_COL; j++){
                    animFrames[index++] = tmp[i][j];
                }
            }


            return animFrames;
        }

        public TextureRegion[] createFlippedAnimFrames(Texture animSheet, int FRAME_COL, int FRAME_ROW){

            TextureRegion[][] tmp = TextureRegion.split(animSheet, animSheet.getWidth()/FRAME_COL, animSheet.getHeight()/FRAME_ROW);


            int index = 0;

            TextureRegion[] animFrames = new TextureRegion[FRAME_COL * FRAME_ROW];
            for (int i = 0; i < FRAME_ROW; i++){
                for(int j = 0; j < FRAME_COL; j++){
                    animFrames[index++] = tmp[i][j];
                }
            }

            for (TextureRegion region : animFrames) {
                region.flip(true, false);
            }

            return animFrames;
        }
    }
}
