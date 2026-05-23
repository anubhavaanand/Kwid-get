package com.example.parser

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

data class FormulaToken(
    val type: TokenType,
    val value: String,
    val start: Int,
    val end: Int
)

enum class TokenType {
    FORMULA_START, FORMULA_END,
    FUNCTION_NAME, OPEN_PAREN, CLOSE_PAREN,
    ARG_SEPARATOR, STRING_LITERAL, NUMBER,
    VARIABLE, OPERATOR, IDENTIFIER, WHITESPACE, UNKNOWN
}

data class FormulaResult(
    val rawText: String,
    val resolved: String,
    val tokens: List<FormulaToken>,
    val error: String? = null
)

class KwgtFormulaEngine {

    // Known KWGT built-in functions
    val knownFunctions = mapOf(
        "df" to FunctionDoc("df", "Date/time format", listOf("format"), "df(HH:mm) → 14:30"),
        "bi" to FunctionDoc("bi", "Battery info", listOf("property"), "bi(level) → 87"),
        "wi" to FunctionDoc("wi", "Weather info", listOf("property"), "wi(temp) → 22"),
        "si" to FunctionDoc("si", "System info", listOf("property"), "si(model) → Pixel 8"),
        "mi" to FunctionDoc("mi", "Music info", listOf("property"), "mi(title) → Song Name"),
        "ni" to FunctionDoc("ni", "Notification info", listOf("property"), "ni(count) → 3"),
        "if" to FunctionDoc("if", "Conditional", listOf("condition","true","false"), "if(a>b, yes, no)"),
        "gv" to FunctionDoc("gv", "Global variable", listOf("name"), "gv(myVar)"),
        "fl" to FunctionDoc("fl", "Float math", listOf("expression"), "fl(1+2*3)"),
        "mu" to FunctionDoc("mu", "Math/unit conversion", listOf("operation","value"), "mu(round,3.7)"),
        "tc" to FunctionDoc("tc", "Text case", listOf("case","text"), "tc(up,hello)"),
        "br" to FunctionDoc("br", "Line break", emptyList(), "br()"),
        "kn" to FunctionDoc("kn", "Kustom number format", listOf("format","number"), "kn(0.0,3.14)"),
        "dn" to FunctionDoc("dn", "Day name", listOf("offset"), "dn(0) → Monday"),
        "np" to FunctionDoc("np", "Network provider", emptyList(), "np()"),
        "wp" to FunctionDoc("wp", "Wifi password helper", emptyList(), "wp()"),
        "ai" to FunctionDoc("ai", "Air quality info", listOf("property"), "ai(aqi)"),
        "cm" to FunctionDoc("cm", "Color manipulation", listOf("op","color","amount"), "cm(lighten,#fff,20)"),
        "rm" to FunctionDoc("rm", "Random / rotate", listOf("min","max"), "rm(1,100)")
    )

    fun tokenize(formula: String): List<FormulaToken> {
        val tokens = mutableListOf<FormulaToken>()
        var i = 0
        while (i < formula.length) {
            when {
                formula[i] == '$' -> {
                    tokens.add(FormulaToken(TokenType.FORMULA_START, "$", i, i + 1))
                    i++
                }
                i > 0 && formula[i - 1] == '$' && formula[i].isLetter() -> {
                    val start = i
                    while (i < formula.length && formula[i].isLetterOrDigit()) i++
                    tokens.add(FormulaToken(TokenType.FUNCTION_NAME, formula.substring(start, i), start, i))
                }
                formula[i] == '(' -> { tokens.add(FormulaToken(TokenType.OPEN_PAREN, "(", i, i+1)); i++ }
                formula[i] == ')' -> { tokens.add(FormulaToken(TokenType.CLOSE_PAREN, ")", i, i+1)); i++ }
                formula[i] == ',' -> { tokens.add(FormulaToken(TokenType.ARG_SEPARATOR, ",", i, i+1)); i++ }
                formula[i] == '"' || formula[i] == '\'' -> {
                    val q = formula[i]; val start = i++
                    while (i < formula.length && formula[i] != q) i++
                    val s = formula.substring(start, minOf(i+1, formula.length))
                    tokens.add(FormulaToken(TokenType.STRING_LITERAL, s, start, i+1)); i++
                }
                formula[i].isDigit() || (formula[i] == '-' && i+1 < formula.length && formula[i+1].isDigit()) -> {
                    val start = i
                    if (formula[i] == '-') i++
                    while (i < formula.length && (formula[i].isDigit() || formula[i] == '.')) i++
                    tokens.add(FormulaToken(TokenType.NUMBER, formula.substring(start, i), start, i))
                }
                formula[i].isLetter() -> {
                    val start = i
                    while (i < formula.length && formula[i].isLetterOrDigit()) i++
                    tokens.add(FormulaToken(TokenType.IDENTIFIER, formula.substring(start, i), start, i))
                }
                formula[i] in "+-*/><=" -> { tokens.add(FormulaToken(TokenType.OPERATOR, formula[i].toString(), i, i+1)); i++ }
                formula[i] == ' ' -> { i++ }
                else -> { tokens.add(FormulaToken(TokenType.UNKNOWN, formula[i].toString(), i, i+1)); i++ }
            }
        }
        return tokens
    }

