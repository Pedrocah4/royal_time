# Royal Time 💧

HUD Tático e Contador de Elixir do Adversário para **Clash Royale**, desenvolvido como um aplicativo nativo para **Android (Kotlin)**.

O aplicativo funciona através de um widget flutuante (overlay) em formato de **esfera de vidro 3D** com anel metálico e indicador de progresso de elixir fluido rodando a **60 FPS** (ciclo de ~16ms). O widget é extremamente compacto, medindo exatamente `75dp x 75dp` (equivalente ao tamanho de um ícone de aplicativo padrão no celular), e pode ser arrastado para qualquer lugar da tela sobre o jogo Clash Royale.

---

## 🚀 Funcionalidades

- **Design Circular 3D Premium**:
  - **Outer Glow (Brilho Externo)** roxo translúcido.
  - **Bezel Metálico** duplo (moldura prateada externa e interna).
  - **Orbe de Vidro Interno** com efeito tridimensional de luz e reflexo reflexivo (gloss).
  - **Número de Elixir Central** estilizado em gradiente dourado vertical e sombra de profundidade (drop shadow).
- **Animação Fluida a 60 FPS**: O anel roxo de carregamento de elixir cresce continuamente no sentido horário de forma suave, sem saltos.
- **Reconhecimento de Voz Inteligente**: Conversão de fala local e offline para comandos do jogo sem depender de servidores ou conexões.
- **Mapeamento de Cartas**: Mapeamento dos nomes de cartas populares (ex: "Gigante", "Corredor", "Golem", "Pekka", "Tronco") para descontar o elixir correspondente automaticamente.
- **Multiplicadores Dinâmicos**: Ajuste suave e sem interrupções do tempo de recarga de elixir para os modos `1x` (2.8s), `2x` (1.4s) e `3x` (0.93s) por meio de comandos de voz.
- **Botão de Fechar Miniatura**: Um botão redondo `✕` discreto de `18dp` posicionado no canto superior do widget para encerrar a HUD rapidamente.

---

## 📂 Estrutura do Repositório

O projeto está organizado da seguinte forma:

- **[android/](file:///c:/Royal_time/android)**: Código-fonte completo do projeto Android.
  - **app/src/main/java/com/example/royaltime/**:
    - `MainActivity.kt`: Interface principal para controle e ativação do serviço e concessão de permissões.
    - `ElixirCircleView.kt`: View customizada em Kotlin que renderiza o design circular 3D, bezels e sombras da HUD.
    - `ElixirHudService.kt`: Serviço de segundo plano responsável pela janela flutuante, escuta contínua de voz e loop do timer a 60 FPS.
    - `VoiceInputManager.kt`: Gerenciador da SpeechRecognizer API nativa para escuta de comandos de voz.
  - **app/src/main/res/**:
    - `layout/layout_floating_hud.xml`: Layout XML compacto que define a estrutura de sobreposição.
    - `drawable/close_button_background.xml`: Shape circular semitransparente para o botão de fechar.
    - `drawable/hud_background.xml`: Borda metálica roxa e fundo do HUD.

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
2. Dê permissão para o **Microfone** (escuta dos comandos) e **Sobreposição a Outros Apps** (desenhar o HUD na tela).
3. Toque em **Iniciar HUD Flutuante**.
4. Abra o **Clash Royale** e posicione o widget roxo redondo na tela.
5. Fale os comandos de voz:
   - **"Iniciar"** ou **"Começar"**: Ativa a contagem de elixir (inicia em 5).
   - **"Vezes Dois"** ou **"Dobro"**: Ativa a velocidade de elixir duplo (fase final).
   - **"Vezes Três"**: Ativa a velocidade de elixir triplo.
   - Fale o nome de uma carta (ex: **"Corredor"**) ou o número (ex: **"4"**) para debitar o elixir do adversário.
6. Clique no pequeno **✕** no canto superior do HUD para fechá-lo.
