package com.yashkasera.learnwithus.repository

import com.yashkasera.learnwithus.RetrofitService

/**
 * @author yashkasera
 * Created 19/04/22 at 8:30 PM
 */
class Repository {
    private val service by lazy { RetrofitService.getInstance() }

    suspend fun getSounds(text: String) = service.getSounds(hashMapOf("text" to text))

    suspend fun getSound(text: String) = service.getSound(hashMapOf("text" to text))

    suspend fun checkGrammar(text: String) = service.checkGrammar(hashMapOf("text" to text))

    suspend fun getKeywords(text: String) = service.getKeywords(hashMapOf("text" to text))
}