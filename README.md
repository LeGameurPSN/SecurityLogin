# 🔐 SecurityLogin — v2.3.9

**Plugin de login sécurisé pour serveurs Minecraft (Spigot / Paper 1.20+)**
Créateur : **LeGameurPSN_YT**

---

## 📦 Installation

1. Dépose `SecurityLogin-2.3.9.jar` dans ton dossier `plugins/`
2. Redémarre le serveur
3. Configure `plugins/SecurityLogin/config.yml`
4. Redémarre une seconde fois pour appliquer ta config

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Délai avant kick si non connecté (secondes)
time-login: 120

# Délai avant kick si non inscrit (secondes)
time-register: 300

# Durée de validité du captcha (secondes)
captcha-expire-seconds: 300

security:
  max-login-attempts: 5       # Tentatives max avant kick
  brute-force-attempts: 3     # Tentatives rapides avant kick
  brute-force-window-seconds: 2
  min-password-length: 6

# ⚠️ CHANGE CETTE CLÉ — 16 caractères exactement — NE JAMAIS PARTAGER
aes-secret-key: "SecLogin_Key_2024"
```

> ⚠️ **Important** : modifie `aes-secret-key` avant le premier lancement. Les mots de passe sont chiffrés avec cette clé — si tu la changes après inscription, tous les comptes existants seront invalides.

---

## 🎮 Commandes joueurs

| Commande | Description |
|---|---|
| `/login <password>` | Se connecter |
| `/register <password> <confirm>` | Créer son compte |
| `/captcha <code>` | Valider le captcha avant l'inscription |
| `/change-pass <ancien> <nouveau> <confirm>` | Changer son mot de passe |

---

## 🛡️ Commandes staff — permission `securitylogin.staff`

| Commande | Description |
|---|---|
| `/reset-pass <pseudo>` | Réinitialiser le mot de passe d'un joueur |
| `/login-attempts <pseudo>` | Voir les tentatives échouées |
| `/unban-login <pseudo>` | Débloquer un joueur banni par brute-force |
| `/captcha reload <pseudo>` | Générer un nouveau captcha pour un joueur |
| `/captcha bypass <pseudo>` | Ignorer le captcha pour un joueur |

---

## ⚡ Commandes admin — permission `securitylogin.admin`

| Commande | Description |
|---|---|
| `/timelogin <secondes>` | Modifier le délai avant kick (connexion) |
| `/timeregister <secondes>` | Modifier le délai avant kick (inscription) |

---

## 🔑 Permissions LuckPerms

```
securitylogin.staff   — reset-pass, login-attempts, unban-login, captcha reload/bypass
securitylogin.admin   — timelogin, timeregister (inclut les droits staff)
```

Exemple LuckPerms :
```
/lp group staff permission set securitylogin.staff true
/lp group admin permission set securitylogin.admin true
```

---

## 🔒 Fonctionnalités de sécurité

- **Chiffrement AES-128** — les mots de passe ne sont jamais stockés en clair
- **Captcha** — obligatoire à l'inscription pour bloquer les bots
- **Anti brute-force** — kick automatique si trop de tentatives rapides
- **Ban login** — bannissement temporaire après trop d'échecs (déblocable par staff)
- **Kick inactivité** — expulsion si le joueur ne se connecte/inscrit pas dans le délai configuré
- **Longueur minimale** — mot de passe refusé si trop court ou trop simple

---

## 📁 Structure des fichiers

```
plugins/
└── SecurityLogin/
    └── config.yml     ← configuration principale
```

Les données joueurs sont stockées en mémoire pendant la session (pas de base de données externe requise).

---

## 🧩 Compatibilité

- **Serveur** : Spigot / Paper 1.20+
- **Java** : 17+
- **Folia** : non supporté

---

*SecurityLogin v2.3.9 — Créateur : LeGameurPSN_YT*
