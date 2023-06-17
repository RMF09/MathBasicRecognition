package com.rmf.mathbasicrecognition.domain.model

data class DataMathExpression(
    val input: String,
    val result: String
){
    val displayResult get() = "result : $result"
}
