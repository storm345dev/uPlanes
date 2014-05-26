package net.stormdev.uPlanes.main;

import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.Lang;

import org.bukkit.ChatColor;
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
			double health = plane.getHealth();
			double maxHealth = main.config.getDouble("general.planes.maxHealth");
			double bonus = (9*upgrade.getAmount());
			health = health + bonus; //Add 9 to health stat per item
			if(health > maxHealth){
				health = maxHealth;
			}
			if(main.upgradePerms && !player.hasPermission("uplanes.upgrade.health")){
				player.sendMessage(ChatColor.RED+"You don't have the permission 'uplanes.upgrade.health' required to upgrade your plane's health!");
				return;
			}
			upgradeMsg = Colors.colorise(upgradeMsg);
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
			player.sendMessage(upgradeMsg);
			upgrade.setAmount(0);
			plane.setHealth(health);
		}
		else if(upgrade.getType() == Material.IRON_INGOT){
			//Health upgrade
			double health = plane.getHealth();
			double maxHealth = main.config.getDouble("general.planes.maxHealth");
			double bonus = upgrade.getAmount();
			health = health + bonus; //Add 1 to health stat per item
			if(health > maxHealth){
				health = maxHealth;
			}
			if(main.upgradePerms && !player.hasPermission("uplanes.upgrade.health")){
				player.sendMessage(ChatColor.RED+"You don't have the permission 'uplanes.upgrade.health' required to upgrade your plane's health!");
				return;
			}
			upgradeMsg = Colors.colorise(upgradeMsg);
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
			player.sendMessage(upgradeMsg);
			upgrade.setAmount(0);
			plane.setHealth(health);
		}
		else if(upgrade.getType() == Material.REDSTONE){
			//Health upgrade
			double speed = plane.getSpeed();
			double maxSpeed = main.maxSpeed;
			double bonus = (1*upgrade.getAmount());
			speed += bonus; //Add 1 to health stat per item
			if(speed > maxSpeed){
				speed = maxSpeed;
			}
			if(main.upgradePerms && !player.hasPermission("uplanes.upgrade.speed")){
				player.sendMessage(ChatColor.RED+"You don't have the permission 'uplanes.upgrade.speed' required to upgrade your plane's speed!");
				return;
			}
			upgradeMsg = Colors.colorise(upgradeMsg);
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "speed");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), speed+" (Max: "+maxSpeed+")");
			player.sendMessage(upgradeMsg);
			upgrade.setAmount(0);
			plane.setSpeed(speed);
		}
		else if(upgrade.getType() == Material.REDSTONE_BLOCK){
			//Health upgrade
			double speed = plane.getSpeed();
			double maxSpeed = main.maxSpeed;
			double bonus = (9*upgrade.getAmount());
			speed += bonus; //Add 9 to health stat per item
			if(speed > maxSpeed){
				speed = maxSpeed;
			}
			if(main.upgradePerms && !player.hasPermission("uplanes.upgrade.speed")){
				player.sendMessage(ChatColor.RED+"You don't have the permission 'uplanes.upgrade.speed' required to upgrade your plane's speed!");
				return;
			}
			upgradeMsg = Colors.colorise(upgradeMsg);
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "speed");
			upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), speed+" (Max: "+maxSpeed+")");
			player.sendMessage(upgradeMsg);
			upgrade.setAmount(0);
			plane.setSpeed(speed);
		}
		//Perform actions to clear up what we've done
		inv.clear(1);
		if(update){
			inv.setItem(0, PlaneItemMethods.getItem(plane));
			player.updateInventory();
		}
	}
}
