package net.stormdev.uPlanes.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import net.stormdev.uPlanes.api.AutopilotDestination;
import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.utils.CartOrientationUtil;
import net.stormdev.uPlanes.utils.ClosestFace;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.PEntityMeta;
import net.stormdev.uPlanes.utils.StatValue;

public class FlightControl {
	/*private static double speed = 1.2;*/
	
	private static class PosTracking {
		private Vector lastPos;
		private long lastMoveTime;
		private Vector lastAscPos = null;
		private boolean pitchForward = true;
		private float pitch = 0;
		
		public PosTracking(Vector nowPos){
			setLastPos(nowPos);
			setLastMoveTime(System.currentTimeMillis());
		}

		public Vector getLastPos() {
			return lastPos;
		}

		public void setLastPos(Vector lastPos) {
			this.lastPos = lastPos;
		}

		public long getLastMoveTime() {
			return lastMoveTime;
		}

		public void setLastMoveTime(long lastMoveTime) {
			this.lastMoveTime = lastMoveTime;
		}

		public Vector getLastAscPos() {
			return lastAscPos;
		}

		public void setLastAscPos(Vector lastAscPos) {
			this.lastAscPos = lastAscPos;
		}
	}
	
	public static double getSpeed(Vehicle veh, Plane plane, int motion, PosTracking pt){
		if(plane == null){
			return 1.2;
		}
		
		double accelMod = 1;
		
		switch(motion){
			case -1: {
				accelMod = AccelerationManager.decelerateAndGetMult(null, veh, plane);
			}
			case 1: {
				accelMod = AccelerationManager.getMultiplier(null, veh, plane, false);
			}
			case 0: {
				accelMod = AccelerationManager.getCurrentMultiplier(veh);
			}
		}
		
		float pitch = (float) (20f*accelMod);
		if(pitch > 10f){
			pitch = 10f;
		}
		if(pt.pitchForward){
			if(pt.pitch < pitch){
				pt.pitch += 0.75f;
				if(pt.pitch > pitch){
					pt.pitch = pitch;
				}
			}
			pitch = pt.pitch;
		}
		else {
			pitch = pt.pitch;
			pitch -= 0.75f;
			if(pitch <= 0){
				pitch = 0;
			}
			pt.pitch = pitch;
		}
		plane.setCurrentPitch((float) (pitch));
		CartOrientationUtil.setPitch(veh, (float) pitch);
		
		double s = plane.getSpeed() * accelMod;
		return Math.min(s, 1.2);
	}
	
