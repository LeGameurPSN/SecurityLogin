package fr.securitylogin.listeners;

import fr.securitylogin.SecurityLogin;
import fr.securitylogin.managers.AuthManager;
import fr.securitylogin.managers.CaptchaManager;
import fr.securitylogin.managers.SecurityManager;
import fr.securitylogin.managers.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;

/**
 * Bloque toutes les actions des joueurs non authentifiés
 * et gère la connexion / déconnexion.
 */
public class PlayerListener implements Listener {

    private final SecurityLogin plugin;
    private final AuthManager authManager;
    private final SessionManager sessionManager;
    private final CaptchaManager captchaManager;
    private final SecurityManager securityManager;

    public PlayerListener(SecurityLogin plugin) {
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
        this.sessionManager = plugin.getSessionManager();
        this.captchaManager = plugin.getCaptchaManager();
        this.securityManager = plugin.getSecurityManager();
    }

    // ─────────────────────────────────────────────
    //  Connexion
    // ─────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        boolean hasAccount = authManager.hasAccount(p.getUniqueId().toString());

        if (hasAccount) {
            // Joueur déjà inscrit → doit se connecter
            p.sendMessage(plugin.getMessage("not-logged-in"));
            sessionManager.startKickTimer(p, true);
        } else {
            // Nouveau joueur → captcha OBLIGATOIRE avant le register
            captchaManager.generate(p);
            sessionManager.startKickTimer(p, false);
        }
    }

    // ─────────────────────────────────────────────
    //  Déconnexion → reset la session
    // ─────────────────────────────────────────────

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        sessionManager.removeSession(e.getPlayer().getUniqueId());
        captchaManager.remove(e.getPlayer().getUniqueId());
        securityManager.clearAttempts(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        sessionManager.removeSession(e.getPlayer().getUniqueId());
        captchaManager.remove(e.getPlayer().getUniqueId());
    }

    // ─────────────────────────────────────────────
    //  Blocage des commandes
    // ─────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (sessionManager.isLoggedIn(p.getUniqueId())) return;

        String cmd = e.getMessage().toLowerCase().split(" ")[0];

        // Commandes autorisées sans authentification
        if (cmd.equals("/login") || cmd.equals("/register") || cmd.equals("/captcha")
                || cmd.equals("/reset-pass") || cmd.equals("/timelogin")
                || cmd.equals("/timeregister") || cmd.equals("/oui")
                || cmd.equals("/non")) return;

        e.setCancelled(true);
        boolean hasAccount = authManager.hasAccount(p.getUniqueId().toString());
        if (hasAccount) {
            p.sendMessage(plugin.getMessage("not-logged-in"));
        } else if (!captchaManager.hasValidated(p.getUniqueId())) {
            p.sendMessage(plugin.getMessage("captcha-required-before-register"));
        } else {
            p.sendMessage(plugin.getMessage("not-registered"));
        }
    }

    // ─────────────────────────────────────────────
    //  Blocage des actions en jeu
    // ─────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!sessionManager.isLoggedIn(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {
        if (sessionManager.isLoggedIn(e.getPlayer().getUniqueId())) return;
        if (e.getFrom().getBlockX() != e.getTo().getBlockX()
                || e.getFrom().getBlockY() != e.getTo().getBlockY()
                || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            e.setTo(e.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (!sessionManager.isLoggedIn(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!sessionManager.isLoggedIn(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!sessionManager.isLoggedIn(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) {
            if (!sessionManager.isLoggedIn(p.getUniqueId())) e.setCancelled(true);
        }
        if (e.getEntity() instanceof Player p) {
            if (!sessionManager.isLoggedIn(p.getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (!sessionManager.isLoggedIn(p.getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent e) {
        if (!sessionManager.isLoggedIn(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (!sessionManager.isLoggedIn(p.getUniqueId())) e.setCancelled(true);
        }
    }
}
