package net.azisaba.lifemoney.money.coin;

import net.azisaba.lifemoney.LifeMoney;
import net.azisaba.lifemoney.money.Moneys;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Coin {

    public static void addCoin(UUID uuid, Moneys money, double amount) {
        Economy economy = economyProcess(uuid, money, amount);
        if (economy != null) {
            economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
        }
    }

    public static void subtractCoin(UUID uuid, Moneys money, double amount) {
        Economy economy = economyProcess(uuid, money, amount);
        if (economy != null) {
            economy.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), amount);
        }
    }

    public static void addCoinForce(UUID uuid, Moneys money, double amount) {
        Economy economy = economyProcessForce(uuid, money, amount);
        if (economy != null) {
            economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
        }
    }

    public static void addCoinLog(UUID uuid, Moneys money, double amount) {
        if (isEnabled()) {
            if (amount <= 0) return;
            LifeMoney.getInstance().getCoinTimer().addCoin(uuid, money, amount);
        }
    }

    @Nullable
    private static Economy economyProcess(UUID uuid, Moneys money, double amount) {
        if (isEnabled()) {
            if (amount <= 0) return null;
            LifeMoney.getInstance().getCoinTimer().addCoin(uuid, money, amount);

            return LifeMoney.getInstance().getEconomy();
        }
        return null;
    }

    @Nullable
    private static Economy economyProcessForce(UUID uuid, Moneys money, double amount) {
        if (isEnabled()) {
            if (amount <= 0) return null;
            LifeMoney.getInstance().getCoinTimer().addCoinForce(uuid, money, amount);

            return LifeMoney.getInstance().getEconomy();
        }
        return null;
    }

    private static boolean isEnabled() {
        return LifeMoney.getInstance().isEconomy() && LifeMoney.getInstance().getEconomy() != null;
    }

    public static class Log extends Coin {

        public static void setLog(UUID uuid, Moneys money, long amount) {
            if (isEnabled()) {
                LifeMoney.getInstance().getCoinTimer().setCoin(uuid, money, amount);
            }
        }

        public static long getCoinLog(UUID uuid, Moneys money) {
            if (isEnabled()) {
                LifeMoney.getInstance().getCoinTimer().getCoin(uuid, money);
            }
            return 0;
        }

        public static void removeLog(UUID uuid, Moneys money) {
            if (isEnabled()) {
                LifeMoney.getInstance().getCoinTimer().removeCoin(uuid, money);
            }
        }

        public static void addLog(UUID uuid, Moneys money, long amount) {
            if (isEnabled()) {
                LifeMoney.getInstance().getCoinTimer().addCoin(uuid, money, amount);
            }
        }

        public static void clearLog(UUID uuid) {
            if (isEnabled()) {
                LifeMoney.getInstance().getCoinTimer().clear(uuid);
            }
        }

        public static void clearAllLog() {
            if (isEnabled()) {
                LifeMoney.getInstance().getCoinTimer().clearAll();
            }
        }

        public static boolean hasLog(UUID uuid, Moneys money) {
            if (isEnabled()) {
                return LifeMoney.getInstance().getCoinTimer().hasCoin(uuid, money);
            }
            return false;
        }
    }
}