    fun evaluate(input: String, globals: Map<String, String> = emptyMap()): FormulaResult {
        val formulaRegex = Regex("""\$([a-z]+)\(([^)]*)\)\$""")
        val tokens = tokenize(input)
        var resolved = input
        var error: String? = null

        try {
            resolved = formulaRegex.replace(input) { match ->
                val fn = match.groupValues[1]
                val args = match.groupValues[2].split(",").map { it.trim() }
                evalFunction(fn, args, globals)
            }
        } catch (e: Exception) {
            error = e.message
        }

        return FormulaResult(input, resolved, tokens, error)
    }

    private fun evalFunction(fn: String, args: List<String>, globals: Map<String, String>): String {
        val cal = Calendar.getInstance()
        return when (fn) {
            "df" -> {
                val fmt = args.firstOrNull() ?: "HH:mm"
                try { SimpleDateFormat(fmt, Locale.getDefault()).format(cal.time) } catch (e: Exception) { fmt }
            }
            "bi" -> when (args.firstOrNull()) {
                "level" -> "87"
                "status" -> "Charging"
                "acstatus" -> "1"
                else -> "87"
            }
            "wi" -> when (args.firstOrNull()) {
                "temp" -> "22"
                "tempf" -> "72"
                "cond" -> "Clear"
                "hum" -> "55"
                "wind" -> "12"
                "loc" -> "City"
                else -> "N/A"
            }
            "si" -> when (args.firstOrNull()) {
                "model" -> android.os.Build.MODEL
                "brand" -> android.os.Build.BRAND
                "android" -> android.os.Build.VERSION.RELEASE
                "serial" -> "N/A"
                else -> android.os.Build.MODEL
            }
            "mi" -> when (args.firstOrNull()) {
                "title" -> "Nothing playing"
                "artist" -> "Unknown"
                "album" -> "Unknown"
                "state" -> "stopped"
                else -> "N/A"
            }
            "gv" -> globals[args.firstOrNull() ?: ""] ?: args.firstOrNull() ?: ""
            "fl" -> {
                try {
                    val expr = args.firstOrNull() ?: "0"
                    evalMathExpr(expr).toString()
                } catch (e: Exception) { "0" }
            }
            "mu" -> {
                val op = args.firstOrNull() ?: "round"
                val v = args.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                when (op) {
                    "round" -> kotlin.math.round(v).toInt().toString()
                    "floor" -> floor(v).toInt().toString()
                    "ceil" -> ceil(v).toInt().toString()
                    "abs" -> abs(v).toString()
                    "sqrt" -> sqrt(v).toString()
                    "sin" -> sin(Math.toRadians(v)).toString()
                    "cos" -> cos(Math.toRadians(v)).toString()
                    "tan" -> tan(Math.toRadians(v)).toString()
                    "km2mi" -> (v * 0.621371).toString()
                    "mi2km" -> (v * 1.60934).toString()
                    "c2f" -> (v * 9/5 + 32).toString()
                    "f2c" -> ((v - 32) * 5/9).toString()
                    else -> v.toString()
                }
            }
            "tc" -> {
                val op = args.firstOrNull() ?: "up"
                val text = args.getOrNull(1) ?: ""
                when (op) {
                    "up", "upper" -> text.uppercase()
                    "low", "lower" -> text.lowercase()
                    "cap", "title" -> text.split(" ").joinToString(" ") { w ->
                        w.replaceFirstChar { it.uppercase() }
                    }
                    else -> text
                }
            }
            "if" -> {
                val cond = args.firstOrNull() ?: "false"
                val trueVal = args.getOrNull(1) ?: ""
                val falseVal = args.getOrNull(2) ?: ""
                if (evalCondition(cond)) trueVal else falseVal
            }
            "br" -> "\n"
            "ni" -> when (args.firstOrNull()) {
                "count" -> "0"
                "title" -> ""
                else -> "0"
            }
            "rm" -> {
                val min = args.firstOrNull()?.toIntOrNull() ?: 0
                val max = args.getOrNull(1)?.toIntOrNull() ?: 100
                (min + (Math.random() * (max - min)).toInt()).toString()
            }
            "kn" -> {
                val fmt = args.firstOrNull() ?: "0"
                val num = args.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                try { String.format(fmt.replace("0", "%.0f").replace("#","%.0f"), num) } catch (e: Exception) { num.toString() }
            }
            "dn" -> {
                val offset = args.firstOrNull()?.toIntOrNull() ?: 0
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, offset)
                SimpleDateFormat("EEEE", Locale.getDefault()).format(c.time)
            }
            else -> match(fn, args)
        }
    }

    private fun evalMathExpr(expr: String): Double {
        // Simple math eval: handles +, -, *, /
        return try {
            val cleaned = expr.replace(" ", "")
            when {
                cleaned.contains("+") -> {
                    val parts = cleaned.split("+")
                    parts.sumOf { evalMathExpr(it) }
                }
                cleaned.contains("-") && cleaned.indexOf("-") > 0 -> {
                    val idx = cleaned.lastIndexOf("-")
                    evalMathExpr(cleaned.substring(0, idx)) - evalMathExpr(cleaned.substring(idx+1))
                }
                cleaned.contains("*") -> {
                    val parts = cleaned.split("*")
                    parts.fold(1.0) { acc, s -> acc * evalMathExpr(s) }
                }
                cleaned.contains("/") -> {
                    val parts = cleaned.split("/")
                    val first = evalMathExpr(parts[0])
                    val second = evalMathExpr(parts[1])
                    if (second == 0.0) 0.0 else first / second
                }
                else -> cleaned.toDouble()
            }
        } catch (e: Exception) { 0.0 }
    }

    private fun evalCondition(cond: String): Boolean {
        return when {
            cond.contains(">=") -> {
                val parts = cond.split(">=")
                (parts[0].trim().toDoubleOrNull() ?: 0.0) >= (parts[1].trim().toDoubleOrNull() ?: 0.0)
            }
            cond.contains("<=") -> {
                val parts = cond.split("<=")
                (parts[0].trim().toDoubleOrNull() ?: 0.0) <= (parts[1].trim().toDoubleOrNull() ?: 0.0)
            }
            cond.contains(">") -> {
                val parts = cond.split(">")
                (parts[0].trim().toDoubleOrNull() ?: 0.0) > (parts[1].trim().toDoubleOrNull() ?: 0.0)
            }
            cond.contains("<") -> {
                val parts = cond.split("<")
                (parts[0].trim().toDoubleOrNull() ?: 0.0) < (parts[1].trim().toDoubleOrNull() ?: 0.0)
            }
            cond.contains("!=") -> {
                val parts = cond.split("!=")
                parts[0].trim() != parts[1].trim()
            }
            cond.contains("=") -> {
                val parts = cond.split("=")
                parts[0].trim() == parts[1].trim()
            }
            else -> cond.trim().lowercase() == "true" || cond.trim() == "1"
        }
    }

    private fun match(fn: String, args: List<String>): String = args.firstOrNull() ?: fn

    fun highlight(formula: String): List<HighlightSpan> {
        val spans = mutableListOf<HighlightSpan>()
        val tokens = tokenize(formula)
        tokens.forEach { token ->
            val color = when (token.type) {
                TokenType.FUNCTION_NAME -> SpanColor.PURPLE
                TokenType.STRING_LITERAL -> SpanColor.GREEN
                TokenType.NUMBER -> SpanColor.AMBER
                TokenType.OPERATOR -> SpanColor.BLUE
                TokenType.IDENTIFIER -> SpanColor.TEAL
                TokenType.FORMULA_START, TokenType.FORMULA_END -> SpanColor.PINK
                else -> SpanColor.DEFAULT
            }
            spans.add(HighlightSpan(token.start, token.end, color, token.type))
        }
        return spans
    }
}

data class HighlightSpan(val start: Int, val end: Int, val color: SpanColor, val type: TokenType)
enum class SpanColor { PURPLE, GREEN, AMBER, BLUE, TEAL, PINK, DEFAULT }
data class FunctionDoc(val name: String, val description: String, val args: List<String>, val example: String)
