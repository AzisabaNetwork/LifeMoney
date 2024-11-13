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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LifeMoneyShowCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return finish(commandSender, "§c/lms [-p <プレイヤー名>] [-d <確認する時間の範囲>] [-m <お金のタイプ>]");
        }
        UUID uuid = null;
        long time = -1;
        Moneys money = null;

        for (int i = 0; i < args.length; i++) {
            if (args.length % 2 != 0) {
                return finish(commandSender, "§c/lms [-p <プレイヤー名>] [-d <確認する時間の範囲>] [-m <お金のタイプ>]");
            }
            switch (args[i].toLowerCase()) {
                case "-p" -> uuid = Bukkit.getOfflinePlayer(args[++i]).getUniqueId();
                case "-d" -> time = convertTime(args[++i]);
                case "-m" -> money = parseMoneyType(args[++i]);
            }
        }

        if (uuid == null && time == -1 && money == null) {
            return finish(commandSender, "§c/lms [-p <プレイヤー名>] [-d <確認する時間の範囲>] [-m <お金のタイプ>]");
        }

        processCommand(commandSender, uuid, time, money);
        return true;
    }

    private long convertTime(@NotNull String timeStr) {
        try {
            long time = Long.parseLong(timeStr.substring(0, timeStr.length() - 1));
            return switch (timeStr.charAt(timeStr.length() - 1)) {
                case 's' -> time;
                case 'm' -> time * 60L;
                case 'h' -> time * 3600L;
                case 'd' -> time * 86400L;
                case 'w' -> time * 604800L;
                case 'y' -> time * 31536000L;
                default -> throw new NumberFormatException();
            };
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Nullable
    private Moneys parseMoneyType(@NotNull String type) {
        try {
            return Moneys.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void processCommand(CommandSender sender, UUID uuid, long time, Moneys money) {
        AtomicReference<List<CoinLog>> list = new AtomicReference<>(new ArrayList<>());
        if (uuid != null) {
            processCommandForSingleUser(sender, uuid, time, money, list);
        } else {
            processCommandForAllUsers(sender, time, money, list);
        }
    }

    private void processCommandForSingleUser(@NotNull CommandSender sender, UUID uuid, long time, Moneys money, @NotNull AtomicReference<List<CoinLog>> list) {
        sender.sendMessage(Component.text("§7処理中です..."));
        AtomicBoolean end = new AtomicBoolean(false);
        Set<Moneys> set = (money != null) ? Set.of(money) : new HashSet<>(Set.of(Moneys.values()));
        long epochSecond = Instant.now().getEpochSecond();

        list.set(createInitialCoinLogList(money, epochSecond));
        LifeMoney.getInstance().runAsyncDelayed(() -> {
            list.set(new DBCon().getLogsCoin(uuid, list.get(), epochSecond, time, set));
            list.set(merge(list.get()));
            end.set(true);
            message(sender, list.get(), uuid);
        }, 1);

        LifeMoney.getInstance().runAsyncDelayed(() -> {
            if (!end.get()) finish(sender, "§cデータがありません。");
        }, 100);
    }

    private void processCommandForAllUsers(CommandSender sender, long time, Moneys money, AtomicReference<List<CoinLog>> list) {
        LifeMoney.getInstance().runAsync(() -> {
            Set<UUID> uuids = new DBCon().getUUIDs();
            if (uuids.isEmpty()) {
                finish(sender, "§cデータがありません。");
                return;
            }
            long epochSecond = Instant.now().getEpochSecond();
            Set<Moneys> set = (money != null) ? Set.of(money) : new HashSet<>(Set.of(Moneys.values()));

            list.set(createInitialCoinLogList(money, epochSecond));
            uuids.forEach(it -> list.set(new DBCon().getLogsCoin(it, list.get(), epochSecond, time, set)));
            LifeMoney.getInstance().runAsyncDelayed(() -> {
                list.set(merge(list.get()));
                list.get().forEach(it -> sender.sendMessage(Component.text("§a§l全体の金額詳細ログ: §e§l" + it.moneys().name() + "§f -> §b§l" + it.coin() + "LM")));
            }, 20L);
        });
    }

    private List<CoinLog> createInitialCoinLogList(Moneys money, long epochSecond) {
        if (money != null) {
            return new ArrayList<>(Collections.singleton(new CoinLog(money, 0D, epochSecond)));
        } else {
            return Arrays.stream(Moneys.values()).map(m -> new CoinLog(m, 0D, epochSecond)).toList();
        }
    }

    private List<CoinLog> merge(@NotNull List<CoinLog> list1) {
        Map<Moneys, Double> map = new HashMap<>();
        for (CoinLog log : list1) {
            map.merge(log.moneys(), log.coin(), Double::sum);
        }
        return map.entrySet().stream().map(it -> new CoinLog(it.getKey(), it.getValue(), 0)).toList();
    }

    private boolean finish(@NotNull CommandSender sender, String... messages) {
        Arrays.stream(messages).forEach(it ->
                sender.sendMessage(Component.text(it)));
        return false;
    }

    private void message(CommandSender sender, @NotNull List<CoinLog> list, UUID uuid) {
        if (list.isEmpty()) {
            finish(sender, "§cデータがありません。");
            return;
        }
        list.forEach(it -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            String name = (p.getName() == null ? "§a§l" + p.getUniqueId() : "§a§l" + p.getName()) + "§fの金額詳細ログ: ";
            sender.sendMessage(Component.text(name + "§e§l" + it.moneys().name() + "§f -> §b§l" + it.coin() + "LM"));
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return null;

        final String INITIAL_COMMAND = "/lms -p <プレイヤー名> -d <確認する時間の範囲> -m <お金のタイプ>";
        final List<String> COMMAND_OPTIONS = List.of("-p", "-d", "-m");

        if (strings.length == 0) return List.of(INITIAL_COMMAND);
        if (strings.length % 2 == 1) return COMMAND_OPTIONS;

        int lastIndex = strings.length - 1;
        String lastArgument = strings[lastIndex - 1];
        String currentInput = strings[lastIndex];

        return switch (lastArgument) {
            case "-p" -> getMatchingOnlinePlayers(currentInput);
            case "-d" -> List.of("6s", "5m", "4h", "3d", "2w", "1y");
            case "-m" -> getMatchingMoneys(currentInput);
            default -> null;
        };
    }

    private List<String> getMatchingOnlinePlayers(String currentInput) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(currentInput.toLowerCase()))
                .toList();
    }

    private List<String> getMatchingMoneys(String currentInput) {
        return Arrays.stream(Moneys.values())
                .map(Moneys::name)
                .filter(name -> name.toLowerCase().startsWith(currentInput.toLowerCase()))
                .toList();
    }
}
