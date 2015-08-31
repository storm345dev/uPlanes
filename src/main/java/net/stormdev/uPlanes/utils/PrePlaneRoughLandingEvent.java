package net.stormdev.uPlanes.utils;

import net.stormdev.uPlanes.api.Plane;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PrePlaneRoughLandingEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean changePlayerYaw = false;
	private boolean cancelled = false;
	private Player player;
	private double acceleration;
	private Minecart vehicle;
	private Plane plane;
	private double damage;

	public PrePlaneRoughLandingEvent(Minecart vehicle, Player player, double accelMod, Plane pln, double damage) {
	    this.vehicle = vehicle;
		this.player = player;
		this.acceleration = accelMod;
		this.plane = pln;
		this.setDamage(damage);
	}
	
	public Minecart getVehicle(){
		return this.vehicle;
	}
	
	public Plane getPlane(){
		return this.plane;
	}

	public Player getPlayer() {
		return player;
	}

	public void setChangePlayerYaw(Boolean change) {
		this.changePlayerYaw = change;
		return;
	}

	public boolean getChangePlayerYaw() {
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
