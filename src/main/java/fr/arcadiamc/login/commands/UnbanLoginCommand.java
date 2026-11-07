package fr.arcadiamc.login.commands;

import fr.arcadiamc.login.ArcadiaMCLogin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /unban-login <pseudo> — Débloquer un joueur banni par brute-force
 * Permission : arcadiamc.login.staff
 */
public class UnbanLoginCommand implements CommandExecutor {

    private final ArcadiaMCLogin plugin;

    public UnbanLoginCommand(ArcadiaMCLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("arcadiamc.login.staff")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ArcadiaMCLogin.colorize("&cUsage : /unban-login <pseudo>"));
            return true;
        }

        UUID uuid = null;
        String name = args[0];

        Player onlineTarget = Bukkit.getPlayer(args[0]);
        if (onlineTarget != null) {
            uuid = onlineTarget.getUniqueId();
            name = onlineTarget.getName();
        } else {
            java.io.File f = plugin.getAuthManager().findFileByName(args[0]);
            if (f != null) {
                org.bukkit.configuration.file.FileConfiguration cfg =
                    org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(f);
                String uuidStr = cfg.getString("uuid", null);
                if (uuidStr != null) {
                    uuid = UUID.fromString(uuidStr);
                    name = cfg.getString("name", args[0]);
                }
            }
        }

        if (uuid == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return true;
        }

        plugin.getSecurityManager().unbanLogin(uuid);
        sender.sendMessage(plugin.getMessage("unban-login-success", "player", name));

        // Si le joueur est en ligne, l'informer
        if (onlineTarget != null) {
            onlineTarget.sendMessage(plugin.getMessage("unban-login-notify"));
        }

        return true;
    }
}
