package net.stormdev.uPlanes.shops;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.guis.PagedMenu;
import net.stormdev.uPlanes.guis.PagedMenu.MenuDetails;
import net.stormdev.uPlanes.main.PlaneGenerator;
import net.stormdev.uPlanes.main.PlaneItemMethods;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.presets.PlanePreset;
import net.stormdev.uPlanes.presets.PresetManager;
import net.stormdev.uPlanes.utils.Lang;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlaneShop {
	private double value;
	private PagedMenu menu = null;
	private main plugin;
	public PlaneShop(main plugin){
		this.plugin = plugin;
		value = main.config.getDouble("general.planes.price");
		int v = (int)value*100;
		value = (double) v/100;
		setupMenu(plugin);
	}
	
	public void destroy(){
		//Not needed
	}

	public PagedMenu getShopWindow(){
		if(menu == null){
			setupMenu(plugin);
		}
		return menu;
	}
	
	/*public void onClick(OptionClickEvent event){
		event.setWillClose(false);
		event.setWillDestroy(false);
		
		int slot = event.getPosition();
		
		if(!PresetManager.usePresets && slot == 4){
			//Buy a car
			event.setWillClose(true);
			buyPlane(event.getPlayer());
		}
		return;
	}*/
	
	public void open(Player player){
		getShopWindow().open(player);
		return;
	}
	
	public void buyPlane(Player player, double cost, Plane plane){
		if(main.economy == null){
			main.plugin.setupEconomy();
			if(main.economy == null){
				player.sendMessage(main.colors.getError()+"No economy plugin found! Error!");
				return;
			}
		}
		double bal = main.economy.getBalance(player.getName());
		if(cost < 1){
			return;
		}
		double rem = bal-cost;
		if(rem<0){
			player.sendMessage(main.colors.getError()+Lang.get("general.buy.notEnoughMoney").replaceAll(Pattern.quote("%balance%"), bal+""));
			return;
		}
		main.economy.withdrawPlayer(player.getName(), cost);
		
		String currency = main.config.getString("general.currencySign");
		String msg = Lang.get("general.buy.success");
		msg = msg.replaceAll(Pattern.quote("%item%"), "a plane");
		msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(currency+cost));
		msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(currency+rem));
		player.sendMessage(main.colors.getSuccess()+msg);
		
		//Give them the plane
		ItemStack i = PlaneItemMethods.getItem(plane);
		player.getInventory().addItem(i);
		
		return;
	}
	
	public void setupMenu(main plugin){
		final String currency = main.config.getString("general.currencySign");
		
		this.menu = new PagedMenu(new MenuDetails(){

			@Override
			public List<MenuItem> getDisplayItems(Player player) {
				List<MenuItem> res = new ArrayList<MenuItem>();
				if(!PresetManager.usePresets){
					res.add(new MenuItem(){

						@Override
						public ItemStack getDisplayItem() {
							return new ItemStack(Material.MINECART);
						}

						@Override
						public String getColouredTitle() {
							return ChatColor.WHITE+"Buy a random plane";
						}

						@Override
						public String[] getColouredLore() {
							return new String[]{main.colors.getInfo()+currency+value};
						}

						@Override
						public void onClick(Player player) {
							buyPlane(player, value, PlaneGenerator.gen());
						}});
				}
				else {
					for(final PlanePreset pp:main.plugin.presets.getPresets()){
						res.add(new MenuItem(){

							@Override
							public ItemStack getDisplayItem() {
								ItemStack it = pp.toItemStack();
								ItemStack res = new ItemStack(it.getType());
								res.setData(it.getData());
								return res;
							}

							@Override
							public String getColouredTitle() {
								return ChatColor.WHITE+pp.getName();
							}

							@Override
							public String[] getColouredLore() {
								return pp.getSellLore();
							}

							@Override
							public void onClick(Player player) {
								buyPlane(player, pp.getCost(), pp.toPlane());
							}});
					}
				}
				return res;
			}

			@Override
			public String getColouredMenuTitle(Player player) {
				return ChatColor.BLUE+"Plane Shop";
			}

			@Override
			public int getPageSize() {
				return 18;
			}

			@Override
			public String noDisplayItemMessage() {
				return ChatColor.RED+"There are no planes available to purchase!";
			}});
		/*this.menu = new IconMenu("Plane Shop", 9, new OptionClickEventHandler(){

			public void onOptionClick(OptionClickEvent event) {
				onClick(event);
				return;
			}}, plugin);*/
		/*List<String> info = new ArrayList<String>();
		info.add(main.colors.getTitle()+"[Price:] "+main.colors.getInfo()+currency+value);
		this.menu.setOption(4, new ItemStack(Material.MINECART), main.colors.getTitle()+"Buy Random Plane", info);*/
	}
	
}
