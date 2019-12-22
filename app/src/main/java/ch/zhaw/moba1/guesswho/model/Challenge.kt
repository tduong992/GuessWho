package ch.zhaw.moba1.guesswho.model

import java.io.Serializable

data class Challenge(
    val uid: String,
    val email: String,
    val displayName: String,
    val imgUrl: String,
    val matchId: String
) : Serializable