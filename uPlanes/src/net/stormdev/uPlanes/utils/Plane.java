package net.stormdev.uPlanes.utils;

import java.io.Serializable;
import java.util.UUID;

public class Plane implements Serializable {
	private static final long serialVersionUID = 1L;
	public double mutliplier = 30;
	public String name = "Plane";
	public UUID id = UUID.randomUUID();
	public Boolean isPlaced = false;
}
