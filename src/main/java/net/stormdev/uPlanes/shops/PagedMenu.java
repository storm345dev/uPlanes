package net.stormdev.uPlanes.shops;

import java.util.List;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.shops.IconMenu.OptionClickEvent;
import net.stormdev.uPlanes.shops.IconMenu.OptionClickEventHandler;
import net.stormdev.uPlanes.shops.PagedMenu.MenuDetails.MenuItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PagedMenu {
	private final int PAGE_SIZE;
	
	private Plugin plug;
	private MenuDetails details;
	
	public static interface MenuDetails {
		public static interface MenuItem {
			public ItemStack getDisplayItem();
			public String getColouredTitle();
			public String[] getColouredLore();
			public void onClick(Player player);
		}
		
		public static abstract class AbstractMenuItem implements MenuItem {
			private ItemStack display;
			private String title;
			private String[] lore;
			
			public AbstractMenuItem(ItemStack display, String colouredTitle, String[] lore){
				this.display = display;
				this.title = colouredTitle;
				this.lore = lore;
			}
			
			public ItemStack getDisplayItem(){
				return display;
			}
			
			public String getColouredTitle(){
				return title;
			}
			
			public String[] getColouredLore(){
				return lore;
			}
		}
		
		public List<MenuItem> getDisplayItems(Player player);
		public String getColouredMenuTitle(Player player);
		public int getPageSize();
		public String noDisplayItemMessage();
	}
	
	public PagedMenu(MenuDetails details){
		this.details = details;
		this.plug = main.plugin;
		PAGE_SIZE = details.getPageSize();
		if(PAGE_SIZE % 9 != 0 || PAGE_SIZE < 1){
			throw new RuntimeException("Invalid Paged Menu! Page Size MUST be divisible by 9");
		}
	}
	
	public void open(final Player player){
		player.sendMessage(ChatColor.GRAY+"Opening...");
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){
			
			@Override
			public void run() {
				final List<MenuItem> items = details.getDisplayItems(player);
				
				Bukkit.getScheduler().runTask(plug, new Runnable(){

					@Override
					public void run() {
						createDisplay(player, items, 1);
						return;
					}});
				return;
			}});
	}
	
	private void createDisplay(Player player, final List<MenuItem> owned, final int pageNo){
		if(pageNo < 1){
			createDisplay(player, owned, 1);
			return;
		}
		int page = pageNo-1;
		final int startPos = (PAGE_SIZE - 2)*page; //if page 1 then (0*52) which is 0, if page 2 then it's 1*52 which is 52
		int endPos = startPos + (PAGE_SIZE - 2); //If startPos = 0, endPos = 52
		
		if(plug == null){
			Bukkit.broadcastMessage("UH OH MEMORY LEAK ALERT SAFEGUARD #1 PLEASE REPORT");
		}
		final IconMenu menu = new IconMenu(details.getColouredMenuTitle(player), PAGE_SIZE, new OptionClickEventHandler(){

			@Override
			public void onOptionClick(OptionClickEvent event) {
				Player player = event.getPlayer();
				event.setWillDestroy(false);
				
				int i = event.getPosition();
				
				if(i == (PAGE_SIZE - 2)){
					//Last page
					final int newPage = pageNo-1;
					final Player pl = player;
					Bukkit.getScheduler().runTaskLater(plug, new Runnable(){

						@Override
						public void run() {
							createDisplay(pl, owned, newPage);
							return;
						}}, 2l);
					event.setWillClose(true);
					event.setWillDestroy(true);
					return;
				}
				else if(i == (PAGE_SIZE - 1)){
					//Next page
					final int newPage = pageNo+1;
					final Player pl = player;
					Bukkit.getScheduler().runTaskLater(plug, new Runnable(){

						@Override
						public void run() {
							createDisplay(pl, owned, newPage);
							return;
						}}, 2l);
					event.setWillClose(true);
					event.setWillDestroy(true);
					return;
				}
				
				int listPos = startPos + i; //Aka if start is 0 and slot is 0, pos is 0; if start is 52 and slot is 3 then it's 52+3 and listPos = 55
				
				MenuItem c;
				try {
					c = owned.get(listPos);
				} catch (Exception e) {
					event.setWillClose(false);
					return;
				}
				if(c == null){
					event.setWillClose(false);
					return;
				}
				
				event.setWillClose(true);
				event.setWillDestroy(true);
				c.onClick(player);
				return;
			}}, plug, true);
		
		int z = 0;
		boolean valid = false;
		for(int i=startPos;i<owned.size()&&i<endPos&&z<(endPos+1);i++){
			MenuItem c = owned.get(i);
			ItemStack display = c.getDisplayItem();
			
			menu.setOption(z, display, c.getColouredTitle(), 
					c.getColouredLore());
			z++;
			valid = true;
		}
		if(!valid){
			//Go to last page
			final int newPage = pageNo-1;
			if(newPage < 1){
				player.sendMessage(ChatColor.RED+details.noDisplayItemMessage());
				return;
			}
			final Player pl = player;
			Bukkit.getScheduler().runTaskLater(plug, new Runnable(){

				@Override
				public void run() {
					menu.destroy();
					createDisplay(pl, owned, newPage);
					return;
				}}, 2l);
			return;
		}
		
		menu.setOption((PAGE_SIZE - 2), new ItemStack(Material.PAPER), "Last Page", ChatColor.GRAY+"<<<<<");
		menu.setOption((PAGE_SIZE - 1), new ItemStack(Material.PAPER), "Next Page", ChatColor.GRAY+">>>>>");
		
		menu.open(player);
	}
}
