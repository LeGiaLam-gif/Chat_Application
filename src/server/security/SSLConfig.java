package server.security;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLConfig {
    
    public static SSLServerSocketFactory createServerSocketFactory(
            String keystorePath, String password) throws Exception {
        
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, password.toCharArray());
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        
        return sslContext.getServerSocketFactory();
    }
    
    public static SSLSocketFactory createClientSocketFactory() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, getTrustAllManagers(), null);
        return sslContext.getSocketFactory();
    }
    
    private static TrustManager[] getTrustAllManagers() {
        return new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
    }
}
