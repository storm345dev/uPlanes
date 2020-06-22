package net.stormdev.uPlanes.api;

import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public interface AccelerationModifier<T extends uPlanesVehicle> {
	/**
	 * Get an acceleration mutliplier for a player; eg. '2' would mean accelerate 2x the normal rate
	 * @param player The player
	 * @param cart The cart they're riding in
	 * @param vehicle The vehicle
	 * @return the multiplier
	 */
	public double getAccelerationMultiplier(Player player, Vehicle cart, T vehicle);
}
