package com.signflow.infrastructure.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
@Component
public class EncryptionConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static byte[] KEY;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${signflow.encryption.key}")
    public void setKey(String key) {
        if (key == null || key.getBytes().length < 32) {
            throw new IllegalArgumentException("A chave de criptografia (SIGNFLOW_ENCRYPTION_KEY) deve ter pelo menos 32 bytes.");
        }
        KEY = key.getBytes();
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), parameterSpec);

            byte[] cipherText = cipher.doFinal(attribute.getBytes());
            
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            String cipherTextBase64 = Base64.getEncoder().encodeToString(cipherText);

            return ivBase64 + ":" + cipherTextBase64;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar dado", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            if (!dbData.contains(":")) {
                return dbData; // Pode ser dado legado ou não criptografado
            }

            String[] parts = dbData.split(":");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), parameterSpec);

            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) {
            // Em caso de falha na descriptografia, retorna o dado original
            return dbData;
        }
    }
}
