package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.*;
import net.stormdev.uPlanes.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import java.util.*;

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
	
	public static class MovePacketInfo {
		public float f = 0;
		public float s = 0;
		public boolean jumping = false;
		
		public MovePacketInfo(float f, float s, boolean jumping){
			this.f = f;
			this.s = s;
			this.jumping = jumping;
		}
	}
	private static Map<UUID, MovePacketInfo> playerPacketUpdates = new HashMap<UUID, MovePacketInfo>();
	
	public static void removePlayer(Player player){
		playerPacketUpdates.remove(player.getUniqueId());
	}
	
	public static void onPacket(Player player, float f, float s, boolean jumping){
		if(!player.isOnline()){
			return;
		}
		playerPacketUpdates.put(player.getUniqueId(), new MovePacketInfo(f, s, jumping));
	}
	
	public static MovePacketInfo getMostRecentPacketInfo(Player player){
		MovePacketInfo m = playerPacketUpdates.get(player.getUniqueId());
		if(m == null){
			m = new MovePacketInfo(0, 0, false);
		}
		return m;
	}

	public static void moveBoat(Vehicle vehicle, Boat boat, boolean hasPassenger){
		//Called every single game tick
		//Bobbing and vehicle dynamics
		BoatState state = boat.getBoatState();
		if(state == null){
			state = new BoatState(boat);
			boat.setBoatState(state);
		}
		if(!hasPassenger){
			state.setThrottleAmt(0); //No need to throttle
		}

		state.updateVelocitiesYawForThisGameTick(vehicle);

		//Update yaw, pitch, roll
		CartOrientationUtil.setYaw(vehicle, (float) state.getCurYaw()+90);
		//Bukkit.broadcastMessage("Setting pitch to "+boat.getCurrentPitch());
		CartOrientationUtil.setPitch(vehicle,boat.getCurrentPitch());
		CartOrientationUtil.setRoll(vehicle,boat.getRoll());

		//Update velocity
		Vector vel = state.getVelBlocksPerSec().clone().multiply(1/20.0);
		vehicle.setVelocity(vel);
	}

	private static void moveBoat(Player player, Vehicle vehicle, Boat boat, List<Keypress> keysPressed){
		BoatState state = boat.getBoatState();
		//Throttle control
		if(keysPressed.contains(Keypress.W)){
			double newThrottle = state.getThrottleAmt()+0.1;
			if(newThrottle > 1){
				newThrottle = 1;
			}
			state.setThrottleAmt(newThrottle);
		}
		else if(keysPressed.contains(Keypress.S)){
			double newThrottle = state.getThrottleAmt()-0.1;
			if(newThrottle < -0.5){
				newThrottle = -0.5;
			}
			state.setThrottleAmt(newThrottle);
		}
		else { //No key pressed
			double newThrottle = state.getThrottleAmt();
			if(newThrottle > 0.1){
				newThrottle = newThrottle-0.1;
			}
			else if(newThrottle < -0.1){
				newThrottle = newThrottle+0.1;
			}
			else {
				newThrottle = 0;
			}
			state.setThrottleAmt(newThrottle);
		}

		//Steering control
		double newSteeringAngle = state.getThrustYawOffsetAngleDeg(); //Positive anticlockwise
		double maxSteeringAngle = BoatState.MAX_STEERING_ANGLE; //25 degrees
		double steerPerTick = (uPlanesAPI.getBoatManager().getAlteredRotationAmountPerTick(player,vehicle,boat)/boat.getTurnAmountPerTick())*10/20.0;
		if(keysPressed.contains(Keypress.D)){
			newSteeringAngle = newSteeringAngle + steerPerTick;
			if(newSteeringAngle > maxSteeringAngle){
				newSteeringAngle = maxSteeringAngle;
			}
		} else if(keysPressed.contains(Keypress.A)){
			newSteeringAngle = newSteeringAngle - steerPerTick;
			if(newSteeringAngle < -maxSteeringAngle){
				newSteeringAngle = -maxSteeringAngle;
			}
		} else {
			if(newSteeringAngle > steerPerTick){
				newSteeringAngle = newSteeringAngle-steerPerTick;
			}
			else if(newSteeringAngle < -steerPerTick){
				newSteeringAngle = newSteeringAngle+steerPerTick;
			}
			else {
				newSteeringAngle = 0;
			}
		}
		state.setThrustYawOffsetAngleDeg(newSteeringAngle);

		//Called every single game tick when player is inside
		moveBoat(vehicle,boat, true);

		final BoatUpdateEvent event = new BoatUpdateEvent(vehicle, boat.getBoatState().getVelBlocksPerSec().clone().multiply(1/20.0), player, keysPressed, boat);

		main.plugin.getServer().getScheduler()
				.runTask(main.plugin, new Runnable() {
					public void run() {
						if(main.fireUpdateEvent){
							main.plugin.getServer().getPluginManager()
									.callEvent(event);
						}
						else {
							main.plugin.listener.boatControl(event);
						}
					}
				});
	}
	
	public static void move(Player player, float f, float s, boolean jumping) {
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
		uPlanesVehicle pluginVehicle = main.plugin.listener.getPluginVehicle(plane);
		Plane pln = main.plugin.listener.getPlane(plane);
		if(pluginVehicle == null){
			return;
		}
		
		if(plane.hasMetadata("plane.frozen") || PEntityMeta.hasMetadata(plane, "plane.frozen")){
			return;
		}

		List<Keypress> pressedKeys = new ArrayList<Keypress>();
		if(jumping){
			pressedKeys.add(Keypress.JUMP);
		}
		Keypress speedModKey = Keypress.NONE;
		int side = 0; // -1=left, 0=straight, 1=right
		boolean turning = false;
		boolean decel = true;
		if(f > 0){
			speedModKey = Keypress.W;
			pressedKeys.add(speedModKey);
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
		boolean isRight = false;
		boolean isLeft = false;
		if (turning) {
			if (side < 0) {// do left action
				isLeft = true;
				pressedKeys.add(Keypress.A);
				PEntityMeta.setMetadata(plane, "plane.left", new StatValue(true, main.plugin));
			} else if (side > 0) {// do right action
				isRight = true;
				pressedKeys.add(Keypress.D);
				PEntityMeta.setMetadata(plane, "plane.right", new StatValue(true, main.plugin));
			}
		}
		if (!isRight) {
			PEntityMeta.removeMetadata(plane, "plane.right");
		}
		if (!isLeft) {
			PEntityMeta.removeMetadata(plane, "plane.left");
		}
		if(f < 0 && (pln == null || pln.isHover())){
			speedModKey = Keypress.S;
			pressedKeys.add(Keypress.S);
		}

		if(pln == null){ //Not a plane
			if(pluginVehicle.getType().equals(uPlanesVehicle.VehicleType.BOAT)){
				moveBoat(player,plane,(Boat) pluginVehicle,pressedKeys);
			}
			return;
		}
		

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
		
		if(PEntityMeta.hasMetadata(plane, "plane.destination")){
			planeDirection = playD.clone().setY(0).normalize();
		}
		else if(!main.doTurningCircles){
			if((AccelerationManager.getCurrentMultiplier(plane) >= 0.2 || pln.isHover()) && !PEntityMeta.hasMetadata(plane, "plane.destination")){
				planeDirection = playD.clone().setY(0).normalize();
				float vYaw = (float) Math.toDegrees(Math.atan2(planeDirection.getX() , -planeDirection.getZ()));
				CartOrientationUtil.setYaw(plane, vYaw-90);
			}
		}
		else {
			try {
				planeDirection = (Vector) PEntityMeta.getMetadata(plane, "plane.direction").get(0).value();
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
			if((am >= 0.2 || ((pln.isHover()&&am>0)||(pln.canPlaneHoverMidair()&&!pln.isHover()&&!plane.isOnGround()))) && !PEntityMeta.hasMetadata(plane, "plane.destination")){
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
			PEntityMeta.removeMetadata(plane, "plane.direction");
			PEntityMeta.setMetadata(plane, "plane.direction", new StatValue(planeDirection.clone().normalize(), main.plugin));
		}
		

		/*if(f==0 && pln.isHover()){
			forwardMotion = false;
		}*/

		if(jumping && !pln.isHover()){
			decel = false;
			if(!pln.isSpeedLocked()){
				pln.setSpeedLocked(true);
				player.sendMessage(ChatColor.YELLOW+"Thrust locked at "+Math.round(AccelerationManager.getCurrentMultiplier(plane)*100)+"%. Unlock it again with w.");
				pln.setSpeedLockTime(System.currentTimeMillis());
			}
		}
		if(f != 0 && pln.isSpeedLocked() && !pln.isHover() && !jumping
				&& System.currentTimeMillis()-pln.getSpeedLockTime() > 500){
			pln.setSpeedLocked(false);
			player.sendMessage(ChatColor.YELLOW+"Thrust unlocked. Lock it again with spacebar.");
		}

		double d = 27; // A number that happens to get realistic motion

		if(pln.isSpeedLocked()){
			decel = false;
		}
		
		double accelMod = 1;
		
		if(!PEntityMeta.hasMetadata(plane, "plane.destination")){
			accelMod = !decel ? AccelerationManager.getMultiplier(player, plane, pln, pln.isSpeedLocked()) 
					: (f == 0 ? AccelerationManager.decelerateAndGetMult(player, plane, pln) 
							: AccelerationManager.decelerateAndGetMult(player, plane, pln));
			if(f < 0 /*&& pln.isHover()*/){
				accelMod = AccelerationManager.decelerateAndGetMult(player, plane, pln); //Decelerate faster by calling again
			}
		}
		
		float hoverAmount = (float) (0.0002 * pln.getSpeed());
		if(hoverAmount < 0.005){
			hoverAmount = 0.005f;
		}
		if(hoverAmount > 0.02){
			hoverAmount = 0.02f;
		}

		if (turning) {
			if (side < 0) {// do left action
				if(pln.isHover()){
					y = hoverAmount;
				}
			} else if (side > 0) {// do right action
				if(pln.isHover()){
					y = -hoverAmount;
				}
			}
		}
		
		double x = planeDirection.getX() / d;
		double z = planeDirection.getZ() / d;

		/*if(!forwardMotion && pln.isHover()){
			x = 0;
			z = 0;
		}*/

		double xOriginal = x;
		double zOriginal = z;

		x *= accelMod;
		z *= accelMod;
		
		double speedLength = pln.getSpeed() * accelMod;
		boolean hasFlightSpeed = (speedLength > 10 || accelMod > 0.87);
		boolean planeFloats = false;
		if(pln.canPlaneHoverMidair()){
		    planeFloats = !hasFlightSpeed;
		    hasFlightSpeed = true;
        }
		if(pln.isHover()){
			double pitch = 20*accelMod;
			if(pitch > 10){
				pitch = 10;
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
				else if(pressedKeys.contains(Keypress.D) && (!plane.isOnGround() || pln.getCurrentPitch() > 0)){
					vertModKey = Keypress.D;
				}
				
				if(vertModKey != Keypress.NONE){
					float pitch = pln.getCurrentPitch();
					float change = (float) (planeFloats?0.5:2);
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

		if(planeFloats && vec.clone().setY(0).lengthSquared() < 1){
		    //Hover forwards / backwards in line with pitch
            double m = Math.sin(pln.getCurrentPitch()*(Math.PI/180.0));
            double yAmt = (m*m)*0.005;
            if(m > 0){
                yAmt = - yAmt;
            }
            if(Math.abs(pln.getCurrentPitch()) < 45){
                yAmt = 0;
            }
            Vector floatVec = new Vector(xOriginal, 0, zOriginal);
            floatVec.multiply(m);
            floatVec.setY(yAmt);
            vec.add(floatVec);
        }

		/*if(!speedModKey.equals(Keypress.NONE)){
			pressedKeys.add(speedModKey);
		}*/

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
