package fr.securitylogin.commands;

import fr.securitylogin.SecurityLogin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /login-attempts <pseudo> — Voir les tentatives échouées d'un joueur
 * Permission : securitylogin.staff
 */
public class LoginAttemptsCommand implements CommandExecutor {

    private final SecurityLogin plugin;

    public LoginAttemptsCommand(SecurityLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("securitylogin.staff")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(SecurityLogin.colorize("&cUsage : /login-attempts <pseudo>"));
            return true;
        }

        // Chercher le joueur (en ligne ou hors ligne)
        Player onlineTarget = Bukkit.getPlayer(args[0]);
        UUID uuid = null;
        String name = args[0];

        if (onlineTarget != null) {
            uuid = onlineTarget.getUniqueId();
            name = onlineTarget.getName();
        } else {
            // Chercher via le fichier de compte
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

        int failed = plugin.getSecurityManager().getFailedAttempts(uuid);
        int max = plugin.getConfig().getInt("security.max-login-attempts", 5);
        boolean banned = plugin.getSecurityManager().isLoginBanned(uuid);

        sender.sendMessage(SecurityLogin.colorize("&8&m---------------------------------"));
        sender.sendMessage(SecurityLogin.colorize("&e Tentatives de login — &f" + name));
        sender.sendMessage(SecurityLogin.colorize("&7 Échecs : &c" + failed + " &7/ &c" + max));
        sender.sendMessage(SecurityLogin.colorize("&7 Statut : " + (banned ? "&cBanni (brute-force)" : "&aActif")));
        sender.sendMessage(SecurityLogin.colorize("&8&m---------------------------------"));

        return true;
    }
}
