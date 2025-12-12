import android.util.Base64
import org.json.JSONObject

fun isTokenValid(token: String?): Boolean {
    if (token == null) return false

    return try {
        // 1. Split the token (Header.Payload.Signature)
        val parts = token.split(".")
        if (parts.size != 3) return false

        // 2. Decode the Payload (index 1)
        val payload = parts[1]

        // Add padding if needed (Base64 requirement)
        val padding = "=".repeat((4 - payload.length % 4) % 4)
        val decodedBytes = Base64.decode(payload + padding, Base64.URL_SAFE)
        val decodedString = String(decodedBytes)

        // 3. Parse JSON to find "exp"
        val json = JSONObject(decodedString)
        if (!json.has("exp")) return false

        val expTimestamp = json.getLong("exp") // Time in seconds
        val currentTimestamp = System.currentTimeMillis() / 1000

        // 4. Return true if expiration is in the future
        // We add a 10-second buffer to be safe
        return expTimestamp > (currentTimestamp + 10)

    } catch (e: Exception) {
        e.printStackTrace()
        false // If we can't read it, assume it's invalid
    }
}