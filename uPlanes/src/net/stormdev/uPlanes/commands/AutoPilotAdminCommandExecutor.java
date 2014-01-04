package net.stormdev.uPlanes.commands;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Lang;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoPilotAdminCommandExecutor implements CommandExecutor {
	private Boolean autoPilotEnabled;
	public AutoPilotAdminCommandExecutor(){
		this.autoPilotEnabled = main.config.getBoolean("general.planes.autopilot");
	}
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
		}
		if(!autoPilotEnabled){
			sender.sendMessage(main.colors.getError()+Lang.get("general.disabled.msg"));
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("setDestination")){
			//Set the destination
			if(player == null){
				sender.sendMessage(main.colors.getError()+Lang.get("general.playersOnly"));
				return true;
			}
			if(args.length < 1){
				return false;
			}
			String name = args[0];
			for(int i=1;i<args.length;i++){
				name += " "+args[i];
			}
			main.plugin.destinationManager.setDestination(name, player.getLocation());
			sender.sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.destinations.set"));
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("delDestination")){
			//Delete the destination
			if(player == null){
				sender.sendMessage(main.colors.getError()+Lang.get("general.playersOnly"));
				return true;
			}
			if(args.length < 1){
				return false;
			}
			String name = args[0];
			for(int i=1;i<args.length;i++){
				name += " "+args[i];
			}
			main.plugin.destinationManager.delDestination(name);
			sender.sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.destinations.del"));
			return true;
		}
		return false;
	}
	
}
