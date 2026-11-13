package com.example.matchjournalapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    val startDestination = if (tokenManager.getToken() != null) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(navController, tokenManager)
        }

        composable("dashboard") {
            DashboardScreen(navController, tokenManager)
        }

        composable("search_match") {
            val token = tokenManager.getToken() ?: ""
            SearchMatchScreen(
                token = token,
                onMatchAdded = {
                    navController.popBackStack()
                }
            )
        }

        composable("edit_match/{matchId}") { backStackEntry ->
            val matchIdString = backStackEntry.arguments?.getString("matchId")
            val matchId = matchIdString?.toIntOrNull() ?: 0
            val token = tokenManager.getToken() ?: ""

            EditMatchScreen(
                token = token,
                matchId = matchId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, tokenManager: TokenManager) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Match Journal", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Parolă") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    val request = LoginRequest(email, password)

                    RetrofitClient.instance.loginUser(request).enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            isLoading = false
                            if (response.isSuccessful && response.body() != null) {
                                val token = response.body()!!.token

                                // 1. SALVĂM TOKEN-UL
                                tokenManager.saveToken(token)

                                Toast.makeText(context, "Logare reușită!", Toast.LENGTH_SHORT).show()

                                // 2. NAVIGĂM CĂTRE DASHBOARD ȘI ȘTERGEM LOGIN-UL DIN ISTORIC
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Email sau parolă greșită", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            isLoading = false
                            Toast.makeText(context, "Eroare: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Login")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DashboardScreen(navController: NavController, tokenManager: TokenManager) {
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        if (token != null) {
            RetrofitClient.instance.getMatches(token).enqueue(object : Callback<List<Match>> {
                override fun onResponse(call: Call<List<Match>>, response: Response<List<Match>>) {
                    isLoading = false
                    if (response.isSuccessful) {
                        matches = response.body() ?: emptyList()
                    } else {
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string() ?: "Fără detalii"
                        Toast.makeText(context, "Eroare $errorCode: $errorBody", Toast.LENGTH_LONG).show()
                        println("Eroare server: $errorCode - $errorBody")
                    }
                }
                override fun onFailure(call: Call<List<Match>>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(context, "Eroare conexiune: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("search_match") }) {
                Icon(Icons.Default.Add, contentDescription = "Adăugare meci")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Jurnalul Meu", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = {
                    tokenManager.clearToken()
                    navController.navigate("login") { popUpTo("dashboard") { inclusive = true } }
                }) {
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (matches.isEmpty()) {
                Text("Nu ai niciun meci salvat încă.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(matches) { match ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    navController.navigate("edit_match/${match.id}")
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "${match.home_team} vs ${match.away_team}", style = MaterialTheme.typography.titleLarge)
                                Text(text = "Scor: ${match.home_score ?: "-"} : ${match.away_score ?: "-"}")
                                Text(text = "Stadion: ${match.stadium_name ?: "Necunoscut"}")
                                Text(text = "Data: ${match.match_date.take(10)}")
                                Text(text = "Nota mea: ${match.rating}/5", color = MaterialTheme.colorScheme.primary)
                                if(match.review.isNotBlank()) Text(text = "Recenzie: ${match.review}")
                            }
                        }
                    }
                }
            }
        }
    }
}
