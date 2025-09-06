package com.example.myapitest.utils

import android.text.Editable
import android.text.TextWatcher
import java.util.Calendar

object ValidationUtils {

    /**
     * Cria um TextWatcher para aplicar máscara na placa
     */
    fun createPlateTextWatcher(): TextWatcher {
        return object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                s?.let {
                    val formatted = formatPlate(it.toString())
                    if (formatted != it.toString()) {
                        it.replace(0, it.length, formatted)
                    }
                }

                isUpdating = false
            }
        }
    }

    /**
     * Valida se o ano está dentro do range válido
     */
    fun isValidYear(year: String): Boolean {
        return try {
            val y = year.toInt()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            y in 1900..(currentYear + 1)
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Valida placa brasileira (formato antigo e Mercosul)
     */
    fun isValidBrazilianPlate(plate: String): Boolean {
        val oldPattern = Regex("^[A-Z]{3}-\\d{4}\$", RegexOption.IGNORE_CASE)
        val mercosulWithHyphen = Regex("^[A-Z]{3}-[0-9][A-Z][0-9]{2}\$", RegexOption.IGNORE_CASE)
        val mercosulWithoutHyphen = Regex("^[A-Z]{3}[0-9][A-Z][0-9]{2}\$", RegexOption.IGNORE_CASE)

        return oldPattern.matches(plate) ||
                mercosulWithHyphen.matches(plate) ||
                mercosulWithoutHyphen.matches(plate)
    }

    /**
     * Aplica máscara na placa
     */
    fun formatPlate(text: String): String {
        val cleanText = text.replace(Regex("[^A-Z0-9]"), "")
        return when {
            cleanText.length <= 3 -> cleanText
            cleanText.length > 3 && !cleanText.contains("-") ->
                "${cleanText.substring(0,3)}-${cleanText.substring(3)}"
            else -> cleanText
        }
    }

    /**
     * Dados de validação para um campo
     */
    data class FieldValidation(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    /**
     * Resultado de validação completa
     */
    data class ValidationResult(
        val isValid: Boolean,
        val fieldErrors: Map<String, String> = emptyMap()
    )

    /**
     * Valida todos os campos de um carro
     */
    fun validateCarFields(
        name: String,
        year: String,
        licence: String,
        hasLocation: Boolean
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (name.trim().isEmpty()) {
            errors["name"] = "Campo obrigatório"
        }

        if (!isValidYear(year)) {
            errors["year"] = "Ano inválido"
        }

        if (!isValidBrazilianPlate(licence)) {
            errors["licence"] = "Placa inválida"
        }

        if (!hasLocation) {
            errors["location"] = "Selecione uma localização"
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            fieldErrors = errors
        )
    }
}