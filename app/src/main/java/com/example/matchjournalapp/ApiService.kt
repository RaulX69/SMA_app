package com.example.matchjournalapp

import retrofit2.Call
import retrofit2.http.*

data class LoginRequest(
    val email: String,
    val password: String
)
data class LoginResponse(
    val message: String,
    val token: String,
    val role: String
)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)
data class RegisterResponse(
    val message: String,
    val user: UserInfo
)
data class UserInfo(
    val id: Int,
    val name: String,
    val email: String
)

data class MatchRequest(
    val home_team: String,
    val away_team: String,
    val home_score: Int,
    val away_score: Int,
    val match_date: String,
    val rating: Int?,
    val review: String,
    val stadium_name: String,
    val city: String,
    val api_match_id: String? = null,
    val home_logo: String? = null,
    val away_logo: String? = null
)

data class MatchResponse(
    val message: String,
    val match: Match
)

data class Match(
    val id: Int,
    val home_team: String,
    val away_team: String,
    val home_score: Int,
    val away_score: Int,
    val match_date: String,
    val rating: Int?,
    val review: String,
    val stadium_name: String?,
    val city: String?,
    val lat: Double?,
    val lng: Double?
)

data class ApiMatchResult(
    val fixture: ApiFixture,
    val teams: ApiTeams,
    val goals: ApiGoals
)

data class ApiFixture(
    val id: Int,
    val date: String,
    val venue: ApiVenue
)

data class ApiVenue(
    val name: String?,
    val city: String?
)

data class ApiTeams(
    val home: ApiTeamDetail,
    val away: ApiTeamDetail
)

data class ApiTeamDetail(
    val name: String,
    val logo: String
)

data class ApiGoals(
    val home: Int?,
    val away: Int?
)

data class UpdateMatchRequest(
    val rating: Int?,
    val review: String
)

data class SimpleResponse(
    val message: String
)

interface ApiService {

    @Headers(
        "Bypass-Tunnel-Reminder: true",
    )
    @POST("/api/login")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @Headers(
        "Bypass-Tunnel-Reminder: true",
    )
    @POST("/api/register")
    fun registerUser(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @Headers(
        "Bypass-Tunnel-Reminder: true"
    )
    @POST("/api/matches")
    fun addMatch(
        @Header("token") token: String,
        @Body request: MatchRequest
    ): Call<MatchResponse>

    @Headers(
        "Bypass-Tunnel-Reminder: true"
    )
    @GET("/api/matches")
    fun getMatches(
        @Header("token") token: String
    ): Call<List<Match>>

    @Headers(
        "Bypass-Tunnel-Reminder: true"
    )
    @GET("/api/search-matches")
    fun searchMatches(
        @Header("token") token: String,
        @Query("team") teamName: String
    ): Call<List<ApiMatchResult>>

    @Headers(
        "Bypass-Tunnel-Reminder: true"
    )
    @PUT("/api/matches/{id}")
    fun updateMatch(
        @Header("token") token: String,
        @Path("id") matchId: Int,
        @Body request: UpdateMatchRequest
    ): Call<MatchResponse>

    @Headers(
        "Bypass-Tunnel-Reminder: true"
    )
    @DELETE("/api/matches/{id}")
    fun deleteMatch(
        @Header("token") token: String,
        @Path("id") matchId: Int
    ): Call<SimpleResponse>
}