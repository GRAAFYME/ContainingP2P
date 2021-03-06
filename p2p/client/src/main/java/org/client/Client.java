package org.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jme3tools.optimize.GeometryBatchFactory;

import org.protocol.ClientProtocol;
import org.protocol.MotionPathProtocol;
import org.protocol.ProtocolParser;
import org.protocol.ServerProtocol;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Spline.SplineType;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

import de.lessvoid.nifty.Nifty;

/*
 * Authors
 * Joshua Bergsma
 * Remco de Bruin
 * Yme van der Graaf
 * Jeffrey Harders
 * Arjen Pander
 * Melinda de Roo 
 * */

public class Client extends SimpleApplication {
	float tpf;

	// Protocol variables
	private ProtocolParser protocolParser;
	private ClientProtocol protocol;
	private networkClient c;

	// Scene
	Spatial sceneModel;
	public Node waterNode; // Different nodes have different physics
	private BulletAppState bulletAppState; // Physics machine
	FlyByCamera FBC;

	// Container
	public Spatial container;
	List<Containers> containerList;

	// AGV
	private Spatial AGV, AGV2;
	private AGV agv1, agv2;
	Vector3f location;

	private Node allAgvNodes = new Node();
	private boolean active = true;
	private boolean playing = false;
	private List<AGV> agvlist = new ArrayList<AGV>();
	private boolean playing2 = false;
	private MotionPath path;
	private boolean setWireFrame = false;

	// Motionpaths
	MotionPaths mp;
	MotionEvent motionControl;

	// Crane
	Crane crane;

	// Vehicle Spatials
	Spatial seaShip;
	Spatial truck;
	Spatial train;
	Spatial barge;

	// Spatials of the Storage Crane
	Spatial stCrane;
	Spatial stSCrane;
	Spatial stHCrane;

	// Spatials of the Seaship Crane
	Spatial ssCrane;
	Spatial ssSCrane;
	Spatial ssHCrane;

	// Spatials of the Truck Crane
	Spatial tCrane;
	Spatial tSCrane;
	Spatial tHCrane;

	// Spatials of the Train Crane
	Spatial trCrane;
	Spatial trSCrane;
	Spatial trHCrane;

	Crane[] storageCranes = new Crane[24];
	Crane[] seaShipCranes = new Crane[10];
	Crane[] truckCranes = new Crane[20];
	Crane[] trainCranes = new Crane[4];
	Crane[] bargeCranes = new Crane[8];

	// Vehicles
	List<Vehicle> trucks = new ArrayList<Vehicle>();

	Storage storage = new Storage();

	public static void main(String[] args) {
		Client app = new Client();
		app.start(); // start the game
	}

	@Override
	public void simpleInitApp() {

		initInputs();
		initNifty();

		// agv code
		GeometryBatchFactory.optimize(allAgvNodes);
		rootNode.attachChild(allAgvNodes);
		GeometryBatchFactory.optimize(rootNode);
		initScene();
		loadAssets();

		addAllAGVs(location);

		// waypoints code
		c = new networkClient(6666);

		// Cam code

		flyCam.setMoveSpeed(300f);
		FBC = new FlyByCamera(cam, inputManager);
		mp = new MotionPaths(assetManager, allAgvNodes);

		// Send the routes to the server
		protocol = new ClientProtocol();
		protocolParser = new ProtocolParser();
		protocol.motionPathList = mp.getMotionPathProtocols();
		c.sendMessage(protocolParser.serialize(protocol));
	}

