package com.mygdx.game.Handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.App;
import com.mygdx.game.Controls.JoyStick;
import com.mygdx.game.Controls.JumpButton;
import com.mygdx.game.Entities.Player;
import com.mygdx.game.Screens.PLAY_SCREEN;

import javax.xml.soap.Text;

import box2dLight.RayHandler;

public class WorldManager {
    private int MAP_ID;
    //-------------------------------------------------------------------------------------//
    protected OrthographicCamera cam, foregroundCam, backgroundCam, B2DCam;
    protected Box2DDebugRenderer B2DR;
    protected SpriteBatch batch, foregroundBatch, backgroundBatch;
    protected CameraManager graphicsCameraManager;
    protected CameraManager B2DDebugCamera;
    //-------------------------------------------------------------------------------------//
    private final Maps Map1 = new Maps() {
        //World
        private World world;
        private TiledMap tiledMap;
        private Array<TiledMapImageLayer> Ground;

        //Entities
        private Player player;

        //Particles and Lights
        private RayHandler rayHandler;
        private TiledObjectUtil.ParticleEffectMapLocation particleEffectMapLocation;

        //Parallax Backgrounds and Foregrounds
        private Array<TextureRegion> parallaxBackgrounds;
        private TextureRegion borderColor;
        private TextureRegion foregroundMap;
        private CameraManager foregroundCamManager;
        private float BackgroundOffset1, BackgroundOffset2, BackgroundOffset3, BackgroundOffset4, BackgroundOffset5;

        //Hud and UI
        private Stage HudStage;
        private JoyStick joyStick;
        private JumpButton jumpButton;

        //Pickable Objects
        private TiledObjectUtil.PickableObjectsManager pickableObjectsManager;
        private boolean bambooReset = false, secondBatch = false;

        @Override
        public void create() {
            //--------------TiledMap setup-------------------//
            tiledMap = new TmxMapLoader().load("Maps/Map1/Map1.tmx");
            Ground = new Array<>();
            Ground.add((TiledMapImageLayer)tiledMap.getLayers().get("Ground"));
            borderColor = new TextureRegion(App.assetManager.get("Textures/BlackBox32x32.png", Texture.class));
            //--------------Box 2D world setup---------------//
            world = new World(new Vector2(0, -12f), true);
            world.setContactListener(new MyContactListener());
            TiledObjectUtil.parseSpawnObjectLayer(world, tiledMap.getLayers().get("Spawn").getObjects());
            TiledObjectUtil.parseCollisionObjectLayer(world, tiledMap.getLayers().get("CollisionLayer").getObjects());
            //temporary
            TiledObjectUtil.parseConditionalCollision(world, tiledMap.getLayers().get("ConditionalCollision").getObjects());
            TiledObjectUtil.parseDeathCollision(world, tiledMap.getLayers().get("DeathCollision").getObjects());
            //CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            //-----------HudStage and Controls setup---------//
            HudStage = new Stage(new StretchViewport(App.V_WIDTH, App.V_HEIGHT));
            joyStick = new JoyStick();
            jumpButton = new JumpButton();
            if(PLAY_SCREEN.MobileControls) {
                HudStage.addActor(joyStick);
                HudStage.addActor(jumpButton);
            }
            //------Entities and Camera Manager setup--------//
            player = new Player(world, joyStick, jumpButton);
            player.setSpawnX(TiledObjectUtil.playerSpawnX);
            player.setSpawnY(TiledObjectUtil.playerSpawnY);
            player.createChara();
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            graphicsCameraManager = new CameraManager(cam, player);
            B2DDebugCamera = new CameraManager(B2DCam, player);
            foregroundCamManager = new CameraManager(foregroundCam, player);
            foregroundCamManager.setLerpfactor(0.1f);
            //--------RayHandler and Box2d Lights------------//
            rayHandler = new RayHandler(world);
            rayHandler.setAmbientLight(0.9f);
            TiledObjectUtil.createLights(tiledMap.getLayers().get("Lights").getObjects(), rayHandler, 1000, 100, 15f, Color.CORAL);
            //---Parallax Backgrounds and Foreground Maps----//
            foregroundMap = new TextureRegion(App.assetManager.get("Maps/Map1/ForegroundMap.png", Texture.class));
            parallaxBackgrounds = new Array<>();
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map1/Backgrounds/MainBackground.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map1/Backgrounds/Background1.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map1/Backgrounds/Background2.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map1/Backgrounds/Background3.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map1/Backgrounds/Background4.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map1/Backgrounds/Background5.png", Texture.class)));
            //-----------Particle Effects and Pooling--------//
            particleEffectMapLocation = new TiledObjectUtil.ParticleEffectMapLocation();
            particleEffectMapLocation.parseParticleLocation(tiledMap.getLayers().get("ParticleEmitters").getObjects(),
                    "Particles/RedBurst.p", "Textures");

            //--------------Pickable Objects-----------------//
            pickableObjectsManager = new TiledObjectUtil.PickableObjectsManager(world, tiledMap.getLayers().get("Pickables").getObjects());
            pickableObjectsManager.createObjects();
            //--------------Camera Positioning---------------//
            cam.position.set(player.getPositionX(), player.getPositionY(), 0);
            foregroundCam.position.set(player.getPositionX(), player.getPositionY(), 0);
            cam.update();
            foregroundCam.update();

            created = true;
        }

        @Override
        public void render() {
            //Batch Setup
            batch.setProjectionMatrix(cam.combined);
            backgroundBatch.setProjectionMatrix(backgroundCam.combined);
            foregroundBatch.setProjectionMatrix(foregroundCam.combined);

            if( - BackgroundOffset1 * 0.1f < - App.V_WIDTH || - BackgroundOffset1 * 0.1f > App.V_WIDTH) BackgroundOffset1 = 0;
            if( - BackgroundOffset2 * 0.15f < - App.V_WIDTH || - BackgroundOffset2 * 0.15f > App.V_WIDTH) BackgroundOffset2 = 0;
            if( - BackgroundOffset3 * 0.2f < - App.V_WIDTH || - BackgroundOffset3 * 0.2f > App.V_WIDTH) BackgroundOffset3 = 0;
            if( - BackgroundOffset4 * 0.25f < - App.V_WIDTH || - BackgroundOffset4 * 0.25f > App.V_WIDTH) BackgroundOffset4 = 0;
            if( - BackgroundOffset5 * 0.3f < - App.V_WIDTH || - BackgroundOffset5 * 0.3f > App.V_WIDTH) BackgroundOffset5 = 0;


            //Backgrounds
            backgroundBatch.begin();
            backgroundBatch.draw(parallaxBackgrounds.get(0), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            drawParallaxBackGround(parallaxBackgrounds.get(1), 0.1f, BackgroundOffset1);
            drawParallaxBackGround(parallaxBackgrounds.get(2), 0.15f, BackgroundOffset2);
            drawParallaxBackGround(parallaxBackgrounds.get(3), 0.2f, BackgroundOffset3);
            drawParallaxBackGround(parallaxBackgrounds.get(4), 0.25f, BackgroundOffset4);
            drawParallaxBackGround(parallaxBackgrounds.get(5), 0.3f, BackgroundOffset5);
            backgroundBatch.end();

            //Main(Ground, Border, Entities, Particles)
            batch.begin();
            for(TiledMapImageLayer imageLayer : Ground) {
                batch.draw(imageLayer.getTextureRegion(), imageLayer.getX(), imageLayer.getY());
            }

            //drawBorder(batch);

            player.render(batch);
            pickableObjectsManager.renderObjects(batch);
            particleEffectMapLocation.renderParticles(batch);

            batch.end();

            //Lights
            rayHandler.updateAndRender();
            rayHandler.setCombinedMatrix(B2DCam);

            //Foregrounds

            foregroundBatch.begin();
            foregroundBatch.draw(foregroundMap, 0, 0);
            drawBorder(foregroundBatch);
            foregroundBatch.end();


            //Hud Stage
            HudStage.draw();
            //B2DR.render(world, B2DCam.combined);


        }

        @Override
        public void update(float dt) {
            //World Step
            world.step(dt, 6, 2);
            MyContactListener.update();
            checkPlayerDeath();

            //Parallax calculations
            BackgroundOffset1 += graphicsCameraManager.getDx();
            BackgroundOffset2 += graphicsCameraManager.getDx();
            BackgroundOffset3 += graphicsCameraManager.getDx();
            BackgroundOffset4 += graphicsCameraManager.getDx();
            BackgroundOffset5 += graphicsCameraManager.getDx();
            MyContactListener.playerGroundCollisionUpperBounds = 3;
            MyContactListener.playerGroundCollisionLowerBounds = 0;
            //Entities calculations
            player.update();
            //Camera Manager Calculations
            graphicsCameraManager.updateCameraRelativeToMap(false);
            B2DDebugCamera.updateCameraRelativeToMap(true);
            foregroundCamManager.updateCameraRelativeToMap(false);
            //Hud Stage update

            HudStage.act(Gdx.graphics.getDeltaTime());
            Gdx.input.setInputProcessor(HudStage);


            if(MyContactListener.CollectedBamboo){
                bamboosCollected++;
                MyContactListener.CollectedBamboo = false;
            }
        }

        private void checkPlayerDeath(){
            if(player.isPlayerDead()){
                player.createNewPlayer();
            }
        }

        @Override
        public void mapTransition() {
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            //graphicsCameraManager = new CameraManager(cam, player);
            graphicsCameraManager.setPlayer(player);
            graphicsCameraManager.setCamera(cam);
            //B2DDebugCamera = new CameraManager(B2DCam, player);
            B2DDebugCamera.setCamera(B2DCam);
            B2DDebugCamera.setPlayer(player);
            //foregroundCamManager = new CameraManager(foregroundCam, player);
            foregroundCamManager.setPlayer(player);
            foregroundCamManager.setCamera(foregroundCam);
            foregroundCamManager.setLerpfactor(0.1f);
        }

        @Override
        public void dispose() {
            HudStage.dispose();
            world.dispose();
            tiledMap.dispose();
            particleEffectMapLocation.dispose();
            for(TiledMapImageLayer imageLayer : Ground){
                imageLayer.getTextureRegion().getTexture().dispose();
            }
        }

        public void drawBorder(Batch batch){
            batch.draw(borderColor, Ground.get(0).getX() - 5920, Ground.get(0).getY(), 5920, 2160);
            batch.draw(borderColor, Ground.get(0).getX() + 5920 - 20, Ground.get(0).getY(), 5920, 2160);
            //------------------------------------------------------------------------------------------------------//
            batch.draw(borderColor, Ground.get(0).getX(), Ground.get(0).getY() + 2160 - 20, 5920, 2160);
            batch.draw(borderColor, Ground.get(0).getX(), Ground.get(0).getY() - 2160, 5920, 2160);
            //-----------------------------------------------Corner-------------------------------------------------//
            batch.draw(borderColor, Ground.get(0).getX() - 5920, Ground.get(0).getY() + 2160 - 20, 5920, 2160);
            batch.draw(borderColor, Ground.get(0).getX() + 5920, Ground.get(0).getY() + 2160 - 20, 5920, 2160);
            batch.draw(borderColor, Ground.get(0).getX() - 5920, Ground.get(0).getY() - 2160, 5920, 2160);
            batch.draw(borderColor, Ground.get(0).getX() + 5920, Ground.get(0).getY() - 2160, 5920, 2160);
        }
    };
    private final Maps Map2 = new Maps() {
        //World
        private World world;
        private TiledMap tiledMap;
        private Array<TiledMapImageLayer> Ground;

        //Entities
        private Player player;

        //Particles and Lights
        private RayHandler rayHandler;
        private TiledObjectUtil.ParticleEffectMapLocation particleEffectMapLocation;

        //Parallax Backgrounds and Foregrounds
        private Array<TextureRegion> parallaxBackgrounds;
        private TextureRegion borderColor;
        private TextureRegion foregroundMap;
        private CameraManager foregroundCamManager;
        private float BackgroundOffset1, BackgroundOffset2, BackgroundOffset3, BackgroundOffset4;

        //Hud and UI
        private Stage HudStage;
        private JoyStick joyStick;
        private JumpButton jumpButton;

        //Pickable Objects
        private TiledObjectUtil.PickableObjectsManager pickableObjectsManager;
        @Override
        public void create() {
            //-----------------TiledMap setup----------------//
            tiledMap = new TmxMapLoader().load("Maps/Map2/Map2.tmx");
            Ground = new Array<>();
            Ground.add((TiledMapImageLayer) tiledMap.getLayers().get("Ground"));
            borderColor = new TextureRegion(App.assetManager.get("Textures/BlackBox32x32.png", Texture.class));

            //--------------Box 2D world setup---------------//
            world = new World(new Vector2(0, -12f), true);
            world.setContactListener(new MyContactListener());
            TiledObjectUtil.parseSpawnObjectLayer(world, tiledMap.getLayers().get("Spawn").getObjects());
            TiledObjectUtil.parseCollisionObjectLayer(world, tiledMap.getLayers().get("CollisionLayer").getObjects());

            TiledObjectUtil.parseConditionalCollision(world, tiledMap.getLayers().get("ConditionalCollision").getObjects());
            TiledObjectUtil.parseDeathCollision(world, tiledMap.getLayers().get("DeathCollision").getObjects());
            //CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());

            //-----------HudStage and Controls setup---------//
            HudStage = new Stage(new StretchViewport(App.V_WIDTH, App.V_HEIGHT));
            joyStick = new JoyStick();
            jumpButton = new JumpButton();
            if(PLAY_SCREEN.MobileControls) {
                HudStage.addActor(joyStick);
                HudStage.addActor(jumpButton);
            }
            //------Entities and Camera Manager setup--------//
            player = new Player(world, joyStick, jumpButton);
            player.setSpawnX(TiledObjectUtil.playerSpawnX);
            player.setSpawnY(TiledObjectUtil.playerSpawnY);
            player.createChara();
            //No need to setup Graphics Manager and B2DGraphicsCamera
            //foregroundCamManager = new CameraManager(foregroundCam, player);
            //foregroundCamManager.setLerpfactor(0.1f);
            //--------RayHandler and Box2d Lights------------//
            rayHandler = new RayHandler(world);
            rayHandler.setAmbientLight(0.9f);
            TiledObjectUtil.createLights(tiledMap.getLayers().get("Lights").getObjects(), rayHandler, 50, 100, 3f, Color.NAVY);

            //---Parallax Backgrounds and Foreground Maps----//
            foregroundMap = new TextureRegion(App.assetManager.get("Maps/Map2/ForeGroundMap.png", Texture.class));
            parallaxBackgrounds = new Array<>();
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map2/Backgrounds/Mainbackground.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map2/Backgrounds/Background1.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map2/Backgrounds/Background2.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map2/Backgrounds/Background3.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map2/Backgrounds/Background4.png", Texture.class)));
            //-----------Particle Effects and Pooling--------//
            particleEffectMapLocation = new TiledObjectUtil.ParticleEffectMapLocation();
            particleEffectMapLocation.parseParticleLocation(tiledMap.getLayers().get("ParticleEmitters").getObjects(),
                    "Particles/RedBurst.p", "Textures");
            //--------------Pickable Objects-----------------//
            pickableObjectsManager = new TiledObjectUtil.PickableObjectsManager(world, tiledMap.getLayers().get("Pickables").getObjects());
            pickableObjectsManager.createObjects();
            //--------------Camera Positioning---------------//

            created = true;
        }

        @Override
        public void render() {
            //Batch Set-Up
            batch.setProjectionMatrix(cam.combined);
            backgroundBatch.setProjectionMatrix(backgroundCam.combined);
            foregroundBatch.setProjectionMatrix(foregroundCam.combined);

            //Backgrounds
            backgroundBatch.begin();
            backgroundBatch.draw(parallaxBackgrounds.get(0), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            drawParallaxBackGround(parallaxBackgrounds.get(1), 0.1f, BackgroundOffset1);
            drawParallaxBackGround(parallaxBackgrounds.get(2), 0.15f, BackgroundOffset2);
            drawParallaxBackGround(parallaxBackgrounds.get(3), 0.2f, BackgroundOffset3);
            drawParallaxBackGround(parallaxBackgrounds.get(4), 0.25f, BackgroundOffset4);
            backgroundBatch.end();

            //Main(Ground, Border, Entities, Particles)
            batch.begin();
            for(TiledMapImageLayer imageLayer : Ground) {
                batch.draw(imageLayer.getTextureRegion(), imageLayer.getX(), imageLayer.getY());
            }

            //drawBorder(batch);

            player.render(batch);
            MyContactListener.playerGroundCollisionUpperBounds = 3;
            MyContactListener.playerGroundCollisionLowerBounds = 0;
            particleEffectMapLocation.renderParticles(batch);
            //pickableObjects.render(batch);
            pickableObjectsManager.renderObjects(batch);

            drawBorder(batch);
            batch.end();

            //Lights
            rayHandler.updateAndRender();
            rayHandler.setCombinedMatrix(B2DCam);

            //Foregrounds

            foregroundBatch.begin();
            foregroundBatch.draw(foregroundMap, 0, 0);
            drawBorder(foregroundBatch);
            foregroundBatch.end();


            //Hud Stage
            HudStage.draw();
            //B2DR.render(world, B2DCam.combined);
        }

        @Override
        public void update(float dt) {
            //World Step
            //Temporary fix
            //mapTransition();
            updateCamera();
            world.step(dt, 6, 2);
            MyContactListener.update();
            checkPlayerDeath();

            //Parallax calculations
            BackgroundOffset1 += graphicsCameraManager.getDx();
            BackgroundOffset2 += graphicsCameraManager.getDx();
            BackgroundOffset3 += graphicsCameraManager.getDx();
            BackgroundOffset4 += graphicsCameraManager.getDx();

            if( - BackgroundOffset1 * 0.1f < - App.V_WIDTH || - BackgroundOffset1 * 0.1f > App.V_WIDTH) BackgroundOffset1 = 0;
            if( - BackgroundOffset2 * 0.15f < - App.V_WIDTH || - BackgroundOffset2 * 0.15f > App.V_WIDTH) BackgroundOffset2 = 0;
            if( - BackgroundOffset3 * 0.2f < - App.V_WIDTH || - BackgroundOffset3 * 0.2f > App.V_WIDTH) BackgroundOffset3 = 0;
            if( - BackgroundOffset4 * 0.25f < - App.V_WIDTH || - BackgroundOffset4 * 0.25f > App.V_WIDTH) BackgroundOffset4 = 0;
            //Entities calculations
            player.update();
            //Camera Manager Calculations
            graphicsCameraManager.updateCameraRelativeToMap(false);
            B2DDebugCamera.updateCameraRelativeToMap(true);
            foregroundCamManager.updateCameraRelativeToMap(false);
            //Hud Stage update
            HudStage.act(Gdx.graphics.getDeltaTime());
            Gdx.input.setInputProcessor(HudStage);

            if(MyContactListener.CollectedBamboo){
                bamboosCollected++;
                MyContactListener.CollectedBamboo = false;
            }
        }

        private void checkPlayerDeath(){
            if(player.isPlayerDead()){
                /*
                if(Gdx.input.isTouched()){
                    player.createNewPlayer();
                }
                */
                player.createNewPlayer();
            }
        }


        //Have to do it Every update now? Just why?
        @Override
        public void mapTransition() {
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            graphicsCameraManager = new CameraManager(cam, player);
            B2DDebugCamera = new CameraManager(B2DCam, player);
            foregroundCamManager = new CameraManager(foregroundCam, player);
            foregroundCamManager.setLerpfactor(0.1f);

        }

        private void updateCamera(){
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            graphicsCameraManager.setCamera(cam);
            graphicsCameraManager.setPlayer(player);
            B2DDebugCamera.setCamera(B2DCam);
            B2DDebugCamera.setPlayer(player);
            foregroundCamManager.setPlayer(player);
            foregroundCamManager.setCamera(foregroundCam);

            foregroundCamManager.setLerpfactor(0.1f);
        }



        @Override
        public void dispose() {
            HudStage.dispose();
            world.dispose();
            tiledMap.dispose();
            particleEffectMapLocation.dispose();
            for(TiledMapImageLayer imageLayer : Ground){
                imageLayer.getTextureRegion().getTexture().dispose();
            }
        }

        public void drawBorder(Batch batch){
            batch.draw(borderColor, Ground.get(0).getX() - 5920, Ground.get(0).getY(), 5920, 4320);
            batch.draw(borderColor, Ground.get(0).getX() + 5920 - 20, Ground.get(0).getY(), 5920, 4320);
            //------------------------------------------------------------------------------------------------------//
            batch.draw(borderColor, Ground.get(0).getX(), Ground.get(0).getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.get(0).getX(), Ground.get(0).getY() - 4320, 5920, 4320);
            //-----------------------------------------------Corner-------------------------------------------------//
            batch.draw(borderColor, Ground.get(0).getX() - 5920, Ground.get(0).getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.get(0).getX() + 5920, Ground.get(0).getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.get(0).getX() - 5920, Ground.get(0).getY() - 4320, 5920, 4320);
            batch.draw(borderColor, Ground.get(0).getX() + 5920, Ground.get(0).getY() - 4320, 5920, 4320);
        }
    };
    private final Maps Map3 = new Maps() {
        //World
        private World world;
        private TiledMap tiledMap;
        private TiledMapImageLayer Ground;

        //Entities
        private Player player;

        //Particles and Lights
        private RayHandler rayHandler;
        private TiledObjectUtil.ParticleEffectMapLocation particleEffectMapLocation;

        //Parallax Backgrounds and Foregrounds
        private Array<TextureRegion> parallaxBackgrounds;
        private TextureRegion borderColor;
        private TextureRegion foregroundMap;
        private CameraManager foregroundCamManager; //Best to keep it local
        private float BackgroundOffset1, BackgroundOffset2, BackgroundOffset3, BackgroundOffset4, BackgroundOffset5;

        //Hud and UI
        private Stage HudStage;
        private JoyStick joyStick;
        private JumpButton jumpButton;

        //Pickable Objects
        private TiledObjectUtil.PickableObjectsManager pickableObjectsManager;

        @Override
        public void create() {
            //---------------TiledMap setup----------------------//
            tiledMap = new TmxMapLoader().load("Maps/Map3/Map3.tmx");
            Ground = (TiledMapImageLayer) tiledMap.getLayers().get("Ground");
            borderColor = new TextureRegion(App.assetManager.get("Textures/BlackBox32x32.png", Texture.class));

            //---------------Box 2D world setup------------------//
            world = new World(new Vector2(0, -12f), true);
            world.setContactListener(new MyContactListener());
            TiledObjectUtil.parseSpawnObjectLayer(world, tiledMap.getLayers().get("Spawn").getObjects());
            TiledObjectUtil.parseCollisionObjectLayer(world, tiledMap.getLayers().get("CollisionLayer").getObjects());

            TiledObjectUtil.parseDeathCollision(world, tiledMap.getLayers().get("DeathCollision").getObjects());
            //CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());

            //-----------HudStage and Controls setup---------//
            HudStage = new Stage(new StretchViewport(App.V_WIDTH, App.V_HEIGHT));
            joyStick = new JoyStick();
            jumpButton = new JumpButton();
            if(PLAY_SCREEN.MobileControls) {
                HudStage.addActor(joyStick);
                HudStage.addActor(jumpButton);
            }

            //------Entities and Camera Manager setup--------//
            player = new Player(world, joyStick, jumpButton);
            player.setSpawnX(TiledObjectUtil.playerSpawnX);
            player.setSpawnY(TiledObjectUtil.playerSpawnY);
            player.createChara();
            //No need to setup Graphics Manager and B2DGraphicsCamera
            //foregroundCamManager = new CameraManager(foregroundCam, player);
            //foregroundCamManager.setLerpfactor(0.1f);

            //--------RayHandler and Box2d Lights------------//
            rayHandler = new RayHandler(world);
            rayHandler.setAmbientLight(0.9f);
            TiledObjectUtil.createLights(tiledMap.getLayers().get("OutLight").getObjects(), rayHandler, 100, 120, 4f, Color.BLUE);
            TiledObjectUtil.createLights(tiledMap.getLayers().get("InLight").getObjects(), rayHandler, 80, 120, 4f, Color.RED);
            //prolly have an InLight as well (Need to Set it Up)

            //---Parallax Backgrounds and Foreground Maps----//
            foregroundMap = new TextureRegion(App.assetManager.get("Maps/Map3/ForegroundMap.png", Texture.class));
            parallaxBackgrounds = new Array<>();
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map3/Parallax/Background.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map3/Parallax/1.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map3/Parallax/2.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map3/Parallax/3.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map3/Parallax/4.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map3/Parallax/5.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map3/Parallax/12.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map3/Parallax/345.png", Texture.class)));
            //-----------Particle Effects and Pooling--------//
            particleEffectMapLocation = new TiledObjectUtil.ParticleEffectMapLocation();
            particleEffectMapLocation.parseParticleLocation(tiledMap.getLayers().get("ParticleEmitters").getObjects(),
                    "Particles/RedBurst.p", "Textures");
            //--------------Pickable Objects-----------------//
            pickableObjectsManager = new TiledObjectUtil.PickableObjectsManager(world, tiledMap.getLayers().get("Pickables").getObjects());
            pickableObjectsManager.createObjects();

            created = true;
        }

        @Override
        public void render() {
            //Batch Set-Up
            batch.setProjectionMatrix(cam.combined);
            backgroundBatch.setProjectionMatrix(backgroundCam.combined);
            foregroundBatch.setProjectionMatrix(foregroundCam.combined);


            //Backgrounds
            backgroundBatch.begin();
            backgroundBatch.draw(parallaxBackgrounds.get(0), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            drawParallaxBackGround(parallaxBackgrounds.get(1), 0.1f, BackgroundOffset1);
            drawParallaxBackGround(parallaxBackgrounds.get(2), 0.15f, BackgroundOffset2);
            backgroundBatch.draw(parallaxBackgrounds.get(6), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            drawParallaxBackGround(parallaxBackgrounds.get(3), 0.2f, BackgroundOffset3);
            drawParallaxBackGround(parallaxBackgrounds.get(4), 0.25f, BackgroundOffset4);
            drawParallaxBackGround(parallaxBackgrounds.get(5), 0.3f, BackgroundOffset5);
            backgroundBatch.draw(parallaxBackgrounds.get(7), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            backgroundBatch.end();

            //Main(Ground, Border, Entities, Particles)

            batch.begin();
            /*
            for(TiledMapImageLayer imageLayer : Ground) {
                batch.draw(imageLayer.getTextureRegion(), imageLayer.getX(), imageLayer.getY());
            }

            */
            batch.draw(Ground.getTextureRegion(), Ground.getX(), Ground.getY());

            //drawBorder(batch);

            player.render(batch);
            MyContactListener.playerGroundCollisionUpperBounds = 3;
            MyContactListener.playerGroundCollisionLowerBounds = 0;
            particleEffectMapLocation.renderParticles(batch);
            //pickableObjects.render(batch);
            pickableObjectsManager.renderObjects(batch);

            drawBorder(batch);
            batch.end();

            //Lights
            rayHandler.updateAndRender();
            rayHandler.setCombinedMatrix(B2DCam);

            //Foregrounds

            foregroundBatch.begin();
            foregroundBatch.draw(foregroundMap, 0, 0);
            drawBorder(foregroundBatch);
            foregroundBatch.end();

            //Hud Stage
            HudStage.draw();
            //B2DR.render(world, B2DCam.combined);

        }

        @Override
        public void update(float dt) {
            //World Step
            //Temporary fix
            //mapTransition();
            updateCamera();
            world.step(dt, 6, 2);
            MyContactListener.update();
            checkPlayerDeath();

            //Parallax calculations
            BackgroundOffset1 += graphicsCameraManager.getDx();
            BackgroundOffset2 += graphicsCameraManager.getDx();
            BackgroundOffset3 += graphicsCameraManager.getDx();
            BackgroundOffset4 += graphicsCameraManager.getDx();
            BackgroundOffset5 += graphicsCameraManager.getDx();

            if( - BackgroundOffset1 * 0.1f < - App.V_WIDTH || - BackgroundOffset1 * 0.1f > App.V_WIDTH) BackgroundOffset1 = 0;
            if( - BackgroundOffset2 * 0.15f < - App.V_WIDTH || - BackgroundOffset2 * 0.15f > App.V_WIDTH) BackgroundOffset2 = 0;
            if( - BackgroundOffset3 * 0.2f < - App.V_WIDTH || - BackgroundOffset3 * 0.2f > App.V_WIDTH) BackgroundOffset3 = 0;
            if( - BackgroundOffset4 * 0.25f < - App.V_WIDTH || - BackgroundOffset4 * 0.25f > App.V_WIDTH) BackgroundOffset4 = 0;
            if( - BackgroundOffset4 * 0.3f < - App.V_WIDTH || - BackgroundOffset4 * 0.3f > App.V_WIDTH) BackgroundOffset5 = 0;
            //Entities calculations
            player.update();
            MyContactListener.playerGroundCollisionLowerBounds = 0;
            MyContactListener.playerGroundCollisionUpperBounds = 3;
            //Camera Manager Calculations
            graphicsCameraManager.updateCameraRelativeToMap(false);
            B2DDebugCamera.updateCameraRelativeToMap(true);
            foregroundCamManager.updateCameraRelativeToMap(false);
            //Hud Stage update
            HudStage.act(Gdx.graphics.getDeltaTime());
            Gdx.input.setInputProcessor(HudStage);

            if(MyContactListener.CollectedBamboo){
                bamboosCollected++;
                MyContactListener.CollectedBamboo = false;
            }
        }

        private void checkPlayerDeath(){
            if(player.isPlayerDead()){
                /*
                if(Gdx.input.isTouched()){
                    player.createNewPlayer();
                }
                */
                player.createNewPlayer();
            }
        }

        private void updateCamera(){
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            graphicsCameraManager.setCamera(cam);
            graphicsCameraManager.setPlayer(player);
            B2DDebugCamera.setCamera(B2DCam);
            B2DDebugCamera.setPlayer(player);
            foregroundCamManager.setPlayer(player);
            foregroundCamManager.setCamera(foregroundCam);

            foregroundCamManager.setLerpfactor(0.1f);
        }


        @Override
        public void dispose() {
            HudStage.dispose();
            world.dispose();
            tiledMap.dispose();
            particleEffectMapLocation.dispose();
            Ground.getTextureRegion().getTexture().dispose();
        }

        @Override
        public void mapTransition() {
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            graphicsCameraManager = new CameraManager(cam, player);
            B2DDebugCamera = new CameraManager(B2DCam, player);
            foregroundCamManager = new CameraManager(foregroundCam, player);
        }

        public void drawBorder(Batch batch){
            batch.draw(borderColor, Ground.getX() - 5920, Ground.getY(), 5920, 4320);
            batch.draw(borderColor, Ground.getX() + 5920 - 20, Ground.getY(), 5920, 4320);
            //------------------------------------------------------------------------------------------------------//
            batch.draw(borderColor, Ground.getX(), Ground.getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.getX(), Ground.getY() - 4320, 5920, 4320);
            //-----------------------------------------------Corner-------------------------------------------------//
            batch.draw(borderColor, Ground.getX() - 5920, Ground.getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.getX() + 5920, Ground.getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.getX() - 5920, Ground.getY() - 4320, 5920, 4320);
            batch.draw(borderColor, Ground.getX() + 5920, Ground.getY() - 4320, 5920, 4320);
        }

    };
    private final Maps Map4 = new Maps() {
        //World
        private World world;
        private TiledMap tiledMap;
        private TiledMapImageLayer Ground;

        //Entities
        private Player player;

        //Particles and Lights
        private RayHandler rayHandler;
        private TiledObjectUtil.ParticleEffectMapLocation particleEffectMapLocation;

        //Parallax Backgrounds and Foregrounds
        private Array<TextureRegion> parallaxBackgrounds;
        private TextureRegion borderColor;
        private TextureRegion foregroundMap;
        private CameraManager foregroundCamManager;
        private float BackgroundOffset1, BackgroundOffset2, BackgroundOffset3;

        //Hud and UI
        private Stage HudStage;
        private JoyStick joyStick;
        private JumpButton jumpButton;

        //Pickable Objects
        private TiledObjectUtil.PickableObjectsManager pickableObjectsManager;

        @Override
        public void create() {
            //------------------------TiledMap setup-----------------------//
            tiledMap = new TmxMapLoader().load("Maps/Map4/Map4.tmx");
            Ground = (TiledMapImageLayer)tiledMap.getLayers().get("Ground");
            borderColor = new TextureRegion(App.assetManager.get("Textures/BlackBox32x32.png", Texture.class));
            //--------------Box 2D world setup---------------//
            world = new World(new Vector2(0, -12f), true);
            world.setContactListener(new MyContactListener());
            TiledObjectUtil.parseSpawnObjectLayer(world, tiledMap.getLayers().get("Spawn").getObjects());
            TiledObjectUtil.parseCollisionObjectLayer(world, tiledMap.getLayers().get("CollisionLayer").getObjects());
            //Important to not have other shapes other than chainshapes

            //TiledObjectUtil.parseConditionalCollision(world, tiledMap.getLayers().get("").getObjects());
            TiledObjectUtil.parseDeathCollision(world, tiledMap.getLayers().get("DeathCollision").getObjects());

            //-----------HudStage and Controls setup---------//
            HudStage = new Stage(new StretchViewport(App.V_WIDTH, App.V_HEIGHT));
            joyStick = new JoyStick();
            jumpButton = new JumpButton();
            // 8/06/2021 change (forgot to do check for mobile controls)
            if(PLAY_SCREEN.MobileControls){
                HudStage.addActor(joyStick);
                HudStage.addActor(jumpButton);
            }

            //------Entities and Camera Manager setup--------//
            player = new Player(world, joyStick, jumpButton);
            player.setSpawnX(TiledObjectUtil.playerSpawnX);
            player.setSpawnY(TiledObjectUtil.playerSpawnY);
            player.createChara();
            player.setJumpForce(190f);

            //No need to setup Graphics Manager and B2DGraphicsCamera
            //foregroundCamManager = new CameraManager(foregroundCam, player);
            //foregroundCamManager.setLerpfactor(0.1f);
            //--------RayHandler and Box2d Lights------------//
            rayHandler = new RayHandler(world);
            rayHandler.setAmbientLight(0.9f);
            TiledObjectUtil.createLights(tiledMap.getLayers().get("Lights").getObjects(), rayHandler, 50, 100, 3f, Color.NAVY);
            TiledObjectUtil.createLights(tiledMap.getLayers().get("RedLight").getObjects(), rayHandler, 100, 100, 6f, Color.FIREBRICK);
            //---Parallax Backgrounds and Foreground Maps----//
            foregroundMap = new TextureRegion(App.assetManager.get("Maps/Map2/ForeGroundMap.png", Texture.class));
            parallaxBackgrounds = new Array<>();
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map4/Parallax/Background.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map4/Parallax/1.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map4/Parallax/2.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map4/Parallax/3.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map4/Parallax/Mist.png", Texture.class)));
            //-----------Particle Effects and Pooling--------//
            particleEffectMapLocation = new TiledObjectUtil.ParticleEffectMapLocation();
            particleEffectMapLocation.parseParticleLocation(tiledMap.getLayers().get("ParticleEmitters").getObjects(),
                    "Particles/RedBurst.p", "Textures");
            //--------------Pickable Objects-----------------//
            pickableObjectsManager = new TiledObjectUtil.PickableObjectsManager(world, tiledMap.getLayers().get("Pickables").getObjects());
            pickableObjectsManager.createObjects();

            created = true;
        }

        @Override
        public void render() {
            //Batch Set-Up
            batch.setProjectionMatrix(cam.combined);
            backgroundBatch.setProjectionMatrix(backgroundCam.combined);
            foregroundBatch.setProjectionMatrix(foregroundCam.combined);

            //Backgrounds
            backgroundBatch.begin();
            backgroundBatch.draw(parallaxBackgrounds.get(0), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            drawParallaxBackGround(parallaxBackgrounds.get(1), 0.1f, BackgroundOffset1);
            drawParallaxBackGround(parallaxBackgrounds.get(2), 0.15f, BackgroundOffset2);
            drawParallaxBackGround(parallaxBackgrounds.get(3), 0.2f, BackgroundOffset3);
            backgroundBatch.draw(parallaxBackgrounds.get(4), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            backgroundBatch.end();

            //Main(Ground, Border, Entities, Particles)
            batch.begin();
            batch.draw(Ground.getTextureRegion(), Ground.getX(), Ground.getY());
            player.render(batch);
            MyContactListener.playerGroundCollisionUpperBounds = 3;
            MyContactListener.playerGroundCollisionLowerBounds = 0;
            particleEffectMapLocation.renderParticles(batch);
            //pickableObjects.render(batch);
            pickableObjectsManager.renderObjects(batch);
            drawBorder(batch);
            batch.end();

            //Lights
            rayHandler.updateAndRender();
            rayHandler.setCombinedMatrix(B2DCam);

            //Foregrounds
            /*
            foregroundBatch.begin();
            foregroundBatch.draw(foregroundMap, 0, 0);
            drawBorder(foregroundBatch);
            foregroundBatch.end();
            */
            //Hud Stage
            HudStage.draw();
            //B2DR.render(world, B2DCam.combined);
        }

        @Override
        public void update(float dt) {
            //World Step
            //Temporary fix
            //mapTransition();
            updateCamera();
            world.step(dt, 6, 2);
            MyContactListener.update();
            checkPlayerDeath();

            BackgroundOffset1 += graphicsCameraManager.getDx();
            BackgroundOffset2 += graphicsCameraManager.getDx();
            BackgroundOffset3 += graphicsCameraManager.getDx();

            if( - BackgroundOffset1 * 0.1f < - App.V_WIDTH || - BackgroundOffset1 * 0.1f > App.V_WIDTH) BackgroundOffset1 = 0;
            if( - BackgroundOffset2 * 0.15f < - App.V_WIDTH || - BackgroundOffset2 * 0.15f > App.V_WIDTH) BackgroundOffset2 = 0;
            if( - BackgroundOffset3 * 0.2f < - App.V_WIDTH || - BackgroundOffset3 * 0.2f > App.V_WIDTH) BackgroundOffset3 = 0;

            //Entities calculations
            player.update();

            //Camera Manager Calculations
            graphicsCameraManager.updateCameraRelativeToMap(false);
            B2DDebugCamera.updateCameraRelativeToMap(true);
            foregroundCamManager.updateCameraRelativeToMap(false);
            //Hud Stage update
            HudStage.act(Gdx.graphics.getDeltaTime());
            Gdx.input.setInputProcessor(HudStage);

            if(MyContactListener.CollectedBamboo){
                bamboosCollected++;
                MyContactListener.CollectedBamboo = false;
            }

        }

        private void checkPlayerDeath(){
            if(player.isPlayerDead()){
                player.createNewPlayer();
            }
        }

        private void updateCamera(){
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            graphicsCameraManager.setCamera(cam);
            graphicsCameraManager.setPlayer(player);
            B2DDebugCamera.setCamera(B2DCam);
            B2DDebugCamera.setPlayer(player);
            foregroundCamManager.setPlayer(player);
            foregroundCamManager.setCamera(foregroundCam);

            foregroundCamManager.setLerpfactor(0.1f);
        }


        @Override
        public void dispose() {
            HudStage.dispose();
            world.dispose();
            tiledMap.dispose();
            particleEffectMapLocation.dispose();
            Ground.getTextureRegion().getTexture().dispose();
        }

        @Override
        public void mapTransition() {
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            graphicsCameraManager = new CameraManager(cam, player);
            B2DDebugCamera = new CameraManager(B2DCam, player);
            foregroundCamManager = new CameraManager(foregroundCam, player);
        }


        public void drawBorder(Batch batch){
            batch.draw(borderColor, Ground.getX() - 5920, Ground.getY(), 5920, 4320);
            batch.draw(borderColor, Ground.getX() + 5920 - 20, Ground.getY(), 5920, 4320);
            //------------------------------------------------------------------------------------------------------//
            batch.draw(borderColor, Ground.getX(), Ground.getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.getX(), Ground.getY() - 4320, 5920, 4320);
            //-----------------------------------------------Corner-------------------------------------------------//
            batch.draw(borderColor, Ground.getX() - 5920, Ground.getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.getX() + 5920, Ground.getY() + 4320 - 20, 5920, 4320);
            batch.draw(borderColor, Ground.getX() - 5920, Ground.getY() - 4320, 5920, 4320);
            batch.draw(borderColor, Ground.getX() + 5920, Ground.getY() - 4320, 5920, 4320);
        }
    };
    private final Maps Map5 = new Maps() {
        //World
        private World world;
        private TiledMap tiledMap;
        private TiledMapImageLayer Ground;

        //Entities
        private Player player;

        //Particles and Lights
        private RayHandler rayHandler;
        private TiledObjectUtil.ParticleEffectMapLocation particleEffectMapLocation;

        //Parallax Backgrounds and Foregrounds
        private Array<TextureRegion> parallaxBackgrounds;
        private TextureRegion borderColor;
        private float BackgroundOffset1, BackgroundOffset2;

        //Hud and UI
        private Stage HudStage;
        private JoyStick joyStick;
        private JumpButton jumpButton;

        @Override
        public void create() {
            //-----------------TiledMap setup----------------//
            tiledMap = new TmxMapLoader().load("Maps/Map5/untitled.tmx");
            Ground = (TiledMapImageLayer) tiledMap.getLayers().get("Ground");
            borderColor = new TextureRegion(App.assetManager.get("Textures/BlackBox32x32.png", Texture.class));
            //--------------Box 2D world setup---------------//
            world = new World(new Vector2(0, -12f), true);
            world.setContactListener(new MyContactListener());
            TiledObjectUtil.parseSpawnObjectLayer(world, tiledMap.getLayers().get("Spawn").getObjects());
            TiledObjectUtil.parseCollisionObjectLayer(world, tiledMap.getLayers().get("Collision").getObjects());

            //-----------HudStage and Controls setup---------//
            HudStage = new Stage(new StretchViewport(App.V_WIDTH, App.V_HEIGHT));
            joyStick = new JoyStick();
            jumpButton = new JumpButton();
            if(PLAY_SCREEN.MobileControls) {
                HudStage.addActor(joyStick);
                HudStage.addActor(jumpButton);
            }

            //------Entities and Camera Manager setup--------//
            player = new Player(world, joyStick, jumpButton);
            player.setSpawnX(TiledObjectUtil.playerSpawnX);
            player.setSpawnY(TiledObjectUtil.playerSpawnY);
            player.createChara();
            //--------RayHandler and Box2d Lights------------//
            rayHandler = new RayHandler(world);
            rayHandler.setAmbientLight(0.9f);
            TiledObjectUtil.createLights(tiledMap.getLayers().get("Lights").getObjects(), rayHandler, 50, 100, 3f, Color.NAVY);
            //---Parallax Backgrounds and Foreground Maps----//
            parallaxBackgrounds = new Array<>();
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map5/Backgrounds/Background.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map5/Backgrounds/Parallax1.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map5/Backgrounds/TransparentBackground1.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map5/Backgrounds/Parallax2.png", Texture.class)));
            parallaxBackgrounds.add(new TextureRegion(App.assetManager.get("Maps/Map5/Backgrounds/TransparantBackground2.png", Texture.class)));
            //-----------Particle Effects and Pooling--------//
            particleEffectMapLocation = new TiledObjectUtil.ParticleEffectMapLocation();
            particleEffectMapLocation.parseParticleLocation(tiledMap.getLayers().get("Particles").getObjects(),
                    "Particles/RedBurst.p", "Textures");

            created = true;
        }

        @Override
        public void render() {
            //Batch Set-Up
            batch.setProjectionMatrix(cam.combined);
            backgroundBatch.setProjectionMatrix(backgroundCam.combined);
            //Backgrounds
            backgroundBatch.begin();
            backgroundBatch.draw(parallaxBackgrounds.get(0), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            drawParallaxBackGround(parallaxBackgrounds.get(1), 0.1f, BackgroundOffset1);
            backgroundBatch.draw(parallaxBackgrounds.get(2), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            drawParallaxBackGround(parallaxBackgrounds.get(3), 0.15f, BackgroundOffset2);
            backgroundBatch.draw(parallaxBackgrounds.get(4), 0,0, backgroundCam.viewportWidth, backgroundCam.viewportHeight);
            backgroundBatch.end();

            batch.begin();
            batch.draw(Ground.getTextureRegion().getTexture(),Ground.getX(), Ground.getY());

            //drawBorder(batch);

            player.render(batch);
            MyContactListener.playerGroundCollisionUpperBounds = 3;
            MyContactListener.playerGroundCollisionLowerBounds = 0;
            drawBorder(batch);

            App.font.getData().setScale(1);
            App.font.draw(batch, "Hey You Did It!!", player.getPositionX() - 50, player.getPositionY() + 50);
            for(MapObject object: tiledMap.getLayers().get("TextLayer").getObjects()){
                if(object instanceof RectangleMapObject){
                    Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                    if(object.getName().equals("Finished")){
                        App.font.getData().setScale(3);
                        App.font.draw(batch,"Thanks for Playing", rectangle.x, rectangle.y, rectangle.width, 1, false);
                    }
                    if(object.getName().equals("Bamboo")){
                        App.font.getData().setScale(2);
                        App.font.draw(batch,"Bamboos Collected: " + PLAY_SCREEN.worldDescriptor.BamboosCollected, rectangle.x, rectangle.y, rectangle.width, 1, false);
                    }
                }
            }
            batch.end();


            //Lights
            rayHandler.updateAndRender();
            rayHandler.setCombinedMatrix(B2DCam);



            //Hud Stage
            HudStage.draw();
            //B2DR.render(world, B2DCam.combined);
        }

        @Override
        public void update(float dt) {
            //World Step
            //Temporary fix
            //mapTransition();
            updateCamera();
            world.step(dt, 6, 2);
            MyContactListener.update();
            checkPlayerDeath();

            //Parallax calculations
            BackgroundOffset1 += graphicsCameraManager.getDx();
            BackgroundOffset2 += graphicsCameraManager.getDx();

            if( - BackgroundOffset1 * 0.1f < - App.V_WIDTH || - BackgroundOffset1 * 0.1f > App.V_WIDTH) BackgroundOffset1 = 0;
            if( - BackgroundOffset2 * 0.15f < - App.V_WIDTH || - BackgroundOffset2 * 0.15f > App.V_WIDTH) BackgroundOffset2 = 0;

            //Entities calculations
            player.update();

            //Camera Manager Calculations
            graphicsCameraManager.updateCameraRelativeToMap(false);
            B2DDebugCamera.updateCameraRelativeToMap(true);

            //Hud Stage update
            HudStage.act(Gdx.graphics.getDeltaTime());
            Gdx.input.setInputProcessor(HudStage);

        }

        private void updateCamera(){
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            graphicsCameraManager.setElevatedView(true);
            graphicsCameraManager.setCamera(cam);
            graphicsCameraManager.setPlayer(player);
            B2DDebugCamera.setElevatedView(true);
            B2DDebugCamera.setCamera(B2DCam);
            B2DDebugCamera.setPlayer(player);
        }

        private void checkPlayerDeath(){
            if(player.isPlayerDead()){
                player.createNewPlayer();
            }
        }

        @Override
        public void dispose() {
            Ground.getTextureRegion().getTexture().dispose();
            tiledMap.dispose();
            world.dispose();
            HudStage.dispose();
        }

        @Override
        public void mapTransition() {
            CameraManager.mapUpdated(tiledMap.getLayers().get("Camera").getObjects());
            /*
            graphicsCameraManager.setCamera(cam);
            graphicsCameraManager.setPlayer(player);
            B2DDebugCamera.setPlayer(player);
            B2DDebugCamera.setCamera(B2DCam);

            */
            graphicsCameraManager = new CameraManager(cam, player);
            B2DDebugCamera = new CameraManager(B2DCam, player);
        }

        public void drawBorder(Batch batch){
            batch.draw(borderColor, Ground.getX() - 1480, Ground.getY(), 1480, 1098);
            batch.draw(borderColor, Ground.getX() + 1480, Ground.getY(), 1480, 1098);
            //------------------------------------------------------------------------------------------------------//
            batch.draw(borderColor, Ground.getX(), Ground.getY() + 1098, 1480, 1098);
            batch.draw(borderColor, Ground.getX(), Ground.getY() - 1098, 1480, 1098);
            //-----------------------------------------------Corner-------------------------------------------------//
            batch.draw(borderColor, Ground.getX() - 1480, Ground.getY() + 1098, 1480, 1098);
            batch.draw(borderColor, Ground.getX() + 1480, Ground.getY() + 1098, 1480, 1098);
            batch.draw(borderColor, Ground.getX() - 1480, Ground.getY() - 1098, 1480, 1098);
            batch.draw(borderColor, Ground.getX() + 1480, Ground.getY() - 1098, 1480, 1098);
        }

    };

    //---------------------------------------------------------------------------------------------------------------------//
    public WorldManager(PLAY_SCREEN play_screen){
        cam = play_screen.gsm.app.getCam();
        foregroundCam = play_screen.gsm.app.getForegroundCam();
        backgroundCam = play_screen.gsm.app.getBackGroundCam();
        B2DCam = play_screen.gsm.app.getB2DCam();
        B2DR = play_screen.gsm.app.getB2DR();
        batch = play_screen.gsm.app.getBatch();
        foregroundBatch = play_screen.gsm.app.getForegroundBatch();
        backgroundBatch = play_screen.gsm.app.getBackGroundBatch();

        Map1.create();
        Map2.create();
        Map3.create();
        Map4.create();
        Map5.create();
    }

    public void render(){
        if(getMap(MAP_ID) != null)getMap(MAP_ID).render();
    }

    private void checkMapTransition(){
        if(MyContactListener.MapTransitionOccurred){
            if(getMap(MAP_ID) != null) getMap(MAP_ID).mapTransition();
            if(MAP_ID != 1){
                PLAY_SCREEN.worldDescriptor.BamboosCollected += getMap(MAP_ID - 1).bamboosCollected;

                System.out.println("Transitioned" + " >> Last Map : " +  (MAP_ID - 1) + " >> Bamboos Collected : " +
                        PLAY_SCREEN.worldDescriptor.BamboosCollected);

            }
            MyContactListener.MapTransitionOccurred = false;
        }
    }

    public void update(float dt){
        if(PLAY_SCREEN.worldDescriptor != null) MAP_ID = PLAY_SCREEN.worldDescriptor.Map_Id;
        checkMapTransition();
        if(getMap(MAP_ID) != null) getMap(MAP_ID).update(dt);
    }


    public void handleInput() {

    }

    public void dispose(){
        Map1.dispose();
        Map2.dispose();
        Map3.dispose();
        Map4.dispose();
        Map5.dispose();
    }

    public Maps getMap(int MAP_ID){
        switch (MAP_ID){
            case 1: return Map1;
            case 2: return Map2;
            case 3: return Map3;
            case 4: return Map4;
            case 5: return Map5;
            default: return null;
        }
    }

    public abstract class Maps{

        public boolean created = false;
        protected int bamboosCollected = 0;

        public abstract void create();

        public abstract void render();

        public abstract void update(float dt);

        public abstract void dispose();

        public abstract void mapTransition();



        /**
         * @param lerpFactor to decide how the speed at which the background scrolls
         *                   <p>
         *                   The "y" variable is predetermined relative to the game screen dimension
         *                   Suitable for images with the dimension of 1480 x 720
         *                   (Same Logic could be used in drawing a parallax Background for the foreground)
         */

        public void drawParallaxBackGround(TextureRegion textureRegion, float lerpFactor, float BackgroundOffsetX) {
            backgroundBatch.draw(textureRegion, -App.V_WIDTH - BackgroundOffsetX * lerpFactor, -App.V_HEIGHT * 0.10f - graphicsCameraManager.getDy() * lerpFactor * 3, App.V_WIDTH, App.V_HEIGHT);
            backgroundBatch.draw(textureRegion, 0 - BackgroundOffsetX * lerpFactor, -App.V_HEIGHT * 0.10f - graphicsCameraManager.getDy() * lerpFactor * 3, App.V_WIDTH, App.V_HEIGHT);
            backgroundBatch.draw(textureRegion, App.V_WIDTH - BackgroundOffsetX * lerpFactor, -App.V_HEIGHT * 0.10f - graphicsCameraManager.getDy() * lerpFactor * 3, App.V_WIDTH, App.V_HEIGHT);

        }
    }

}
