package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.util.Stack

private var exprBuffer = ""

class MainActivity : AppCompatActivity() {
    private var postfixExpr: String = ""
    private var position = 0
    private val priorityMap: Map<Char, Int> = mapOf(
        '(' to 0,
        '+' to 1,
        '-' to 1,
        '×' to 2,
        '÷' to 2,
        '~' to 3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val expr = findViewById<TextView>(R.id.expression)
        val btnResult = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonResult)
        val btnReset = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonReset)
        val btnBack = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonBackspace)
        val buttonPercent = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonPercent)
        val btnDot = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonDot)

        val operators = arrayOf(
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonPlus),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonMinus),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonDividing),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonMultiplication)
        )

        val numbers = arrayOf(
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber0),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber1),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber2),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber3),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber4),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber5),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber6),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber7),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber8),
            findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.buttonNumber9)
        )

        btnReset.setOnClickListener{
            exprBuffer = ""
            expr.text = "0"
        }

        btnBack.setOnClickListener {
            if(exprBuffer.length > 1) {
                exprBuffer = exprBuffer.dropLast(1)
                expr.text = exprBuffer
            } else {
                exprBuffer = ""
                expr.text = "0"
            }
        }

        btnResult.setOnClickListener {
            checkPreviousOperator()
            if(exprBuffer.isNotEmpty()) {
                calculate()
                if(exprBuffer == "Error") {
                    expr.text = exprBuffer
                    exprBuffer = ""
                } else
                    expr.text = exprBuffer
            }
        }

        btnDot.setOnClickListener {
            position = exprBuffer.length - 1
            var flag = false
            if(exprBuffer.isEmpty() || exprBuffer.takeLast(1).matches(Regex("[+\\-÷×]"))) {
                exprBuffer += "0."
                expr.text = exprBuffer
            } else {
                while (position >= 0 && (exprBuffer[position].isDigit() || exprBuffer[position] == '.')) {
                    if (exprBuffer[position] == '.') {
                        flag = true
                        break
                    }
                    position--
                }
                if (!flag) {
                    exprBuffer += btnDot.text
                    expr.text = exprBuffer
                }
            }
        }

        buttonPercent.setOnClickListener {
            if(exprBuffer.isNotEmpty() && !exprBuffer.takeLast(1).matches(Regex("[+\\-÷×]"))){
                var operator: Char = '@'
                var strNumber: String = ""
                val stack = Stack<Char>()
                val number: Double
                position = exprBuffer.length - 1
                while(position >= 0 && (exprBuffer[position].isDigit() || exprBuffer[position] == '.')) {
                    stack.push(exprBuffer[position])
                    position--
                }
                for(i in 0..<stack.size){
                    strNumber += stack.pop()
                }
                if(position == -1 || exprBuffer[position].toString().matches(Regex("[÷×]")) || (position == 0 && exprBuffer[position] == '-')) {
                    number = strNumber.toDouble() / 100
                    exprBuffer = exprBuffer.dropLast(exprBuffer.length - position - 1) + number
                    expr.text = exprBuffer
                } else if(exprBuffer[position].toString().matches(Regex("[+\\-]"))) {
                    operator = exprBuffer[position]
                    exprBuffer = exprBuffer.dropLast(exprBuffer.length - position)
                    var tempBuffer = exprBuffer
                    calculate()
                    if(exprBuffer == "Error") {
                        expr.text = exprBuffer
                        exprBuffer = ""
                    } else {
                        number = strNumber.toDouble()
                        var result = exprBuffer.toDouble() / 100 * number
                        exprBuffer = tempBuffer + operator + "$result"
                        expr.text = exprBuffer
                    }
                }
            }
        }

        for(number in numbers) {
            number.setOnClickListener{
                if (number.text != "0" || exprBuffer.isNotEmpty()) {
                    exprBuffer += number.text
                    expr.text = exprBuffer
                }
            }
        }

        for(operator in operators) {
            operator.setOnClickListener{
                if(exprBuffer.isEmpty()) {
                    exprBuffer += "0"
                }
                checkPreviousOperator()
                exprBuffer += operator.text
                expr.text = exprBuffer
            }
        }

    }

    private fun checkPreviousOperator() {
        if(exprBuffer.takeLast(1).matches(Regex("[+\\-÷×]"))){
            exprBuffer = exprBuffer.dropLast(1)
        }
    }


    private fun readNumber(buffer: String) : String {
        var number: String = ""
        while(position < buffer.length && (buffer[position].isDigit() || buffer[position] == '.')) {
            number += buffer[position]
            position++
        }
        --position
        return number
    }

    private fun execute(op: Char, first: Double, second: Double) : Double {
        return when(op) {
            '+' -> (first + second)
            '-' -> (first - second)
            '×' -> (first * second)
            '÷' -> (first / second)
            else -> Double.NaN
        }
    }

    private fun toPostfix() {
        position = 0
        val operatorsStack = Stack<Char>()
        while (position < exprBuffer.length){
            var symbol: Char = exprBuffer[position]
            if(symbol.isDigit()) {
                postfixExpr += readNumber(exprBuffer) + " "
            } else if(symbol == '(') {
                operatorsStack.push(symbol)
            } else if(symbol == ')') {
                while(operatorsStack.isNotEmpty() && operatorsStack.peek() != '(') {
                    postfixExpr += operatorsStack.pop()
                }
                operatorsStack.pop()
            } else if(priorityMap.containsKey(symbol)) {
                if (symbol == '-' && (position == 0 || (position > 1 && priorityMap.containsKey(exprBuffer[position-1]))))
                    symbol = '~';
                while(operatorsStack.isNotEmpty() && priorityMap[operatorsStack.peek()]!! >= priorityMap[symbol]!!) {
                    postfixExpr += operatorsStack.pop()
                }
                operatorsStack.push(symbol)
            }
            position++
        }
        for(c in operatorsStack) {
            postfixExpr += c
        }
    }

    private fun calculate() {
        if(exprBuffer[0] == '-') {
            exprBuffer = "0$exprBuffer"
        }
        exprBuffer = "($exprBuffer)"
        val localsStack = Stack<Double>()
        toPostfix()
        position = 0
        while(position < postfixExpr.length) {
            val symbol: Char = postfixExpr[position]

            if(symbol.isDigit()) {
                val number: String = readNumber(postfixExpr)
                localsStack.push(number.toDouble())
            } else if(priorityMap.containsKey(symbol)) {
                if (symbol == '~') {
                    val last: Double = if (localsStack.isNotEmpty()) localsStack.pop() else 0.0
                    localsStack.push(execute('-', 0.0, last))
                    continue;
                }
                var second: Double = if (localsStack.isNotEmpty()) localsStack.pop() else 0.0
                if (symbol == '÷' && second == 0.0) {
                    exprBuffer = "Error"
                    postfixExpr = ""
                    return
                }
                var first: Double = if (localsStack.isNotEmpty()) localsStack.pop() else 0.0
                localsStack.push(execute(symbol, first, second))
            }
            position++
        }
        postfixExpr = ""
        exprBuffer = "" + localsStack.pop()
    }

}