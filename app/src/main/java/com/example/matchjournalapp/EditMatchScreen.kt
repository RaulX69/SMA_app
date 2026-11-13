package com.example.matchjournalapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun EditMatchScreen(
    token: String,
    matchId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var rating by remember { mutableStateOf("") }
    var review by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Adaugă Recenzie", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Nota (1 - 5)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = review,
            onValueChange = { review = it },
            label = { Text("Impresii despre meci") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val ratingInt = rating.toIntOrNull()
                isLoading = true

                val request = UpdateMatchRequest(rating = ratingInt, review = review)

                RetrofitClient.instance.updateMatch(token, matchId, request).enqueue(object : Callback<MatchResponse> {
                    override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                        isLoading = false
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Jurnal actualizat!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        } else {
                            Toast.makeText(context, "Eroare: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                        isLoading = false
                        Toast.makeText(context, "Eroare rețea: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && rating.isNotBlank() && review.isNotBlank()
        ) {
            Text(if (isLoading) "Se salvează..." else "Salvează Recenzia")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Se procesează..." else "Șterge Meciul")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmare ștergere") },
            text = { Text("Ești sigur că vrei să ștergi acest meci din jurnal? Acțiunea este ireversibilă.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isLoading = true

                        RetrofitClient.instance.deleteMatch(token, matchId).enqueue(object : Callback<SimpleResponse> {
                            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                                isLoading = false
                                if (response.isSuccessful) {
                                    Toast.makeText(context, response.body()?.message ?: "Meci șters!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                } else {
                                    Toast.makeText(context, "Eroare: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                                isLoading = false
                                Toast.makeText(context, "Eroare rețea: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                ) {
                    Text("Șterge", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Renunță")
                }
            }
        )
    }
}