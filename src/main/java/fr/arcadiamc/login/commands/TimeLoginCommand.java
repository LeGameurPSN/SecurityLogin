package fr.arcadiamc.login.commands;

import fr.arcadiamc.login.ArcadiaMCLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TimeLoginCommand implements CommandExecutor {

    private final ArcadiaMCLogin plugin;

    public TimeLoginCommand(ArcadiaMCLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!p.hasPermission("arcadiamc.login.admin")) {
            p.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(plugin.getMessage("invalid-usage") + " §7/timelogin <secondes>");
            return true;
        }

        try {
            int seconds = Integer.parseInt(args[0]);
            if (seconds < 1) throw new NumberFormatException();
            plugin.getConfig().set("time-login", seconds);
            plugin.saveConfig();
            p.sendMessage(plugin.getMessage("time-updated", "seconds", String.valueOf(seconds)));
        } catch (NumberFormatException ex) {
            p.sendMessage(plugin.getMessage("invalid-usage") + " §7Entier positif attendu.");
        }

        return true;
    }
}
