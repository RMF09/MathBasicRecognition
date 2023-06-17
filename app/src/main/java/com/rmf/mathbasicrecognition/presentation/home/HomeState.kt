package com.rmf.mathbasicrecognition.presentation.home

import com.rmf.mathbasicrecognition.domain.model.DataMathExpression

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val list: List<DataMathExpression> = emptyList()
)
