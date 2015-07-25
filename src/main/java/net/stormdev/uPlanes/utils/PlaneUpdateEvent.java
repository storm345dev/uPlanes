package net.stormdev.uPlanes.utils;

import net.stormdev.uPlanes.api.Keypress;
import net.stormdev.uPlanes.api.Plane;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class PlaneUpdateEvent extends VehicleUpdateEvent implements Cancellable {
	private Vector toTravel;
	private boolean changePlayerYaw = false;
	private boolean cancelled = false;
	private Player player;
	private List<Keypress> pressed;
	private double acceleration;
	private Plane plane;

	public PlaneUpdateEvent(Vehicle vehicle, Vector toTravel, Player player, List<Keypress> pressed, double accelMod, Plane pln) {
		super(vehicle);
		this.toTravel = toTravel;
		this.player = player;
		this.pressed = pressed;
		this.acceleration = accelMod;
		this.plane = pln;
	}
	
	public Plane getPlane(){
		return this.plane;
	}
	
	public List<Keypress> getPressedKeys(){
		return pressed;
	}
	
	public boolean wasKeypressed(Keypress press){
		return pressed.contains(press);
	}

	public Player getPlayer() {
		return player;
	}

	public Vector getTravelVector() {
		return this.toTravel;
	}

	public void setChangePlayerYaw(Boolean change) {
		this.changePlayerYaw = change;
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

}
