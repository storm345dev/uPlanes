package net.stormdev.uPlanes.api;

import org.bukkit.plugin.Plugin;

import java.io.Serializable;

/**
 * A simple plane stat class used for adding data to the plane
 * 
 *
 */
public class Stat implements Serializable {
	private static final long serialVersionUID = -7275161546527988371L;
	private Object val = null;
	private String statName = "";
	private boolean display = false;
	public Stat(String statName, Object val, Plugin plugin, Boolean display){
		this.val = val;
		this.statName = statName;
		this.display = display;
	}
	public Object getValue(){
		return val;
	}
	public String getStatName(){
		return statName;
	}
	public boolean shouldDisplay(){
		return display;
	}
}
