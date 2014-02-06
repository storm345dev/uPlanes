package net.stormdev.uPlanes.api;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.Location;
import org.bukkit.entity.Vehicle;

/**
 * A class for managing auto pilot for planes 
 *
 *
 */
public class AutopilotManager {
	
	protected AutopilotManager(){
		//Only classes in package can instantate
	}
	
	/**
	 * Start the autopilot on a plane
	 * 
	 * @param planeVehicle The vehicle to manipulate
	 * @param destination The destination to fly to
	 */
	public void startAutopilot(Vehicle planeVehicle, AutopilotDestination destination){
		Location dest = destination.getDestination();
		planeVehicle.setMetadata("plane.destination", new StatValue(dest, main.plugin));
		
		planeVehicle.setMetadata("plane.autopilotData", new StatValue(destination, main.plugin));
	
		destination.autoPilotEngaged();
	}
}
