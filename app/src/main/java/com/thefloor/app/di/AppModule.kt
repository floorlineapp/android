package com.thefloor.app.di

import com.thefloor.app.core.analytics.AnalyticsTracker
import com.thefloor.app.core.analytics.LoggingAnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun analyticsTracker(impl: LoggingAnalyticsTracker): AnalyticsTracker
}
