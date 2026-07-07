# Royal Time 💧

HUD Tático e Contador de Elixir do Adversário para **Clash Royale**, desenvolvido como um aplicativo nativo para **Android (Kotlin)**.

O aplicativo funciona através de um widget flutuante (overlay) em formato de **esfera de vidro 3D** com anel metálico e indicador de progresso de elixir fluido rodando a **60 FPS** (ciclo de ~16ms). O widget é extremamente compacto, medindo exatamente `75dp x 75dp` (equivalente ao tamanho de um ícone de aplicativo padrão no celular), e pode ser arrastado para qualquer lugar da tela sobre o jogo Clash Royale.

---

## 🚀 Funcionalidades

- **Serviço em Primeiro Plano (Foreground Service)**:
  - Roda como um serviço de primeiro plano associado a uma notificação persistente na barra de status.
  - Configurado com o tipo `microphone` e permissão `FOREGROUND_SERVICE_MICROPHONE` (compatível com as regras rígidas do Android 14+), impedindo que o sistema operacional encerre o app em segundo plano durante a partida.
  - Solicitação dinâmica da permissão de notificações (`POST_NOTIFICATIONS`) no Android 13+.
- **Design Circular 3D Premium & Animações**:
  - **Outer Glow (Brilho Externo)** roxo translúcido.
  - **Breathing Shimmer (Cintilação Senoidal)**: Quando o elixir atinge o limite máximo (10), o brilho roxo pulsa suavemente em efeito neon de "respiração", indicando visualmente que o elixir do oponente está cheio.
  - **Bounce/Pulse (Feedback Visual)**: O círculo do widget executa uma pulsação elástica instantânea no exato momento em que um comando de voz é detectado com sucesso.
  - **Bezel Metálico** duplo (moldura prateada externa e interna).
  - **Orbe de Vidro Interno** com efeito tridimensional de luz e reflexo reflexivo (gloss).
  - **Número de Elixir Central** estilizado em gradiente dourado vertical e sombra de profundidade (drop shadow).
- **Reconhecimento de Voz Avançado (Tempo Real & Offline)**:
  - **Preferência Offline**: Ativa a flag de reconhecimento local offline para reduzir a latência de rede a quase zero, permitindo reações em tempo real.
  - **Fallback Automático**: Se o dispositivo do usuário não possuir o pacote de voz offline em português (`pt-BR`), o app detecta a indisponibilidade (Erros 12 e 13) e alterna automaticamente para o modo de reconhecimento online (nuvem) sem interromper a partida.
  - **Resultados Parciais**: Processa as palavras instantaneamente à medida que são faladas (`onPartialResults`), reduzindo o tempo de resposta a milissegundos sem esperar silêncios.
  - **Mecanismo de De-duplicação**: Controla as ativações de voz na mesma sessão para evitar cobranças duplicadas de elixir.
- **Filtro de Deck Inteligente (Acurácia aprimorada)**:
  - O reconhecedor aprende dinamicamente até 8 cartas faladas durante o jogo.
  - Assim que o deck de 8 cartas do adversário é identificado, o app passa a restringir as buscas, ignorando ruídos ou palavras que correspondam a cartas fora do baralho aprendido.
  - A lista de cartas aprendidas é resetada automaticamente ao falar "Iniciar" ou "Começar".
- **Guia de Comandos Integrado (In-App)**:
  - Uma tela moderna dividida em abas (**Comandos**, **Números** e **Cartas Mapeadas**) acessível diretamente na interface inicial do app, exibindo com precisão todas as palavras-chave suportadas e custos de elixir.
- **Multiplicadores Dinâmicos**: Ajuste do tempo de recarga de elixir para os modos `1x` (2.8s), `2x` (1.4s) e `3x` (0.93s) por meio de comandos de voz ("Normal", "Dobro", "Vezes Três").
- **Botão de Fechar Miniatura**: Um botão redondo `✕` discreto de `18dp` posicionado no canto superior do widget para encerrar a HUD rapidamente.

---

## 📂 Estrutura do Repositório

O projeto está limpo e organizado da seguinte forma:

- **[android/](file:///c:/Royal_time/android)**: Código-fonte completo do projeto Android.
  - **app/src/main/java/com/example/royaltime/**:
    - `MainActivity.kt`: Tela de controle principal, gerenciamento de permissões (Microfone, Sobreposição e Notificações), controle de estado de navegação e a tela de Guia de Comandos.
    - `ElixirCircleView.kt`: Custom View que renderiza o design circular 3D, bezels, pulso elástico e animação senoidal do brilho externo. Otimizada para evitar alocações de memória no `onDraw()`.
    - `ElixirHudService.kt`: Foreground Service responsável pelo widget flutuante, atualização da HUD, canal de notificação persistente e loop a 60 FPS.
    - `VoiceInputManager.kt`: Gerenciador da SpeechRecognizer API com preferência offline, de-duplicação e detecção dinâmica do deck de 8 cartas.
  - **app/src/main/res/**:
    - `layout/layout_floating_hud.xml`: Layout XML compacto que define a estrutura de sobreposição.
    - `drawable/close_button_background.xml`: Shape circular semitransparente para o botão de fechar.

---

## 🛠️ Como Compilar e Instalar

### Requisitos
- Android Studio ou SDK do Android configurado.
- Dispositivo Android (ou emulador) com modo de Depuração USB ativado.

### Passos
1. Compile o projeto executando o comando a partir da pasta raiz:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```
2. Instale o APK gerado no dispositivo conectado via ADB:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🎮 Como Usar

1. Abra o aplicativo **RoyalTime** no seu celular.
2. Dê permissão para o **Microfone** (escuta dos comandos), **Sobreposição a Outros Apps** (desenhar o HUD na tela) e **Notificações** (manter o serviço rodando em primeiro plano).
3. (Opcional) Toque em **Ver Guia de Comandos de Voz** para consultar a lista completa de comandos gerais, números e custos de cartas mapeadas.
4. Toque em **Iniciar HUD Flutuante**.
5. Abra o **Clash Royale** e posicione o widget roxo redondo na tela.
6. Fale os comandos de voz:
   - **"Iniciar"** ou **"Começar"**: Ativa a contagem de elixir (inicia em 5 e reseta o deck do adversário).
   - **"Vezes Dois"** ou **"Dobro"**: Ativa a velocidade de elixir duplo.
   - **"Vezes Três"**: Ativa a velocidade de elixir triplo.
   - **"Normal"** ou **"Vezes Um"**: Retorna à velocidade simples de elixir.
   - Fale o nome de uma carta (ex: **"Corredor"**) ou o número (ex: **"4"**) para debitar o elixir do adversário.
7. Clique no pequeno **✕** no canto superior do HUD para fechá-lo.
