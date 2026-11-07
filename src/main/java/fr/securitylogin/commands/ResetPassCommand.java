package fr.securitylogin.commands;

import fr.securitylogin.SecurityLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * /reset-pass <pseudo> → demande confirmation via /oui ou /non
 *
 * Les commandes /oui et /non sont interceptées comme des sous-commandes ici
 * via un Listener interne enregistré au démarrage du plugin.
 */
public class ResetPassCommand implements CommandExecutor, Listener {

    private final SecurityLogin plugin;

    public ResetPassCommand(SecurityLogin plugin) {
        this.plugin = plugin;
        // Enregistrement du listener pour /oui et /non
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!staff.hasPermission("securitylogin.staff")) {
            staff.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length != 1) {
            staff.sendMessage(plugin.getMessage("invalid-usage") + " §7/reset-pass <pseudo>");
            return true;
        }

        String targetName = args[0];

        if (!plugin.getAuthManager().accountExistsByName(targetName)) {
            staff.sendMessage(plugin.getMessage("reset-pass-no-account"));
            return true;
        }

        // Enregistrer la demande de confirmation
        plugin.getSessionManager().setPendingReset(staff.getUniqueId(), targetName);
        staff.sendMessage(plugin.getMessage("reset-pass-confirm", "player", targetName));

        return true;
    }

    // ─────────────────────────────────────────────
    //  Interception de /oui et /non
    // ─────────────────────────────────────────────

    @EventHandler
    public void onConfirmCommand(PlayerCommandPreprocessEvent e) {
        Player staff = e.getPlayer();
        if (!plugin.getSessionManager().hasPendingReset(staff.getUniqueId())) return;

        String cmd = e.getMessage().trim().toLowerCase();

        if (cmd.equals("/oui")) {
            e.setCancelled(true);
            String targetName = plugin.getSessionManager().getPendingReset(staff.getUniqueId());
            plugin.getSessionManager().clearPendingReset(staff.getUniqueId());

            boolean success = plugin.getAuthManager().resetPasswordByName(targetName);
            if (success) {
                staff.sendMessage(plugin.getMessage("reset-pass-success", "player", targetName));
            } else {
                staff.sendMessage(plugin.getMessage("reset-pass-no-account"));
            }

        } else if (cmd.equals("/non")) {
            e.setCancelled(true);
            plugin.getSessionManager().clearPendingReset(staff.getUniqueId());
            staff.sendMessage(plugin.getMessage("reset-pass-cancelled"));
        }
    }
}
