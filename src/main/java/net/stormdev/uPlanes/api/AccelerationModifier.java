package net.stormdev.uPlanes.api;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public interface AccelerationModifier {
	/**
	 * Get an acceleration mutliplier for a player; eg. '2' would mean accelerate 2x the normal rate
	 * @param player The player
	 * @param cart The cart they're riding in
	 * @param vehicle The vehicle
	 * @return the multiplier
	 */
	public double getAccelerationMultiplier(Player player, Minecart cart, Plane vehicle);
}
