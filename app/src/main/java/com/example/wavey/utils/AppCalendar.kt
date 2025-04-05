package com.example.wavey.utils

import android.os.Bundle
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wavey.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppCalendar : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)
        createCalendar()
    }

    fun createCalendar() {
        calendarView = findViewById(R.id.calendarView)
        calendar = Calendar.getInstance()

        // Set initial date
        setDate(3, 1, 2023)

        calendarView.setOnDateChangeListener(object : CalendarView.OnDateChangeListener {
            override fun onSelectedDayChange(calendarView: CalendarView, year: Int, month: Int, day: Int) {
                // Note: month is 0-indexed, so add 1 for actual month number
                Toast.makeText(this@AppCalendar, "$day/${month + 1}/$year", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setDate(day: Int, month: Int, year: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Adjust for 0-indexing
        calendar.set(Calendar.DAY_OF_MONTH, day)

        val milli = calendar.timeInMillis
        calendarView.date = milli
    }

    private fun getDate() {
        val date = calendarView.date
        val simpleDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        calendar.timeInMillis = date

        val selectedDate = simpleDateFormat.format(calendar.time)
        Toast.makeText(this, selectedDate, Toast.LENGTH_SHORT).show()
    }
}