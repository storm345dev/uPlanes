package net.stormdev.uPlanes.commands;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.main.PlaneGenerator;
import net.stormdev.uPlanes.main.PlaneItemMethods;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AdminCommandExecutor implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		if(!(sender instanceof Player)){
			//Player's only
			sender.sendMessage(main.colors.getError()+Lang.get("general.playersOnly"));
			return true;
		}

		Player player = ((Player) sender);

		if(args.length < 1){
			return false;
		}

		String action = args[0];
		if(action.equalsIgnoreCase("give")){
			if(args.length < 2){
				return false;
			}
			String spawnMsg = main.colors.getSuccess() + Lang.get("general.spawn.msg");
			Plane plane = null;
			if(args[1].equalsIgnoreCase("random")){
				//Give them a random plane
				boolean hover = args[args.length-1].equalsIgnoreCase("hover");
				
				plane = PlaneGenerator.gen();
				
				if(hover){
					plane.setName("Hover Plane");
					plane.setHover(true);
				}
				
				player.getInventory().addItem(PlaneItemMethods.getItem(plane));
				sender.sendMessage(main.colors.getSuccess()+spawnMsg);
				return true;
			}
			else{
				//Speed, health, name
				if(args.length < 4){
					return false;
				}
				
				boolean hover = args[args.length-1].equalsIgnoreCase("hover");
				String rawSpeed = args[1];
				String rawHealth = args[2];
				String name = args[3];
				double speed = 30;
			    double health = 50;
				
			    for(int i=4;i<args.length;i++){
			    	if(hover && i>=(args.length-1)){
			    	continue;
			    	}
					name += " "+args[i];
				}
			    
				try {
					speed = Double.parseDouble(rawSpeed);
				}  catch (NumberFormatException e) {
					sender.sendMessage(main.colors.getError()+"NaN: "+rawSpeed);
					return true;
				}
				try {
					health = Double.parseDouble(rawHealth);
				} catch (NumberFormatException e) {
					sender.sendMessage(main.colors.getError()+"NaN: "+rawHealth);
					return true;
				}
				
				plane = new Plane();
				plane.setSpeed(speed);
				plane.setName(Colors.colorise(name));
				plane.setHealth(health);
				plane.setId(UUID.randomUUID());
				
				if(hover){
					plane.setHover(true);
				}
				
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
