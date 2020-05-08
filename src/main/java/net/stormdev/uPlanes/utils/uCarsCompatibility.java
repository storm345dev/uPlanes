package net.stormdev.uPlanes.utils;

import java.util.List;
import java.util.UUID;

import net.stormdev.uPlanes.main.main;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.uCarsAPI.CarCheck;
import com.useful.uCarsAPI.ItemCarCheck;
import com.useful.uCarsAPI.uCarsAPI;


public class uCarsCompatibility {
	public static void run(){
		main.logger.info("Sorting uCars compatibility...");
		
		uCarsAPI api = uCarsAPI.getAPI();
		api.hookPlugin(main.plugin); //Authenticate with the API
		
		api.registerCarCheck(main.plugin, new CarCheck(){

			public Boolean isACar(Entity car) {
				if(main.plugin.planeManager.isPlaneInUse(car.getUniqueId())){
					PEntityMeta.setMetadata(car, "ucars.ignore", new StatValue(true, main.plugin));
					return false;
				}
				return true;
			}});
		api.registerItemCarCheck(main.plugin, new ItemCarCheck(){

			public Boolean isACar(ItemStack carStack) {
				ItemMeta im = carStack.getItemMeta();
				List<String> lore = im.getLore();
				if(im == null || lore == null || lore.size() < 1){
					return true;
				}
				String id = lore.get(0);
				UUID uuid;
				try {
					uuid = UUID.fromString(id);
				} catch (Exception e) {
					return true;
				}
				if(main.plugin.planeManager.isAPlane(carStack)){
					return false;
				}
				if(main.plugin.planeManager.isPlaneInUse(uuid)){
					return false;
				}
				return true;
			}});
		//Done
		main.logger.info("Allowed for car compatibility!");
	}
}
