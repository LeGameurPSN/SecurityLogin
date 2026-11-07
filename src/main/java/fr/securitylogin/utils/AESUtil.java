package fr.securitylogin.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilitaire de chiffrement / déchiffrement AES-128.
 * La clé doit faire exactement 16 caractères (128 bits).
 */
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * Chiffre un texte en clair avec AES-128.
     * @param plainText texte à chiffrer
     * @param secretKey clé de 16 caractères
     * @return texte chiffré encodé en Base64
     */
    public static String encrypt(String plainText, String secretKey) throws Exception {
        SecretKeySpec keySpec = buildKey(secretKey);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Déchiffre un texte chiffré en Base64 avec AES-128.
     * @param encryptedText texte chiffré en Base64
     * @param secretKey clé de 16 caractères
     * @return texte déchiffré
     */
    public static String decrypt(String encryptedText, String secretKey) throws Exception {
        SecretKeySpec keySpec = buildKey(secretKey);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    }

    /**
     * Vérifie si un mot de passe en clair correspond au mot de passe chiffré stocké.
     */
    public static boolean verify(String plainPassword, String encryptedPassword, String secretKey) {
        try {
            String decrypted = decrypt(encryptedPassword, secretKey);
            return decrypted.equals(plainPassword);
        } catch (Exception e) {
            return false;
        }
    }

    private static SecretKeySpec buildKey(String secretKey) {
        byte[] keyBytes = new byte[16];
        byte[] provided = secretKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(provided, 0, keyBytes, 0, Math.min(provided.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
