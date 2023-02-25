/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp.sampleservice

import java.util.ArrayList
import java.util.HashMap

class Data(thing: String) {
    private val things: MutableList<String> = ArrayList()
    private val thingsMap = HashMap<String, Int>()

    init {
        var i = 10
        while (i > 0) {
            things.add(thing + "_" + i)
            thingsMap[thing + "_key_" + i] = i
            i--
        }
    }
}