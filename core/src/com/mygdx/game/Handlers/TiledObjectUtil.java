package com.mygdx.game.Handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.App;
import com.mygdx.game.Entities.Player;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import static com.mygdx.game.Handlers.B2DVars.BIT_GROUND;
import static com.mygdx.game.Handlers.B2DVars.PPM;

public class TiledObjectUtil {
    private static Array<PointLight> pointLights = new Array<>();
    public static float playerSpawnX, playerSpawnY;
    public static void parseCollisionObjectLayer(World world, MapObjects objects){

        Body body;
        BodyDef bDef = new BodyDef();
        FixtureDef fDef = new FixtureDef();
        bDef.type = BodyDef.BodyType.StaticBody;

        for(MapObject object : objects){
            Shape shape = createCollisionShape(object);

            if(shape != null) { //Just the shapes are not of ChainShape or PolygonShape
                body = world.createBody(bDef);
                fDef.shape = shape;
                fDef.friction = 0.25f;
                fDef.filter.categoryBits = BIT_GROUND;
                body.createFixture(fDef);
                shape.dispose();
            }
        }
    }


    public static void parseDeathCollision(World world, MapObjects objects){

        Body body;
        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        for(MapObject object : objects){
            if(object instanceof RectangleMapObject){
                PolygonShape shape = new PolygonShape();
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                bodyDef.position.set(rect.x/PPM + rect.width/2/PPM, rect.y/PPM + rect.height/2/PPM);
                body = world.createBody(bodyDef);
                shape.setAsBox(rect.width/2/PPM, rect.height/2/PPM);
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;
                body.createFixture(fixtureDef).setUserData("DEATH");
            }
        }
;    }

    public static void parseConditionalCollision(World world, MapObjects objects){
        Body body;
        BodyDef bDef = new BodyDef();
        FixtureDef fDef = new FixtureDef();
        bDef.type = BodyDef.BodyType.StaticBody;

        for(MapObject object : objects){
            Shape shape = createCollisionShape(object);

            body = world.createBody(bDef);
            fDef.shape = shape;
            fDef.friction = 0.25f;
            fDef.filter.categoryBits = BIT_GROUND;
            fDef.isSensor = true;
            body.createFixture(fDef).setUserData("ConditionalPlatform");
            shape.dispose();
        }
    }

    private static Shape createCollisionShape(Object object){
        if(object instanceof PolylineMapObject) return createPolyLine((PolylineMapObject) object);
        if(object instanceof PolygonMapObject) return createPolygon((PolygonMapObject) object);
        return null;
    }
    public static ChainShape createPolyLine(PolylineMapObject polylineMapObject){
        float[] vertices = polylineMapObject.getPolyline().getTransformedVertices();
        Vector2[] worldVertices = new Vector2[vertices.length/2];

        for(int i = 0; i < worldVertices.length; i++){
            worldVertices[i] = new Vector2(vertices[i*2]/ PPM, vertices[i*2 + 1]/ PPM);
        }
        ChainShape cs = new ChainShape();
        cs.createChain(worldVertices);
        return cs;
    }

    public static ChainShape createPolygon(PolygonMapObject polygonMapObject){
        float[] vertices = polygonMapObject.getPolygon().getTransformedVertices();
        Vector2[] worldVertices = new Vector2[vertices.length/2];

        for(int i = 0; i < worldVertices.length; i ++) {
            worldVertices[i] = new Vector2(vertices[i*2]/PPM, vertices[i*2 + 1]/PPM);
        }

        ChainShape cs = new ChainShape();
        cs.createChain(worldVertices);
        return cs;
    }

    public static void parseSpawnObjectLayer(World world,MapObjects objects){

        Body body;
        BodyDef bDef = new BodyDef();
        bDef.type = BodyDef.BodyType.StaticBody;
        FixtureDef fDef = new FixtureDef();

        for(MapObject object : objects){
            if(!(object instanceof RectangleMapObject)) continue;
            PolygonShape shape = new PolygonShape();
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            switch (object.getName()){
                case "Spawn":
                    //Player.spawnX = ((RectangleMapObject) object).getRectangle().x/ PPM + ((RectangleMapObject) object).getRectangle().width/2/PPM;
                    //Player.spawnY = ((RectangleMapObject) object).getRectangle().y/ PPM + ((RectangleMapObject) object).getRectangle().height/2/PPM;
                    playerSpawnX = ((RectangleMapObject) object).getRectangle().x/ PPM + ((RectangleMapObject) object).getRectangle().width/2/PPM;
                    playerSpawnY = ((RectangleMapObject) object).getRectangle().y/ PPM + ((RectangleMapObject) object).getRectangle().height/2/PPM;

                    break;
                case "EndSpawn":
                    shape.setAsBox(rectangle.width/2/PPM, rectangle.height/2/PPM,
                            new Vector2(rectangle.x/PPM + rectangle.getWidth()/PPM/2,
                                    rectangle.y/PPM + rectangle.height/2/PPM), 0);
                    fDef.shape = shape;
                    fDef.isSensor = true;
                    body = world.createBody(bDef);
                    body.createFixture(fDef).setUserData("EndSpawn");
                    break;
                case "BackSpawn":
                    shape.setAsBox(rectangle.width/2/PPM, rectangle.height/2/PPM,
                            new Vector2(rectangle.x/PPM + rectangle.getWidth()/PPM/2,
                                    rectangle.y/PPM + rectangle.height/2/PPM), 0);
                    fDef.shape = shape;
                    fDef.isSensor = true;
                    body = world.createBody(bDef);
                    body.createFixture(fDef).setUserData("BackSpawn");
                    break;
            }
        }
    }

