package net.stormdev.uPlanes.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.vehicle.VehicleUpdateEvent;

/**
 * Simple event which is fired when Planes are destroyed.
 * 
 */

public class BoatDamageEvent extends VehicleUpdateEvent implements Cancellable {
	private Boat boat;
	private Boolean cancelled = false;
	private double damage;
	private String cause;

	public BoatDamageEvent(Vehicle vehicle, Boat boat, double damage, String cause) {
		super(vehicle);
		this.vehicle = vehicle;
	    this.boat = boat;
	    this.damage = damage;
	    this.cause = cause;
	}
	
	public boolean wasAttackedByEntity(){
		return boat.getLastDamager() != null;
	}
	
	public Entity getAttacker(){
		return boat.getLastDamager();
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

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

}
