package net.stormdev.uPlanes.shops;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.main.PlaneGenerator;
import net.stormdev.uPlanes.main.PlaneItemMethods;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.shops.IconMenu.OptionClickEvent;
import net.stormdev.uPlanes.shops.IconMenu.OptionClickEventHandler;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.Plane;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlaneShop {
	private double value;
	private IconMenu menu = null;
	private main plugin;
	public PlaneShop(main plugin){
		this.plugin = plugin;
		value = main.config.getDouble("general.planes.price");
		int v = (int)value*100;
		value = (double) v/100;
		setupMenu(plugin);
	}
	
	public void destroy(){
		menu.destroy();
	}

	public IconMenu getShopWindow(){
		if(menu == null){
			setupMenu(plugin);
		}
		return menu;
	}
	
	public void onClick(OptionClickEvent event){
		event.setWillClose(false);
		event.setWillDestroy(false);
		
		int slot = event.getPosition();
		
		if(slot == 4){
			//Buy a car
			event.setWillClose(true);
			buyPlane(event.getPlayer());
		}
		return;
	}
	
	public void open(Player player){
		getShopWindow().open(player);
		return;
	}
	
	public void buyPlane(Player player){
		if(main.economy == null){
			main.plugin.setupEconomy();
			if(main.economy == null){
				player.sendMessage(main.colors.getError()+"No economy plugin found! Error!");
				return;
			}
		}
		double bal = main.economy.getBalance(player.getName());
		double cost = value;
		if(cost < 1){
			return;
		}
		double rem = bal-cost;
		if(rem<0){
			player.sendMessage(main.colors.getError()+Lang.get("general.buy.notEnoughMoney"));
			return;
		}
		main.economy.withdrawPlayer(player.getName(), cost);
		
		String currency = main.config.getString("general.currencySign");
		String msg = Lang.get("general.buy.success");
		msg = msg.replaceAll(Pattern.quote("%item%"), "a plane");
		msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(currency+cost));
		msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(currency+rem));
		player.sendMessage(main.colors.getSuccess()+msg);
		
		//Give them the car
		Plane c = PlaneGenerator.gen();
		ItemStack i = PlaneItemMethods.getItem(c);
		player.getInventory().addItem(i);
		
		return;
	}
	
	public void setupMenu(main plugin){
		String currency = main.config.getString("general.currencySign");
		
		this.menu = new IconMenu("Plane Shop", 9, new OptionClickEventHandler(){

			public void onOptionClick(OptionClickEvent event) {
				onClick(event);
				return;
			}}, plugin);
		List<String> info = new ArrayList<String>();
		info.add(main.colors.getTitle()+"[Price:] "+main.colors.getInfo()+currency+value);
		this.menu.setOption(4, new ItemStack(Material.MINECART), main.colors.getTitle()+"Buy Random Plane", info);
	}
	
}
