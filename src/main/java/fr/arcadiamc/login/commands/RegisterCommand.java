package fr.arcadiamc.login.commands;

import fr.arcadiamc.login.ArcadiaMCLogin;
import fr.arcadiamc.login.managers.SecurityManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {

    private final ArcadiaMCLogin plugin;

    public RegisterCommand(ArcadiaMCLogin plugin) {
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

        if (args.length != 2) {
            p.sendMessage(plugin.getMessage("invalid-usage") + " §7/register <password> <confirm>");
            return true;
        }

        String uuid = p.getUniqueId().toString();

        if (plugin.getAuthManager().hasAccount(uuid)) {
            p.sendMessage(plugin.getMessage("register-already-exists"));
            return true;
        }

        // ── Vérification captcha OBLIGATOIRE avant le register ──
        if (!plugin.getCaptchaManager().hasValidated(p.getUniqueId())) {
            p.sendMessage(plugin.getMessage("captcha-required-before-register"));
            // Regénérer le captcha si aucun n'est en cours
            if (!plugin.getCaptchaManager().hasPendingCaptcha(p.getUniqueId())) {
                plugin.getCaptchaManager().generate(p);
            }
            return true;
        }

        if (!args[0].equals(args[1])) {
            p.sendMessage(plugin.getMessage("register-passwords-mismatch"));
            return true;
        }

        // ── Validation du mot de passe (sécurité #4) ──
        SecurityManager.PasswordResult result = plugin.getSecurityManager().validatePassword(args[0]);
        switch (result) {
            case TOO_SHORT -> {
                p.sendMessage(plugin.getMessage("password-too-short",
                    "min", String.valueOf(plugin.getSecurityManager().getMinPasswordLength())));
                return true;
            }
            case BANNED -> {
                p.sendMessage(plugin.getMessage("password-too-simple"));
                return true;
            }
            default -> {} // OK
        }

        boolean success = plugin.getAuthManager().register(uuid, p.getName(), args[0]);
        if (success) {
            plugin.getSessionManager().setLoggedIn(p.getUniqueId());
            plugin.getCaptchaManager().remove(p.getUniqueId()); // nettoyer
            p.sendMessage(plugin.getMessage("register-success"));
        } else {
            p.sendMessage(plugin.getMessage("register-already-exists"));
        }

        return true;
    }
}
