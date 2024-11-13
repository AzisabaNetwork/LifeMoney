package net.azisaba.lifemoney.commands;

import net.azisaba.lifemoney.LifeMoney;
import net.azisaba.lifemoney.database.DBCon;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LifeMoneySilentCommand implements TabExecutor {

    private static final Set<UUID> isSet = new HashSet<>();


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player p)) return false;
        if (strings.length == 0) return   set(p);
        if (strings.length == 1) {
            boolean b;
            try {
                b = Boolean.parseBoolean(strings[0]);
                return set(p, b);
            } catch (NumberFormatException e) {
                return set(p);
            }
        }
        return false;
    }

    private boolean set(Player p) {
        LifeMoney.getInstance().runAsync(() -> {
            boolean b = DBCon.isHide(p.getUniqueId());
            set(p, !b);
        });
        return true;
    }

    private boolean set(Player p, boolean b) {
        LifeMoney.getInstance().runAsync(() -> {
            DBCon.setHide(p.getUniqueId(), b);
            if (b) {
                isSet.remove(p.getUniqueId());
                p.sendMessage(Component.text("§aLifeMoney獲得ログ§fを、チャットに §c§l表示しない §fようにしました。"));
            } else {
                isSet.add(p.getUniqueId());
                p.sendMessage(Component.text("§aLifeMoney獲得ログ§fを、チャットに §b§l表示する §fようにしました。"));
            }
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return null;
        if (strings.length == 1) {
            return List.of("true", "false");
        }
        return null;
    }

    public static boolean isSet(UUID uuid) {
        return isSet.contains(uuid);
    }
}
