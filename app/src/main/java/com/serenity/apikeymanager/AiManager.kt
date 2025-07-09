package himanshu.com.apikeymanager

import android.content.Context
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * AiManager manages AI provider requests, key rotation, model selection, and error handling.
 *
 * Usage:
 *   val aiManager = AiManager(context)
 *   val result = aiManager.postRequest("your prompt", model = "optional-model")
 */
class AiManager(private val context: Context, private val timeoutSeconds: Long = 60) {
    private val apiKeyManager = ApiKeyManager(context)
    private val providers: List<AiProvider> = listOf(
        GeminiProvider(apiKeyManager, timeoutSeconds),
        HuggingFaceProvider(apiKeyManager, timeoutSeconds),
        OpenRouterProvider(apiKeyManager, timeoutSeconds),
        GroqProvider(apiKeyManager, timeoutSeconds),
//        ArliAIProvider(apiKeyManager, timeoutSeconds),
//        ShaleProtocolProvider(apiKeyManager, timeoutSeconds)
    )
    private var requestIndex = 0
    private val mutex = Mutex()

    private fun logInfo(tag: String, message: String) {
        android.util.Log.i(tag, message)
    }

    private fun logError(tag: String, message: String, throwable: Throwable? = null) {
        android.util.Log.e(tag, message, throwable)
    }

    private fun logDebug(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }

    /**
     * Post a prompt to the AI providers, using round-robin across all (provider, key) pairs.
     * Optionally override the model for this request.
     * Throws if all providers/keys fail.
     */
    suspend fun postRequest(prompt: String, model: String? = null): String = mutex.withLock {
        // Build a flat list of (provider, key) pairs in order
        val providerKeyPairs = providers.flatMap { provider ->
            apiKeyManager.getApiKeysForProvider(provider.name).map { key -> provider to key }
        }
        if (providerKeyPairs.isEmpty()) throw Exception("No providers or keys configured.")
        val totalPairs = providerKeyPairs.size
        var attempts = 0
        // Prepend language instruction to prompt
        val promptWithLang = "Please answer in English.\n" + prompt
        Log.d("CLUBAPI",totalPairs.toString())
        while (attempts < totalPairs) {
            val (provider, key) = providerKeyPairs[(requestIndex + attempts) % totalPairs]
            provider.setApiKey(key)
            Log.d("CLUBAPI", "Calling provider: ${provider.name} with key: $key")
            try {
                val response = provider.sendRequest(promptWithLang)
                Log.d("CLUBAPI", "${provider.name} response: $response")
                // Advance the round-robin index for next call
                requestIndex = (requestIndex + 1) % totalPairs
                return response
            } catch (e: Exception) {
                Log.d("CLUBAPI", "${provider.name} error: ${e.message}", e)
                // Optionally log error: e
                attempts++
                continue
            }
        }
        // Advance the index even if all fail
        requestIndex = (requestIndex + 1) % totalPairs
        throw Exception("All providers and keys failed to process the request.")
    }
} 