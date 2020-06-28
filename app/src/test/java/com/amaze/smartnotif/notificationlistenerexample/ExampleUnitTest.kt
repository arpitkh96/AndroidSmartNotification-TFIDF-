package com.amaze.smartnotif.notificationlistenerexample

import org.junit.Assert
import org.junit.Test
import java.lang.StringBuilder

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class ExampleUnitTest {
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2.toLong())
    }
    private fun cleanEmoji(string:String,prefix:String,suffix:String):String{
        //"(([\uD800-\uDBFF]|[\u2702-\u27B0]|[\uF680-\uF6C0]|[\u24C2-\uF251])+)"
        val out=StringBuilder()
        for(c in string){
            if (!c.isSurrogate())
            out.append(c)
        }
        return out.toString()//.replace(Regex("(?:[\\u2700-\\u27bf]|(?:\\ud83c[\\udde6-\\uddff]){2}|[\\ud800-\\udbff][\\udc00-\\udfff])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|\\ud83c[\\udffb-\\udfff])?(?:\\u200d(?:[^\\ud800-\\udfff]|(?:\\ud83c[\\udde6-\\uddff]){2}|[\\ud800-\\udbff][\\udc00-\\udfff])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|\\ud83c[\\udffb-\\udfff])?)*"), "");
    }
    @Test
    fun emojiTest() {
        println(cleanEmoji("\uD83D\uDCAC","[emoji","]"))
    }
}