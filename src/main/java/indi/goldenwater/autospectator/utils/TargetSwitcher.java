package indi.goldenwater.autospectator.utils;

import indi.goldenwater.autospectator.AutoSpectator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TargetSwitcher extends BukkitRunnable {
    private final Map<String, BukkitRunnable> tasks = new HashMap<>();
    private Configuration config;

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        if (onlinePlayers.size() <= 1) return; // 如果玩家数小于等于1

        for (Player onlinePlayer : onlinePlayers) { // 寻找启用了 自动旁观者 并 为旁观者模式 且 没旁观目标的玩家
            if (!onlinePlayer.getScoreboardTags().contains("autoSpectatorEnable")) continue; // 如果没启用自动旁观者

            if (onlinePlayer.getGameMode().compareTo(GameMode.SPECTATOR) != 0) continue; // 如果非旁观者模式

            Entity spectatorTarget = onlinePlayer.getSpectatorTarget();
            if (spectatorTarget != null) continue; // 如果有旁观目标

            String onlinePlayerName = onlinePlayer.getName();
            if (tasks.containsKey(onlinePlayerName)) { // 如果任务列表有
                if (!tasks.get(onlinePlayerName).isCancelled()){ // 如果被取消
                    continue;
                }
            }

            for (Player player : onlinePlayers) { // 寻找 不为旁观者 的目标
                if (player.getName().compareTo(onlinePlayer.getName()) == 0) continue; // 如果是同一个玩家

                if (player.getGameMode().compareTo(GameMode.SPECTATOR) == 0) continue; // 如果是旁观者模式

                double triggerDistance = config.getDouble("settings.teleport.distance");
                boolean enableTeleport = config.getBoolean("settings.teleport.enable");
                if (calcDistance(player.getLocation(), onlinePlayer.getLocation()) > triggerDistance &&
                        enableTeleport) { // 如果 两者距离 大于 设置的触发距离 且 启用了传送
                    onlinePlayer.teleport(player);
                    BukkitRunnable task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (onlinePlayer.isOnline() && player.isOnline() &&
                                    calcDistance(player.getLocation(), onlinePlayer.getLocation()) < triggerDistance){ // 如果都在线 且 距离 小于 设置的触发距离
                                onlinePlayer.setSpectatorTarget(player);
                            }

                            tasks.remove(onlinePlayerName);
                        }
                    };

                    task.runTaskLater(AutoSpectator.getInstance(),
                            config.getLong("settings.teleport.switchDelay")); // 延迟设定的tick

                    tasks.put(onlinePlayerName, task);
                } else {
                    onlinePlayer.setSpectatorTarget(player);
                }
            }
        }
    }

    public void setConfig(Configuration config){
        this.config = config;
    }

    private double calcDistance(Location a, Location b) {
        if (!Objects.equals(a.getWorld(), b.getWorld())) return Double.MAX_VALUE;

        return a.distance(b);
    }
}
