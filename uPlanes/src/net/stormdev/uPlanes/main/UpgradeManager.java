package net.stormdev.uPlanes.main;

import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.Plane;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class UpgradeManager {
	@SuppressWarnings("deprecation")
	public static void applyUpgrades(ItemStack upgrade, Plane plane,
			boolean update, boolean save, Player player, Inventory inv, UUID id){
		//TODO Apply it
		String upgradeMsg = Lang.get("general.upgrade.msg");
		if(upgrade.getType() == Material.IRON_BLOCK){
			//Health upgrade
			double health = plane.health;
			double maxHealth = main.config.getDouble("general.planes.maxHealth");
			double bonus = (9*upgrade.getAmount());
			health = health + bonus; //Add 9 to health stat per item
			if(health > maxHealth){
				health = maxHealth;
			}
			upgradeMsg = Colors.colorise(upgradeMsg);
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
			player.sendMessage(upgradeMsg);
			upgrade.setAmount(0);
			plane.health = health;
		}
		else if(upgrade.getType() == Material.IRON_INGOT){
			//Health upgrade
			double health = plane.health;
			double maxHealth = main.config.getDouble("general.planes.maxHealth");
			double bonus = upgrade.getAmount();
			health = health + bonus; //Add 1 to health stat per item
			if(health > maxHealth){
				health = maxHealth;
			}
			upgradeMsg = Colors.colorise(upgradeMsg);
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
			player.sendMessage(upgradeMsg);
			upgrade.setAmount(0);
			plane.health = health;
		}
		else if(upgrade.getType() == Material.REDSTONE){
			//Health upgrade
			double speed = plane.mutliplier;
			double maxSpeed = 200;
			double bonus = (1*upgrade.getAmount());
			speed += bonus; //Add 1 to health stat per item
			if(speed > maxSpeed){
				speed = maxSpeed;
			}
			upgradeMsg = Colors.colorise(upgradeMsg);
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "speed");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), speed+" (Max: "+maxSpeed+")");
			player.sendMessage(upgradeMsg);
			upgrade.setAmount(0);
			plane.mutliplier = speed;
		}
		else if(upgrade.getType() == Material.REDSTONE_BLOCK){
			//Health upgrade
			double speed = plane.mutliplier;
			double maxSpeed = 200;
			double bonus = (9*upgrade.getAmount());
			speed += bonus; //Add 9 to health stat per item
			if(speed > maxSpeed){
				speed = maxSpeed;
			}
			upgradeMsg = Colors.colorise(upgradeMsg);
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "speed");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), speed+" (Max: "+maxSpeed+")");
			player.sendMessage(upgradeMsg);
			upgrade.setAmount(0);
			plane.mutliplier = speed;
		}
		//Perform actions to clear up what we've done
		inv.clear(1);
		if(update){
			inv.setItem(0, PlaneItemMethods.getItem(plane));
			main.plugin.planeManager.setPlane(id, plane);
			player.updateInventory();
		}
	}
}
