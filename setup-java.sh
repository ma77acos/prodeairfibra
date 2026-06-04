#!/bin/bash

echo "🔍 Verificando instalación de Java..."

# Verificar si Java 17 está instalado
if java -version 2>&1 | grep -q "version \"17"; then
    echo "✅ Java 17 ya está instalado"
else
    echo "❌ Java 17 no encontrado. Instalando..."

    # Instalar Java 17 con Homebrew
    if command -v brew &> /dev/null; then
        echo "📦 Instalando OpenJDK 17 con Homebrew..."
        brew install openjdk@17

        # Crear enlace simbólico
        sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

        # Configurar JAVA_HOME
        export JAVA_HOME=/opt/homebrew/opt/openjdk@17
        export PATH="$JAVA_HOME/bin:$PATH"

        # Agregar a .zshrc si no existe
        if ! grep -q "JAVA_HOME=/opt/homebrew/opt/openjdk@17" ~/.zshrc; then
            echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17' >> ~/.zshrc
            echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
        fi

        echo "✅ Java 17 instalado correctamente"
    else
        echo "❌ Homebrew no está instalado. Por favor instálalo primero:"
        echo "/bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
        exit 1
    fi
fi

# Verificar versión final
echo ""
echo "📊 Versión de Java instalada:"
java -version

echo ""
echo "🎯 JAVA_HOME configurado en:"
echo $JAVA_HOME

echo ""
echo "✅ Setup completado. Ahora puedes ejecutar:"
echo "   ./gradlew clean build"