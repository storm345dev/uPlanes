package net.stormdev.uPlanes.utils;

import java.util.List;
import java.util.UUID;

import net.stormdev.uPlanes.main.main;

import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.uCarsAPI.CarCheck;
import com.useful.uCarsAPI.ItemCarCheck;
import com.useful.uCarsAPI.uCarsAPI;


public class uCarsCompatibility {
	public static void run(){
		uCarsAPI api = uCarsAPI.getAPI();
		api.hookPlugin(main.plugin); //Authenticate with the API
		
		api.registerCarCheck(main.plugin, new CarCheck(){

			public Boolean isACar(Minecart car) {
				if(main.plugin.planeManager.isAPlane(car.getUniqueId())){
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
				UUID uuid = UUID.fromString(id);
				if(main.plugin.planeManager.isAPlane(uuid)){
					return false;
				}
				return true;
			}});
		//Done
		main.logger.info("Allowed for car compatibility!");
	}
}
