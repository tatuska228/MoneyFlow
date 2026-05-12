package com.example.moneyflow.model

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

sealed class BiometricResult {
    object Success : BiometricResult()
    object Cancelled : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

object BiometricHelper {

    /**
     * Проверяет, доступна ли биометрия на устройстве
     */
    fun isAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Показывает диалог биометрической аутентификации.
     * activity — FragmentActivity (MainActivity наследует его через ComponentActivity)
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Подтверждение личности",
        subtitle: String = "Приложите палец к сканеру",
        negativeButtonText: String = "Отмена",
        onResult: (BiometricResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED
                ) {
                    onResult(BiometricResult.Cancelled)
                } else {
                    onResult(BiometricResult.Error(errString.toString()))
                }
            }

            override fun onAuthenticationFailed() {
                // Палец не распознан — BiometricPrompt сам показывает ошибку,
                // не прерываем поток, даём пользователю попробовать ещё раз
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        prompt.authenticate(promptInfo)
    }
}