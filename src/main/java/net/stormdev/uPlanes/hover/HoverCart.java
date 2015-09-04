package net.stormdev.uPlanes.hover;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;

public interface HoverCart extends ArmorStand,Vehicle {
	public void setDisplay(ItemStack stack, double d);
	public double getDisplayOffset();
	public void setYaw(float yaw);
	public void setPitch(float pitch);
	public void setRoll(float roll);
	public void setYawPitch(float yaw, float pitch);
}
