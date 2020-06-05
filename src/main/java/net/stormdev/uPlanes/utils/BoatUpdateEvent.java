package net.stormdev.uPlanes.utils;

import net.stormdev.uPlanes.api.Boat;
import net.stormdev.uPlanes.api.Keypress;
import net.stormdev.uPlanes.api.Plane;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class BoatUpdateEvent extends VehicleUpdateEvent implements Cancellable {
	private Vector toTravel;
	private Boolean changePlayerYaw = false;
	private Boolean cancelled = false;
	private Player player;
	private List<Keypress> pressed;
	private double acceleration;
	private Boat boat;

	public BoatUpdateEvent(Vehicle vehicle, Vector toTravel, Player player, List<Keypress> pressed, double accelMod, Boat pln) {
		super(vehicle);
		this.toTravel = toTravel;
		this.player = player;
		this.pressed = pressed;
		this.acceleration = accelMod;
		this.boat = pln;
	}
	
	public Boat getBoat(){
		return this.boat;
	}
	
	public List<Keypress> getPressedKeys(){
		return pressed;
	}
	
	public boolean wasKeypressed(Keypress press){
		return pressed.contains(press) || press.equals(Keypress.NONE) && pressed.size() < 1;
	}

	public Player getPlayer() {
		return player;
	}

	public Vector getTravelVector() {
		return this.toTravel;
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

}
