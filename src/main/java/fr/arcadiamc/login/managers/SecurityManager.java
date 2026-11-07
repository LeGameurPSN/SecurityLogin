package fr.arcadiamc.login.managers;

import fr.arcadiamc.login.ArcadiaMCLogin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Gère :
 *  1. Limite de tentatives de login (kick après N échecs)
 *  2. Cooldown anti-brute force (kick si trop rapide)
 *  4. Validation de mot de passe (longueur min + mots de passe bannis)
 */
public class SecurityManager {

    private final ArcadiaMCLogin plugin;

    // ── Tentatives échouées ──────────────────────
    /** Nombre d'échecs de /login par UUID */
    private final Map<UUID, Integer> failedAttempts = new HashMap<>();

    /** Joueurs temporairement bannis (login) */
    private final Set<UUID> loginBanned = new HashSet<>();

    // ── Anti-brute force ────────────────────────
    /** Timestamps des derniers /login en ms */
    private final Map<UUID, List<Long>> loginTimestamps = new HashMap<>();

    // ── Mots de passe bannis ────────────────────
    private static final Set<String> BANNED_PASSWORDS = new HashSet<>(Arrays.asList(
        "123456", "password", "123456789", "12345678", "12345", "1234567",
        "qwerty", "abc123", "111111", "000000", "azerty", "motdepasse",
        "minecraft", "password1", "admin", "iloveyou", "sunshine", "princess"
    ));

    public SecurityManager(ArcadiaMCLogin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    //  1. Tentatives de login
    // ─────────────────────────────────────────────

    /**
     * Enregistre un échec de login.
     * @return true si le joueur doit être kické (limite atteinte)
     */
    public boolean recordFailedLogin(Player player) {
        UUID uuid = player.getUniqueId();
        int attempts = failedAttempts.getOrDefault(uuid, 0) + 1;
        failedAttempts.put(uuid, attempts);

        int maxAttempts = plugin.getConfig().getInt("security.max-login-attempts", 5);
        if (attempts >= maxAttempts) {
            loginBanned.add(uuid);
            failedAttempts.remove(uuid);
            return true; // doit être kické
        }
        return false;
    }

    /**
     * Retourne le nombre de tentatives restantes avant kick.
     */
    public int getRemainingAttempts(UUID uuid) {
        int maxAttempts = plugin.getConfig().getInt("security.max-login-attempts", 5);
        int failed = failedAttempts.getOrDefault(uuid, 0);
        return maxAttempts - failed;
    }

    public int getFailedAttempts(UUID uuid) {
        return failedAttempts.getOrDefault(uuid, 0);
    }

    public boolean isLoginBanned(UUID uuid) {
        return loginBanned.contains(uuid);
    }

    /** Débloquer un joueur banni (commande staff) */
    public void unbanLogin(UUID uuid) {
        loginBanned.remove(uuid);
        failedAttempts.remove(uuid);
        loginTimestamps.remove(uuid);
    }

    /** Réinitialise les tentatives au succès ou déco */
    public void clearAttempts(UUID uuid) {
        failedAttempts.remove(uuid);
        loginTimestamps.remove(uuid);
    }

    // ─────────────────────────────────────────────
    //  2. Anti-brute force (logins trop rapides)
    // ─────────────────────────────────────────────

    /**
     * Vérifie si le joueur envoie trop de /login trop vite.
     * Config : security.brute-force-attempts (défaut 3) sur security.brute-force-window-seconds (défaut 2)
     * @return true si brute force détecté → doit être kické
     */
    public boolean checkBruteForce(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        List<Long> timestamps = loginTimestamps.computeIfAbsent(uuid, k -> new ArrayList<>());

        int windowMs = plugin.getConfig().getInt("security.brute-force-window-seconds", 2) * 1000;
        int maxInWindow = plugin.getConfig().getInt("security.brute-force-attempts", 3);

        // Nettoyer les vieux timestamps hors fenêtre
        timestamps.removeIf(t -> now - t > windowMs);
        timestamps.add(now);

        return timestamps.size() >= maxInWindow;
    }

    // ─────────────────────────────────────────────
    //  4. Validation de mot de passe
    // ─────────────────────────────────────────────

    /**
     * Résultat de la validation du mot de passe.
     */
    public enum PasswordResult {
        OK,
        TOO_SHORT,
        BANNED
    }

    /**
     * Valide un mot de passe selon les règles de sécurité.
     */
    public PasswordResult validatePassword(String password) {
        int minLength = plugin.getConfig().getInt("security.min-password-length", 6);
        if (password.length() < minLength) return PasswordResult.TOO_SHORT;
        if (BANNED_PASSWORDS.contains(password.toLowerCase())) return PasswordResult.BANNED;
        return PasswordResult.OK;
    }

    public int getMinPasswordLength() {
        return plugin.getConfig().getInt("security.min-password-length", 6);
    }
}
