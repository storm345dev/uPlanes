package net.stormdev.uPlanes.api;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.vehicle.VehicleUpdateEvent;

/**
 * Simple event which is fired when Planes are destroyed.
 * 
 */

public class PlaneDeathEvent extends VehicleUpdateEvent implements Cancellable {
	private Plane plane;
	private Boolean cancelled = false;

	public PlaneDeathEvent(Vehicle vehicle, Plane plane) {
		super(vehicle);
		this.vehicle = vehicle;
	    this.plane = plane;
	}
	
	public Plane getPlane(){
		return plane;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

}