    public static void createLights(MapObjects objects, RayHandler rayHandler,int Intensity, float dist, float softness, Color color){
        for (MapObject object : objects){
            if(object instanceof RectangleMapObject){
                float positionX = ((RectangleMapObject) object).getRectangle().x / PPM + (((RectangleMapObject) object).getRectangle().width/2) / PPM;
                float positionY = ((RectangleMapObject) object).getRectangle().y / PPM + (((RectangleMapObject) object).getRectangle().height/2) / PPM;
                createPointLight(rayHandler, Intensity, color, dist, positionX, positionY, softness);
            }
        }
    }
    public static void createPointLight(RayHandler rayHandler, int Intensity, Color color, float dist, float x, float y, float softness){
        PointLight pl = new PointLight(rayHandler, Intensity, color, dist, x ,y);
        pl.setSoftnessLength(softness);
        pl.setSoft(true);
        //pl.attachToBody(body);
        pl.setXray(false);

        pointLights.add(pl);
    }

    public static class ParticleEffectMapLocation{
        public Array<ParticleEffectPool.PooledEffect> effects;
        ParticleEffect particleEffect;
        public ParticleEffectMapLocation(){effects = new Array<>();}

        public void parseParticleLocation(MapObjects objects, String StringPath, String ImgDir){

            //Pool's template particle
            particleEffect = new ParticleEffect();
            particleEffect.load(Gdx.files.internal(StringPath), Gdx.files.internal(ImgDir));
            //Creating the effect:
            ParticleEffectPool purpleBurstPool = new ParticleEffectPool(particleEffect, 1, 100);

            for(MapObject object: objects){
                if(object instanceof RectangleMapObject){
                    Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                    ParticleEffectPool.PooledEffect effect = purpleBurstPool.obtain();
                    effect.setPosition(rectangle.x, rectangle.y);
                    effects.add(effect);
                }
            }
        }

        public void renderParticles(SpriteBatch batch){
            for(ParticleEffectPool.PooledEffect effect : effects){
                effect.draw(batch, Gdx.graphics.getDeltaTime());
                if(effect.isComplete()){
                    effect.free();
                    effects.removeValue(effect, true);
                }
            }
        }

        public void dispose(){
            for(ParticleEffect effect: effects){
                effect.dispose();
            }

            particleEffect.dispose();
        }
    }

    public static void dispose(){
        for(PointLight pl : pointLights){
            pl.dispose();
        }
    }



    //--------------------------------------------------------------------------------------------------------------------//

    public static class PickableObjects{
        private Animation<TextureRegion> animation;
        private TextureRegion frames;
        private float x, y, width, height;
        private float stateTime;
        private Body body;

        public PickableObjects(float x, float y,float width,float height, Body body){
            this.body = body;
            animation = new Animation<TextureRegion>(1/20f, createAnimFrames(App.assetManager.get("CharaAnim/BambooSpin.png", Texture.class), 6, 3));
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void render(Batch batch){
            stateTime += Gdx.graphics.getDeltaTime();
            frames = animation.getKeyFrame(stateTime, true);
            batch.draw(frames, x, y, width, height);
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

        public Body getBody() {
            return body;
        }
    }

    public static class PickableObjectsManager{
        public Array<PickableObjects> pickableObjects = new Array<>();
        private World world;
        private MapObjects objects;
        private Body body;
        private Array<MapObject> cObjects;
        public PickableObjectsManager(World world, MapObjects objects){
            this.world = world;
            this.objects = objects;
            cObjects = new Array<>();
            for(MapObject object : objects) {
                if(object != null) cObjects.add(object);
            }
        }

        public void createObjects(){
            BodyDef bodyDef = new BodyDef();
            FixtureDef fixtureDef = new FixtureDef();


            for(MapObject object : cObjects){
                if(object instanceof RectangleMapObject){
                    PolygonShape shape = new PolygonShape();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    bodyDef.position.set(rect.x/PPM + rect.width/2/PPM, rect.y/PPM + rect.height/2/PPM);
                    body = world.createBody(bodyDef);
                    shape.setAsBox(rect.width/2/PPM, rect.height/2/PPM);
                    fixtureDef.shape = shape;
                    fixtureDef.isSensor = true;
                    fixtureDef.filter.categoryBits = B2DVars.BIT_BAMBOOS;
                    fixtureDef.filter.maskBits = B2DVars.BIT_PLAYER;
                    body.createFixture(fixtureDef).setUserData("PickableObjects");

                    PickableObjects po = new PickableObjects(rect.x, rect.y, rect.width, rect.height, body);
                    pickableObjects.add(po);
                    shape.dispose();
                }
            }


        }


        public void renderObjects(Batch batch){
            for(PickableObjects objects : pickableObjects){
                if(objects.getBody().isActive()) {
                    objects.render(batch);
                }
                else {
                    pickableObjects.removeValue(objects, true);
                }
            }
        }


        public void reset(){
            for (PickableObjects objects : pickableObjects) {
                MyContactListener.bodiesToDestroy.add(objects.getBody());
                pickableObjects.removeValue(objects, true);
                cObjects = new Array<>();
            }
        }
    }


}
