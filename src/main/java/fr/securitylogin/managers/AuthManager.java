package fr.securitylogin.managers;

import fr.securitylogin.SecurityLogin;
import fr.securitylogin.utils.AESUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Gère la persistance des comptes joueurs.
 * Chaque joueur a son propre fichier : plugins/SecurityLogin/players/<UUID>.yml
 * Le mot de passe est stocké chiffré en AES-128.
 */
public class AuthManager {

    private final SecurityLogin plugin;
    private final File playersFolder;

    public AuthManager(SecurityLogin plugin) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), "players");
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    // ─────────────────────────────────────────────
    //  Vérifications d'existence
    // ─────────────────────────────────────────────

    public boolean hasAccount(String uuid) {
        File f = getPlayerFile(uuid);
        if (!f.exists()) return false;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        return cfg.contains("password");
    }

    // ─────────────────────────────────────────────
    //  Inscription
    // ─────────────────────────────────────────────

    /**
     * Crée un compte pour un joueur.
     * @return true si succès, false si déjà inscrit
     */
    public boolean register(String uuid, String playerName, String password) {
        if (hasAccount(uuid)) return false;
        try {
            String encrypted = AESUtil.encrypt(password, getKey());
            FileConfiguration cfg = getOrCreateConfig(uuid);
            cfg.set("uuid", uuid);
            cfg.set("name", playerName);
            cfg.set("password", encrypted);
            save(uuid, cfg);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'inscription de " + playerName, e);
            return false;
        }
    }

    // ─────────────────────────────────────────────
    //  Connexion
    // ─────────────────────────────────────────────

    /**
     * Vérifie le mot de passe d'un joueur.
     * @return true si correct
     */
    public boolean checkPassword(String uuid, String password) {
        if (!hasAccount(uuid)) return false;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(getPlayerFile(uuid));
        String stored = cfg.getString("password", "");
        return AESUtil.verify(password, stored, getKey());
    }

    // ─────────────────────────────────────────────
    //  Changement de mot de passe
    // ─────────────────────────────────────────────

    /**
     * Change le mot de passe si l'ancien est correct.
     * @return true si succès
     */
    public boolean changePassword(String uuid, String oldPassword, String newPassword) {
        if (!checkPassword(uuid, oldPassword)) return false;
        try {
            String encrypted = AESUtil.encrypt(newPassword, getKey());
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(getPlayerFile(uuid));
            cfg.set("password", encrypted);
            save(uuid, cfg);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du changement de mdp pour " + uuid, e);
            return false;
        }
    }

    // ─────────────────────────────────────────────
    //  Réinitialisation (staff)
    // ─────────────────────────────────────────────

    /**
     * Supprime le mot de passe d'un joueur (par pseudo, insensible à la casse).
     * @param playerName pseudo du joueur
     * @return true si un fichier a été trouvé et supprimé
     */
    public boolean resetPasswordByName(String playerName) {
        File f = findFileByName(playerName);
        if (f == null) return false;
        return f.delete();
    }

    /**
     * Cherche le fichier d'un joueur par son pseudo (parcours du dossier).
     */
    public File findFileByName(String playerName) {
        File[] files = playersFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return null;
        for (File f : files) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            String name = cfg.getString("name", "");
            if (name.equalsIgnoreCase(playerName)) return f;
        }
        return null;
    }

    public boolean accountExistsByName(String playerName) {
        return findFileByName(playerName) != null;
    }

    // ─────────────────────────────────────────────
    //  Helpers internes
    // ─────────────────────────────────────────────

    private File getPlayerFile(String uuid) {
        return new File(playersFolder, uuid + ".yml");
    }

    private FileConfiguration getOrCreateConfig(String uuid) {
        File f = getPlayerFile(uuid);
        return f.exists() ? YamlConfiguration.loadConfiguration(f) : new YamlConfiguration();
    }

    private void save(String uuid, FileConfiguration cfg) {
        try {
            cfg.save(getPlayerFile(uuid));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Impossible de sauvegarder le fichier joueur " + uuid, e);
        }
    }

    private String getKey() {
        return plugin.getConfig().getString("aes-secret-key", "SecLogin_Key_2024");
    }
}
