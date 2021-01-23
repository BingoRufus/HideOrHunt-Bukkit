package me.latestion.hoh.events;

import me.latestion.hoh.HideOrHunt;
import me.latestion.hoh.game.GameState;
import me.latestion.hoh.game.HOHPlayer;
import me.latestion.hoh.game.HOHTeam;
import me.latestion.hoh.localization.MessageManager;
import me.latestion.hoh.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.UUID;

public class RespawnScreen implements Listener {

    private HideOrHunt plugin;
    private Util util;

    public RespawnScreen(HideOrHunt plugin) {
        this.plugin = plugin;
        this.util = new Util(plugin);
    }

    @EventHandler
    public void damageEntity(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        if (plugin.game.getGameState() != GameState.ON)
            return;
        Player p = (Player) event.getEntity();
        if (!(p.getHealth() < 1))
            return;
        HOHPlayer player = plugin.game.hohPlayers.get(event.getEntity().getUniqueId());
        MessageManager messageManager = plugin.getMessageManager();
        event.setCancelled(true);

        if (!player.getTeam().hasBeacon()) {

            String msg = messageManager.getMessage("player-eliminated").replace("%player%", player.getPlayer().getDisplayName());
            Bukkit.broadcastMessage(msg);

            if (player.getPlayer().getInventory().containsAtLeast(util.beacon(player.getTeam().getName()), 1)) {
                p.getInventory().remove(util.beacon(player.getTeam().getName()));
            }

            player.banned = true;
            player.dead = true;
            player.getTeam().diedPlayer(player);

            if (player.getTeam().eliminated) {
                msg = messageManager.getMessage("team-eliminated").replace("%team%", player.getTeam().getName());
                Bukkit.broadcastMessage(msg);
                plugin.sbUtil.eliminateTeam(player.getTeam().getName());
            }

            if (this.plugin.getConfig().getBoolean("Ban-Player-On-Death")) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player.getPlayer().getName() + " Eliminated");
                    }
                }, 0L);
            } else {
                player.getPlayer().setGameMode(GameMode.SPECTATOR);
            }

            if (plugin.game.checkEndConditions()) {
                HOHTeam winnerTeam = plugin.game.getWinnerTeam();
                if (winnerTeam == null) return;
                Bukkit.broadcastMessage(messageManager.getMessage("win-message").replace("%winner-team%", winnerTeam.getName()));
                plugin.game.gameState = GameState.OFF;
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        for (UUID p : plugin.game.hohPlayers.keySet()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pardon " + Bukkit.getPlayer(p).getName());
                        }
                    }
                }, 0L);
                plugin.game.endGame();
            }

        }
        else {

			p.teleport(plugin.game.hohPlayers.get(p.getUniqueId()).getTeam().getBeacon().getLocation().clone().add(0, 1, 0));

            if (this.plugin.getConfig().getInt("Inventory-Keep") == 0) {
                return;
            }

            Random rand = new Random();
            int i = rand.nextInt(100);
            if (i + 1 <= this.plugin.getConfig().getInt("Inventory-Keep")) {
                player.getPlayer().sendMessage(messageManager.getMessage("kept-inventory"));
            } else {
                p.getInventory().clear();
                p.updateInventory();
                player.getPlayer().sendMessage(messageManager.getMessage("lost-inventory"));
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    for (String s : plugin.getConfig().getStringList("Item-On-Respawn")) {
                        String[] split = s.split(", ");
                        ItemStack item = new ItemStack(Material.matchMaterial(split[0].toUpperCase()), Integer.parseInt(split[1]));
                        p.getInventory().addItem(item);
                    }
                }
            }, 1);
            p.updateInventory();
        }

    }
}