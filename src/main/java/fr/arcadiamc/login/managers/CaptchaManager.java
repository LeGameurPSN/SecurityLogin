package fr.arcadiamc.login.managers;

import fr.arcadiamc.login.ArcadiaMCLogin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Gère les captchas à l'arrivée des joueurs non inscrits.
 * Génère un code alphanumérique de 8 caractères, l'affiche en titre + chat,
 * et expire après N secondes (configurable : captcha-expire-seconds).
 */
public class CaptchaManager {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sans I,O,0,1 → lisibilité

    private final ArcadiaMCLogin plugin;

    /** Code actuel par joueur */
    private final Map<UUID, String> codes = new HashMap<>();

    /** Joueurs ayant validé leur captcha */
    private final Set<UUID> validated = new HashSet<>();

    /** Tâches d'expiration */
    private final Map<UUID, BukkitTask> expireTasks = new HashMap<>();

    public CaptchaManager(ArcadiaMCLogin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    //  Génération & affichage
    // ─────────────────────────────────────────────

    /**
     * Génère un captcha pour un joueur et démarre le timer d'expiration.
     */
    public void generate(Player player) {
        UUID uuid = player.getUniqueId();

        // Annuler l'ancien captcha s'il y en a un
        cancel(uuid);

        String code = generateCode();
        codes.put(uuid, code);
        validated.remove(uuid);

        // Affichage initial
        sendCaptchaDisplay(player, code);

        // Timer d'expiration
        int expireSeconds = plugin.getConfig().getInt("captcha-expire-seconds", 60);
        BukkitTask expireTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline() && !validated.contains(uuid)) {
                cancel(uuid);
                p.kickPlayer(plugin.getMessage("captcha-expired"));
            }
            expireTasks.remove(uuid);
        }, expireSeconds * 20L);
        expireTasks.put(uuid, expireTask);
    }

    /**
     * Vérifie le code soumis par le joueur.
     * @return true si correct
     */
    public boolean verify(UUID uuid, String input) {
        String expected = codes.get(uuid);
        if (expected == null) return false;
        boolean ok = expected.equalsIgnoreCase(input.trim());
        if (ok) {
            validated.add(uuid);
            cancel(uuid);
        }
        return ok;
    }

    /**
     * Regénère un captcha pour un joueur (commande admin).
     */
    public void regenerate(Player target) {
        validated.remove(target.getUniqueId());
        generate(target);
    }

    /**
     * Bypass le captcha pour un joueur (admin).
     */
    public void bypass(UUID uuid) {
        validated.add(uuid);
        cancel(uuid);
    }

    public boolean hasValidated(UUID uuid) {
        return validated.contains(uuid);
    }

    public boolean hasPendingCaptcha(UUID uuid) {
        return codes.containsKey(uuid) && !validated.contains(uuid);
    }

    public String getCode(UUID uuid) {
        return codes.get(uuid);
    }

    // ─────────────────────────────────────────────
    //  Nettoyage
    // ─────────────────────────────────────────────

    public void cancel(UUID uuid) {
        BukkitTask e = expireTasks.remove(uuid);
        if (e != null) e.cancel();
        codes.remove(uuid);
    }

    public void remove(UUID uuid) {
        cancel(uuid);
        validated.remove(uuid);
    }

    public void cancelAll() {
        expireTasks.values().forEach(BukkitTask::cancel);
        expireTasks.clear();
        codes.clear();
        validated.clear();
    }

    // ─────────────────────────────────────────────
    //  Helpers privés
    // ─────────────────────────────────────────────

    private String generateCode() {
        java.util.Random rng = new java.util.Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(rng.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private void sendCaptchaDisplay(Player player, String code) {
        player.sendMessage(ArcadiaMCLogin.colorize("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(ArcadiaMCLogin.colorize("&e  ✦ CODE CAPTCHA : &b&l" + code));
        player.sendMessage(ArcadiaMCLogin.colorize("&7  Tape &f/captcha " + code + " &7pour continuer"));
        player.sendMessage(ArcadiaMCLogin.colorize("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}
