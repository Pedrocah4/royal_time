# Royal Time 💧

HUD Tático e Contador de Elixir do Adversário para **Clash Royale**, desenvolvido como um aplicativo nativo para **Android (Kotlin)**.

O aplicativo funciona através de um widget flutuante (overlay) que se sobrepõe ao jogo Clash Royale e utiliza reconhecimento de voz contínuo para registrar as jogadas do oponente em tempo real.

---

## 🚀 Funcionalidades

- **Janela Flutuante (Overlay)**: Um widget roxo moderno e compacto que pode ser arrastado e posicionado em qualquer lugar da tela sobre o Clash Royale.
- **Reconhecimento de Voz Inteligente**: Conversão de fala local e offline para comandos do jogo.
- **Mapeamento de Cartas**: Mapeamento dos nomes de cartas populares (ex: "Gigante", "Corredor", "Golem", "Pekka", "Tronco") para descontar o elixir correspondente automaticamente.
- **Multiplicadores de Velocidade**: Suporte aos modos de geração de elixir `1x`, `2x` e `3x` controlados por voz.
- **Histórico no HUD**: Exibição da última carta detectada diretamente no widget flutuante (ex: `🎙️ Corredor (-4)`).

---

## 📂 Estrutura do Repositório

O projeto está organizado da seguinte forma:

- **[android/](file:///c:/Royal_time/android)**: Código-fonte completo do projeto Android.
  - **app/src/main/java/com/example/royaltime/**:
    - `MainActivity.kt`: Tela principal para concessão de permissões e controle.
    - `ElixirHudService.kt`: Serviço responsável pela janela flutuante e lógica de elixir.
    - `VoiceInputManager.kt`: Gerenciador da SpeechRecognizer API do Android.
  - **app/src/main/res/layout/**:
    - `layout_floating_hud.xml`: Layout visual do widget flutuante.

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
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🎮 Como Usar

1. Abra o aplicativo **RoyalTime** no seu celular.
2. Dê permissão para o **Microfone** (escuta dos comandos) e **Sobreposição a Outros Apps** (desenhar o HUD na tela).
3. Toque em **Iniciar HUD Flutuante**.
4. Abra o **Clash Royale** e posicione o widget roxo na tela.
5. Fale os comandos de voz:
   - **"Iniciar"** ou **"Começar"**: Ativa a contagem de elixir (inicia em 5).
   - **"Vezes Dois"** ou **"Dobro"**: Ativa a velocidade de elixir duplo (fase final).
   - **"Vezes Três"**: Ativa a velocidade de elixir triplo.
   - Fale o nome de uma carta (ex: **"Corredor"**) ou o número (ex: **"4"**) para debitar o elixir do adversário.
6. Clique no **✕** do HUD para encerrar a sobreposição.
