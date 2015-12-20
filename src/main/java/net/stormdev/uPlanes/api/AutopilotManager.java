package net.stormdev.uPlanes.api;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.PEntityMeta;
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
		PEntityMeta.setMetadata(planeVehicle, "plane.destination", new StatValue(dest, main.plugin));
		PEntityMeta.setMetadata(planeVehicle, "plane.autopilotData", new StatValue(destination, main.plugin));
	
		destination.autoPilotEngaged();
	}
}
