package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.utils.Lang;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import com.useful.ucars.ClosestFace;

public class FlightControl {
	private static double speed = 1.2;
	public static Vector route(Location targetLoc, Location current, Vehicle vehicle){
		Vector v = new Vector(0,0,0);
		Entity passenger = vehicle.getPassenger();
		if(!(passenger instanceof Player)){
			vehicle.removeMetadata("plane.destination", main.plugin);
			return v;
		}
		
		double targetHeight = 115;
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
		Block next2 = next.getRelative(direction);
		
		if(!next.isEmpty()){
			asc = true;
			x = -x * 0.25;
			z = -z * 0.25;
			y = 1; //Go up fast
		}
		else if(!next2.isEmpty()){
			asc = true;
			x = -x * 0.25;
			z = -z * 0.25;
			y = 1; //Go up fast
		}
		else if(!next2.getRelative(direction).isEmpty()){
			asc = true;
			x *= 0.25;
			z *= 0.25;
			y = 0.6; //Go up
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
				player.sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.destinations.arrive"));
				vehicle.removeMetadata("plane.destination", main.plugin);
			}
		}
		Vector vel = new Vector(x, y, z);
		vehicle.setVelocity(vel);
		return vel;
	}

}
