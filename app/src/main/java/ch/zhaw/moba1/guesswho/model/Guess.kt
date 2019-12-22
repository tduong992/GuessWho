package ch.zhaw.moba1.guesswho.model

data class Guess(val name: String, var answer: Boolean?) {

    fun toPayload(): HashMap<String, Any?> {
        return hashMapOf(
            "name" to name,
            "answer" to answer
        )
    }
}