package net.stormdev.uPlanes.utils;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Plane implements Serializable {
	private static final long serialVersionUID = 1L;
	public double mutliplier = 30;
	public String name = "Plane";
	public double health = 50;
	public ConcurrentHashMap<String, Stat> stats = new ConcurrentHashMap<String, Stat>();
	public UUID id = UUID.randomUUID();
	public Boolean isPlaced = false;
}
