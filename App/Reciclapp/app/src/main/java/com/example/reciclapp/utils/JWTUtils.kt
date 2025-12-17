import android.util.Base64
import org.json.JSONObject

fun isTokenValid(token: String?): Boolean {
    if (token == null) return false

    return try {
        val parts = token.split(".")
        if (parts.size != 3) return false

        val payload = parts[1]

        val padding = "=".repeat((4 - payload.length % 4) % 4)
        val decodedBytes = Base64.decode(payload + padding, Base64.URL_SAFE)
        val decodedString = String(decodedBytes)

        val json = JSONObject(decodedString)
        if (!json.has("exp")) return false

        val expTimestamp = json.getLong("exp")
        val currentTimestamp = System.currentTimeMillis() / 1000

        return expTimestamp > (currentTimestamp + 10)

    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}