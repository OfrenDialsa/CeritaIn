package com.dicoding.picodiploma.loginwithanimation.data

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: String) : Result<Nothing>()
}