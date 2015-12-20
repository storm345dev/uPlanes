package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.api.uPlanesAPI;
import net.stormdev.uPlanes.utils.PEntityMeta;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public class AccelerationManager {
	public static final String ACCEL_META = "uPlanes.accel";
	
	public static class AccelMeta {
		private double current = 0;
		private long time = 0;
		public AccelMeta(double accel){
			this.setCurrent(accel);
			this.setTime(System.currentTimeMillis());
		}
		public double getCurrent() {
			this.time = System.currentTimeMillis();
			return current;
		}
		public void setCurrent(double current) {
			this.time = System.currentTimeMillis();
			this.current = current;
		}
		public long getTime() {
			return time;
		}
		public void setTime(long time) {
			this.time = time;
		}
	}
	
	public static void lastAccelTimeNow(Vehicle cart){
		getMeta(cart).setTime(System.currentTimeMillis());
	}
	
	private static AccelMeta getMeta(Vehicle cart){
		if(!PEntityMeta.hasMetadata(cart, ACCEL_META)){
			AccelMeta am = new AccelMeta(0);
			PEntityMeta.setMetadata(cart, ACCEL_META, new StatValue(am, main.plugin));
			return am;
		}
		try {
			return (AccelMeta) PEntityMeta.getMetadata(cart, ACCEL_META).get(0).value();
		} catch (Exception e) {
			PEntityMeta.removeMetadata(cart, ACCEL_META);
			AccelMeta am = new AccelMeta(0);
			PEntityMeta.setMetadata(cart, ACCEL_META, new StatValue(am, main.plugin));
			return am;
		}
	}
	
	private static double getCurrentAccel(Vehicle cart){
		AccelMeta am = getMeta(cart);
		/*if(System.currentTimeMillis() - am.getTime() > 500){
			am.setCurrent(0);
			return 0;
		}*/
		return am.getCurrent();
	}
	
	public static void setCurrentAccel(Vehicle cart, double accel){
		getMeta(cart).setCurrent(accel);
	}
	
	private static float getA(Player player, Vehicle cart, Plane plane){ //Get the multiplier for accelerating
		return (float) (0.020*uPlanesAPI.getPlaneManager().getAlteredAccelerationMod(player, cart, plane)); //Our constant of 0.025 multiplied by whatever the API is asking for as a modification to the rate of acceleration
	}
	
	private static float getDA(Player player, Vehicle cart, Plane plane){ //Get the multiplier for accelerating
		return (float) (0.030*uPlanesAPI.getPlaneManager().getAlteredDecelerationMod(player, cart, plane)); //Our constant of 0.025 multiplied by whatever the API is asking for as a modification to the rate of acceleration
	}
	
	private static float getGA(Player player, Vehicle cart, Plane plane){ //Get the multiplier for accelerating
		return (float) (0.020*uPlanesAPI.getPlaneManager().getAlteredDecelerationMod(player, cart, plane)); //Our constant of 0.025 multiplied by whatever the API is asking for as a modification to the rate of acceleration
	}
	
	public static long getTimeSinceLastKeypress(Vehicle cart){
		return System.currentTimeMillis()-getMeta(cart).getTime();
	}
	
	public static double stall(Player player, Vehicle cart, Plane plane){
		if(!main.doAcceleration){
			return 0d;
		}
		
		double accel = getCurrentAccel(cart);
		accel *= 0.25;
		
		setCurrentAccel(cart, accel);
		return accel;
	}
	
	public static double decelerateAndGetMult(Player player, Vehicle cart, Plane plane){
		if(!main.doAcceleration){
			return 0d;
		}
		
		double current = getCurrentAccel(cart);
		float diff = (float) (1-current); //The difference between 1 (full speed) and the rate we want to accelerate by
		if(diff > 0.99){
			diff = 0.99f;
		}
		current -= (plane.getAccelMod()*getDA(player, cart, plane)*diff); //Increase the speed by 'a' multiplied by the difference; eg. accelerates faster the slower the vehicle moves (Looks quite realistic)
		if(current <= 0.05){ //Close enough to 0
			current = 0;
		}
		setCurrentAccel(cart, current);
		return current;
	}
	
	public static double glideAndGetMult(Player player, Vehicle cart, Plane plane){
		if(!main.doAcceleration){
			return 0d;
		}
		
		double current = getCurrentAccel(cart);
		float diff = (float) (1-current); //The difference between 1 (full speed) and the rate we want to accelerate by
		if(diff > 0.99){
			diff = 0.99f;
		}
		current -= (getGA(player, cart, plane)*diff); //Increase the speed by 'a' multiplied by the difference; eg. accelerates faster the slower the vehicle moves (Looks quite realistic)
		if(current <= 0.05){ //Close enough to 0
			current = 0;
		}
		setCurrentAccel(cart, current);
		return current;
	}
	
	public static double getCurrentMultiplier(Vehicle cart){
		if(!main.doAcceleration){
			return 1.0d;
		}
		
		return getCurrentAccel(cart);
	}
	
	public static double getMultiplier(Player player, Vehicle cart, Plane plane){
		if(!main.doAcceleration){
			return 1.0d;
		}
		
		double current = getCurrentAccel(cart);
		if(current >= 0.97){ //Close enough to 1; so just be 1 or else you get infinitely close to 1 without getting to it (Wasting time calculating for no visible reason)
			current = 1;
			return current;
		}
		float diff = (float) (1-current); //The difference between 1 (full speed) and the rate we want to accelerate by
		current += (plane.getAccelMod()*getA(player, cart, plane)*diff); //Increase the speed by 'a' multiplied by the difference; eg. accelerates faster the slower the vehicle moves (Looks quite realistic)
		setCurrentAccel(cart, current);
		return current;
	}
}
