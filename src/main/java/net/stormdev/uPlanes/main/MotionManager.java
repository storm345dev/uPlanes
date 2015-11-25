package net.stormdev.uPlanes.main;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.uPlanes.api.Keypress;
import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.api.Plane.RollTarget;
import net.stormdev.uPlanes.api.uPlanesAPI;
import net.stormdev.uPlanes.utils.CartOrientationUtil;
import net.stormdev.uPlanes.utils.PlaneUpdateEvent;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

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
		while (!(ent instanceof Vehicle) && ent.getVehicle() != null) {
			ent = ent.getVehicle();
		}
		if (!(ent instanceof Vehicle)) {
			return;
		}
		Vehicle plane = (Vehicle) ent;
		Plane pln = main.plugin.listener.getPlane(plane);
		if(pln == null){
			//Not a plane
			return;
		}
		
		if(plane.hasMetadata("plane.frozen")){
			return;
		}
		
		boolean decel = true;
		if(f == 0 && s == 0){
			/*if(AccelerationManager.getCurrentMultiplier(plane) == 0){
				
				//Not moving
				return;
			}*/
			/*if(pln.isHover()){
				AccelerationManager.setCurrentAccel(plane, 0);
			}*/
		}
		
		Vector playD = player.getEyeLocation().getDirection();
		Vector planeDirection = null;
		
		if(!main.doTurningCircles){
			if((AccelerationManager.getCurrentMultiplier(plane) >= 0.2 || pln.isHover()) && !plane.hasMetadata("plane.destination")){
				planeDirection = playD.clone().setY(0).normalize();
				float vYaw = (float) Math.toDegrees(Math.atan2(planeDirection.getX() , -planeDirection.getZ()));
				CartOrientationUtil.setYaw(plane, vYaw-90);
			}
		}
		else {
			try {
				planeDirection = (Vector) plane.getMetadata("plane.direction").get(0).value();
			} catch (Exception e) {
				planeDirection = playD.clone().setY(0).normalize(); //Start plane off facing the way the player is
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
			
			boolean roll = Math.abs(yawDiff) > 30;
			if(!roll){
				pln.setRollTarget(RollTarget.NONE);
			}
			
			if(yawDiff < -rotMod){
				pln.setRollTarget(RollTarget.LEFT);
				yawDiff = (float) -rotMod;
			}
			else if(yawDiff > rotMod){
				pln.setRollTarget(RollTarget.RIGHT);
				yawDiff = (float) rotMod;
			}
			
			double am = AccelerationManager.getCurrentMultiplier(plane);
			if((am >= 0.2 || (pln.isHover()&&am>0)) && !plane.hasMetadata("plane.destination")){
				pln.updateRoll();
				CartOrientationUtil.setRoll(plane, pln.getRoll());
				planeDirection = rotateXZVector3dDegrees(planeDirection, yawDiff);
				CartOrientationUtil.setYaw(plane, vYaw-90);
			}
			if(pln.isHover() && am == 0){
				pln.setRollTarget(RollTarget.NONE);
				pln.updateRoll();
				CartOrientationUtil.setRoll(plane, pln.getRoll());
			}
			plane.removeMetadata("plane.direction", main.plugin);
			plane.setMetadata("plane.direction", new StatValue(planeDirection.clone().normalize(), main.plugin));
		}
		
		List<Keypress> pressedKeys = new ArrayList<Keypress>();
		Keypress speedModKey = Keypress.NONE;
		/*if(f==0 && pln.isHover()){
			forwardMotion = false;
		}*/
		int side = 0; // -1=left, 0=straight, 1=right
		Boolean turning = false;
		if(f > 0){
			speedModKey = Keypress.W;
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
		
		double accelMod = !decel ? AccelerationManager.getMultiplier(player, plane, pln) 
				: (f == 0 ? AccelerationManager.decelerateAndGetMult(player, plane, pln) 
						: AccelerationManager.decelerateAndGetMult(player, plane, pln));
		
		float hoverAmount = (float) (0.0005 * pln.getSpeed());
		if(hoverAmount < 0.009){
			hoverAmount = 0.009f;
		}
		
		if (turning) {
			if (side < 0) {// do left action
				isLeft = true;
				pressedKeys.add(Keypress.A);
				plane.setMetadata("plane.left", new StatValue(true, main.plugin));
				if(pln.isHover()){
					y = hoverAmount;
				}
			} else if (side > 0) {// do right action
				isRight = true;
				pressedKeys.add(Keypress.D);
				plane.setMetadata("plane.right", new StatValue(true, main.plugin));
				if(pln.isHover()){
					y = -hoverAmount;
				}
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
			speedModKey = Keypress.S;
		}
		/*if(!forwardMotion && pln.isHover()){
			x = 0;
			z = 0;
		}*/
		
		x *= accelMod;
		z *= accelMod;	
		
		boolean hasFlightSpeed = (new Vector(x, 0, z).lengthSquared() > 0.75 || accelMod > 0.75);
		
		if(pln.isHover()){
			double pitch = 40*accelMod;
			if(pitch > 16){
				pitch = 16;
			}
			pln.setCurrentPitch((float) (pitch));
		}
		else { //Calculate y
			if(plane.isOnGround()){
				if(!hasFlightSpeed){
					pln.setCurrentPitch(0);
				}
				if(pln.getCurrentPitch() > 0){ //Trying to pitch DOWN
					pln.setCurrentPitch(0);
				}
			}
			if(hasFlightSpeed || !plane.isOnGround()){ //At the right speed to take off
				Keypress vertModKey = Keypress.NONE;
				if(pressedKeys.contains(Keypress.A)){
					vertModKey = Keypress.A;
				}
				else if(pressedKeys.contains(Keypress.D)){
					vertModKey = Keypress.D;
				}
				
				if(vertModKey != Keypress.NONE){
					float pitch = pln.getCurrentPitch();
					float change = (float) 2;
					pitch += vertModKey.equals(Keypress.D) ? change : -change;
					if(pitch > 89){
						pitch = 89;
					}
					if(pitch < -89){
						pitch = -89;
					}
					pln.setCurrentPitch(pitch);
				}
			}
			
			//use tan
			double adjacentLength = new Vector(x, 0, z).length();
			y = -Math.tan(Math.toRadians(pln.getCurrentPitch())) * adjacentLength;
			
			double aPitch = -pln.getCurrentPitch();
			double yMult = 1 - (aPitch / (aPitch > 0 ? 70.0d : 300.0d)); //If they're going up too steep then go slower
			if(yMult < 0){
				yMult = 0;
			}
			/*if(yMult > 1.225){ //45 degrees
				double xzMult = 1.225-yMult;
				yMult = 1.225;
				x *= xzMult;
				z *= xzMult;
			}*/
			y *= yMult;
			if(aPitch > 60 && y < 0.012){ //Pitching up too much! Make them stall!
				accelMod = AccelerationManager.stall(player, plane, pln);
				//player.sendMessage(ChatColor.RED+"STALL");
			}
			x *= yMult;
			z *= yMult;
			
			if(!hasFlightSpeed){
				if(y > 0){
					y = 0; //Don't go upwards
				}
				/*y = plane.getVelocity().getY(); //FALL
				if(y > 0){
					if(y > 0.5){
						y = 0.5;
					}
					y *= 1.25;
				}*/
			}
		}
		
		double resY = plane.getLocation().getY() + y;
		if(resY > plane.getLocation().getWorld().getMaxHeight()
				|| resY > uPlanesListener.heightLimit){
			y = 0; //Go up no further
		}
		
		vec = new Vector(x, y, z);
		
		if(!speedModKey.equals(Keypress.NONE)){
			pressedKeys.add(speedModKey);
		}
		
		final PlaneUpdateEvent event = new PlaneUpdateEvent(plane, vec, player, pressedKeys, accelMod, pln);
		
		main.plugin.getServer().getScheduler()
				.runTask(main.plugin, new Runnable() {
					public void run() {
						if(main.fireUpdateEvent){
							main.plugin.getServer().getPluginManager()
								.callEvent(event);
						}
						else {
							main.plugin.listener.planeFlightControl(event);
						}
					}
				});
		return;
	}

}
