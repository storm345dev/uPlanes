package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.AutopilotDestination;
import net.stormdev.uPlanes.hover.HoverCart;
import net.stormdev.uPlanes.utils.CartOrientationUtil;
import net.stormdev.uPlanes.utils.ClosestFace;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.PEntityMeta;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class FlightControl {
	private static double speed = 1.2;
	public static Vector route(Location targetLoc, Location current, final Vehicle vehicle){
		Vector v = new Vector(0,0,0);
		Entity passenger = vehicle.getPassenger();
		AutopilotDestination aData = null;
		
		if(PEntityMeta.hasMetadata(vehicle, "plane.autopilotData")){
			aData = (AutopilotDestination) PEntityMeta.getMetadata(vehicle, "plane.autopilotData").get(0).value();
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
		
		Player player = (Player) passenger;
		
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
		Boolean asc = false;
		if(current.getY()<targetHeight){
			asc = true;
			y = 0.6; //Go up
		}
		if((!b.isEmpty()
				|| !under.isEmpty()
				|| !underunder.isEmpty())
				&& b.getRelative(BlockFace.UP).isEmpty()
				&& b.getRelative(BlockFace.UP,2).isEmpty() && !asc){
			asc = true;
			y = 0.6; //Go up
		}
		
		Block next = current.add(toGo).getBlock();
		
		for(int i=0;i<5;i++){
			Block n = next;
			if(i > 0){
				n = next.getRelative(direction, i);
			}
			if(!n.isEmpty()){
				asc = true;
				x = -x * 0.25;
				z = -z * 0.25;
				y = 1; //Go up fast
				break;
			}
		}
		if(asc
				&& (!next.getRelative(BlockFace.UP).isEmpty()
						|| !next.getRelative(BlockFace.UP, 2).isEmpty()
						|| !next.getRelative(BlockFace.UP, 3).isEmpty()
						|| !next.getRelative(BlockFace.UP, 4).isEmpty()
				|| !left.isEmpty()
				|| !right.isEmpty())){
			z = -toGo.getZ();
			x = -toGo.getX();
			y = 0;
		}
		if(pz < 1 && px < 1){
			x = 0;
			z = 0;
			y = -0.6; //Down
			if(current.getY() < targetLoc.getY()){
				y = 0.6; //Ascend
			}
			else if(current.getY() - targetLoc.getY() < 2){
				//Arrived
				AccelerationManager.setCurrentAccel(vehicle, 1.0);
				vehicle.setVelocity(new Vector(0,0,0));
				PEntityMeta.setMetadata(vehicle, "arrivedAtDest", new StatValue(null, main.plugin));
				AccelerationManager.setCurrentAccel(vehicle, 0);
				PEntityMeta.removeMetadata(vehicle, "plane.destination");
				Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){

					@Override
					public void run() {
						PEntityMeta.removeMetadata(vehicle, "arrivedAtDest");
						return;
					}}, 5l);
				
				if(aData == null){
					player.sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.destinations.arrive"));
				}
				else{
					aData.arrivedAtDestination();
					PEntityMeta.removeMetadata(vehicle, "plane.autopilotData");
				}
			}
		}
		Vector vel = new Vector(x, y, z);
		Vector behind = vel.clone().multiply(-2);
		Location back = current.add(behind);
		back.getWorld().playEffect(back, Effect.SMOKE, 1);
		vehicle.setVelocity(vel.clone().multiply(0.5));
		float vYaw = (float) Math.toDegrees(Math.atan2(toGo.getX() , -toGo.getZ())); 
		CartOrientationUtil.setYaw(vehicle, vYaw-90);
		return vel;
	}

}
