import pygame
import sys
import keyboard
import ctypes
import threading

pygame.init()

# ==========================================
# DADOS EXPLÍCITOS (Constantes e Variáveis)
# ==========================================
LARGURA_TELA = 800
ALTURA_TELA = 250
PRETO = (0, 0, 0)
BRANCO = (255, 255, 255) 

# O Sistema de 3 Cores
CINZA_FUNDO = (70, 70, 70)         # Cor 1: Vazio
ROXO_FANTASMA = (150, 100, 180)    # Cor 2: Carregando (Opaco/Acinzentado)
ROXO_ELIXIR = (218, 112, 214)      # Cor 3: Pronto/Sólido (Vibrante)

fonte_status = pygame.font.SysFont("arial", 20)
fonte_elixir_numero = pygame.font.SysFont("arial", 48, bold=True)
fonte_max = pygame.font.SysFont("arial", 16) 
fonte_max = pygame.font.SysFont("arial", 16)

ELIXIR_MAXIMO = 10
elixir_atual = 5

LARGURA_CAIXA = 60
ALTURA_CAIXA = 50
ESPACAMENTO_CAIXA = 5
RAIO_BORDA = 8
POSICAO_X_INICIAL = 80 
POSICAO_X_INICIAL = 80
POSICAO_Y_BARRA = 110 

custo_cartas = {
    '1': 1, '2': 2, '3': 3, '4': 4, '5': 5,
    '6': 6, '7': 7, '8': 8, '9': 9, '0': 10
}

import queue
import asyncio
import websockets
import re


# Dicionário para converter voz em custo de elixir
mapa_voz_elixir = {
    'um': 1, '1': 1, 
    'dois': 2, '2': 2,
    'três': 3, 'tres': 3, '3': 3,
    'quatro': 4, '4': 4,
    'cinco': 5, '5': 5,
    'seis': 6, '6': 6,
    'sete': 7, '7': 7,
    'oito': 8, '8': 8,
    'nove': 9, '9': 9,
    'dez': 10, '10': 10
}

# Conecta o dicionário de teclas (custo_cartas) ao de voz,
# garantindo que todas as entradas numéricas também funcionem via voz.
for tecla, custo in custo_cartas.items():
    if tecla not in mapa_voz_elixir:
        mapa_voz_elixir[tecla] = custo

# =======================================================
# INFRAESTRUTURA DE CLIENTE WEBSOCKET / VOZ
# =======================================================
fila_comandos = queue.Queue()

async def conectar_ao_servidor():
    """Conecta-se ao servidor WebSocket e escuta por comandos de voz."""
    # A URI agora aponta para o caminho específico do Pygame
    uri = "ws://localhost:8765/pygame"
    # Loop infinito para garantir a reconexão automática
    while True:
        try:
            async with websockets.connect(uri) as websocket:
                print("Cliente Pygame conectado ao servidor WebSocket.")
                print("🎧 Escutando comandos de voz...")
                # Loop para receber mensagens do servidor
                async for message in websocket:
                    fila_comandos.put(message.lower())
        except (websockets.exceptions.ConnectionClosed, ConnectionRefusedError):
            print("Conexão com o servidor perdida. Tentando reconectar em 5 segundos...")
            await asyncio.sleep(5)

def iniciar_cliente_ws():
    """Inicia o loop de eventos asyncio para o cliente WebSocket."""
    asyncio.run(conectar_ao_servidor())

threading.Thread(target=iniciar_cliente_ws, daemon=True).start()

tela = pygame.display.set_mode((LARGURA_TELA, ALTURA_TELA))
pygame.display.set_caption("Barra de elixir - HUD Tático")

# ==========================================
# LÓGICA DE 'SEMPRE NO TOPO' (Windows)
# ==========================================
hwnd = pygame.display.get_wm_info()["window"]

# Informamos os tipos de argumentos corretos (argtypes) para evitar que o Python corte
# o ID da janela (hwnd) pela metade em sistemas 64-bits, o que fazia a função falhar silenciosamente.
ctypes.windll.user32.SetWindowPos.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_int, ctypes.c_int, ctypes.c_int, ctypes.c_int, ctypes.c_uint]
ctypes.windll.user32.SetWindowPos(hwnd, -1, 0, 0, 0, 0, 3)

relogio = pygame.time.Clock()

partida_iniciada = False
tempo_ultimo_clique = 0
COOLDOWN_CLIQUE = 300 
COOLDOWN_CLIQUE = 300

