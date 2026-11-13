package com.example.matchjournalapp

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun SearchMatchScreen(
    token: String,
    onMatchAdded: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ApiMatchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Caută Meciuri", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Nume Echipă") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        isLoading = true
                        RetrofitClient.instance.searchMatches(token, searchQuery)
                            .enqueue(object : Callback<List<ApiMatchResult>> {
                                override fun onResponse(call: Call<List<ApiMatchResult>>, response: Response<List<ApiMatchResult>>) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        searchResults = response.body() ?: emptyList()
                                        if (searchResults.isEmpty()) {
                                            Toast.makeText(context, "Nu am găsit meciuri", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<List<ApiMatchResult>>, t: Throwable) {
                                    isLoading = false
                                    Toast.makeText(context, "Eroare rețea", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "..." else "Caută")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(searchResults) { match ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            isLoading = true

                            val request = MatchRequest(
                                home_team = match.teams.home.name,
                                away_team = match.teams.away.name,
                                home_score = match.goals.home ?: 0,
                                away_score = match.goals.away ?: 0,
                                match_date = match.fixture.date.take(10),
                                rating = null,
                                review = "",
                                stadium_name = match.fixture.venue.name ?: "",
                                city = match.fixture.venue.city ?: "",
                                api_match_id = match.fixture.id.toString(),
                                home_logo = match.teams.home.logo,
                                away_logo = match.teams.away.logo
                            )

                            RetrofitClient.instance.addMatch(token, request).enqueue(object : Callback<MatchResponse> {
                                override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Meci salvat în jurnal!", Toast.LENGTH_SHORT).show()
                                        onMatchAdded()
                                    } else {
                                        Toast.makeText(context, "Eroare la salvare: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                                    isLoading = false
                                    Toast.makeText(context, "Eroare conexiune: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${match.teams.home.name} vs ${match.teams.away.name}", style = MaterialTheme.typography.titleMedium)
                        Text("Scor: ${match.goals.home ?: "-"} : ${match.goals.away ?: "-"}")
                        Text("Data: ${match.fixture.date.take(10)}")
                    }
                }
            }
        }
    }
}