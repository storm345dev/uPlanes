package net.stormdev.uPlanes.items;

import net.stormdev.uPlanes.api.Boat;
import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.utils.Colors;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ItemBoatValidation {
	public static Boat getBoat(ItemStack item){
		if(item == null /*|| !item.getType().equals(Material.MINECART)*/){
			return null;
		}
		
		ItemMeta im = item.getItemMeta();
		if(im == null || im.getLore() == null){
			return null;
		}
		
		List<String> lore = im.getLore();
		
		String name = im.getDisplayName();
		if(name == null || name.equalsIgnoreCase("null")){
			name = "Boat";
		}
		
		Boat c = getBoatFromLore(im.getDisplayName(), lore);

		if(c == null)
			return null;

		return c;
	}
	
	public static Boat getBoatFromLore(String name, List<String> lore){
		if(lore.size() < 3){
			return null;
		}
		int i = 0;
		if(!Colors.strip((lore.get(i))).toLowerCase().contains("[speed:]")){
			String firstLine = Colors.strip((lore.get(i))).toLowerCase();
			if(!firstLine.equalsIgnoreCase("boat")){
				return null;
			}
			else if(!firstLine.equalsIgnoreCase("boat")){ //It doesn't say boat...
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
		if(i >= lore.size()){ //Old item type
			return new Boat(speed, name, health, 1, Boat.DEFAULT_TURN_AMOUNT);
		}
		else {
			line = Colors.strip(lore.get(i)).toLowerCase(); //[Hover:] Yes
			if(!line.contains("acceleration")){ //It's invalid
				return null;
			}
			try {
				String accelRaw = line.replaceFirst(Pattern.quote("[acceleration:] "), "").trim();
				double accelMod = Double.parseDouble(accelRaw) / 10.0d;

				i++;
				line = Colors.strip(lore.get(i)).toLowerCase(); //[Handling:] <amt>
				if(!line.contains("handling")){ //It's invalid
					return null;
				}

				String hRaw = line.replaceAll(Pattern.quote("[handling:] "), "").trim();
				double turnAmount = Double.parseDouble(hRaw) / 10.0d;

				return new Boat(speed, name, health, accelMod, turnAmount);
			} catch (Exception e) {
				return null; //Invalid item!
			}
		}
		/*return new Boat(speed, name, health, 1, Boat.DEFAULT_TURN_AMOUNT);*/
	}
}
