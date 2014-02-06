package net.stormdev.uPlanes.api;


/**
 * Public API for controlling and managing planes from other plugins
 * 
 *
 */
public class uPlanesAPI {
	private static uPlanesAPI instance;
	private static uPlaneManager planeManager;
	
	/**
	 * Grab the current instance of the uPlanesAPI so you can do stuff!
	 * 
	 * @return Returns the API instance
	 */
	public static uPlanesAPI getAPI(){ //Retrieve the API instance
		if(instance == null){
			instance = new uPlanesAPI(); //Work as a factory for the API also
		}
		return instance;
	}
	
	/**
	 * Contains methods for creating, destroying and manipulating planes
	 * 
	 * @return Returns the uPlaneManager instance
	 */
	public static uPlaneManager getPlaneManager(){ //Retrieve instance of PlaneManager
		if(planeManager == null){
			planeManager = new uPlaneManager(); //Work as a factory also
		}
		return planeManager;
	}
	
	
	
	private uPlanesAPI(){ //Stop other instantating the API
		
	}
	
	
	
	
}
