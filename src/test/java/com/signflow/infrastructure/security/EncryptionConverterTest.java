package com.signflow.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncryptionConverterTest {

    private EncryptionConverter converter;
    private final String encryptionKey = "12345678901234567890123456789012"; // 32 bytes

    @BeforeEach
    void setUp() {
        converter = new EncryptionConverter();
        converter.setKey(encryptionKey);
    }

    @Test
    void shouldEncryptAndDecryptSuccessfully() {
        String originalData = "SensivelData123";
        String encrypted = converter.convertToDatabaseColumn(originalData);
        
        assertNotNull(encrypted);
        assertNotEquals(originalData, encrypted);
        
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(originalData, decrypted);
    }

    @Test
    void shouldReturnNullWhenDataIsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void shouldReturnOriginalDataWhenDecryptionFails() {
        String invalidData = "not-encrypted-data";
        String result = converter.convertToEntityAttribute(invalidData);
        assertEquals(invalidData, result);
    }
    
    @Test
    void shouldGenerateDifferentCiphertextForSameInput() {
        String data = "SameData";
        String encrypted1 = converter.convertToDatabaseColumn(data);
        String encrypted2 = converter.convertToDatabaseColumn(data);
        
        assertNotEquals(encrypted1, encrypted2, "AES-GCM should be non-deterministic (different IVs)");
    }
}
