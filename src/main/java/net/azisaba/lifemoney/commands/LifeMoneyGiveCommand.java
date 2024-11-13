package net.azisaba.lifemoney.commands;

import net.azisaba.lifemoney.money.Moneys;
import net.azisaba.lifemoney.money.coin.Coin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

public class LifeMoneyGiveCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 3) return finish(commandSender,"§c/lmg <player> <amount> <money-type> [-s <true|false>]");
        Player p = Bukkit.getPlayer(strings[0]);
        if (p == null) return finish(commandSender, "§cそのプレイヤーは存在しません。");

        double amount;
        try {
            amount = Double.parseDouble(strings[1]);
            if (amount == 0) return finish(commandSender, "§c<amount>が0です。");
        } catch (NumberFormatException e) {
            return finish(commandSender, "§c<amount>は正の数でなくてはなりません。");
        }

        Moneys money;
        try {
            money = Moneys.valueOf(strings[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            return finish(commandSender, "§c無効な<money-type>です。");
        }
        Coin.addCoin(p.getUniqueId(), money, amount);
        if (strings.length != 4 || !strings[2].equalsIgnoreCase("-s") || !strings[3].equalsIgnoreCase("true")) {
            NumberFormat num = NumberFormat.getInstance();
            num.setMaximumFractionDigits(2);
            commandSender.sendMessage("§a" + p.getName() + "に、" + num.format(amount) + "コインを付与しました。");
        }
        return true;
    }

    private boolean finish(@NotNull CommandSender sender, String... message) {
        Arrays.stream(message).forEach(it ->
                sender.sendMessage(Component.text(it)));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            String arg1 = strings[0];
            if (arg1.isEmpty()) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(arg1.toLowerCase())).toList();

        }

        if (strings.length == 2) {
            return List.of("渡したい金額を記入する");
        }

        if (strings.length == 3) {
            String arg3 = strings[2];
            if (arg3.isEmpty()) {
                return Arrays.stream(Moneys.values()).map(Moneys::name).toList();
            }
            return Arrays.stream(Moneys.values()).map(Moneys::name).filter(name -> name.toLowerCase().startsWith(arg3.toLowerCase())).toList();
        }

        if (strings.length == 5 && strings[3].equalsIgnoreCase("-s")) {
            return List.of("true", "false");
        }
        return null;
    }
}
