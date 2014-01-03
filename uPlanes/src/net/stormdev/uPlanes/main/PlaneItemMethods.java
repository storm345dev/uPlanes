package net.stormdev.uPlanes.main;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.uPlanes.utils.Plane;
import net.stormdev.uPlanes.utils.Stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlaneItemMethods {
	public static ItemStack getItem(Plane plane){
		ItemStack item = new ItemStack(Material.MINECART);
		ItemMeta im = item.getItemMeta();
		String name = plane.name;
		List<String> lore = new ArrayList<String>();
		
		if(plane.isPlaced){
			return item; //Invalid
		}
		
		im.setDisplayName(name);
		lore.add(plane.id.toString());
		lore.add(main.colors.getTitle()+"[Speed:] "
				+main.colors.getInfo()+plane.mutliplier+"x");
		lore.add(main.colors.getTitle()+"[Health:] "
				+main.colors.getInfo()+plane.health);
		
		for(Stat s:plane.stats.values()){
			if(s.shouldDisplay()){
				lore.add(main.colors.getTitle()+"["+s.getStatName()+":] "
						+main.colors.getInfo()+s.getValue().toString());
			}
		}
		
		im.setLore(lore);
		
		item.setItemMeta(im);
		
		return item;
	}
}
