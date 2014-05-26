package net.stormdev.uPlanes.items;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.utils.Colors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemPlaneValidation {
	public static Plane getPlane(ItemStack item){
		if(item == null || !item.getType().equals(Material.MINECART)){
			return null;
		}
		
		ItemMeta im = item.getItemMeta();
		if(im == null || im.getLore() == null){
			return null;
		}
		
		List<String> lore = im.getLore();
		
		String name = im.getDisplayName();
		if(name == null || name.equalsIgnoreCase("null")){
			name = "Plane";
		}
		
		Plane c = getPlaneFromLore(im.getDisplayName(), lore);
		return c;
	}
	
	public static Plane getPlaneFromLore(String name, List<String> lore){
		if(lore.size() < 3){
			return null;
		}
		int i = 0;
		if(!Colors.strip((lore.get(i))).toLowerCase().contains("[speed:]")){
			String firstLine = Colors.strip((lore.get(i))).toLowerCase();
			if(firstLine.equalsIgnoreCase("car")){
				return null;
			}
			else if(!firstLine.equalsIgnoreCase("plane")){ //It doesn't say plane...
				try {
					UUID.fromString(firstLine); //Test if it's a UUID string
				} catch (Exception e) {
					// Not an old plane either
					return null;
				}
				//It's a UUID...
				
			}
			i = 1;
			if(!Colors.strip((lore.get(i))).toLowerCase().contains("[speed:]")){ //Using deprecated format
				return null;
			}
		}
		
		double speed = 1;
		double health = 50;
		boolean hover = false;
		
		String line = Colors.strip(lore.get(i)).toLowerCase(); //[Speed:] 0.8x
		String speedRaw = line.replaceFirst(Pattern.quote("[speed:] "), "").trim();
		try {
			speed = Double.parseDouble(speedRaw);
		} catch (NumberFormatException e) {
			//Oh well
		}
		
		i++;
		line = Colors.strip(lore.get(i)).toLowerCase(); //[Health:] 10.0/100.0
		String healthRaw = line.replaceFirst(Pattern.quote("[health:] "), "").trim();
		try {
			health = Double.parseDouble(healthRaw);
		} catch (NumberFormatException e) {
			//Oh well, unable to read value
		}
		
		i++;
		if(i >= lore.size()){
			return new Plane(speed, name, health, hover);
		}
		else {
			line = Colors.strip(lore.get(i)).toLowerCase(); //[Hover:] Yes
			if(!line.contains("hover")){
				return null;
			}
			String handlingRaw = line.replaceFirst(Pattern.quote("[hover:] "), "").trim();
			if(handlingRaw.equalsIgnoreCase("Yes")){
				hover = true;
			}
		}
		return new Plane(speed, name, health, hover);
	}
}
