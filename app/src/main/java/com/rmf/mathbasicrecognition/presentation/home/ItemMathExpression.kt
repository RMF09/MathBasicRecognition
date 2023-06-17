package com.rmf.mathbasicrecognition.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rmf.mathbasicrecognition.domain.model.DataMathExpression

@Composable
fun ItemMathExpression(
    number: Int,
    data: DataMathExpression
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = number.toString(), fontSize = 48.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Text(text = data.input)
            Text(text = data.displayResult)
        }
    }

}