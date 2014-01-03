package net.stormdev.uPlanes.commands;

import net.stormdev.uPlanes.main.PlaneGenerator;
import net.stormdev.uPlanes.main.PlaneItemMethods;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.Plane;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommandExecutor implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
		}
		if(args.length < 1){
			return false;
		}
		String action = args[0];
		if(action.equalsIgnoreCase("give")){
			if(player == null){
				//Player's only
				sender.sendMessage(main.colors.getError()+Lang.get("general.playersOnly"));
				return true;
			}
			if(args.length < 2){
				return false;
			}
			String type = args[1];
			String spawnMsg = main.colors.getSuccess() + Lang.get("general.spawn.msg");
			Plane plane = null;
			if(type.equalsIgnoreCase("random")){
				//Give them a random plane
				plane = PlaneGenerator.gen();
				player.getInventory().addItem(PlaneItemMethods.getItem(plane));
				sender.sendMessage(main.colors.getSuccess()+spawnMsg);
				return true;
			}
			else{
				//Speed, health, name
				if(args.length < 5){
					return false;
				}
				
				String rawSpeed = args[2];
				String rawHealth = args[3];
				String name = args[4];
				double speed = 30;
			    double health = 50;
				
			    for(int i=5;i<args.length;i++){
					name += " "+args[i];
				}
			    
				try {
					speed = Double.parseDouble(rawSpeed);
					health = Double.parseDouble(rawHealth);
				} catch (NumberFormatException e) {
					return false;
				}
				
				plane = new Plane();
				plane.mutliplier = speed;
				plane.name = Colors.colorise(name);
				plane.health = health;
				
				player.getInventory().addItem(PlaneItemMethods.getItem(plane));
				sender.sendMessage(main.colors.getSuccess()+spawnMsg);
				return true;
			}
		}
		else{
			return false;
		}
	}

}
