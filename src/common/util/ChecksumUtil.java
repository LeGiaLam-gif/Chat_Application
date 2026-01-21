package common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ChecksumUtil - Utility class để tính checksum cho file integrity verification
 * Sử dụng MD5 hash algorithm
 * 
 * Network Concept: Data Integrity Check
 * Use Case: Verify file không bị corrupt trong quá trình transfer
 */
public class ChecksumUtil {
    
    /**
     * Calculate MD5 checksum của file
     * 
     * @param file File cần tính checksum
     * @return MD5 hash string (hex format)
     * @throws IOException if file read error
     */
    public static String calculateMD5(File file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            
            // Convert byte array to hex string
            byte[] digest = md.digest();
            return bytesToHex(digest);
            
        } catch (NoSuchAlgorithmException e) {
            // MD5 should always be available
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * Calculate MD5 checksum của byte array
     * Sử dụng cho individual chunks
     * 
     * @param data Byte array
     * @return MD5 hash string
     */
    public static String calculateMD5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * Verify checksum matches expected value
     * 
     * @param file File to verify
     * @param expectedChecksum Expected checksum
     * @return true if checksums match
     */
    public static boolean verifyChecksum(File file, String expectedChecksum) {
        try {
            String actualChecksum = calculateMD5(file);
            return actualChecksum.equalsIgnoreCase(expectedChecksum);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Verify byte array checksum
     */
    public static boolean verifyChecksum(byte[] data, String expectedChecksum) {
        String actualChecksum = calculateMD5(data);
        return actualChecksum.equalsIgnoreCase(expectedChecksum);
    }
    
    /**
     * Convert byte array to hexadecimal string
     * 
     * @param bytes Byte array
     * @return Hex string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Format file size to human-readable string
     * Utility method cho UI display
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * Private constructor - utility class
     */
    private ChecksumUtil() {
        throw new AssertionError("Cannot instantiate ChecksumUtil");
    }
}
