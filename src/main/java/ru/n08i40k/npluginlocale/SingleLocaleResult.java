package ru.n08i40k.npluginlocale;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SingleLocaleResult {
    String line;

    public SingleLocaleResult(String line) {
        this.line = line;
    }

    public SingleLocaleResult sendMessage(CommandSender sender) {
        sender.sendMessage(this::getC);
        return this;
    }

    public SingleLocaleResult sendServerMessage() {
        return sendMessage(Locale.getInstance().getPlugin().getServer().getConsoleSender());
    }

    public SingleLocaleResult sendToAllServer() {
        Bukkit.getOnlinePlayers().forEach(this::sendMessage);
        return this;
    }

    public SingleLocaleResult sendToAdmins() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.isOp() || player.hasPermission(Locale.getInstance().getPlugin().getName() + ".admin-notify"))
                sendMessage(player);
        });

        return this;
    }

    public String get() {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public TextComponent getC() {
        return NColorUtils.translate(NColorUtils.parseGradientTags(line), true);
    }

    @Override
    public String toString() {
        return get();
    }
}
