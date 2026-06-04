#!/bin/bash

echo "🔍 Diagnóstico completo de classpath..."
echo "========================================"

cd backend

# 1. Estructura del proyecto
echo ""
echo "1️⃣ Estructura del proyecto:"
find src/main -type f | head -20

# 2. Archivos de configuración
echo ""
echo "2️⃣ Archivos de configuración:"
find src/main/resources -type f 2>/dev/null || echo "❌ No se encontró src/main/resources"

# 3. Contenido de application.*
echo ""
echo "3️⃣ Contenido de application.properties:"
if [ -f "src/main/resources/application.properties" ]; then
    cat src/main/resources/application.properties | head -20
else
    echo "❌ No existe application.properties"
fi

echo ""
echo "3️⃣ Contenido de application.yml:"
if [ -f "src/main/resources/application.yml" ]; then
    cat src/main/resources/application.yml | head -20
else
    echo "❌ No existe application.yml"
fi

# 4. Verificar build.gradle
echo ""
echo "4️⃣ SourceSets en build.gradle:"
grep -A 10 "sourceSets" build.gradle || echo "⚠️  No se encontró configuración de sourceSets"

# 5. Limpiar y compilar
echo ""
echo "5️⃣ Limpiando proyecto..."
./gradlew clean

echo ""
echo "6️⃣ Compilando (sin tests)..."
./gradlew build -x test

# 6. Verificar el JAR generado
echo ""
echo "7️⃣ Contenido del JAR generado:"
JAR_FILE=$(find build/libs -name "*.jar" -type f | head -1)
if [ -f "$JAR_FILE" ]; then
    echo "JAR encontrado: $JAR_FILE"
    echo ""
    echo "Archivos de configuración en el JAR:"
    unzip -l "$JAR_FILE" | grep -E "application\.(yml|properties)" || echo "❌ NO se encontraron archivos de configuración"
    echo ""
    echo "Estructura BOOT-INF/classes:"
    unzip -l "$JAR_FILE" | grep "BOOT-INF/classes" | head -20
else
    echo "❌ No se generó el JAR"
fi

# 7. Verificar directorio build
echo ""
echo "8️⃣ Recursos en build/resources/main:"
if [ -d "build/resources/main" ]; then
    ls -la build/resources/main/
else
    echo "❌ No existe build/resources/main"
fi

echo ""
echo "========================================"
echo "✅ Diagnóstico completado"