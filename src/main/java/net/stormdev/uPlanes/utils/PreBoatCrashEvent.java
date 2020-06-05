package net.stormdev.uPlanes.utils;

import net.stormdev.uPlanes.api.Boat;
import net.stormdev.uPlanes.api.Plane;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PreBoatCrashEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private Boolean changePlayerYaw = false;
	private Boolean cancelled = false;
	private Player player;
	private double acceleration;
	private Vehicle vehicle;
	private Boat boat;
	private double damage;

	public PreBoatCrashEvent(Vehicle cart, Player player, double accelMod, Boat pln, double damage) {
	    this.vehicle = cart;
		this.player = player;
		this.acceleration = accelMod;
		this.boat = pln;
		this.setDamage(damage);
	}
	
	public Vehicle getVehicle(){
		return this.vehicle;
	}
	
	public Boat getBoat(){
		return this.boat;
	}

	public Player getPlayer() {
		return player;
	}

	public void setChangePlayerYaw(Boolean change) {
		this.changePlayerYaw = change;
		return;
	}

	public Boolean getChangePlayerYaw() {
		return this.changePlayerYaw;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

	public double getAcceleration() {
		return acceleration;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
