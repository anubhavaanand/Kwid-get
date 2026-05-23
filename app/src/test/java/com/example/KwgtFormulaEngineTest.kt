package com.example

import com.example.parser.KwgtFormulaEngine
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KwgtFormulaEngineTest {

    private lateinit var engine: KwgtFormulaEngine

    @Before
    fun setup() {
        engine = KwgtFormulaEngine()
    }

    @Test
    fun testBasicMath() {
        // float math function
        val result = engine.evaluate("\$fl(1+2*3)\$")
        assertEquals("7.0", result.resolved)
    }

    @Test
    fun testTextCase() {
        // text case function
        val result = engine.evaluate("\$tc(up, hello)\$")
        assertEquals("HELLO", result.resolved)
    }

    @Test
    fun testConditional() {
        val result = engine.evaluate("\$if(5>3, yes, no)\$")
        assertEquals("yes", result.resolved)
    }

    @Test
    fun testGlobals() {
        val globals = mapOf("color" to "#FFCA28", "text" to "Hello World")
        val result = engine.evaluate("\$gv(color)\$", globals)
        assertEquals("#FFCA28", result.resolved)
    }
}
