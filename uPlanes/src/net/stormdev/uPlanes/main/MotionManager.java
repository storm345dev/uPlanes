package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.utils.Keypress;
import net.stormdev.uPlanes.utils.PlaneUpdateEvent;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MotionManager {

	public static void move(Player player, float f, float s) {
		if(f == 0 && s == 0){
			//Not moving
			return;
		}
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
		if(!main.plugin.listener.isAPlane(plane)){
			//Not a plane
			return;
		}
		Vector plaD = player.getEyeLocation().getDirection();
		Keypress pressed = Keypress.W;
		Boolean forwardMotion = true;
		if(f==0){
			forwardMotion = false;
		}
		Boolean forwards = false; // if true, forwards, else backwards
		int side = 0; // -1=left, 0=straight, 1=right
		Boolean turning = false;
		if (f < 0) {
			forwards = false;
		} else {
			forwards = true;
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
		if(!forwards){
			pressed = Keypress.S;
			x =  - x;
			z =  - z;
		}
		if(!forwardMotion){
			x = 0;
			z = 0;
		}
		vec = new Vector(x, y, z);
		final PlaneUpdateEvent event = new PlaneUpdateEvent(plane, vec, player, pressed);
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
