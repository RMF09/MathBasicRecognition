package com.rmf.mathbasicrecognition.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rmf.mathbasicrecognition.utils.exhaustive
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
) : ViewModel() {

    var state by mutableStateOf(HomeState())

    fun onEvent(e: HomeUIEvent) {
        when (e) {
            is HomeUIEvent.OnResult -> {
                val listTemp =
                    state.list.toMutableList()

                listTemp.add(e.dataMathExpression)
                state = state.copy(list = listTemp, isLoading = false)

            }
            is HomeUIEvent.OnError -> {
                state = state.copy(error = e.error, isLoading = false)
            }
            HomeUIEvent.OnDismissDialog -> {
                state = state.copy(error = null)
            }
            HomeUIEvent.OnProcess -> {
                state = state.copy(isLoading = true)
            }
        }.exhaustive
    }


}