	@Override
	public void simpleUpdate(float tpf) {
				
		
		String message = c.getMessages();
		if (message != "") {
			System.out.println(message);
			try {
				ServerProtocol p = (ServerProtocol) protocolParser
						.deserialize(message);
				// more than 0 elements in the list? yes - get(0), no - null
				org.protocol.Vehicle networkVehicle = (p.vehicles.size() > 0) ? p.vehicles
						.get(0) : null;
				String vehicle = networkVehicle.getClassName();

				init_vehicle(vehicle, networkVehicle.location.x,
						networkVehicle.location.y, networkVehicle.location.z);
				Vector3f location = new Vector3f(networkVehicle.location.x,
						networkVehicle.location.y, networkVehicle.location.z);
				getMessage(vehicle, location);

				// AGV route
				List<MotionPathProtocol> l = p.agvs.get(0).routes;
				String route = l.get(0).getName();
				final MotionPath path = mp.getMotionPath(route);

				int index = p.agvs.get(0).index;
				DriveRoute(index, path);
			} catch (Exception e) {
				System.out.println("Received incorrect package: \n\n"
						+ e.getMessage());
			}
		}

		// To let the server know we're still alive, server will get confused
		// and presume disconnection
		// when you've hit a breakpoint, but that's why heartbeat timeout @
		// server is disabled by default
		// c.SendHeartbeat();
	}

	public void DriveRoute(int index, final MotionPath path) {
		motionControl = new MotionEvent(agvlist.get(index), path);
		motionControl.setDirectionType(MotionEvent.Direction.Path);
		motionControl.setRotation(new Quaternion().fromAngleNormalAxis(
				-FastMath.HALF_PI, Vector3f.UNIT_Y));
		motionControl.play();
	}

	public void addAllAGVs(Vector3f location) {
		float x1 = -470f;
		float z1 = 150f;
		float x2 = -470f;
		float z2 = 735f;
		for (int i = 0; i < 50; i++) {
			location = new Vector3f(x1, 260, z1);
			agv1 = new AGV(String.valueOf(i), location, AGV, "AGV" + i);
			x1 += 25;
			rootNode.attachChild(agv1);
			agv1.setLocalTranslation(location);
			agvlist.add(agv1);
		}

		for (int j = 0; j < 50; j++) {
			location = new Vector3f(x2, 260, z2);
			agv2 = new AGV(String.valueOf(j), location, AGV2, "AGV" + j);
			x2 += 25;
			rootNode.attachChild(agv2);
			agv2.setLocalTranslation(location);
			agvlist.add(agv2);
		}
	}



