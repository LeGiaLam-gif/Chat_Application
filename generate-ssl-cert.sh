#!/bin/bash
# Generate self-signed SSL certificate for testing

KEYSTORE="server.jks"
ALIAS="chatserver"
PASSWORD="password123"
VALIDITY=365

echo "Generating SSL Certificate..."
echo "=============================="
echo "Keystore: $KEYSTORE"
echo "Alias: $ALIAS"
echo "Validity: $VALIDITY days"
echo ""

keytool -genkeypair \
    -alias $ALIAS \
    -keyalg RSA \
    -keysize 2048 \
    -validity $VALIDITY \
    -keystore $KEYSTORE \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -dname "CN=localhost, OU=ChatApp, O=Dev, L=HCM, ST=HCM, C=VN"

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Certificate generated successfully!"
    echo "✓ File: $KEYSTORE"
    echo ""
    echo "To enable SSL, edit server.properties:"
    echo "  ssl.enabled=true"
else
    echo "✗ Certificate generation failed!"
    exit 1
fi