TAXA_GERACAO_NORMAL = 2800
taxa_atual = TAXA_GERACAO_NORMAL
tempo_ultimo_elixir = 0
tempo_ultimo_topmost = 0
multiplicador_texto = "1x" 

rodando = True

while rodando:
    tempo_atual = pygame.time.get_ticks()

    for evento in pygame.event.get():
        if evento.type == pygame.QUIT:
            rodando = False

    # ==========================================
    # LÓGICA DE ESCUTA POR COMANDO DE VOZ
    # ==========================================
    while not fila_comandos.empty():
        comando = fila_comandos.get()
        print(f"\n🎙️ Voz transcrita: '{comando}'")
        print("⚙️ Processando...")
        comando_reconhecido = False

        # Remove pontuações (.,!?) usando Expressão Regular para deixar apenas letras e números
        comando_limpo = re.sub(r'[^\w\s]', '', comando)
        palavras_faladas = comando_limpo.split()

        if "iniciar" in comando_limpo and not partida_iniciada:
            partida_iniciada = True
            tempo_ultimo_elixir = tempo_atual
            print("✅ Resultado: Partida Iniciada via voz!")
            comando_reconhecido = True
            
        elif partida_iniciada:
            if "vezes dois" in comando_limpo or "dobro" in comando_limpo:
                taxa_atual = 1400
                multiplicador_texto = "2x"
                print("✅ Resultado: Multiplicador alterado para 2x")
                comando_reconhecido = True
            elif "vezes três" in comando_limpo or "vezes tres" in comando_limpo:
                taxa_atual = 933
                multiplicador_texto = "3x"
                print("✅ Resultado: Multiplicador alterado para 3x")
                comando_reconhecido = True
            elif "vezes um" in comando_limpo or "normal" in comando_limpo:
                taxa_atual = TAXA_GERACAO_NORMAL
                multiplicador_texto = "1x"
                print("✅ Resultado: Multiplicador alterado para 1x")
                comando_reconhecido = True
            
            # Apenas processa o gasto de elixir se um comando de multiplicador não foi dito.
            # Isso evita que "vezes dois" também gaste 2 de elixir.
            if not comando_reconhecido:
                total_a_gastar = 0
                numeros_encontrados = []
                for palavra in palavras_faladas:
                    if palavra in mapa_voz_elixir:
                        total_a_gastar += mapa_voz_elixir[palavra]
                        numeros_encontrados.append(palavra)
                
                # Se algum número foi somado, executa o gasto
                if total_a_gastar > 0:
                    if elixir_atual >= total_a_gastar:
                        elixir_atual -= total_a_gastar
                        print(f"✅ Resultado: Gastou um total de {total_a_gastar} de elixir (de: {', '.join(numeros_encontrados)}). Restante: {elixir_atual}")
                    else:
                        print(f"⚠️ Resultado: Tentou gastar {total_a_gastar}, mas possui apenas {elixir_atual}.")
                    comando_reconhecido = True

        if not comando_reconhecido:
            print("❌ Resultado: Nenhum comando válido identificado na frase.")

    # Lógica de Início
    if not partida_iniciada:
        if keyboard.is_pressed('space'):
            partida_iniciada = True
            tempo_ultimo_elixir = tempo_atual
    else:
        # Multiplicadores
        if keyboard.is_pressed('z') and taxa_atual != TAXA_GERACAO_NORMAL:
            taxa_atual = TAXA_GERACAO_NORMAL
            multiplicador_texto = "1x"
        elif keyboard.is_pressed('x') and taxa_atual != 1400:
            taxa_atual = 1400
            multiplicador_texto = "2x"
        elif keyboard.is_pressed('c') and taxa_atual != 933:
            taxa_atual = 933
            multiplicador_texto = "3x"

        # Subtração
        if tempo_atual - tempo_ultimo_clique > COOLDOWN_CLIQUE:
            for tecla, custo in custo_cartas.items():
                if keyboard.is_pressed(tecla):
                    if elixir_atual >= custo:
                        elixir_atual -= custo
                    tempo_ultimo_clique = tempo_atual
                    break 

        # Geração Passiva
        if tempo_atual - tempo_ultimo_elixir >= taxa_atual:
            if elixir_atual < ELIXIR_MAXIMO:
                elixir_atual += 1
            tempo_ultimo_elixir = tempo_atual
            
    # ==========================================
    # REFORÇO DE 'SEMPRE NO TOPO' (A cada 2 seg)
    # ==========================================
    if tempo_atual - tempo_ultimo_topmost > 2000:
        ctypes.windll.user32.SetWindowPos(hwnd, -1, 0, 0, 0, 0, 3)
        tempo_ultimo_topmost = tempo_atual

    # ==========================================
    # CÁLCULO DA BARRA CONTÍNUA (FANTASMA)
    # ==========================================
    if partida_iniciada and elixir_atual < ELIXIR_MAXIMO:
        tempo_decorrido = tempo_atual - tempo_ultimo_elixir
        # Calcula a porcentagem do tempo já passado (de 0.0 a 1.0)
        progresso_atual = min(1.0, tempo_decorrido / taxa_atual)
    else:
        progresso_atual = 0.0

    # ==========================================
    # RENDERIZAÇÃO VISUAL 
    # ==========================================
    tela.fill(PRETO)
    
    if not partida_iniciada:
        status_string = "Aguardando... Pressione ESPAÇO para iniciar"
    else:
        status_string = f"Partida em andamento | Multiplicador: {multiplicador_texto}"
    texto_status = fonte_status.render(status_string, True, BRANCO)
    tela.blit(texto_status, (LARGURA_TELA // 2 - texto_status.get_width() // 2, 20))

    # Desenha o Ícone
    pygame.draw.circle(tela, ROXO_ELIXIR, (POSICAO_X_INICIAL - 35, POSICAO_Y_BARRA + ALTURA_CAIXA // 2), 18)
    points_drop = [(POSICAO_X_INICIAL - 35, POSICAO_Y_BARRA + ALTURA_CAIXA // 2 - 18),
                  (POSICAO_X_INICIAL - 50, POSICAO_Y_BARRA + ALTURA_CAIXA // 2 - 28),
                  (POSICAO_X_INICIAL - 35, POSICAO_Y_BARRA + ALTURA_CAIXA // 2 - 42),
                  (POSICAO_X_INICIAL - 20, POSICAO_Y_BARRA + ALTURA_CAIXA // 2 - 28)]
    pygame.draw.polygon(tela, ROXO_ELIXIR, points_drop)

    # ==========================================
    # MOTOR DE DESENHO (O SISTEMA DE 3 CORES)
    # ==========================================
    for i in range(1, 11):
        x_seg = POSICAO_X_INICIAL + (i - 1) * (LARGURA_CAIXA + ESPACAMENTO_CAIXA)
        y_seg = POSICAO_Y_BARRA
        
        # 1. Cor 1: Fundo base sempre cinza para caixas vazias
        pygame.draw.rect(tela, CINZA_FUNDO, (x_seg, y_seg, LARGURA_CAIXA, ALTURA_CAIXA), border_radius=RAIO_BORDA)
        
        if i <= elixir_atual:
            # 2. Cor 3: Elixir pronto para uso (Sólido)
            pygame.draw.rect(tela, ROXO_ELIXIR, (x_seg, y_seg, LARGURA_CAIXA, ALTURA_CAIXA), border_radius=RAIO_BORDA)
            
        elif i == elixir_atual + 1 and partida_iniciada:
            # 3. Cor 2: O Fantasma crescendo
            largura_carregando = int(LARGURA_CAIXA * progresso_atual)
            if largura_carregando > 0:
                rect_fantasma = pygame.Rect(x_seg, y_seg, largura_carregando, ALTURA_CAIXA)
                # Arredonda apenas os cantos esquerdos para o carregamento parecer fluido
                pygame.draw.rect(tela, ROXO_FANTASMA, rect_fantasma, border_top_left_radius=RAIO_BORDA, border_bottom_left_radius=RAIO_BORDA)

        # Desenha a borda Branca finalizando o quadrado
        pygame.draw.rect(tela, BRANCO, (x_seg, y_seg, LARGURA_CAIXA, ALTURA_CAIXA), width=2, border_radius=RAIO_BORDA)

    # Textos Numéricos
    texto_num = fonte_elixir_numero.render(str(elixir_atual), True, BRANCO)
    x_num = POSICAO_X_INICIAL + (5 * (LARGURA_CAIXA + ESPACAMENTO_CAIXA) // 2) - (texto_num.get_width() // 2)
    y_num = POSICAO_Y_BARRA - texto_num.get_height() - 5
    tela.blit(texto_num, (x_num, y_num))

    texto_max = fonte_max.render("Max: 10", True, BRANCO)
    x_max = POSICAO_X_INICIAL + (5 * (LARGURA_CAIXA + ESPACAMENTO_CAIXA) // 2) - (texto_max.get_width() // 2)
    y_max = POSICAO_Y_BARRA + ALTURA_CAIXA + 5
    tela.blit(texto_max, (x_max, y_max))

    pygame.display.flip()
    relogio.tick(60)

pygame.quit()
sys.exit()