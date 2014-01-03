package net.stormdev.uPlanes.commands;

import java.util.regex.Pattern;

import net.stormdev.uPlanes.main.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.useful.ucars.Lang;

public class InfoCommandExecutor implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		String msg = Lang.get("general.info.msg");
		msg = msg.replaceAll(Pattern.quote("%version%"), "v"+main.plugin.getDescription().getVersion());
		sender.sendMessage(main.colors.getInfo()+msg);
		return true;
	}

}