	public static Vector route(Location targetLoc, Location current, final Vehicle vehicle, Plane plane){
		double ascSpeed = Math.max(0.9, Math.min(0.6, (plane == null ? 1:plane.getSpeed()) / 50.0d));
		Vector v = new Vector(0,0,0);
		Entity passenger = vehicle.getPassenger();
		AutopilotDestination aData = null;
		PosTracking posTracking;
		if(PEntityMeta.hasMetadata(vehicle, "plane.autopilotPosTracking")){
			posTracking = (PosTracking) PEntityMeta.getMetadata(vehicle, "plane.autopilotPosTracking").get(0).value();
		}
		else {
			posTracking = new PosTracking(current.toVector());
			PEntityMeta.setMetadata(vehicle, "plane.autopilotPosTracking", new StatValue(posTracking, main.plugin));
		}
		double speed = getSpeed(vehicle, plane, 1, posTracking);
		
		if(current.toVector().distanceSquared(posTracking.getLastPos()) > 5){
			posTracking.setLastPos(current.toVector());
			posTracking.setLastMoveTime(System.currentTimeMillis());
		}
		
		if(PEntityMeta.hasMetadata(vehicle, "plane.autopilotData")){
			aData = (AutopilotDestination) PEntityMeta.getMetadata(vehicle, "plane.autopilotData").get(0).value();
			targetLoc = aData.getDestination().clone();
		}
		
		if(System.currentTimeMillis() - posTracking.getLastMoveTime() > 5000){
			if(aData != null){
				aData.autopilotStuck();
			}
		}
		
		if(!(passenger instanceof Player)){
			boolean cont = false;
			if(aData != null){
				cont = aData.flyWithoutPlayer();
			}
			
			if(!cont){
				if(aData != null){
					aData.autoPilotCancelled();
				}
				AccelerationManager.setCurrentAccel(vehicle, 0);
				PEntityMeta.removeMetadata(vehicle, "plane.destination");
				PEntityMeta.removeMetadata(vehicle, "plane.autopilotData");
				PEntityMeta.removeMetadata(vehicle, "plane.autopilotPosTracking");
				return v;
			}
		}
		
		double targetHeight = 115;
		
		if(aData != null && aData.useCustomCruiseAltitude()){
			targetHeight = aData.getTargetCruiseAltitude();
		}
		
		if(targetHeight < targetLoc.getY()){
			targetHeight = targetLoc.getY()+30;
		}
		
		/*Player player = (Player) passenger;*/
		
		BlockFace direction = ClosestFace.getClosestFace(current.getYaw());
		
		// Calculate vector
		
		double x = targetLoc.getX() - current.getX();
		double z = targetLoc.getZ() - current.getZ();
		Boolean ux = true;
		double px = Math.abs(x);
		double pz = Math.abs(z);
		if (px > pz) {
			ux = false;
		}

		if (ux) {
			// x is smaller
			// long mult = (long) (pz/speed);
			x = (x / pz) * speed;
			z = (z / pz) * speed;
		} else {
			// z is smaller
			// long mult = (long) (px/speed);
			x = (x / px) * speed;
			z = (z / px) * speed;
		}
		Vector toGo = new Vector(x,0,z);
		double y = 0;
		//y calculations
		Block b = current.getBlock();
		Block right = b.getRelative(DirectionUtil.getRightOf(direction));
		Block left = b.getRelative(DirectionUtil.getLeftOf(direction));
		Block under = b.getRelative(BlockFace.DOWN);
		Block underunder = under.getRelative(BlockFace.DOWN);
		boolean asc = false;
		boolean desc = false;
		
		posTracking.pitchForward = true;
		if(current.getY()<targetHeight){
			asc = true;
			desc = false;
			y = ascSpeed; //Go up
		}
		if(current.getY() > targetHeight + 2
				&& (
						b.isEmpty() && under.isEmpty()
						&& underunder.isEmpty()
						&& (posTracking.getLastAscPos() == null || current.toVector().distanceSquared(posTracking.getLastAscPos()) > 9)
						)){
			posTracking.setLastAscPos(null);
			asc = false;
			desc = true;
			y = -ascSpeed; //Go down
		}
		if((!b.isEmpty()
				|| !under.isEmpty()
				|| !underunder.isEmpty())
				&& b.getRelative(BlockFace.UP).isEmpty()
				&& b.getRelative(BlockFace.UP,2).isEmpty() && !asc){
			asc = true;
			desc = false;
			y = ascSpeed; //Go up
		}
		
		Block next = current.add(toGo).getBlock();
		
		Vector up = new Vector(0, 1, 0);
		Vector forwards = toGo.clone().setY(0).normalize();
		Vector rightV = forwards.clone().crossProduct(up);
		Vector leftV = rightV.clone().multiply(-1);
		
		for(int i=0;i<5;i++){
			List<Block> bls = new ArrayList<Block>();
			Block n = next;
			if(i > 0){
				n = next.getRelative(direction, i);
			}
			bls.add(n);
			bls.add(n.getLocation().clone().add(leftV).getBlock());
			bls.add(n.getLocation().clone().add(rightV).getBlock());
			for(Block n1:bls){
				if(!n1.isEmpty()){
					asc = true;
					desc = false;
					x = -x * 0.25;
					z = -z * 0.25;
					posTracking.pitchForward = false;
					y = ascSpeed*2; //Go up fast
					break;
				}
			}
		}
		if(desc && !(
				next.isEmpty()
				|| next.getRelative(BlockFace.DOWN).isEmpty()
				|| next.getLocation().clone().add(toGo).getBlock().isEmpty()
				)
				){
			desc = false;
			asc = true;
			y = ascSpeed;
		}
		if(asc
				&& (!next.getRelative(BlockFace.UP).isEmpty()
						|| !next.getRelative(BlockFace.UP, 2).isEmpty()
						|| !next.getRelative(BlockFace.UP, 3).isEmpty()
						|| !next.getRelative(BlockFace.UP, 4).isEmpty()
				/*|| !left.isEmpty()
				|| !right.isEmpty()*/)){
			desc = false;
			asc = false;
			z = -toGo.getZ();
			x = -toGo.getX();
			posTracking.pitchForward = false;
			y = 0;
		}
		if(pz < 1 && px < 1){
			x = 0;
			z = 0;
			posTracking.pitchForward = false;
			y = -ascSpeed; //Down
			if(current.getY() < targetLoc.getY()){
				y = ascSpeed; //Ascend
			}
			else if(Math.abs(current.getY() - targetLoc.getY()) < 2){
				//Arrived
				AccelerationManager.setCurrentAccel(vehicle, 1.0);
				vehicle.setVelocity(new Vector(0,0,0));
				AccelerationManager.setCurrentAccel(vehicle, 0);
				if(aData != null && aData.isEndedWhenArrive()){
					PEntityMeta.setMetadata(vehicle, "arrivedAtDest", new StatValue(null, main.plugin));
					
					PEntityMeta.removeMetadata(vehicle, "plane.destination");
					PEntityMeta.removeMetadata(vehicle, "plane.autopilotPosTracking");
					Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){
	
						@Override
						public void run() {
							PEntityMeta.removeMetadata(vehicle, "arrivedAtDest");
							return;
						}}, 5l);
				}
				
				if(aData == null){
					if(passenger instanceof Player){
						((Player)passenger).sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.destinations.arrive"));
						PEntityMeta.removeMetadata(vehicle, "plane.destination");
					}
				}
				else{
					posTracking.setLastMoveTime(System.currentTimeMillis());
					aData.arrivedAtDestination();
					if(aData.isEndedWhenArrive()){
						PEntityMeta.removeMetadata(vehicle, "plane.autopilotData");
						aData.onEnd();
						PEntityMeta.removeMetadata(vehicle, "plane.destination");
					}
					else {
						y = targetLoc.getY() - current.getY();
					}
				}
			}
		}
		else {
			float vYaw = (float) Math.toDegrees(Math.atan2(toGo.getX() , -toGo.getZ())); 
			CartOrientationUtil.setYaw(vehicle, vYaw-90);
		}
		Vector vel = new Vector(x, y, z);
		Vector behind = vel.clone().multiply(-2);
		Location back = current.add(behind);
		back.getWorld().playEffect(back, Effect.SMOKE, 1);
		Vector toMove = vel.clone().multiply(0.5);
		vehicle.setVelocity(toMove);
		if(asc){
			posTracking.setLastAscPos(vehicle.getLocation().toVector().clone());
		}
		return vel;
	}

}
