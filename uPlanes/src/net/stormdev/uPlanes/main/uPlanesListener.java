package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.utils.Keypress;
import net.stormdev.uPlanes.utils.PlaneUpdateEvent;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class uPlanesListener implements Listener {
	private main plugin;
	private double defaultSpeed;
	public uPlanesListener(main instance){
		this.plugin = instance;
		
		defaultSpeed = main.config.getDouble("general.planes.defaultSpeed");
	}
	@EventHandler
	void planeFlightControl(PlaneUpdateEvent event){
		Vehicle vehicle = event.getVehicle();
		Location loc = vehicle.getLocation();
		Vector travel = event.getTravelVector();
		double y = 0.0;
		
		travel.multiply(defaultSpeed);
		Keypress press = event.getPressedKey();
		
		switch(press){
		case A: 
			y = 0.5; break; //Go up
		case D: 
			y = -0.5; break; //Go down
		default:
			break;
		}
		
		travel.setY(y);
		
		Vector behind = travel.clone().multiply(-1); //Behind the plane
		Location exhaust = loc.add(behind);
		exhaust.getWorld().playEffect(exhaust, Effect.SMOKE, 1);
		
		vehicle.setVelocity(event.getTravelVector());
		return;
	}
	public Boolean isAPlane(Minecart m){
		//TODO
		return true;
	}
}
