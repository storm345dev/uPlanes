package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.Keypress;
import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.api.uPlanesAPI;
import net.stormdev.uPlanes.utils.PlaneUpdateEvent;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.useful.ucars.CarDirection;
import com.useful.ucars.ControlInput;

public class MotionManager {
	
	public static Vector rotateXZVector3dDegrees(Vector original, double degrees){
		double[] out = rotateVector2dRadians(original.getX(), original.getZ(), Math.toRadians(degrees));
		original.setX(out[0]);
		original.setZ(out[1]);
		return original;
	}
	
	public static double[] rotateVector2dDegrees(double x, double y, double degrees){
		return rotateVector2dRadians(x, y, Math.toRadians(degrees));
	}
	
	public static double[] rotateVector2dRadians(double x, double y, double radians){
	    double[] result = new double[2];
	    result[0] = x * Math.cos(radians) - y * Math.sin(radians);
	    result[1] = x * Math.sin(radians) + y * Math.cos(radians);
	    return result;
	}
	
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
		
		Vector playD = player.getEyeLocation().getDirection();
		Vector planeDirection = null;
		
		if(!main.doTurningCircles){
			planeDirection = playD;
		}
		else {
			try {
				planeDirection = (Vector) plane.getMetadata("plane.direction").get(0).value();
			} catch (Exception e) {
				planeDirection = playD; //Start plane off facing the way the player is
			}
			
			double rotMod = uPlanesAPI.getPlaneManager().getAlteredRotationAmountPerTick(player, plane, pln);
			
			float pYaw = (float) Math.toDegrees(Math.atan2(playD.getX() , -playD.getZ())); //Calculate yaw from 'player direction' vector
			float vYaw = (float) Math.toDegrees(Math.atan2(planeDirection.getX() , -planeDirection.getZ())); //Calculate yaw from 'carDirection' vector
			float yawDiff = pYaw - vYaw;
			if(yawDiff <= -180){
				yawDiff += 360;
			}
			else if(yawDiff > 180){
				yawDiff -= 360;
			}
			if(yawDiff < -rotMod){
				yawDiff = (float) -rotMod;
			}
			else if(yawDiff > rotMod){
				yawDiff = (float) rotMod;
			}
			planeDirection = rotateXZVector3dDegrees(planeDirection, ControlInput.getCurrentDriveDir(player).equals(CarDirection.BACKWARDS) ? -yawDiff : yawDiff);
			
			plane.removeMetadata("plane.direction", main.plugin);
			plane.setMetadata("plane.direction", new StatValue(true, main.plugin));
		}
		
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
		double x = planeDirection.getX() / d;
		double z = planeDirection.getZ() / d;
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
