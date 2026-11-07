package fr.arcadiamc.login;

import fr.arcadiamc.login.commands.*;
import fr.arcadiamc.login.listeners.PlayerListener;
import fr.arcadiamc.login.managers.AuthManager;
import fr.arcadiamc.login.managers.CaptchaManager;
import fr.arcadiamc.login.managers.SecurityManager;
import fr.arcadiamc.login.managers.SessionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ArcadiaMCLogin extends JavaPlugin {

    private static ArcadiaMCLogin instance;
    private AuthManager authManager;
    private SessionManager sessionManager;
    private CaptchaManager captchaManager;
    private SecurityManager securityManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Managers
        this.authManager = new AuthManager(this);
        this.sessionManager = new SessionManager(this);
        this.captchaManager = new CaptchaManager(this);
        this.securityManager = new SecurityManager(this);

        // Commandes joueur
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("change-pass").setExecutor(new ChangePassCommand(this));
        getCommand("captcha").setExecutor(new CaptchaCommand(this));

        // Commandes staff / admin
        getCommand("reset-pass").setExecutor(new ResetPassCommand(this));
        getCommand("timelogin").setExecutor(new TimeLoginCommand(this));
        getCommand("timeregister").setExecutor(new TimeRegisterCommand(this));
        getCommand("login-attempts").setExecutor(new LoginAttemptsCommand(this));
        getCommand("unban-login").setExecutor(new UnbanLoginCommand(this));

        // Listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("╔══════════════════════════════════════════╗");
        getLogger().info("║        ArcadiaMC_Login  v2.0.0           ║");
        getLogger().info("║    Créateur : LeGameurPSN_YT             ║");
        getLogger().info("║  ✔ Captcha  ✔ Brute-force  ✔ AES-128   ║");
        getLogger().info("╚══════════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (sessionManager != null) sessionManager.cancelAllTasks();
        if (captchaManager != null) captchaManager.cancelAll();
        getLogger().info("ArcadiaMC_Login désactivé.");
    }

    public static ArcadiaMCLogin getInstance() { return instance; }
    public AuthManager getAuthManager() { return authManager; }
    public SessionManager getSessionManager() { return sessionManager; }
    public CaptchaManager getCaptchaManager() { return captchaManager; }
    public SecurityManager getSecurityManager() { return securityManager; }

    public String getMessage(String key) {
        String prefix = getConfig().getString("messages.prefix", "&8[&bArcadiaMC&8] &r");
        String msg = getConfig().getString("messages." + key, "&cMessage introuvable : " + key);
        return colorize(prefix + msg);
    }

    public String getMessage(String key, String placeholder, String value) {
        return getMessage(key).replace("%" + placeholder + "%", value);
    }

    public static String colorize(String text) {
        return text.replace("&", "§");
    }
}
