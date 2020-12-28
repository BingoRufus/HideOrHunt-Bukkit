package me.latestion.hoh.events;

import me.latestion.hoh.localization.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.latestion.hoh.HideOrHunt;
import me.latestion.hoh.game.GameState;
import me.latestion.hoh.game.HOHPlayer;
import me.latestion.hoh.game.HOHTeam;
import me.latestion.hoh.utils.Util;

public class AsyncChat implements Listener {
    private HideOrHunt plugin;
    
    public AsyncChat(final HideOrHunt plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void ce(AsyncPlayerChatEvent event) {
        if (plugin.chat.contains(event.getPlayer())) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            String name = event.getMessage();
            MessageManager messageManager = plugin.getMessageManager();

            if (name.length() > 15) {
                player.sendMessage(messageManager.getMessage("too-many-characters"));
                return;
            }
        	if (GameState.getCurrentGameState() != GameState.PREPARE) {
        		return;
        	}
            Util util = new Util(plugin);
            if (util.isTeamTaken(name)) {
                player.sendMessage(messageManager.getMessage("team-name-taken"));
                return;
            }
            player.sendMessage(messageManager.getMessage("team-name-set").replace("%name%", name));
            ItemStack item = plugin.inv.getItem(plugin.cache.get(plugin.chat.indexOf(player)));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
            plugin.inv.setItem(plugin.cache.get(plugin.chat.indexOf(player)), item);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.updateInventory();
            }
            
            HOHTeam team = new HOHTeam(plugin, name);
            plugin.hohPlayer.get(player.getUniqueId()).setTeam(team);
            
            if (plugin.game.cache.isEmpty() && this.plugin.cache.isEmpty()) {
    			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
    	            public void run() {
    	            	plugin.game.startGame();
    	            }
    	        }, 1L);
            }
        }
        else {
        	if (GameState.getCurrentGameState() != GameState.ON) {
        		return;
        	}
        	if (plugin.hohPlayer.containsKey(event.getPlayer().getUniqueId())) {
        		HOHPlayer player = plugin.hohPlayer.get(event.getPlayer().getUniqueId());
                event.setCancelled(true);
                if (player.dead) {
        			return;
        		}

                String message = event.getMessage();

                if (!player.teamChat) {
                    String format = plugin.getConfig().getString("Main-Chat-Format");
                    String f = format.replace("%message%", message);
                    String g = f.replace("%playername%", event.getPlayer().getName());
                    String h = g.replace("%playerteam%", player.getTeam().getName());
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', h));
                    return;
        		}
        		
                String format = plugin.getConfig().getString("Team-Chat-Format");
                String f = format.replace("%message%", message);
                String g = f.replace("%playername%", event.getPlayer().getName());
                String h = g.replace("%playerteam%", player.getTeam().getName());
    			for (HOHPlayer p : player.getTeam().players) {
    				if (p.getPlayer().isOnline()) {
                        p.getPlayer().getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', h));
    				}
    			}
        		
        	}
        }
    }
}