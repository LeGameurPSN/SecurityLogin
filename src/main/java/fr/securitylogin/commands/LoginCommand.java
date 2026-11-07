package fr.securitylogin.commands;

import fr.securitylogin.SecurityLogin;
import fr.securitylogin.managers.SecurityManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {

    private final SecurityLogin plugin;

    public LoginCommand(SecurityLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (plugin.getSessionManager().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(plugin.getMessage("already-logged-in"));
            return true;
        }

        // Vérifier si le joueur est banni (brute-force)
        if (plugin.getSecurityManager().isLoginBanned(p.getUniqueId())) {
            p.kickPlayer(plugin.getMessage("login-banned"));
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(plugin.getMessage("invalid-usage") + " §7/login <password>");
            return true;
        }

        String uuid = p.getUniqueId().toString();

        if (!plugin.getAuthManager().hasAccount(uuid)) {
            p.sendMessage(plugin.getMessage("not-registered"));
            return true;
        }

        // ── Anti-brute force (trop de tentatives rapides) ──
        if (plugin.getSecurityManager().checkBruteForce(p)) {
            plugin.getSecurityManager().unbanLogin(p.getUniqueId()); // reset puis ban
            plugin.getSecurityManager().recordFailedLogin(p);
            p.kickPlayer(plugin.getMessage("login-brute-force"));
            return true;
        }

        if (plugin.getAuthManager().checkPassword(uuid, args[0])) {
            plugin.getSessionManager().setLoggedIn(p.getUniqueId());
            plugin.getSecurityManager().clearAttempts(p.getUniqueId());
            p.sendMessage(plugin.getMessage("login-success"));
        } else {
            boolean kicked = plugin.getSecurityManager().recordFailedLogin(p);
            if (kicked) {
                p.kickPlayer(plugin.getMessage("login-too-many-attempts"));
            } else {
                int remaining = plugin.getSecurityManager().getRemainingAttempts(p.getUniqueId());
                p.sendMessage(plugin.getMessage("login-wrong-password"));
                p.sendMessage(SecurityLogin.colorize("&7Tentatives restantes : &c" + remaining));
            }
        }

        return true;
    }
}
