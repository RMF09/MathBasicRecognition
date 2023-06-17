package com.rmf.mathbasicrecognition.utils

import java.text.DecimalFormat
import java.util.regex.Pattern
import javax.script.ScriptEngineManager


fun formatMathResult(result: Double?): String {
    if (result == null) {
        return "NaN"
    }

    val decimalFormat = DecimalFormat("#.####")
    decimalFormat.isGroupingUsed = false

    val formattedResult = decimalFormat.format(result)
    return formattedResult.replace('.', ',')
}

fun extractMathExpression(input: String): String? {
    val pattern = Pattern.compile("\\d+\\s*[-+*/]\\s*\\d+")
    val matcher = pattern.matcher(input)

    return if (matcher.find()) {
        matcher.group(0)
    } else {
        null
    }
}

fun evaluateMathExpression(input: String): Double? {
    val scriptEngine = ScriptEngineManager().getEngineByName("js")

    return try {
        val result = scriptEngine.eval(input)
        result as? Double
    } catch (e: Exception) {
        null
    }
}