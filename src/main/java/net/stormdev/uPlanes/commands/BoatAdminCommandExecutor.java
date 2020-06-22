package net.stormdev.uPlanes.commands;

import net.stormdev.uPlanes.api.Boat;
import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.main.BoatGenerator;
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

public class BoatAdminCommandExecutor implements CommandExecutor {

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
			String spawnMsg = main.colors.getSuccess() + Lang.get("general.spawn.msg");
			Boat boat = null;
			if(args[1].equalsIgnoreCase("random")){
				//Give them a random plane
				boat = BoatGenerator.gen();
				
				player.getInventory().addItem(boat.toItemStack());
				sender.sendMessage(main.colors.getSuccess()+spawnMsg);
				return true;
			}
			return false;
		}
		else{
			return false;
		}
	}

}
