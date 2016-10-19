package net.stormdev.uPlanes.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.stormdev.uPlanes.hover.HoverCart;
import net.stormdev.uPlanes.hover.HoverCartEntity;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;

public class CartOrientationUtil { //TODO Convert yaw to eular angle
	public static void setPitch(Vehicle cart, float pitch){
		if(!(cart instanceof Minecart)){
			HoverCart hc = HoverCartEntity.getCart(cart);
			if(hc != null){
				hc.setPitch(pitch);
			}
			return;
		}
		try {
			Class<?> cmr = cart.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("EntityMinecartAbstract");
			Object nmsCart = getHandle.invoke(cmr.cast(cart));
			Field p = ema.getField("pitch");
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), -pitch);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setRoll(Vehicle cart, float roll){
		if(!(cart instanceof Minecart)){
			HoverCart hc = HoverCartEntity.getCart(cart);
			if(hc != null){
				hc.setRoll(roll);
			}
			return;
		}
	}
	
	public static void setYaw(Vehicle cart, float yaw){
		if(!(cart instanceof Minecart)){
			HoverCart hc = HoverCartEntity.getCart(cart);
			if(hc != null){
				hc.setYaw(yaw);
			}
			return;
		}
		try {
			Class<?> cmr = cart.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("EntityMinecartAbstract");
			Object nmsCart = getHandle.invoke(cmr.cast(cart));
			Field p = ema.getField("yaw");
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), yaw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
