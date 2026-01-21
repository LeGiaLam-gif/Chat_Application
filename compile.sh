#!/bin/bash
# Compile script for ChatAppV2

echo "Compiling ChatAppV2..."
echo "====================="

# Create output directory
mkdir -p bin

# Compile all Java files
find src -name "*.java" -print0 | xargs -0 javac -d bin -sourcepath src

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo "✓ Class files in: bin/"
    echo ""
    echo "To run:"
    echo "  Server: java -cp bin server.core.ChatServer"
    echo "  Client: java -cp bin client.gui.ClientController"
else
    echo "✗ Compilation failed!"
    exit 1
fi
