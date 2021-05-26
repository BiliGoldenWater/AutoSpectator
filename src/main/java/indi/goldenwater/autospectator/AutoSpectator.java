package indi.goldenwater.autospectator;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public final class AutoSpectator extends JavaPlugin {
    BukkitRunnable detector;

    @Override
    public void onEnable() {
        // Plugin startup logic
        detector = new BukkitRunnable() {

            @Override
            public void run() {
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

                if (onlinePlayers.isEmpty() || onlinePlayers.size() == 1) return;

                for (Player player : onlinePlayers) {
                    if (!player.getScoreboardTags().contains("autoSpectator")) continue;

                    GameMode gamemode = player.getGameMode();
                    if (gamemode.compareTo(GameMode.SPECTATOR) != 0) continue;

                    Entity target = player.getSpectatorTarget();
                    if (target != null) continue;

                    for (Player onlinePlayer : onlinePlayers) {
                        if (onlinePlayer.getUniqueId().compareTo(player.getUniqueId()) == 0) continue;

                        if (onlinePlayer.getGameMode().equals(GameMode.SPECTATOR)) continue;

                        player.setSpectatorTarget(onlinePlayer);
                        break;
                    }
                }
            }
        };

        detector.runTaskTimer(this, 0, 5);

        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (detector != null) {
            detector.cancel();
        }
        getLogger().info("Disabled.");
    }
}
