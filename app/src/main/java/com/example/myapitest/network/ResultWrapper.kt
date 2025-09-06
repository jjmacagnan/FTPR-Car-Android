package com.example.myapitest.network

/**
 * Uma classe selada para encapsular os resultados da API, representando os estados de
 * sucesso, erro de rede ou erro genérico da API (como 404, 500, etc.).
 */
sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T) : ResultWrapper<T>()
    data class GenericError(val code: Int? = null, val error: String? = null) : ResultWrapper<Nothing>()
    object NetworkError : ResultWrapper<Nothing>()
}