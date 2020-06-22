package net.stormdev.uPlanes.api;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.vehicle.VehicleUpdateEvent;

/**
 * Simple event which is fired when Planes are destroyed.
 * 
 */

public class BoatDeathEvent extends VehicleUpdateEvent implements Cancellable {
	private Boat boat;
	private Boolean cancelled = false;

	public BoatDeathEvent(Vehicle vehicle, Boat boat) {
		super(vehicle);
		this.vehicle = vehicle;
	    this.boat = boat;
	}
	
	public Boat getBoat(){
		return boat;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

}
