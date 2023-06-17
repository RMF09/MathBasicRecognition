package com.rmf.mathbasicrecognition.presentation.home

import android.net.Uri
import com.rmf.mathbasicrecognition.domain.model.DataMathExpression

sealed class HomeUIEvent{
    data class OnResult(val dataMathExpression: DataMathExpression): HomeUIEvent()
    data class OnError(val error: String): HomeUIEvent()
    object OnDismissDialog: HomeUIEvent()
    object OnProcess: HomeUIEvent()
}
