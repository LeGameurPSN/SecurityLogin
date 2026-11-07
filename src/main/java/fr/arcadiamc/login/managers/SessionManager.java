package fr.arcadiamc.login.managers;

import fr.arcadiamc.login.ArcadiaMCLogin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private final ArcadiaMCLogin plugin;

    private final Map<UUID, Boolean>    loggedIn     = new HashMap<>();
    private final Map<UUID, BukkitTask> kickTasks    = new HashMap<>();
    private final Map<UUID, BukkitTask> reminderTasks = new HashMap<>();
    private final Map<UUID, String>     pendingResets = new HashMap<>();

    public SessionManager(ArcadiaMCLogin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    //  Sessions
    // ─────────────────────────────────────────────

    public boolean isLoggedIn(UUID uuid) {
        return loggedIn.getOrDefault(uuid, false);
    }

    public void setLoggedIn(UUID uuid) {
        loggedIn.put(uuid, true);
        cancelKickTask(uuid);
        cancelReminderTask(uuid);
    }

    public void removeSession(UUID uuid) {
        loggedIn.remove(uuid);
        cancelKickTask(uuid);
        cancelReminderTask(uuid);
        pendingResets.remove(uuid);
    }

    // ─────────────────────────────────────────────
    //  Timer de kick + rappel toutes les 5s
    // ─────────────────────────────────────────────

    /**
     * @param isRegistered true = joueur inscrit (doit /login), false = nouveau (doit /captcha + /register)
     */
    public void startKickTimer(Player player, boolean isRegistered) {
        cancelKickTask(player.getUniqueId());
        cancelReminderTask(player.getUniqueId());

        int seconds = isRegistered
                ? plugin.getConfig().getInt("time-login", 120)
                : plugin.getConfig().getInt("time-register", 300);

        String kickKey = isRegistered ? "kick-login-timeout" : "kick-register-timeout";

        // ── Rappel toutes les 5 secondes ──
        BukkitTask reminder = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player p = Bukkit.getPlayer(player.getUniqueId());
            if (p == null || !p.isOnline() || isLoggedIn(p.getUniqueId())) return;

            boolean hasCaptchaValidated = plugin.getCaptchaManager().hasValidated(p.getUniqueId());
            boolean hasAccount = plugin.getAuthManager().hasAccount(p.getUniqueId().toString());

            if (hasAccount) {
                sendLoginReminder(p);
            } else if (!hasCaptchaValidated) {
                sendCaptchaReminder(p);
            } else {
                sendRegisterReminder(p);
            }
        }, 100L, 100L); // 5s = 100 ticks, répète toutes les 5s

        reminderTasks.put(player.getUniqueId(), reminder);

        // ── Kick au bout du délai ──
        BukkitTask kick = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(player.getUniqueId());
            if (p != null && p.isOnline() && !isLoggedIn(p.getUniqueId())) {
                p.kickPlayer(plugin.getMessage(kickKey));
            }
            kickTasks.remove(player.getUniqueId());
        }, seconds * 20L);

        kickTasks.put(player.getUniqueId(), kick);
    }

    // ─────────────────────────────────────────────
    //  Affichages de rappel
    // ─────────────────────────────────────────────

    private void sendLoginReminder(Player p) {
        p.sendMessage(ArcadiaMCLogin.colorize(
            "&8[&bArcadiaMC&8] &cConnecte-toi : &f/login <motdepasse>"));
        p.sendTitle(
            ArcadiaMCLogin.colorize("&c&lCONNEXION REQUISE"),
            ArcadiaMCLogin.colorize("&f/login <motdepasse>"),
            5, 80, 15);
    }

    private void sendCaptchaReminder(Player p) {
        String code = plugin.getCaptchaManager().getCode(p.getUniqueId());
        String codeDisplay = code != null ? "&b&l" + code : "&7(en cours de génération...)";
        p.sendMessage(ArcadiaMCLogin.colorize(
            "&8[&bArcadiaMC&8] &eValide le captcha : &f/captcha &b" + (code != null ? code : "???")));
        p.sendTitle(
            ArcadiaMCLogin.colorize("&e&lCAPTCHA"),
            ArcadiaMCLogin.colorize(codeDisplay + " &7→ /captcha " + (code != null ? code : "???")),
            5, 80, 15);
    }

    private void sendRegisterReminder(Player p) {
        p.sendMessage(ArcadiaMCLogin.colorize(
            "&8[&bArcadiaMC&8] &aInscris-toi : &f/register <motdepasse> <confirmer>"));
        p.sendTitle(
            ArcadiaMCLogin.colorize("&a&lINSCRIPTION"),
            ArcadiaMCLogin.colorize("&f/register <motdepasse> <confirmer>"),
            5, 80, 15);
    }

    // ─────────────────────────────────────────────
    //  Nettoyage
    // ─────────────────────────────────────────────

    public void cancelKickTask(UUID uuid) {
        BukkitTask t = kickTasks.remove(uuid);
        if (t != null) t.cancel();
    }

    public void cancelReminderTask(UUID uuid) {
        BukkitTask t = reminderTasks.remove(uuid);
        if (t != null) t.cancel();
    }

    public void cancelAllTasks() {
        kickTasks.values().forEach(BukkitTask::cancel);
        kickTasks.clear();
        reminderTasks.values().forEach(BukkitTask::cancel);
        reminderTasks.clear();
    }

    // ─────────────────────────────────────────────
    //  Reset-pass
    // ─────────────────────────────────────────────

    public void setPendingReset(UUID staffUuid, String targetName) { pendingResets.put(staffUuid, targetName); }
    public String getPendingReset(UUID staffUuid)                   { return pendingResets.get(staffUuid); }
    public void clearPendingReset(UUID staffUuid)                   { pendingResets.remove(staffUuid); }
    public boolean hasPendingReset(UUID staffUuid)                  { return pendingResets.containsKey(staffUuid); }
}
