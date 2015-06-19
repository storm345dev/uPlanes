package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.Keypress;
import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.utils.PlaneUpdateEvent;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MotionManager {
	
	public static void move(Player player, float f, float s) {
		Vector vec = new Vector();
		Entity ent = player.getVehicle();
		if (ent == null) {
			return;
		}
		while (!(ent instanceof Minecart) && ent.getVehicle() != null) {
			ent = ent.getVehicle();
		}
		if (!(ent instanceof Minecart)) {
			return;
		}
		Minecart plane = (Minecart) ent;
		Plane pln = main.plugin.listener.getPlane(plane);
		if(pln == null){
			//Not a plane
			return;
		}
		
		boolean decel = true;
		if(f == 0 && s == 0){
			if(AccelerationManager.getCurrentMultiplier(plane) == 0){
				
				//Not moving
				return;
			}
			if(pln.isHover()){
				AccelerationManager.setCurrentAccel(plane, 0);
			}
		}
		
		Vector plaD = player.getEyeLocation().getDirection();
		Keypress pressed = Keypress.NONE;
		/*if(f==0 && pln.isHover()){
			forwardMotion = false;
		}*/
		int side = 0; // -1=left, 0=straight, 1=right
		Boolean turning = false;
		if(f > 0){
			pressed = Keypress.W;
			decel = false;
		}
		if (s > 0) {
			side = -1;
			turning = true;
		}
		if (s < 0) {
			side = 1;
			turning = true;
		}
		double y = -0.0; // Don't succumb to gravity
		double d = 27; // A number that happens to get realistic motion
		Boolean isRight = false;
		Boolean isLeft = false;
		if (turning) {
			if (side < 0) {// do left action
				isLeft = true;
				pressed = Keypress.A;
				plane.setMetadata("plane.left", new StatValue(true, main.plugin));
				
			} else if (side > 0) {// do right action
				isRight = true;
				pressed = Keypress.D;
				plane.setMetadata("plane.right", new StatValue(true, main.plugin));
			}
		}
		double x = plaD.getX() / d;
		double z = plaD.getZ() / d;
		if (!isRight) {
			plane.removeMetadata("plane.right", main.plugin);
		}
		if (!isLeft) {
			plane.removeMetadata("plane.left", main.plugin);
		}
		if(f < 0 && pln.isHover()){
			pressed = Keypress.S;
		}
		/*if(!forwardMotion && pln.isHover()){
			x = 0;
			z = 0;
		}*/
		
		double accelMod = !decel ? AccelerationManager.getMultiplier(player, plane, pln) 
				: (f == 0 ? AccelerationManager.decelerateAndGetMult(player, plane, pln) 
						: AccelerationManager.decelerateAndGetMult(player, plane, pln));
		
		x *= accelMod;
		y *= accelMod;
		z *= accelMod;
		
		vec = new Vector(x, y, z);
		final PlaneUpdateEvent event = new PlaneUpdateEvent(plane, vec, player, pressed, accelMod, pln);
		main.plugin.getServer().getScheduler()
				.runTask(main.plugin, new Runnable() {
					public void run() {
						main.plugin.getServer().getPluginManager()
								.callEvent(event);
					}
				});
		return;
	}

}
