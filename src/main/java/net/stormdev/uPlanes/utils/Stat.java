package net.stormdev.uPlanes.utils;

import java.io.Serializable;

import org.bukkit.plugin.Plugin;

public class Stat implements Serializable {
	private static final long serialVersionUID = -7275161546527988371L;
	private Object val = null;
	private String statName = "";
	private Boolean display = false;
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
	public Boolean shouldDisplay(){
		return display;
	}
}
