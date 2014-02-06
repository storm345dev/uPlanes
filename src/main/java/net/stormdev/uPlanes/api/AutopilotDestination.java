package net.stormdev.uPlanes.api;

import java.util.UUID;

import org.bukkit.Location;

public interface AutopilotDestination {
	
	/**
	 * Get the destination the plane is flying to
	 * 
	 * @return Returns the target destination
	 */
	public Location getDestination();
	
	/**
	 * Get the ID of the Plane being flown
	 * 
	 * @return Returns the planeId of the plane being flown
	 */
	public UUID getPlaneId();
	
	/**
	 * Return true if you want the autopilot
	 * to be cancelled when the player uses the controls
	 *  
	 * @return True to cancel. 
	 */
	public boolean isAutopilotOverridenByControlInput();
	
	/**
	 * Check if the autopilot should work for empty planes
	 * 
	 * @return Return true to allow for empty planes
	 */
	public boolean flyWithoutPlayer();
	
	/**
	 * Called when the autopilot is cancelled.
	 * 
	 */
	public void autoPilotCancelled();
	
	/**
	 * Called when the autopilot arrives at the desired destination
	 * 
	 */
	public void arrivedAtDestination();
	
	/**
	 * Called when the autopilot starts
	 * 
	 */
	public void autoPilotEngaged();
	
	/**
	 * True to use getTargetCruiseAltitude() for the flight height.
	 * 
	 * @return Whether or not to use a custom target cruise altitude.
	 */
	public boolean useCustomCruiseAltitude();
	
	/**
	 * The altitude to fly the plane at, if custom cruise altitude enabled
	 * 
	 * @return The Minecraft y coord for the height of the plane
	 */
	public int getTargetCruiseAltitude();
}
