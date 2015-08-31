package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.AutopilotDestination;
import net.stormdev.uPlanes.utils.ClosestFace;
import net.stormdev.uPlanes.utils.Lang;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public class FlightControl {
	private static double speed = 1.2;
	public static Vector route(Location targetLoc, Location current, Vehicle vehicle){
		Vector v = new Vector(0,0,0);
		Entity passenger = vehicle.getPassenger();
		AutopilotDestination aData = null;
		
		if(vehicle.hasMetadata("plane.autopilotData")){
			aData = (AutopilotDestination) vehicle.getMetadata("plane.autopilotData").get(0).value();
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
				vehicle.removeMetadata("plane.destination", main.plugin);
				vehicle.removeMetadata("plane.autopilotData", main.plugin);
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
		boolean ux = true;
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
				|| !left.isEmpty()
				|| !right.isEmpty())){
			z = -toGo.getZ();
			x = -toGo.getX();
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
				vehicle.removeMetadata("plane.destination", main.plugin);
				
				if(aData == null){
					player.sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.destinations.arrive"));
				}
				else{
					aData.arrivedAtDestination();
					vehicle.removeMetadata("plane.autopilotData", main.plugin);
				}
			}
		}
		Vector vel = new Vector(x, y, z);
		Vector behind = vel.clone().multiply(-2);
		Location back = current.add(behind);
		back.getWorld().playEffect(back, Effect.SMOKE, 1);
		vehicle.setVelocity(vel);
		return vel;
	}

}
