#!/bin/bash
set -e

# Если переменная DISPLAY не передана с хоста, запускаем встроенный виртуальный экран
if [ -z "$DISPLAY" ] || [ "$DISPLAY" = ":99" ]; then
    echo "=========================================================="
    echo "Запуск виртуального X11 экрана (Xvfb) на :99..."
    Xvfb :99 -screen 0 1280x800x24 &
    export DISPLAY=:99
    sleep 1

    echo "Запуск легковесного оконного менеджера Fluxbox..."
    fluxbox &
    sleep 1

    echo "Запуск x11vnc сервера..."
    x11vnc -display :99 -forever -nopw -shared -rfbport 5900 -quiet &
    sleep 1

    echo "Запуск noVNC web-сервера на порту 8080..."
    websockify --web=/usr/share/novnc 8080 localhost:5900 &
    sleep 1
    echo "=========================================================="
    echo "Графический интерфейс доступен в браузере: http://localhost:8080/vnc.html"
    echo "=========================================================="
fi

echo "Компиляция и запуск JavaFX приложения..."
exec mvn clean javafx:run
