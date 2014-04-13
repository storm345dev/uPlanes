package net.stormdev.uPlanes.commands;

import java.io.File;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Lang;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FuelCommandExecutor implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		if (!main.config.getBoolean("general.planes.fuel.enable") || main.economy == null) {
			if(!main.plugin.setupEconomy()){
				sender.sendMessage(main.colors.getError()
						+ Lang.get("lang.fuel.disabled"));
				return true;
			}
			else if(!main.config.getBoolean("general.planes.fuel.enable")){
				sender.sendMessage(main.colors.getError()
						+ Lang.get("lang.fuel.disabled"));
				return true;
			}
		}
		if (!sender.hasPermission(main.config
				.getString("general.planes.fuel.cmdPerm"))) {
			sender.sendMessage(main.colors.getError() + "No permission!");
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(Lang.get("lang.messages.playersOnly"));
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		String action = args[0];
		if (action.equalsIgnoreCase("view")) {
			sender.sendMessage(main.colors.getTitle()
					+ "[Fuel cost (Per litre):]" + main.colors.getInfo()
					+ main.config.getDouble("general.planes.fuel.price"));
			double fuel = 0;
			if (main.fuel.containsKey(sender.getName())) {
				fuel = main.fuel.get(sender.getName());
			}
			sender.sendMessage(main.colors.getTitle() + "[Your fuel:]"
					+ main.colors.getInfo() + fuel + " "
					+ Lang.get("lang.fuel.unit"));
			if (main.config.getBoolean("general.planes.fuel.items.enable")) {
				sender.sendMessage(main.colors.getTitle()
						+ Lang.get("lang.fuel.isItem"));
			}
			return true;
		} else if (action.equalsIgnoreCase("buy")) {
			if (args.length < 2) {
				return false;
			}
			double amount = 0;
			try {
				amount = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(main.colors.getError()
						+ Lang.get("lang.fuel.invalidAmount"));
				return true;
			}
			double fuel = 0;
			if (main.fuel.containsKey(sender.getName())) {
				fuel = main.fuel.get(sender.getName());
			}
			double cost = main.config.getDouble("general.planes.fuel.price");
			double value = cost * amount;
			double bal = 0.0;
			try {
				bal = main.economy.getBalance(sender.getName());
			} catch (Exception e) {
				if (!main.plugin.setupEconomy()) {
					sender.sendMessage(main.colors.getError()
							+ "Error finding economy plugin");
					return true;
				} else {
					try {
						bal = main.economy.getBalance(sender.getName());
					} catch (Exception e1) {
						sender.sendMessage(main.colors.getError()
								+ "Error finding economy plugin");
						return true;
					}
				}
			}
			if (bal <= 0) {
				sender.sendMessage(main.colors.getError()
						+ Lang.get("lang.fuel.noMoney"));
				return true;
			}
			if (bal < value) {
				String notEnough = Lang.get("lang.fuel.notEnoughMoney");
				notEnough = notEnough.replaceAll("%amount%", "" + value);
				notEnough = notEnough.replaceAll("%unit%",
						"" + main.economy.currencyNamePlural());
				notEnough = notEnough.replaceAll("%balance%", "" + bal);
				sender.sendMessage(main.colors.getError() + notEnough);
				return true;
			}
			main.economy.withdrawPlayer(sender.getName(), value);
			bal = bal - value;
			fuel = fuel + amount;
			main.fuel.put(sender.getName(), fuel);
			main.saveHashMap(main.fuel, main.plugin.getDataFolder()
					.getAbsolutePath() + File.separator + "fuel.bin");
			String success = Lang.get("lang.fuel.success");
			success = success.replaceAll("%amount%", "" + value);
			success = success.replaceAll("%unit%",
					"" + main.economy.currencyNamePlural());
			success = success.replaceAll("%balance%", "" + bal);
			success = success.replaceAll("%quantity%", "" + amount);
			sender.sendMessage(main.colors.getSuccess() + success);
			return true;
		} else if (action.equalsIgnoreCase("sell")) {
			if (!main.config.getBoolean("general.planes.fuel.sellFuel")) {
				sender.sendMessage(main.colors.getError()
						+ "Not allowed to sell fuel!");
				return true;
			}
			if (args.length < 2) {
				return false;
			}
			double amount = 0;
			try {
				amount = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(main.colors.getError()
						+ Lang.get("lang.fuel.invalidAmount"));
				return true;
			}
			double fuel = 0;
			if (main.fuel.containsKey(sender.getName())) {
				fuel = main.fuel.get(sender.getName());
			}
			if ((fuel - amount) <= 0) {
				sender.sendMessage(main.colors.getError()
						+ Lang.get("lang.fuel.empty"));
				return true;
			}
			double cost = main.config.getDouble("general.planes.fuel.price");
			double value = cost * amount;
			double bal = main.economy.getBalance(sender.getName());
			main.economy.depositPlayer(sender.getName(), value);
			bal = bal + value;
			fuel = fuel - amount;
			main.fuel.put(sender.getName(), fuel);
			main.saveHashMap(main.fuel, main.plugin.getDataFolder()
					.getAbsolutePath() + File.separator + "fuel.bin");
			String success = Lang.get("lang.fuel.sellSuccess");
			success = success.replaceAll("%amount%", "" + value);
			success = success.replaceAll("%unit%",
					"" + main.economy.currencyNamePlural());
			success = success.replaceAll("%balance%", "" + bal);
			success = success.replaceAll("%quantity%", "" + amount);
			sender.sendMessage(main.colors.getSuccess() + success);
			return true;
		} else {
			return false;
		}
	}

}
