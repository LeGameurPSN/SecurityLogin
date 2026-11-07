package fr.arcadiamc.login.commands;

import fr.arcadiamc.login.ArcadiaMCLogin;
import fr.arcadiamc.login.managers.CaptchaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * /captcha <code>              — Valider son captcha
 * /captcha reload <pseudo>     — [admin] Regénérer le captcha d'un joueur
 * /captcha bypass <pseudo>     — [admin] Passer le captcha d'un joueur
 */
public class CaptchaCommand implements CommandExecutor, TabCompleter {

    private final ArcadiaMCLogin plugin;
    private final CaptchaManager captchaManager;

    public CaptchaCommand(ArcadiaMCLogin plugin) {
        this.plugin = plugin;
        this.captchaManager = plugin.getCaptchaManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ArcadiaMCLogin.colorize("&cUsage : /captcha <code>"));
            return true;
        }

        // ── Sous-commandes admin ─────────────────
        if (args[0].equalsIgnoreCase("reload")) {
            return handleReload(p, args);
        }
        if (args[0].equalsIgnoreCase("bypass")) {
            return handleBypass(p, args);
        }

        // ── Validation du code captcha ───────────
        if (plugin.getSessionManager().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(plugin.getMessage("already-logged-in"));
            return true;
        }

        // Le captcha n'est requis que pour les non-inscrits
        if (plugin.getAuthManager().hasAccount(p.getUniqueId().toString())) {
            p.sendMessage(ArcadiaMCLogin.colorize("&cLe captcha n'est pas nécessaire pour se connecter."));
            return true;
        }

        if (captchaManager.hasValidated(p.getUniqueId())) {
            p.sendMessage(plugin.getMessage("captcha-already-validated"));
            return true;
        }

        boolean ok = captchaManager.verify(p.getUniqueId(), args[0]);
        if (ok) {
            p.sendMessage(plugin.getMessage("captcha-success"));
        } else {
            p.sendMessage(plugin.getMessage("captcha-wrong"));
        }
        return true;
    }

    private boolean handleReload(Player p, String[] args) {
        if (!p.hasPermission("arcadiamc.login.admin")) {
            p.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }
        if (args.length != 2) {
            p.sendMessage(ArcadiaMCLogin.colorize("&cUsage : /captcha reload <pseudo>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            p.sendMessage(plugin.getMessage("player-not-found"));
            return true;
        }
        captchaManager.regenerate(target);
        p.sendMessage(plugin.getMessage("captcha-reloaded", "player", target.getName()));
        return true;
    }

    private boolean handleBypass(Player p, String[] args) {
        if (!p.hasPermission("arcadiamc.login.admin")) {
            p.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }
        if (args.length != 2) {
            p.sendMessage(ArcadiaMCLogin.colorize("&cUsage : /captcha bypass <pseudo>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            p.sendMessage(plugin.getMessage("player-not-found"));
            return true;
        }
        captchaManager.bypass(target.getUniqueId());
        p.sendMessage(plugin.getMessage("captcha-bypass", "player", target.getName()));
        target.sendMessage(plugin.getMessage("captcha-bypass-notify"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("arcadiamc.login.admin")) {
            return List.of("reload", "bypass");
        }
        return Collections.emptyList();
    }
}
