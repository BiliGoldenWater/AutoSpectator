package indi.goldenwater.autospectator;

import indi.goldenwater.autospectator.utils.ConfigWatchService;
import indi.goldenwater.autospectator.utils.I18nManager;
import indi.goldenwater.autospectator.utils.TargetSwitcher;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoSpectator extends JavaPlugin {
    private static AutoSpectator instance;

    private ConfigWatchService configWatchService = null;
    private TargetSwitcher detector;
    private I18nManager i18nManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        this.i18nManager = new I18nManager(getDataFolder(), "lang", "en_us");
        this.i18nManager.releaseDefaultLangFile(this, "lang", "langList.json", false);

        configWatchService = new ConfigWatchService(this);
        configWatchService.register("fileWatchService",
                name -> name.endsWith(".yml"),
                new ConfigWatchService.DoSomeThing() {
                    @Override
                    public void reload() {
                        reloadConfig();
                        i18nManager.reload();
                        detector.setConfig(getConfig());
                    }

                    @Override
                    public void release() {
                        saveDefaultConfig();
                        i18nManager.releaseDefaultLangFile(AutoSpectator.getInstance(), "lang", "langList.json", false);
                    }
                });

        detector = new TargetSwitcher();
        detector.setConfig(getConfig());
        detector.runTaskTimer(this, 0, getConfig().getLong("settings.detectPeriod"));

        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (detector != null) {
            detector.cancel();
        }
        if (configWatchService != null) {
            configWatchService.unregister();
        }
        getLogger().info("Disabled.");
    }

    public static AutoSpectator getInstance() {
        return instance;
    }

    public I18nManager getI18nManager() {
        return i18nManager;
    }
}
