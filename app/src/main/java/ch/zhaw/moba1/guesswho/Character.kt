package ch.zhaw.moba1.guesswho

import java.io.Serializable

class Character(
    var img: Int,
    var name: String,
    var hairColor: String,
    var hasBlueEyes: Boolean,
    var hasFacialHair: Boolean,
    var hasLightSkin: Boolean,
    var isBald: Boolean,
    var isMale: Boolean,
    var wearsGlasses: Boolean,
    var wearsHat: Boolean,
    var isFaceDown: Boolean
) : Serializable {

    fun toPayload(): Map<String, Any> {
        return hashMapOf(
            "img" to img,
            "name" to name,
            "hairColor" to hairColor,
            "hasBlueEyes" to hasBlueEyes,
            "hasFacialHair" to hasFacialHair,
            "hasLightSkin" to hasLightSkin,
            "isBald" to isBald,
            "isMale" to isMale,
            "wearsGlasses" to wearsGlasses,
            "wearsHat" to wearsHat,
            "isFaceDown" to isFaceDown
        )
    }

    fun copy(): Character {
        return Character(
            img,
            name,
            hairColor,
            hasBlueEyes,
            hasFacialHair,
            hasLightSkin,
            isBald,
            isMale,
            wearsGlasses,
            wearsHat,
            isFaceDown
        )
    }
}
