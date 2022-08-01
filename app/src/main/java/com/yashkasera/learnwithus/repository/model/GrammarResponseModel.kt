package com.yashkasera.learnwithus.repository.model

import com.google.gson.annotations.SerializedName

/**
 * @author yashkasera
 * Created 21/04/22 at 8:21 PM
 */
data class GrammarResponseModel(
    val message: String = "",
    val isCorrect: Boolean = false,
)

data class GrammarResponseModelList(
    val grammarList: List<GrammarError>? = null
)

data class GrammarError(
    @SerializedName("error")
    val error: String
){
    override fun toString(): String {
        return error
    }
}