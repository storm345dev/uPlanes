package net.stormdev.uPlanes.utils;

import org.bukkit.ChatColor;

public class Colors {
	private String success = "";
	private String error = "";
	private String info = "";
	private String title = "";
	private String tp = "";

	public Colors(String success, String error, String info, String title,
			String tp) {
		this.success = Colors.colorise(success);
		this.error = Colors.colorise(error);
		this.info = Colors.colorise(info);
		this.title = Colors.colorise(title);
		this.tp = Colors.colorise(tp);
	}
	
	public static String strip(String in){
		in = ChatColor.stripColor(in);
		in = ChatColor.translateAlternateColorCodes('&', in);
		in = ChatColor.translateAlternateColorCodes('§', in);
		in = ChatColor.translateAlternateColorCodes('?', in);
		return in;
	}

	public String getSuccess() {
		return this.success;
	}

	public String getError() {
		return this.error;
	}

	public String getInfo() {
		return this.info;
	}

	public String getTitle() {
		return this.title;
	}

	public String getTp() {
		return this.tp;
	}
	
	public static String colorise(String prefix) {
		 return ChatColor.translateAlternateColorCodes('&', prefix);
	}
}
