package net.stormdev.uPlanes.commands;

import java.util.List;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.Lang;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoPilotCommandExecutor implements CommandExecutor {
	private Boolean autoPilotEnabled;
	public AutoPilotCommandExecutor(){
		this.autoPilotEnabled = main.config.getBoolean("general.planes.autopilot");
	}
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		
		if(!autoPilotEnabled){
			sender.sendMessage(main.colors.getError()+Lang.get("general.disabled.msg"));
			return true;
		}
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
		}
		
		if(cmd.getName().equalsIgnoreCase("destination")){
			//Set current plane destination
			if(args.length < 1){
				return false;
			}
			String name = args[0];
			for(int i=1;i<args.length;i++){
				name += " "+args[i];
			}
			//TODO
		}
		else if(cmd.getName().equalsIgnoreCase("destinations")){
			//Display list of destinations
			List<String> dests = main.plugin.destinationManager.getDestinationsList();
			String title = main.colors.getTitle()+"Available Destinations:";
			String msg = "";
			
			for(String s:dests){
				if(msg.length() < 1){
					msg = Colors.colorise(s);
					continue;
				}
				msg += ", "+Colors.colorise(s);
			}
			
			sender.sendMessage(title);
			sender.sendMessage(main.colors.getInfo()+msg);
			
			return true;
		}
		return false;
	}
	
}
