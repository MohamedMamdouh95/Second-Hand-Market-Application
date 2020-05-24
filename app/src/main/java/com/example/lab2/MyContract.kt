package com.example.lab2

//https://stackoverflow.com/questions/18579590/how-to-send-data-from-dialogfragment-to-a-fragment
//this is interface is based on this solution
interface MyContract {
    fun searchCriteria(minValue: Int, maxValue: Int, location: String, category: String)
}