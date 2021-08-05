package com.mygdx.game.Handlers;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.App;
import com.mygdx.game.Entities.Player;

import java.util.ArrayList;

import static com.mygdx.game.Handlers.B2DVars.PPM;

public class CameraManager {

    private static MapObjects mapObjects;
    private Player player;
    private Camera camera;
    private Vector3 position;

    private float positionX, positionY;
    private static ArrayList<Ellipse> ellipses = new ArrayList<>();
    private static ArrayList<Rectangle> rectangles = new ArrayList<>();

    private boolean ElevatedView = false;

    private float lerpfactor = 0.075f;


    public CameraManager(Camera camera, Player player){
        this.camera = camera;
        this.player = player;
        parseMapObjects();
        position = camera.position;
        positionX = player.getPositionX();
        positionY = player.getPositionY();
    }

    public void updateCameraRelativeToMap(boolean B2DCam){
        for (int i = 0; i < rectangles.size(); i++){
            if(player.getPositionX() >= rectangles.get(i).x) {
                positionY = rectangles.get(i).y;
            }else positionY = player.getPositionY();
        }

        for(int i = 0; i < ellipses.size(); i++){
            float ellipsePositionX = ellipses.get(i).x + ellipses.get(i).width/2;
            float ellipsePositionY = ellipses.get(i).y + ellipses.get(i).width/2;
            float dx = ellipsePositionX - player.getPositionX();
            float dy = ellipsePositionY - player.getPositionY();
            float distanceSquared = dx*dx + dy*dy;

            if(distanceSquared <= ellipses.get(i).height/2 * ellipses.get(i).height/2){
                positionX = ellipsePositionX;
                positionY = ellipsePositionY;
                break;
            }
            else if(player.getHorizontalVelocity() > 0) {
                positionX = player.getPositionX() + (float) App.V_WIDTH/8;
                positionY = player.getPositionY() + (float) App.V_HEIGHT/ 4;
            }
            else if(player.getHorizontalVelocity() < 0) {
                positionX = player.getPositionX() - (float) App.V_WIDTH/8;
                positionY = player.getPositionY() + (float) App.V_HEIGHT / 4;
            }
            else {
                positionX = player.getPositionX();
                positionY = player.getPositionY() + (float) App.V_HEIGHT/ 4;
            }
        }

        if(ElevatedView) positionY += App.V_HEIGHT/ 4;

        if(B2DCam){
            positionY /= PPM;
            positionX /= PPM;
        }


        updateCamera();
    }



    private static void parseMapObjects(){
        for(MapObject object : mapObjects){
            if(object instanceof EllipseMapObject){
                ellipses.add(((EllipseMapObject) object).getEllipse());
            }
            if(object instanceof RectangleMapObject){
                rectangles.add(((RectangleMapObject) object).getRectangle());
            }
        }

    }

    public void updateCamera(){
        position.x = camera.position.x + (positionX - position.x) * lerpfactor;
        position.y = camera.position.y + (positionY - position.y) * lerpfactor;
        camera.position.set(position);
        camera.update();
    }

    public static void mapUpdated(MapObjects objects){
        mapObjects = objects;
        ellipses.clear();
        rectangles.clear();
        parseMapObjects();
    }

    public float getDx() {
        return (positionX - position.x) * lerpfactor;
    }
    public float getDy() { return (positionY - position.y) * lerpfactor;}

    //YOU  FUCKIN WOT??
    public void setPlayer(Player player){this.player = player;
        positionX = player.getPositionX();
        positionY = player.getPositionY();}
    public void setCamera(Camera camera){this.camera = camera;
        position = camera.position;}

    public void setElevatedView(boolean ElevatedView){this.ElevatedView = ElevatedView;}
    public void setLerpfactor(float lerpfactor){
        this.lerpfactor = lerpfactor;
    }
}

