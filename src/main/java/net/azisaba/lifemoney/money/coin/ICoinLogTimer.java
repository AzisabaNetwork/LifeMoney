package net.azisaba.lifemoney.money.coin;

import com.google.common.collect.Multimap;
import net.azisaba.lifemoney.money.Moneys;

import java.util.UUID;

public interface ICoinLogTimer {

    void start();

    Multimap<UUID, CoinLog> getCoinData();

    boolean hasCoin(UUID uuid);

    boolean hasCoin(UUID uuid, Moneys money);

    void removeCoin(UUID uuid, Moneys money);

    void addCoin(UUID uuid, Moneys money, double amount);

    double getCoin(UUID uuid, Moneys money);

    double getCoin(UUID uuid);

    void setCoin(UUID uuid, Moneys money, double amount);

    void clearAll();

    void clear(UUID uuid);
}
