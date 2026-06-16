@echo off
echo Iniciando o Servidor WebSocket...
start "Servidor Royal Time" cmd /k "python websocket_server.py"

echo Iniciando Servidor Web Local (Para salvar permissao de voz)...
start "Servidor Web" cmd /k "python -m http.server 8000"

echo Aguardando o servidor ligar...
timeout /t 2 /nobreak >nul

echo Iniciando o HUD Tatico (Pygame)...
start "HUD Royal Time" cmd /k "python main.py"

echo Abrindo o cliente de voz no Microsoft Edge...
start msedge "http://localhost:8000/voice_client.html"

echo Tudo aberto!
exit