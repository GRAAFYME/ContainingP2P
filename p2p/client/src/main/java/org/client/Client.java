package org.client;

/**
 * Hello world!
 *
 */

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.math.ColorRGBA;

/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class Client extends SimpleApplication {

    public static void main(String[] args){
        Client app = new Client();
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1); // create cube shape
        Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        geom.setMaterial(mat);                   // set the cube's material
        rootNode.attachChild(geom);              // make the cube appear in the scene
    }
}