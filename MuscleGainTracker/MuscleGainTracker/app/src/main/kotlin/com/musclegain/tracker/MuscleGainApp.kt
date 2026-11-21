package com.musclegain.tracker

import android.app.Application
import com.musclegain.tracker.core.data.MuscleGainContainer

class MuscleGainApp : Application() {
    lateinit var container: MuscleGainContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = MuscleGainContainer(this)
    }
}
