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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public final class CoinLogTimer implements ICoinLogTimer {

    private final LifeMoney plugin;

    public CoinLogTimer(LifeMoney plugin) {
        this.plugin = plugin;
    }

    private static final Multimap<UUID, CoinLog> coinData = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    @Override
    public void start(long time) {
        plugin.runAsyncTimer(()-> {
            NumberFormat num = NumberFormat.getInstance();
            num.setMaximumFractionDigits(2);
            int delay = 1;
            Set<UUID> copy = coinData.keySet();
            for (UUID uuid : copy) {
                plugin.runAsyncDelayed(()-> {
                    double total = new DBCon().setLogsCoin(uuid, new ArrayList<>(coinData.get(uuid).stream().toList()));
                    coinData.removeAll(uuid);
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null) return;

                    p.sendMessage(Component.text(getTimer(time) +"の間に §e§l<COIN>LM §fを獲得しました。".replaceAll("<COIN>", num.format(total))));
                }, delay);
                delay++;
            }

        }, time * 20, time * 20);
    }

    private String getTimer(long time) {
        long s1;
        long m1 = -1;
        long h1 = -1;
        String pre = "§a";
        String s = "秒";
        String m = "分";
        String h = "時間";

        if (time >= 3600) {
            h1 = time / 3600;
            time %= 3600;
        }
        if (time >= 60) {
            m1 = time / 60;
            time %= 60;
        }
        s1 = time;
        if (h1 != -1) {
            pre+= h1 + h;
        }
        if (m1 != -1) {
            pre+= m1 + m;
        }
        if (s1 != -1 && s1 != 0) {
            pre+= s1 + s;
        }
        return pre;
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
        long epochSecond = Instant.now().getEpochSecond();
        if (!coinData.containsKey(uuid)) {
            coinData.put(uuid, new CoinLog(money, amount, epochSecond));
        } else {
            double coin = getCoin(uuid, money);
            removeCoin(uuid, money);
            coinData.put(uuid, new CoinLog(money, coin + amount, epochSecond));
        }
    }

    public void addCoinForce(UUID uuid, Moneys money, double amount) {
        plugin.runAsync(()-> {
            long epochSecond = Instant.now().getEpochSecond();
            double coin = new DBCon().setLogsCoin(uuid, new ArrayList<>(Collections.singleton(new CoinLog(money, amount, epochSecond))));
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) return;
            p.sendMessage(Component.text("§b§l"+ coin + "LM §fがアカウントに追加されました。"));
        });

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
