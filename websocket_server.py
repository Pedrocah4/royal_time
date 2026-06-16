import asyncio
import websockets

# Este 'set' irá armazenar a conexão com o seu aplicativo Pygame.
# Usamos um set para que, no futuro, você possa ter múltiplos clientes (ex: um para cada jogador).
PYGAME_CLIENTS = set()

async def handler(websocket):
    """
    Gerencia as conexões. Ele diferencia se quem está conectando é o navegador
    com a voz ou a sua aplicação Pygame.
    """
    global PYGAME_CLIENTS

    path = websocket.request.path

    # O cliente de voz (navegador) se conectará em 'ws://localhost:8765/'
    # O cliente Pygame se conectará em 'ws://localhost:8765/pygame'
    if path == "/pygame":
        print("HUD Tático conectado ao servidor.")
        PYGAME_CLIENTS.add(websocket)
        try:
            await websocket.wait_closed()  # Mantém a conexão aberta.
        finally:
            PYGAME_CLIENTS.remove(websocket)
            print("HUD Tático desconectado.")
    else:
        print("Cliente de voz (navegador) conectado.")
        try:
            # Para cada mensagem recebida do navegador...
            async for message in websocket:
                print(f"Comando de voz recebido: '{message}'")
                # ...envie essa mensagem para todos os clientes Pygame conectados.
                websockets.broadcast(PYGAME_CLIENTS, message)
        finally:
            print("Cliente de voz (navegador) desconectado.")

async def main():
    print("Servidor WebSocket iniciado em ws://localhost:8765")
    async with websockets.serve(handler, "localhost", 8765):
        await asyncio.Future()  # Roda o servidor para sempre.

if __name__ == "__main__":
    asyncio.run(main())