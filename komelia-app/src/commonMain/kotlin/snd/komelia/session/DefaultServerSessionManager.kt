package snd.komelia.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import snd.komelia.AppModule
import snd.komelia.db.GlobalDatabase
import snd.komelia.db.settings.ExposedServerProfileRepository
import snd.komelia.settings.model.ServerProfile
import snd.komelia.ui.DependencyContainer
import snd.komelia.ui.session.ServerSessionManager
import java.io.File

class DefaultServerSessionManager(
    private val globalDatabaseDir: String,
    private val appDatabaseDir: String,
    private val cacheDir: String,
    private val appModuleFactory: (serverId: Long?) -> AppModule,
) : ServerSessionManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val globalDatabase = GlobalDatabase(globalDatabaseDir)
    private val serverProfileRepository = ExposedServerProfileRepository(globalDatabase.database)
    private var currentModule: AppModule? = null

    private val _dependencies = MutableStateFlow<DependencyContainer?>(null)
    override val dependencies: StateFlow<DependencyContainer?> = _dependencies.asStateFlow()

    private val _currentServerProfile = MutableStateFlow<ServerProfile?>(null)
    override val currentServerProfile: StateFlow<ServerProfile?> = _currentServerProfile.asStateFlow()

    private val _serverProfiles = MutableStateFlow<List<ServerProfile>>(emptyList())
    override val serverProfiles: StateFlow<List<ServerProfile>> = _serverProfiles.asStateFlow()

    init {
        scope.launch { refreshServerProfiles() }
    }

    private suspend fun refreshServerProfiles() {
        _serverProfiles.value = serverProfileRepository.getAll()
    }

    fun loadLastActiveServer() {
        scope.launch {
            val profiles = serverProfileRepository.getAll()
            val lastActive = profiles.maxByOrNull { it.lastActive ?: kotlinx.datetime.Instant.DISTANT_PAST }
            if (lastActive != null) {
                switchServer(lastActive)
            } else {
                // No servers, load empty app module for login
                switchServer(null)
            }
        }
    }

    override fun switchServer(profile: ServerProfile?) {
        scope.launch {
            _dependencies.value = null
            currentModule?.close()
            val module = appModuleFactory(profile?.id)
            currentModule = module
            val container = module.initDependencies()
            _dependencies.value = container
            _currentServerProfile.value = profile

            if (profile != null) {
                val updatedProfile = profile.copy(lastActive = kotlinx.datetime.Clock.System.now())
                serverProfileRepository.update(updatedProfile)
                refreshServerProfiles()
            }
        }
    }

    override suspend fun addServer(name: String, url: String, username: String) {
        val newProfile = ServerProfile(
            name = name,
            url = url,
            username = username,
            lastActive = kotlinx.datetime.Clock.System.now()
        )
        val inserted = serverProfileRepository.insert(newProfile)
        refreshServerProfiles()
        switchServer(inserted)
    }

    override suspend fun deleteServer(profile: ServerProfile) {
        serverProfileRepository.delete(profile.id)

        File(appDatabaseDir, "server_${profile.id}_komelia.sqlite").delete()
        File(appDatabaseDir, "server_${profile.id}_komelia.sqlite-wal").delete()
        File(appDatabaseDir, "server_${profile.id}_komelia.sqlite-shm").delete()
        File(appDatabaseDir, "server_${profile.id}_offline.sqlite").delete()
        File(appDatabaseDir, "server_${profile.id}_offline.sqlite-wal").delete()
        File(appDatabaseDir, "server_${profile.id}_offline.sqlite-shm").delete()

        File(cacheDir, "okhttp/server_${profile.id}").deleteRecursively()
        File(cacheDir, "coil3_disk_cache/server_${profile.id}").deleteRecursively()
        File(cacheDir, "komelia_reader_cache/server_${profile.id}").deleteRecursively()

        refreshServerProfiles()
        if (_currentServerProfile.value?.id == profile.id) {
            loadLastActiveServer()
        }
    }
}
