package com.musclegain.tracker

import com.musclegain.tracker.ui.navigation.Screen
import com.musclegain.tracker.ui.navigation.bottomNavItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {

    @Test
    fun `four main tabs are configured in bottom navigation`() {
        assertEquals(4, bottomNavItems.size)
        assertEquals(Screen.Today, bottomNavItems[0].screen)
        assertEquals(Screen.History, bottomNavItems[1].screen)
        assertEquals(Screen.Weekly, bottomNavItems[2].screen)
        assertEquals(Screen.Profile, bottomNavItems[3].screen)
    }

    @Test
    fun `route parameter generation formats properly`() {
        val foodRoute = Screen.FoodEntry.createRoute("2026-11-21", 101L)
        assertEquals("food_entry?date=2026-11-21&id=101", foodRoute)

        val workoutRoute = Screen.WorkoutEntry.createRoute("2026-11-21", 202L)
        assertEquals("workout_entry?date=2026-11-21&id=202", workoutRoute)

        val supplementRoute = Screen.SupplementLog.createRoute("2026-11-21")
        assertEquals("supplement_log?date=2026-11-21", supplementRoute)
    }
}
