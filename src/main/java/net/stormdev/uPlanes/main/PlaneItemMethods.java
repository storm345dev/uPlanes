package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.Plane;

import org.bukkit.inventory.ItemStack;

public class PlaneItemMethods {
	public static ItemStack getItem(Plane plane){
		return plane.toItemStack();
	}
}
