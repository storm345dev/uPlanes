package net.stormdev.uPlanes.api;

import org.bukkit.entity.Vehicle;

public interface PlaneTurningModifier {
	/**
	 * Get a turning amount for a cart, eg. if you return 2x original the plane will turn twice as fast
	 * @param cart The cart they're riding in
	 * @param original The amount already calculated by uPlanes
	 * @return the turn amount per tick in degrees (positive)
	 */
	public double getTurnAmountPerTick(Vehicle cart, double original);
}