	// creates most of the physics and scene logic
	public void initScene() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		Scene scene = new Scene(bulletAppState, assetManager); // creates a new
																// scene
		rootNode.attachChild(scene.sceneNode); // adds the scene to the game
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
		rootNode.addLight(Scene.sunLight); // adds the light to the world.
		rootNode.addLight(Scene.ambient); // adds the light to the world.
		waterNode = new Node("Water");
		Water water = new Water(assetManager, waterNode); // creates water
		viewPort.addProcessor(water.fpp);
		rootNode.attachChild(waterNode); // adds water to the world
	}

	public void loadAssets() {
		// Initialize Storage Crane
		stCrane = assetManager.loadModel("Models/crane/storageCrane.obj");
		stSCrane = assetManager
				.loadModel("Models/crane/storageCraneSlider.obj");
		stHCrane = assetManager.loadModel("Models/crane/storageCraneHook.obj");
		Material mat_stCrane, mat_stHCrane;
		mat_stCrane = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat_stHCrane = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat_stCrane.setColor("Color", ColorRGBA.Red);
		mat_stHCrane.setColor("Color", ColorRGBA.Black);
		mat_stCrane.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		mat_stHCrane.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		stCrane.setMaterial(mat_stCrane);
		stSCrane.setMaterial(mat_stHCrane);
		stHCrane.setMaterial(mat_stHCrane);

		// Initialize Seaship Crane & Barge Crane
		ssCrane = assetManager.loadModel("Models/crane/zeeKraan.obj");
		ssSCrane = assetManager.loadModel("Models/crane/zeeKraanSlider.obj");
		ssHCrane = assetManager.loadModel("Models/crane/zeeKraanHook.obj");
		Material mat_ssCrane, mat_ssHCrane;
		mat_ssCrane = new Material(assetManager,
				"Common/MatDefs/Light/Lighting.j3md");
		mat_ssHCrane = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		Texture crane_tex = assetManager
				.loadTexture("Models/crane/zeekraan.png");
		mat_ssCrane.setTexture("DiffuseMap", crane_tex);
		mat_ssHCrane.setColor("Color", ColorRGBA.Black);
		mat_ssCrane.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		mat_ssHCrane.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		ssCrane.setMaterial(mat_ssCrane);
		ssSCrane.setMaterial(mat_ssHCrane);
		ssHCrane.setMaterial(mat_ssHCrane);

		// Initialize Truck Crane
		tCrane = assetManager.loadModel("Models/crane/TruckCrane.obj");
		tSCrane = assetManager.loadModel("Models/crane/TruckCraneSlider.obj");
		tHCrane = assetManager.loadModel("Models/crane/TruckCraneHook.obj");
		Material mat_tCrane, mat_tSCrane;
		mat_tCrane = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat_tSCrane = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat_tCrane.setColor("Color", ColorRGBA.Green);
		mat_tSCrane.setColor("Color", ColorRGBA.Black);
		mat_tCrane.getAdditionalRenderState()
				.setFaceCullMode(FaceCullMode.Back);
		mat_tSCrane.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		tCrane.setMaterial(mat_tCrane);
		tSCrane.setMaterial(mat_tSCrane);
		tHCrane.setMaterial(mat_tSCrane);

		// Initialize Train Crane
		trCrane = assetManager.loadModel("Models/crane/TrainCrane.obj");
		trSCrane = assetManager.loadModel("Models/crane/TrainCraneSlider.obj");
		trHCrane = assetManager.loadModel("Models/crane/TrainCraneHook.obj");
		Material mat_trCrane, mat_trSCrane;
		mat_trCrane = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat_trSCrane = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat_trCrane.setColor("Color", ColorRGBA.Green);
		mat_trSCrane.setColor("Color", ColorRGBA.Black);
		mat_trCrane.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		mat_trSCrane.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		trCrane.setMaterial(mat_trCrane);
		trSCrane.setMaterial(mat_trSCrane);
		trHCrane.setMaterial(mat_trSCrane);

		init_StorageCrane();
		init_SeaShipCrane();
		init_TruckCrane();
		init_TrainCrane();
		init_BargeCrane();

		seaShip = assetManager.loadModel("Models/Vehicles/seaShip.obj");
		truck = assetManager.loadModel("Models/Vehicles/truck.obj");
		train = assetManager.loadModel("Models/Vehicles/train.obj");
		barge = assetManager.loadModel("Models/Vehicles/barge.obj");
		Material mat_vehicles = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat_vehicles.setColor("Color", ColorRGBA.Gray);
		mat_vehicles.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		seaShip.setMaterial(mat_vehicles);
		truck.setMaterial(mat_vehicles);
		train.setMaterial(mat_vehicles);
		barge.setMaterial(mat_vehicles);

		container = assetManager.loadModel("Models/container/Container.obj");
		Material mat_container;
		mat_container = new Material(assetManager,
				"Common/MatDefs/Light/Lighting.j3md");
		Texture cont_tex = assetManager
				.loadTexture("Models/container/container.png");
		mat_container.setTexture("DiffuseMap", cont_tex);
		mat_container.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Back);
		container.setMaterial(mat_container);

		AGV = assetManager.loadModel("Models/AGV/AGV.obj");
		AGV2 = assetManager.loadModel("Models/AGV/AGV.obj");
		Material mat_agv = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat_agv.getAdditionalRenderState().setWireframe(setWireFrame);
		mat_agv.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
		AGV.setMaterial(mat_agv);
		AGV2.setMaterial(mat_agv);
	}

	private void init_StorageCrane() {
		// Create storageCranes
		for (int i = 1; i <= 24; i++) {
			String id = String.valueOf(i);
			Vector3f pos = new Vector3f(-520 + (i * 60), 255, 670);
			Crane c = new StorageCrane(id, pos, stCrane, stSCrane, stHCrane);
			storageCranes[i - 1] = c;
			rootNode.attachChild(c);
			c.setLocalTranslation(pos);
		}
	}

	private void init_SeaShipCrane() {
		// Create seaShipCranes
		for (int i = 1; i <= 10; i++) {
			String id = String.valueOf(i);
			Vector3f pos = new Vector3f(-685, 255, 150 + (i * 50));
			Crane c = new SeaShipCrane(id, pos, ssCrane, ssSCrane, ssHCrane);
			seaShipCranes[i - 1] = c;
			rootNode.attachChild(c);
			c.setLocalTranslation(pos);
		}
	}

	private void init_TruckCrane() {
		// Create truckCranes
		for (int i = 1; i <= 20; i++) {
			String id = String.valueOf(i);
			Vector3f pos = new Vector3f(250 + (i * 40), 255, 90);
			Crane c = new TruckCrane(id, pos, tCrane, tSCrane, tHCrane);
			truckCranes[i - 1] = c;
			rootNode.attachChild(c);
			c.setLocalTranslation(pos);
		}
	}

	private void init_TrainCrane() {
		// Create trainCranes
		for (int i = 1; i <= 4; i++) {
			String id = String.valueOf(i);
			Vector3f pos = new Vector3f(-350 + (i * 250), 255, 795);
			Crane c = new TrainCrane(id, pos, trCrane, trSCrane, trHCrane);
			trainCranes[i - 1] = c;
			rootNode.attachChild(c);
			c.setLocalTranslation(pos);
		}
	}

	private void init_BargeCrane() {
		// Create bargeCranes
		for (int i = 1; i <= 8; i++) {
			String id = String.valueOf(i);
			Vector3f pos = new Vector3f(-600 + (i * 100), 255, 25);
			Crane c = new BargeCrane(id, pos, ssCrane, ssSCrane, ssHCrane);
			bargeCranes[i - 1] = c;
			rootNode.attachChild(c);
			c.setLocalTranslation(pos);
		}
	}

	private void init_vehicle(String vehicle, float x, float y, float z) {
		// Create vehicle and containers on the vehicle
		Vehicle v = null;
		switch (vehicle) {
		case "vrachtauto":
			v = new Truck(String.valueOf(x + 1), truck);
			v.setContainer(container);
			v.setContLocalTranslation(v.getLocalTranslation().x - 5,
					v.getLocalTranslation().y + 2.5f,
					v.getLocalTranslation().z + 0.5f);
			trucks.add(v);
			break;
		case "zeeschip":
			v = new SeaShip(String.valueOf(x + 1), seaShip);
			break;
		case "trein":
			v = new Train(String.valueOf(1), train);
			break;
		case "binnenvaartsschip":
			v = new Barge(String.valueOf(x + 1), barge);
			break;
		}
		v.setLocalTranslation(v.setLocation(vehicle, x, y, z));
		rootNode.attachChild(v);
	}

	private void getMessage(String vehicleName, Vector3f location) {
		// Choose the crane type
		int craneType = 0;
		switch (vehicleName) {
		case "zeeschip":
			craneType = 1;
			break;
		case "vrachtauto":
			craneType = 2;
			break;
		case "trein":
			craneType = 3;
			break;
		case "binnenvaartsschip":
			craneType = 4;
			break;
		default:
			craneType = 5;
			break;
		}
		boolean direction = false;
		int id = 0; // Chosen later
		List<Float> distance = new ArrayList<Float>();
		float smallest = 1000;

		Vector3f conVector = location;

		// Select the crane that has to be used!
		switch (craneType) {
		case 1:
			for (Crane c : seaShipCranes) {
				distance.add(c.distance(vehicleName, conVector));
			}

			for (int i = 0; i < distance.size(); i++) {
				if (distance.get(i) < smallest) {
					if (!seaShipCranes[i].isBusy()) {
						smallest = distance.get(i);
						id = i;
					}
				}
			}
			break;
		case 2:
			for (Crane c : truckCranes) {
				distance.add(c.distance(vehicleName, conVector));
			}

			for (int i = 0; i < distance.size(); i++) {
				if (distance.get(i) == 0) {
					if (!truckCranes[i].isBusy()) {
						smallest = distance.get(i);
						id = i;
					}
				}
			}
			break;
		case 3:
			for (Crane c : trainCranes) {
				distance.add(c.distance(vehicleName, conVector));
			}

			for (int i = 0; i < distance.size(); i++) {
				if (distance.get(i) < smallest) {
					if (!trainCranes[i].isBusy()) {
						smallest = distance.get(i);
						id = i;
					}
				}
			}
			break;
		case 4:
			for (Crane c : bargeCranes) {
				distance.add(c.distance(vehicleName, conVector));
			}

			for (int i = 0; i < distance.size(); i++) {
				if (distance.get(i) < smallest) {
					if (!bargeCranes[i].isBusy()) {
						smallest = distance.get(i);
						id = i;
					}
				}
			}
			break;
		case 5:
			break;
		}

		Vector3f[] des = new Vector3f[4];

		// Start the animation of the crane
		switch (craneType) {
		case 1: {
			Vector3f startPosCrane = new Vector3f(
					seaShipCranes[id].getLocalTranslation());
			Vector3f startPosSlider = new Vector3f(
					seaShipCranes[id].sliderNode.getLocalTranslation());
			Vector3f startPosHook = new Vector3f(
					seaShipCranes[id].hookNode.getLocalTranslation());

			des[0] = new Vector3f(startPosCrane.x, startPosCrane.y, conVector.z); // Destination
																					// of
																					// the
																					// crane
			des[1] = new Vector3f(startPosSlider.x - 45 - (1 * 2.5f),
					startPosSlider.y, startPosSlider.z); // Destination of the
															// slider
			des[2] = new Vector3f(startPosHook.x, startPosHook.y
					- (30 + (id * 2.5f)), startPosHook.z); // Destination of the
															// hook
			des[3] = new Vector3f(startPosCrane.x, startPosCrane.y, conVector.z); // Destination
																					// of
																					// the
																					// crane
			des[4] = new Vector3f(startPosSlider.x, startPosSlider.y,
					startPosSlider.z); // Destination of the slider back
			des[5] = new Vector3f(startPosHook.x, startPosHook.y - 30,
					startPosHook.z); // Destination of the hook

			if (direction)
				seaShipCranes[id].animation(1, des, 5);
			else
				seaShipCranes[id].animation(2, des, 5);
			break;
		}
		case 2: {
			Vector3f startPosCrane = new Vector3f(
					truckCranes[id].getLocalTranslation());
			Vector3f startPosHook = new Vector3f(
					truckCranes[id].hookNode.getLocalTranslation());

			des[0] = new Vector3f(startPosCrane.x, startPosCrane.y,
					startPosCrane.z); // Destination of the crane
			des[1] = new Vector3f(startPosHook.x, startPosHook.y - 16,
					startPosHook.z); // Destination of the hook
			des[2] = new Vector3f(startPosCrane.x, startPosCrane.y,
					startPosCrane.z - 30); // Destination of the crane

			if (!direction)
				truckCranes[id].animation(3, des, 5);
			else
				truckCranes[id].animation(4, des, 5);

			trucks.get(id).deleteContainer();
			truckCranes[id].setContainer(trucks.get(id).getContainer());
			truckCranes[id].attachContainer();

			break;
		}
		case 3: {
			Vector3f startPosCrane = new Vector3f(
					trainCranes[id].getLocalTranslation());
			Vector3f startPosSlider = new Vector3f(
					trainCranes[id].sliderNode.getLocalTranslation());
			Vector3f startPosHook = new Vector3f(
					trainCranes[id].hookNode.getLocalTranslation());

			des[0] = new Vector3f(conVector.x, startPosCrane.y, startPosCrane.z); // Destination
																					// of
																					// the
																					// crane
			des[1] = new Vector3f(startPosSlider.x, startPosSlider.y,
					startPosSlider.z + 18); // Destination of the slider
			des[2] = new Vector3f(startPosHook.x, startPosHook.y - 26,
					startPosHook.z); // Destination of the hook
			des[3] = new Vector3f(startPosCrane.x, startPosCrane.y,
					startPosCrane.z); // Destination of the crane
			des[4] = new Vector3f(startPosSlider.x, startPosSlider.y,
					startPosSlider.z); // Destination of the slider
			des[5] = new Vector3f(startPosHook.x, startPosHook.y - 22,
					startPosHook.z); // Destination of the hook

			if (direction)
				trainCranes[id].animation(1, des, 5);
			else
				trainCranes[id].animation(2, des, 5);
			break;
		}
		case 4: {
			Vector3f startPosCrane = new Vector3f(
					bargeCranes[id].getLocalTranslation());
			Vector3f startPosSlider = new Vector3f(
					bargeCranes[id].sliderNode.getLocalTranslation());
			Vector3f startPosHook = new Vector3f(
					bargeCranes[id].hookNode.getLocalTranslation());

			des[0] = new Vector3f(conVector.x, startPosCrane.y, startPosCrane.z); // Destination
																					// of
																					// the
																					// crane
			des[1] = new Vector3f(startPosSlider.x, startPosSlider.y,
					startPosSlider.z - 45 - (1 * 2.5f)); // Destination of the
															// slider
			des[2] = new Vector3f(startPosHook.x, startPosHook.y
					- (30 + (id * 2.5f)), startPosHook.z); // Destination of the
															// hook
			des[3] = new Vector3f(conVector.x, startPosCrane.y, startPosCrane.z); // Destination
																					// of
																					// the
																					// crane
			des[4] = new Vector3f(startPosSlider.x, startPosSlider.y,
					startPosSlider.z); // Destination of the slider back
			des[5] = new Vector3f(startPosHook.x, startPosHook.y - 30,
					startPosHook.z); // Destination of the hook

			if (direction)
				bargeCranes[id].animation(1, des, 5);
			else
				bargeCranes[id].animation(2, des, 5);
			break;
		}
		case 5: {
			Map<String, Vector3f> spot = storage.storageSpots.get("0");
			Vector3f spotje = spot.get("0");

			Vector3f startPosCrane = new Vector3f(
					storageCranes[id].getLocalTranslation());
			Vector3f startPosSlider = new Vector3f(
					storageCranes[id].sliderNode.getLocalTranslation());
			Vector3f startPosHook = new Vector3f(
					storageCranes[id].hookNode.getLocalTranslation());

			if (spotje.z > 370) // Destination of the crane
			{
				des[0] = new Vector3f(startPosCrane.x, startPosCrane.y,
						startPosCrane.z);
			} else {
				des[0] = new Vector3f(startPosCrane.x, startPosCrane.y,
						startPosCrane.z - 465);
			}
			des[1] = new Vector3f(startPosSlider.x + spotje.x,
					startPosSlider.y, startPosSlider.z); // Destination of the
															// slider
			des[2] = new Vector3f(startPosHook.x, startPosHook.y - 33,
					startPosHook.z); // Destination of the hook
			des[3] = new Vector3f(startPosCrane.x, startPosCrane.y, spotje.z); // Destination
																				// of
																				// the
																				// crane
			des[4] = new Vector3f(startPosSlider.x, startPosSlider.y,
					startPosSlider.z); // Destination of the slider
			des[5] = new Vector3f(startPosHook.x, startPosHook.y - 33
					+ spotje.y, startPosHook.z); // Destination of the hook

			if (direction)
				storageCranes[id].animation(1, des, 5);
			else
				storageCranes[id].animation(2, des, 5);
			break;
		}
		}
	}

	private void initInputs() {
		inputManager.addMapping("speedDown", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("speedUp", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addMapping("speedReset", new KeyTrigger(KeyInput.KEY_R));
		ActionListener acl = new ActionListener() {

			public void onAction(String name, boolean keyPressed, float tpf) {

				if (name.equals("speedDown") && keyPressed) {
					speed = speed / 2;
					System.out.println(speed);

				} else {
					if (name.equals("speedUp") && keyPressed) {
						speed = speed * 2;
						System.out.println(speed);

					} else {
						if (name.equals("speedReset") && keyPressed) {
							speed = 1;
							System.out.println(speed);

						}
					}
				}
			}
		};
		inputManager.addListener(acl, "speedUp", "speedDown", "speedReset");

	}

	public void initNifty() {
		NiftyMenu niftyMenu = new NiftyMenu();
		stateManager.attach(niftyMenu);

		NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
				inputManager, audioRenderer, guiViewPort);
		Nifty nifty = niftyDisplay.getNifty();
		nifty.fromXml("Interface/MainMenu.xml", "start");
		guiViewPort.addProcessor(niftyDisplay);
	}
}