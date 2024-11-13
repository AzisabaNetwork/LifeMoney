package net.azisaba.lifemoney;

import net.azisaba.lifemoney.commands.LifeMoneyForceGiveCommand;
import net.azisaba.lifemoney.commands.LifeMoneyGiveCommand;
import net.azisaba.lifemoney.commands.LifeMoneyShowCommand;
import net.azisaba.lifemoney.database.DBCon;
import net.azisaba.lifemoney.listener.FarmBlockListener;
import net.azisaba.lifemoney.listener.MineBlockListener;
import net.azisaba.lifemoney.listener.WoodCutBlockListener;
import net.azisaba.lifemoney.money.coin.CoinLogTimer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class LifeMoney extends JavaPlugin implements Task {


    private CoinLogTimer coinTimer;

    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        coinTimer = new CoinLogTimer(this);
        coinTimer.start(600);

        if (isEconomy()) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            economy = rsp == null ? null : rsp.getProvider();
        }

        registerDatabase();
        registerListeners();
        registerCommands();

    }

    @Override
    public void onDisable() {
        DBCon.close();
    }

    private void registerDatabase() {
        runAsync(()-> new DBCon().initialize(this));
    }

    private void registerListeners() {
        new MineBlockListener().initialize(this);
        new WoodCutBlockListener().initialize(this);
        new FarmBlockListener().initialize(this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("lifemoneygive")).setExecutor(new LifeMoneyGiveCommand());
        Objects.requireNonNull(getCommand("lifemoneyshow")).setExecutor(new LifeMoneyShowCommand());
        Objects.requireNonNull(getCommand("lifemoneyforcegive")).setExecutor(new LifeMoneyForceGiveCommand());
    }

    public boolean isMythic() {
        Plugin p = getServer().getPluginManager().getPlugin("MythicMobs");
        return p != null && p.isEnabled();
    }

    public boolean isEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("Vault");
        return p != null && p.isEnabled();
    }

    @NotNull
    public static LifeMoney getInstance() {
        return JavaPlugin.getPlugin(LifeMoney.class);
    }

    public CoinLogTimer getCoinTimer() {
        return coinTimer;
    }

    public Economy getEconomy() {
        return economy;
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this, runnable);}

    @Override
    public void runSync(Runnable runnable) {Bukkit.getScheduler().runTask(this, runnable);}

    @Override
    public void runSyncDelayed(Runnable runnable, long delay) {Bukkit.getScheduler().runTaskLater(this, runnable, delay);}

    @Override
    public void runAsyncDelayed(Runnable runnable, long delay) {Bukkit.getScheduler().runTaskLaterAsynchronously(this, runnable, delay);}

    @Override
    public void runSyncTimer(Runnable runnable, long delay, long loop) {Bukkit.getScheduler().runTaskTimer(this, runnable, delay, loop);}

    @Override
    public void runAsyncTimer(Runnable runnable, long delay, long loop) {Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, loop);}

}
