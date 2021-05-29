package indi.goldenwater.autospectator;

import indi.goldenwater.autospectator.utils.ConfigWatchService;
import indi.goldenwater.autospectator.utils.I18nManager;
import indi.goldenwater.autospectator.utils.TargetSwitcher;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoSpectator extends JavaPlugin {
    private static AutoSpectator instance;

    private ConfigWatchService configWatchService = null;
    private TargetSwitcher targetSwitcher;
    private I18nManager i18nManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        registerI18nManager();

        registerWatchService();
        registerTargetSwitcher();

        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (targetSwitcher != null) {
            targetSwitcher.cancel();
        }
        if (configWatchService != null) {
            configWatchService.unregister();
        }
        getLogger().info("Disabled.");
    }

    private void registerI18nManager(){
        this.i18nManager = new I18nManager(getDataFolder(), "lang", "en_us");
        this.i18nManager.releaseDefaultLangFile(this, "lang", "langList.json", false);
    }

    private void registerWatchService(){
        configWatchService = new ConfigWatchService(this);
        configWatchService.register("fileWatchService",
                name -> name.endsWith(".yml"),
                new ConfigWatchService.DoSomeThing() {
                    @Override
                    public void reload() {
                        reloadConfig();
                        i18nManager.reload();
                        targetSwitcher.setConfig(getConfig());
                    }

                    @Override
                    public void release() {
                        saveDefaultConfig();
                        i18nManager.releaseDefaultLangFile(AutoSpectator.getInstance(), "lang", "langList.json", false);
                    }
                });
    }

    private void registerTargetSwitcher(){
        targetSwitcher = new TargetSwitcher();
        targetSwitcher.setConfig(getConfig());
        targetSwitcher.runTaskTimer(this, 0, getConfig().getLong("settings.detectPeriod"));
    }

    public static AutoSpectator getInstance() {
        return instance;
    }

    public I18nManager getI18nManager() {
        return i18nManager;
    }
}
