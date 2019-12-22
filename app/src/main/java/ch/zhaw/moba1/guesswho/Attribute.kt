package ch.zhaw.moba1.guesswho

import java.io.Serializable

class Attribute(
    var img: Int,
    var imgNo: Int,
    var imgYes: Int,
    var name: String,
    var question: String,
    var answer: Boolean?,
    var disabled: Boolean
) : Serializable {

    fun getImage(): Int {
        if (answer == null) {
            return img
        }

        return if (answer!!) imgYes else imgNo
    }

    fun toPayload(): Map<String, Any> {
        return hashMapOf(
            "img" to img,
            "imgNo" to imgNo,
            "imgYes" to imgYes,
            "name" to name,
            "question" to question
        )
    }

    override fun toString(): String {
        return "Attribute(name='$name', question='$question', answer=$answer, disabled=$disabled)"
    }

    fun copy(): Attribute {
        return Attribute(img, imgNo, imgYes, name, question, answer, disabled)
    }
}
