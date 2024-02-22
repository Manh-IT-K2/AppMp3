import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import java.io.File

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @Provides
    @ServiceScoped
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA).build()

    @Provides
    @ServiceScoped
    fun provideExoPlayer (
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer {
        val player = SimpleExoPlayer.Builder(context).build()
        player.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
        player.setHandleAudioBecomingNoisy(true)
        return player
    }


    @Provides
    @ServiceScoped
    fun provideDataSourceFactory(
        @ApplicationContext context: Context
    ) = DefaultHttpDataSource.Factory()

    @Provides
    @ServiceScoped
    fun provideCacheDataSourceFactory(
        @ApplicationContext context: Context,
        dataSource: DefaultDataSourceFactory
    ):CacheDataSource.Factory{
        val cacheDir = File(context.cacheDir, "media")

        val databaseProvider = ExoDatabaseProvider(context)

        val cache = SimpleCache(cacheDir, NoOpCacheEvictor(), databaseProvider)
        return CacheDataSource.Factory().apply{
            setCache(cache)
            setUpstreamDataSourceFactory(dataSource)
        }
    }
}