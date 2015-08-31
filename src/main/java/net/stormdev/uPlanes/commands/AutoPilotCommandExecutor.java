package net.stormdev.uPlanes.commands;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.StatValue;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class AutoPilotCommandExecutor implements CommandExecutor {
	private boolean autoPilotEnabled;
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
			
			Entity vehicle = player.getVehicle();
			UUID id = vehicle != null ? vehicle.getUniqueId():UUID.randomUUID();
			Plane plane = main.plugin.planeManager.getPlane(id);
			
			if(vehicle == null
					|| !(vehicle instanceof Minecart)
					|| plane == null){
				//Not in a plane
				sender.sendMessage(main.colors.getError()+Lang.get("general.cmd.destinations.notInPlane"));
				return true;
			}
			
			Location destination = main.plugin.destinationManager.getLocation(name, main.plugin.getServer());
			
			if(destination == null){
				//Invalid destination
				sender.sendMessage(main.colors.getError()+Lang.get("general.cmd.destinations.invalid"));
				return true;
			}
			if(destination.getWorld() != player.getWorld()){
				//Invalid destination
				sender.sendMessage(main.colors.getError()+Lang.get("general.cmd.destinations.wrongWorld"));
				return true;
			}
			vehicle.removeMetadata("plane.destination", main.plugin); //Remove any existing destinations
			vehicle.setMetadata("plane.destination", new StatValue(destination, main.plugin)); //Set it
			sender.sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.destinations.go"));
			return true;
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
				msg += ", "+main.colors.getInfo()+Colors.colorise(s);
			}
			
			sender.sendMessage(title);
			sender.sendMessage(main.colors.getInfo()+msg);
			
			return true;
		}
		return false;
	}
	
}
