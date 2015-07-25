package net.stormdev.uPlanes.main;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemStackFromId {
	public static ItemStack get(String raw) {
		String[] parts = raw.split(":");
		String m = parts[0];
		Material mat = Material.getMaterial(m);
		if(mat == null){
			main.plugin.getLogger().info("[WARNING] Invalid config value: "+raw+" ("+m+")");
			return new ItemStack(Material.STONE);
		}
		short data = 0;
		boolean hasdata = false;
		if (parts.length > 1) {
			hasdata = true;
			data = Short.parseShort(parts[1]);
		}
		ItemStack item = new ItemStack(mat);
		if (hasdata) {
			item.setDurability(data);
		}
		return item;
	}

	public static boolean equals(String rawid, String materialName, int tdata) {
		String[] parts = rawid.split(":");
		String m = parts[0];
		int data = 0;
		boolean hasdata = false;
		if (parts.length > 1) {
			hasdata = true;
			data = Integer.parseInt(parts[1]);
		}
		if (materialName.equalsIgnoreCase(m)) {
			boolean valid = true;
			if (hasdata) {
				if (!(tdata == data)) {
					valid = false;
				}
			}
			if (valid) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean equals(List<String> rawids, String materialName, int tdata) {
		boolean match = false;
		for(String id:rawids){
			if(match || equals(id, materialName, tdata)){
				match = true;
			}
		}
		return match;
	}
}
