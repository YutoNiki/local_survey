package com.example.local_survey

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object ContextUtils {
    fun updateLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}