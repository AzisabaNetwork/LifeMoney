package net.azisaba.lifemoney.money.coin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.azisaba.lifemoney.LifeMoney;
import net.azisaba.lifemoney.database.DBCon;
import net.azisaba.lifemoney.money.Moneys;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.UUID;

public final class CoinLogTimer implements ICoinLogTimer {

    private final LifeMoney plugin;

    public CoinLogTimer(LifeMoney plugin) {
        this.plugin = plugin;
    }

    private static final Multimap<UUID, CoinLog> coinData = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    @Override
    public void start() {
        plugin.runAsyncTimer(()-> {
            NumberFormat num = NumberFormat.getInstance();
            num.setMaximumFractionDigits(2);
            int delay = 1;
            for (UUID uuid : coinData.keySet()) {
                plugin.runAsyncDelayed(()-> {
                    double total = new DBCon().setLogsCoin(uuid, new ArrayList<>(coinData.get(uuid).stream().toList()));
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null) return;

                    p.sendMessage(Component.text("§a5分の間に §a§l<COIN>コイン §fを獲得しました。".replaceAll("<COIN>", num.format(total))));
                }, delay);
                delay++;
            }
            clearAll();

        }, 6000, 6000);
    }

    @Override
    public Multimap<UUID, CoinLog> getCoinData() {
        return coinData;
    }

    @Override
    public boolean hasCoin(UUID uuid) {
        return coinData.containsKey(uuid);
    }

    @Override
    public boolean hasCoin(UUID uuid, Moneys money) {
        if (!coinData.containsKey(uuid)) return false;
        for (CoinLog data : coinData.get(uuid)) {
            if (data.moneys() == money) return true;
        }
        return false;
    }

    @Override
    public void removeCoin(UUID uuid, Moneys money) {
        for (CoinLog data : coinData.get(uuid)) {
            if (data.moneys() == money) {
                coinData.remove(uuid, data);
                return;
            }
        }
    }

    @Override
    public void addCoin(UUID uuid, Moneys money, double amount) {
        if (!coinData.containsKey(uuid)) {
            coinData.put(uuid, new CoinLog(money, amount));
        } else {
            double coin = getCoin(uuid, money);
            removeCoin(uuid, money);
            coinData.put(uuid, new CoinLog(money, coin + amount));
        }
    }

    @Override
    public double getCoin(UUID uuid, Moneys money) {
        for (CoinLog data : coinData.get(uuid)) {
            if (data.moneys() == money) return data.coin();
        }
        return 0;
    }

    @Override
    public double getCoin(UUID uuid) {
        double coin = 0;
        for (CoinLog data : coinData.get(uuid)) {
            coin+=data.coin();
        }
        return coin;
    }

    @Override
    public void setCoin(UUID uuid, Moneys money, double amount) {
        removeCoin(uuid, money);
        addCoin(uuid, money, amount);
    }

    @Override
    public void clearAll() {
        coinData.clear();
    }

    @Override
    public void clear(UUID uuid) {
        coinData.removeAll(uuid);
    }
}
