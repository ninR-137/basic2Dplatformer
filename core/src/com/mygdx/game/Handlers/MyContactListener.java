package com.mygdx.game.Handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Screens.PLAY_SCREEN;

public class MyContactListener implements ContactListener {
    public static int playerOnLand, playerGroundCollisionUpperBounds, playerGroundCollisionLowerBounds;
    public static boolean MapTransitionOccurred = true;
    public static boolean CollectedBamboo = false;

    //Probably fix this, because each map uses a separate b2d world, not guaranteed to contain the body to destroy
    //Never mind hahhahaha
    public static Array<Body> bodiesToDestroy = new Array<>(); //Queing bodies to be destroyed

    //For Conditional Platforms
    public static Array<Fixture> activationFixtures = new Array<>();
    public static Array<Fixture> deactivationFixtures = new Array<>();

    @Override
    public void beginContact(Contact contact) {
        playerBeginContact(contact);
        checkSpawnCollisions(contact);
        checkConditionalActivation(contact);
        checkDeathCollision(contact);
        checkPickablesCollision(contact);
    }

    @Override
    public void endContact(Contact contact) {
        playerEndContact(contact);
        if(playerOnLand <= 0) {
            checkCollisionDeactivation(contact);
        }
    }

    private void playerBeginContact(Contact contact){
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData() != null && fa.getUserData().equals("Foot") && !fb.isSensor()) playerOnLand++;
        if (fb.getUserData() != null && fb.getUserData().equals("Foot") && !fa.isSensor()) playerOnLand++;
    }

    private void playerEndContact(Contact contact){
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData() != null && fa.getUserData().equals("Foot") && !fb.isSensor()) playerOnLand--;
        if (fb.getUserData() != null && fb.getUserData().equals("Foot") && !fa.isSensor()) playerOnLand--;
    }
    public void checkConditionalActivation(Contact contact){
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if((fa.getUserData() != null && fa.getUserData().equals("ConditionalPlatform"))&& (fb.getUserData() != null && fb.getUserData().equals("Foot"))){
            if(fb.getBody().getLinearVelocity().y < 0 && fa.isSensor()){
                for(Fixture fixture : fa.getBody().getFixtureList()){
                    activationFixtures.add(fixture);
                    playerOnLand++;
                }
            }
        }
        if((fb.getUserData() != null && fb.getUserData().equals("ConditionalPlatform"))&& (fa.getUserData() != null && fa.getUserData().equals("Foot"))){
            if(fa.getBody().getLinearVelocity().y < 0 && fb.isSensor()){
                for(Fixture fixture : fb.getBody().getFixtureList()){
                    activationFixtures.add(fixture);
                    playerOnLand++;
                }
            }
        }
    }
    public void checkCollisionDeactivation(Contact contact){
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if((fa.getUserData() != null && fa.getUserData().equals("ConditionalPlatform"))&& (fb.getUserData() != null && fb.getUserData().equals("Foot"))){
            if(!fa.isSensor()){
                deactivationFixtures.add(fa);
            }
        }
        if((fb.getUserData() != null && fb.getUserData().equals("ConditionalPlatform"))&& (fa.getUserData() != null && fa.getUserData().equals("Foot"))){
            if(!fb.isSensor()){
                deactivationFixtures.add(fb);
            }
        }
    }
    public void checkSpawnCollisions(Contact contact){
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();


        if(fa.getUserData() != null && (fb.getUserData() != null && (fb.getUserData().equals("Player") || fb.getUserData().equals("Foot")))){
            switch (fa.getUserData().toString()){
                case "EndSpawn":
                    PLAY_SCREEN.worldDescriptor.Map_Id++;
                    MapTransitionOccurred = true;
                    break;
                case "BackSpawn":
                    PLAY_SCREEN.worldDescriptor.Map_Id--;
                    MapTransitionOccurred = true;
                    break;
            }
        }
        if(fb.getUserData() != null && (fa.getUserData()!= null && (fa.getUserData().equals("Player") && fa.getUserData().equals("Foot")))) {
            switch (fb.getUserData().toString()) {
                case "EndSpawn":
                    PLAY_SCREEN.worldDescriptor.Map_Id++;
                    MapTransitionOccurred = true;
                    break;
                case "BackSpawn":
                    PLAY_SCREEN.worldDescriptor.Map_Id--;
                    MapTransitionOccurred = true;
                    break;
            }
        }
    }

    private void checkDeathCollision(Contact contact){
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if(fa.getUserData() != null && fa.getUserData().equals("DEATH")){
            bodiesToDestroy.add(fb.getBody());
        }

        if(fb.getUserData() != null && fb.getUserData().equals("DEATH")){
            bodiesToDestroy.add(fa.getBody());
        }
    }

    private void checkPickablesCollision(Contact contact){
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if(fa.getUserData() != null && fa.getUserData().equals("PickableObjects") && (fb.getUserData() != null && (fb.getUserData().equals("Player") || fb.getUserData().equals("Foot")))){
            bodiesToDestroy.add(fa.getBody());
            CollectedBamboo = true;
        }

        if(fb.getUserData() != null && fb.getUserData().equals("PickableObjects") && (fa.getUserData() != null && (fa.getUserData().equals("Player") || fa.getUserData().equals("Foot")))){
            bodiesToDestroy.add(fb.getBody());
            CollectedBamboo = true;
        }

    }
    public static void update(){

        for (Fixture fixture : activationFixtures) {
            fixture.setSensor(false);
            activationFixtures.removeValue(fixture, true);
        }

        for (Fixture fixture : deactivationFixtures) {
            fixture.setSensor(true);
            deactivationFixtures.removeValue(fixture, true);
        }

        if(playerOnLand > playerGroundCollisionUpperBounds) playerOnLand--;
        if(playerOnLand < playerGroundCollisionLowerBounds) playerOnLand++;

        for(Body body: MyContactListener.bodiesToDestroy){
            body.getWorld().destroyBody(body);
            bodiesToDestroy.removeValue(body, true);
            body.setActive(false);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
