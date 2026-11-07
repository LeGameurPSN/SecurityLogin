package fr.securitylogin.commands;

import fr.securitylogin.SecurityLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePassCommand implements CommandExecutor {

    private final SecurityLogin plugin;

    public ChangePassCommand(SecurityLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!plugin.getSessionManager().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(plugin.getMessage("not-logged-in"));
            return true;
        }

        if (args.length != 3) {
            p.sendMessage(plugin.getMessage("invalid-usage") + " §7/change-pass <ancien> <nouveau> <confirmer>");
            return true;
        }

        String oldPass = args[0];
        String newPass = args[1];
        String confirm = args[2];

        if (!newPass.equals(confirm)) {
            p.sendMessage(plugin.getMessage("register-passwords-mismatch"));
            return true;
        }

        boolean success = plugin.getAuthManager().changePassword(p.getUniqueId().toString(), oldPass, newPass);
        if (success) {
            p.sendMessage(plugin.getMessage("change-pass-success"));
        } else {
            p.sendMessage(plugin.getMessage("change-pass-wrong"));
        }

        return true;
    }
}
