package himanshu.com.apikeymanager

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.reflect.Type

data class ProviderConfig(
    val keys: List<String> = listOf(),
    val default_model: String? = null
)

data class KeyStorage(
    val api_keys: Map<String, ProviderConfig> = mapOf()
)

class ApiKeyManager(private val context: Context) {
    private val fileName = "key_storage.json"
    private var keyStorage: KeyStorage = KeyStorage()
    private var lastIndex = 0

    init {
        loadKeys()
    }

    private fun logInfo(tag: String, message: String) {
        android.util.Log.i(tag, message)
    }

    private fun logError(tag: String, message: String, throwable: Throwable? = null) {
        android.util.Log.e(tag, message, throwable)
    }

    private fun logDebug(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }

    private fun loadKeys() {
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val json = inputStream.bufferedReader().use { it.readText() }
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(KeyStorage::class.java)
            keyStorage = adapter.fromJson(json) ?: KeyStorage()
        } catch (e: FileNotFoundException) {
            logInfo("CLUBAPI", "key_storage.json not found, using empty key storage.")
            keyStorage = KeyStorage()
        } catch (e: Exception) {
            logError("CLUBAPI", "Failed to load key_storage.json: ${e.message}", e)
            keyStorage = KeyStorage()
        }
    }

    fun getApiKeysForProvider(providerName: String): List<String> {
        loadKeys()
        return keyStorage.api_keys[providerName]?.keys ?: listOf()
    }

    fun getApiKeyForProvider(providerName: String): String? {
        loadKeys()
        return keyStorage.api_keys[providerName]?.keys?.firstOrNull()
    }

    @Synchronized
    fun getApiKey(): String {
        loadKeys()
        val allKeys = keyStorage.api_keys.values.flatMap { it.keys }
        if (allKeys.isEmpty()) throw Exception("No API keys found!")
        val key = allKeys[(lastIndex % allKeys.size)]
        lastIndex = (lastIndex + 1) % allKeys.size
        return key
    }

    fun getAllApiKeys(): List<String> {
        loadKeys()
        return keyStorage.api_keys.values.flatMap { it.keys }
    }

    fun getDefaultModelForProvider(providerName: String): String? {
        loadKeys()
        return keyStorage.api_keys[providerName]?.default_model
    }
}

// Manual test entry point
fun main() {
    // Replace with your Android context if running in an Android environment
    val context: Context? = null // This must be set to a valid Context in a real app
    if (context == null) {
        println("No Android context available. Manual test cannot run in pure JVM.")
        return
    }
    val apiKeyManager = ApiKeyManager(context)
    val providers = listOf(
        GeminiProvider(apiKeyManager),
        HuggingFaceProvider(apiKeyManager),
        OpenRouterProvider(apiKeyManager),
        GroqProvider(apiKeyManager)
        // Add ArliAIProvider and ShaleProtocolProvider if needed
    )
    val prompt = "Say hello from the test!"
    providers.forEach { provider ->
        try {
            provider.setApiKey(apiKeyManager.getApiKeyForProvider(provider.name) ?: "")
            val response = kotlinx.coroutines.runBlocking { provider.sendRequest(prompt) }
            println("${provider.name} response: $response")
        } catch (e: Exception) {
            println("${provider.name} failed: ${e.message}")
        }
    }
}

