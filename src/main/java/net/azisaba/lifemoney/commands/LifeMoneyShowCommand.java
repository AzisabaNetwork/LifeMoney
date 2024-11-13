package net.azisaba.lifemoney.commands;

import net.azisaba.lifemoney.LifeMoney;
import net.azisaba.lifemoney.database.DBCon;
import net.azisaba.lifemoney.money.Moneys;
import net.azisaba.lifemoney.money.coin.CoinLog;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LifeMoneyShowCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0) {
            return finish(commandSender, "§c/lms [-p <プレイヤー名>] [-d <確認する時間の範囲>] [-m <お金のタイプ>]");
        }
        UUID uuid = null;
        long time = -1;
        Moneys money = null;

        for (int i = 1; i < strings.length; i++) {
            if (strings.length % 2 != 0) {
                return finish(commandSender, "§c/lms [-p <プレイヤー名>] [-d <確認する時間の範囲>] [-m <お金のタイプ>]");
            }

            if (strings[i - 1].equalsIgnoreCase("-p")) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(strings[i]);
                uuid = p.getUniqueId();
            }
            if (strings[i - 1].equalsIgnoreCase("-d")) {

                String timeStr = strings[i];
                if (!timeStr.isEmpty() && !timeStr.isBlank() || !timeStr.contains("-") || !timeStr.contains(".") || !timeStr.startsWith("0")) {
                    timeStr = timeStr.toLowerCase();
                    try {
                        long time1 = Long.parseLong(timeStr.substring(0, timeStr.length() - 1));
                        if (timeStr.endsWith("s")) {
                            time = time1;
                        } else if (timeStr.endsWith("m")) {
                            time = time1;
                            time *= 60L;
                        } else if (timeStr.endsWith("h")) {
                            time = time1;
                            time *= 3600L;
                        } else if (timeStr.endsWith("d")) {
                            time = time1;
                            time *= 86400L;
                        } else if (timeStr.endsWith("w")) {
                            time = time1;
                            time *= 604800L;
                        } else if (timeStr.endsWith("y") && timeStr.length() < 4) {
                            time = time1;
                            time *= 31536000L;
                        }
                    } catch (NumberFormatException e) {
                        return finish(commandSender, "§c無効な時間指定です。");
                    }
                } else {
                    return finish(commandSender, "§c無効な時間指定です。");
                }
            }
            if (strings[i - 1].equalsIgnoreCase("-m")) {
                try {
                    money = Moneys.valueOf(strings[i].toUpperCase());
                } catch (IllegalArgumentException e) {
                    return finish(commandSender, "§c無効な<money-type>です。");
                }
            }
        }

        commandSender.sendMessage(Component.text("§7処理中です..."));
        UUID finalUuid = uuid;
        long finalTime = time;
        AtomicReference<List<CoinLog>> list = new AtomicReference<>(new ArrayList<>());
        if (uuid != null) {
            long epochSecond = Instant.now().getEpochSecond();
            if (money != null) {
                list.set(new ArrayList<>(Collections.singleton(new CoinLog(money, 0D, epochSecond))));
                LifeMoney.getInstance().runAsyncDelayed(() -> {
                    list.set(new DBCon().getLogsCoin(finalUuid, list.get(), epochSecond, finalTime));
                    list.set(merge(list.get()));
                    message(commandSender, list.get(), finalUuid);
                }, 1);
                return true;
            } else {
                list.set(new ArrayList<>(Arrays.stream(Moneys.values()).map((m -> new CoinLog(m, 0D, epochSecond))).toList()));
                LifeMoney.getInstance().runAsyncDelayed(() -> {
                    list.set(new DBCon().getLogsCoin(finalUuid, list.get(), epochSecond, finalTime));
                    list.set(merge(list.get()));
                    message(commandSender, list.get(), finalUuid);
                }, 1);

            }
        } else {
            Moneys finalMoney = money;
            LifeMoney.getInstance().runAsync(() -> {
                Set<UUID> uuids = new DBCon().getUUIDs();
                if (uuids.isEmpty()) {
                    finish(commandSender, "§cデータがありません。");
                    return;
                }

                long epochSecond = Instant.now().getEpochSecond();
                if (finalMoney != null) {

                    list.set(new ArrayList<>(Collections.singleton(new CoinLog(finalMoney, 0D, epochSecond))));
                    uuids.forEach(it -> list.set(new DBCon().getLogsCoin(it, list.get(), epochSecond, finalTime)));
                    list.set(merge(list.get()));
                    LifeMoney.getInstance().runAsyncDelayed(() ->
                            list.get().forEach(it -> commandSender.sendMessage(Component.text("§a§l全体の金額詳細ログ: " + "§e§l" + it.moneys().name() + "§f -> §b§l" + it.coin() + "コイン"))),  + 20L);

                } else {

                    list.set(new ArrayList<>(Arrays.stream(Moneys.values()).map((m -> new CoinLog(m, 0D, epochSecond))).toList()));
                    uuids.forEach(it -> list.set(new DBCon().getLogsCoin(it, list.get(), epochSecond, finalTime)));
                    list.set(merge(list.get()));
                    LifeMoney.getInstance().runAsyncDelayed(() ->
                            list.get().forEach(it -> commandSender.sendMessage(Component.text("§a§l全体の金額詳細ログ: " + "§e§l" + it.moneys().name() + "§f -> §b§l" + it.coin() + "コイン"))), 100L);
                }
            });

        }
        return false;
    }

    private List<CoinLog> merge(@NotNull List<CoinLog> list1) {
        Map<Moneys, Double> map = new HashMap<>();
        for (CoinLog log : list1) {
            map.merge(log.moneys(), log.coin(), Double::sum);
        }
        AtomicInteger i = new AtomicInteger(0);
        return map.entrySet().stream().map(it -> {
                    i.set(i.get() + 1);
                    return new CoinLog(it.getKey(), it.getValue(), i.get());
                }
        ).toList();
    }

    private boolean finish(@NotNull CommandSender sender, String... message) {
        Arrays.stream(message).forEach(it ->
                sender.sendMessage(Component.text(it)));
        return false;
    }

    private void message(CommandSender sender, @NotNull List<CoinLog> list, UUID uuid) {
        list.forEach(it -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            String name = p.getName() == null ? "§a§l" + p.getUniqueId() + "§fの金額詳細ログ: " :  "§a§l" + p.getName() + "§fの金額詳細ログ: ";
            sender.sendMessage(Component.text(name + "§e§l" + it.moneys().name() + "§f -> §b§l" + it.coin() + "コイン"));
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return null;
        if (strings.length == 0) return List.of("/lms -p <プレイヤー名> -d <確認する時間の範囲> -m <お金のタイプ>");
        if (strings.length == 1 || strings.length == 3 || strings.length == 5) {
            return List.of("-p", "-d", "-m");
        }

        int i = strings.length - 1;
        switch (strings[i - 1]) {
            case "-p" -> {
                if (strings[i].isEmpty()) {
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
                }
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(strings[i].toLowerCase())).toList();
            }
            case "-d" -> {
                return List.of("6s", "5m", "4h", "3d", "2w", "1y");
            }
            case "-m" -> {
                if (strings[i].isEmpty()) {
                    return Arrays.stream(Moneys.values()).map(Moneys::name).toList();
                }
                return Arrays.stream(Moneys.values()).map(Moneys::name).filter(name -> name.toLowerCase().startsWith(strings[i].toLowerCase())).toList();
            }
        }
        return null;
    }
}
