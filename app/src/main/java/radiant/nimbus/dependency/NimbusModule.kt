package radiant.nimbus.dependency


import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class NimbusModule {
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context) = EncryptedSharedPreferences.create(
        context,
        "user-data",
        MasterKey(context = context, keyScheme = MasterKey.KeyScheme.AES256_GCM),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Provides
    fun providesGson(): Gson = GsonBuilder().create()
}
