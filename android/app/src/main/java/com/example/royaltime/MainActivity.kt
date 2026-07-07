package com.example.royaltime

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.royaltime.theme.RoyalTimeTheme

class MainActivity : ComponentActivity() {

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val REQUEST_OVERLAY_PERMISSION = 201
    private val REQUEST_NOTIFICATION_PERMISSION = 202

    private var hasAudioPermission by mutableStateOf(false)
    private var hasOverlayPermission by mutableStateOf(false)
    private var hasNotificationPermission by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkPermissions()

        setContent {
            RoyalTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("main") }
                    if (currentScreen == "main") {
                        MainScreenContent(onNavigateToCommands = { currentScreen = "commands" })
                    } else {
                        VoiceCommandsScreen(onBack = { currentScreen = "main" })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        hasAudioPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        hasOverlayPermission = Settings.canDrawOverlays(this)

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_PERMISSION
        )
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    private fun startHudService() {
        if (!hasAudioPermission || !hasOverlayPermission || !hasNotificationPermission) {
            Toast.makeText(this, "Por favor, conceda todas as permissões primeiro!", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, ElixirHudService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "HUD iniciado com sucesso!", Toast.LENGTH_SHORT).show()
    }

    private fun stopHudService() {
        val intent = Intent(this, ElixirHudService::class.java)
        stopService(intent)
        Toast.makeText(this, "HUD encerrado.", Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun MainScreenContent(onNavigateToCommands: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Royal Time",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "HUD de Elixir Inteligente por Voz",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Permissões Necessárias",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    // Audio Permission Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Gravação de Áudio (Microfone)", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Necessário para comandos de voz",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        if (hasAudioPermission) {
                            Text("✅", fontSize = 20.sp)
                        } else {
                            Button(onClick = { requestAudioPermission() }) {
                                Text("Ativar")
                            }
                        }
                    }

                    HorizontalDivider()

                    // Overlay Permission Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Sobrepor a Outros Apps", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Exibe o widget em cima do Clash Royale",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        if (hasOverlayPermission) {
                            Text("✅", fontSize = 20.sp)
                        } else {
                            Button(onClick = { requestOverlayPermission() }) {
                                Text("Permitir")
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        HorizontalDivider()

                        // Notification Permission Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Notificações", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Necessário para manter o serviço ativo",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            if (hasNotificationPermission) {
                                Text("✅", fontSize = 20.sp)
                            } else {
                                Button(onClick = { requestNotificationPermission() }) {
                                    Text("Permitir")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botão "Ver Comandos de Voz" posicionado estrategicamente
            OutlinedButton(
                onClick = onNavigateToCommands,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = "ℹ️ Ver Guia de Comandos de Voz",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { startHudService() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A0DAD)) // Roxo Elixir
            ) {
                Text(
                    text = "Iniciar HUD Flutuante",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = { stopHudService() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Parar HUD",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Instruções: Fale o nome de cartas populares (ex: 'Gigante', 'Corredor', 'Golem') ou números de 1 a 10 para registrar o consumo de elixir do adversário.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }

    @Composable
    fun VoiceCommandsScreen(onBack: () -> Unit) {
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Comandos", "Números", "Cartas")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guia de Comandos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> CommandsTabContent()
                    1 -> NumbersTabContent()
                    2 -> CardsTabContent()
                }
            }
        }
    }

    @Composable
    fun CommandsTabContent() {
        val commands = listOf(
            Triple("Iniciar / Começar", "Inicia o ciclo da partida com 5 de elixir.", "Ex: 'Iniciar'"),
            Triple("Vezes Dois / Dobro", "Ativa o modo de elixir duplo (1.4s por unidade).", "Ex: 'Dobro'"),
            Triple("Vezes Três", "Ativa o modo de elixir triplo (0.93s por unidade).", "Ex: 'Vezes Três'"),
            Triple("Vezes Um / Normal", "Retorna a velocidade para o padrão 1x (2.8s por unidade).", "Ex: 'Normal'")
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            commands.forEach { (cmd, desc, ex) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = cmd, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = ex, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }

    @Composable
    fun NumbersTabContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Uso de Números Diretos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fale qualquer número de 1 a 10 para descontar imediatamente aquela quantidade exata de elixir do adversário.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Útil quando você vê o gasto de elixir mas não sabe ou não lembra o nome da carta jogada.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "Números Suportados",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                val numbers = listOf(
                    "1" to "Um", "2" to "Dois", "3" to "Três", "4" to "Quatro", "5" to "Cinco",
                    "6" to "Seis", "7" to "Sete", "8" to "Oito", "9" to "Nove", "10" to "Dez"
                )
                numbers.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { (num, text) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = num, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(text = text, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CardsTabContent() {
        val cards = listOf(
            "Golem" to 8, "Pekka" to 7, "Megacavaleiro" to 7, "Lava Hound" to 7,
            "Sparky" to 6, "Foguete" to 6, "Relampago" to 6, "X-Besta" to 6, "Barbaros de Elite" to 6,
            "Gigante" to 5, "Balao" to 5, "Mago" to 5, "Principe" to 5, "Bruxa" to 5, "Horda de Servos" to 5, "Torre Inferno" to 5, "Barbaros" to 5, "Cemiterio" to 5,
            "Corredor" to 4, "Valquiria" to 4, "Bebe Dragao" to 4, "Bola de Fogo" to 4, "Veneno" to 4, "Mosqueteira" to 4, "Dragao Infernal" to 4, "Morteiro" to 4, "Lenhador" to 4, "Bruxa Sombria" to 4, "Ariete de Batalha" to 4, "Fenix" to 4,
            "Mineiro" to 3, "Exercito de Esqueletos" to 3, "Barril de Goblins" to 3, "Servos" to 3, "Arqueiras" to 3, "Cavaleiro" to 3, "Gangue de Goblins" to 3, "Lapide" to 3, "Pequeno Principe" to 3, "Bandida" to 3, "Fantasma Real" to 3,
            "Tronco" to 2, "Goblins" to 2,
            "Espirito" to 1
        ).sortedByDescending { it.second }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Cartas Comuns e Seus Custos",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cards) { (card, cost) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                           Text(
                               text = card,
                               fontWeight = FontWeight.SemiBold,
                               fontSize = 14.sp,
                               color = MaterialTheme.colorScheme.onSurfaceVariant,
                               modifier = Modifier.weight(1f)
                           )
                           Spacer(modifier = Modifier.width(4.dp))
                           Text(
                               text = "$cost💧",
                               fontWeight = FontWeight.Bold,
                               fontSize = 14.sp,
                               color = Color(0xFF9C27B0)
                           )
                        }
                    }
                }
            }
        }
    }
}
