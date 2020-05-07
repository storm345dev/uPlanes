package net.stormdev.uPlanes.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.vehicle.VehicleUpdateEvent;

/**
 * Simple event which is fired when Planes are destroyed.
 * 
 */

public class PlaneDamageEvent extends VehicleUpdateEvent implements Cancellable {
	private Plane plane;
	private Boolean cancelled = false;
	private double damage;
	private String cause;

	public PlaneDamageEvent(Vehicle vehicle, Plane plane, double damage, String cause) {
		super(vehicle);
		this.vehicle = vehicle;
	    this.plane = plane;
	    this.damage = damage;
	    this.cause = cause;
	}
	
	public boolean wasAttackedByEntity(){
		return plane.getLastDamager() != null;
	}
	
	public Entity getAttacker(){
		return plane.getLastDamager();
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
