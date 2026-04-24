package snd.komelia.transcription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val CHUNK_THRESHOLD_MS = 5_000L

class WhisperTranscriptionBackend(
    private val store: TranscriptStore,
    private val modelPath: String,
    private val language: String?,
    scope: CoroutineScope,
) : TranscriptionBackend {

    private val _state = MutableStateFlow<TranscriptEngineState>(TranscriptEngineState.Idle)
    override val state: StateFlow<TranscriptEngineState> = _state

    private val innerScope = CoroutineScope(scope.coroutineContext + SupervisorJob())
    private var nativeCtx = 0L

    private val pcmBuffer = mutableListOf<Short>()
    private var bufferStartMs = 0L
    private var bufferDurationMs = 0L
    private val mutex = Mutex()

    override suspend fun start() {
        nativeCtx = WhisperJni.loadModel(modelPath)
        if (nativeCtx == 0L) {
            _state.value = TranscriptEngineState.Error("Failed to load Whisper model: $modelPath")
            return
        }
        _state.value = TranscriptEngineState.Active()
    }

    override suspend fun onPcmChunk(bytes: ByteArray, bookTimeMs: Long, durationMs: Long) {
        if (nativeCtx == 0L) return
        val shouldRunInference = mutex.withLock {
            val shorts = ShortArray(bytes.size / 2) { i ->
                (bytes[i * 2].toInt() and 0xFF or (bytes[i * 2 + 1].toInt() shl 8)).toShort()
            }
            if (pcmBuffer.isEmpty()) bufferStartMs = bookTimeMs
            pcmBuffer.addAll(shorts.toList())
            bufferDurationMs += durationMs
            bufferDurationMs >= CHUNK_THRESHOLD_MS
        }
        if (shouldRunInference) {
            runInference()
        }
    }

    private suspend fun runInference() {
        val (floats, offsetMs) = mutex.withLock {
            if (pcmBuffer.isEmpty()) return
            val f = FloatArray(pcmBuffer.size) { i -> pcmBuffer[i] / 32768f }
            val o = bufferStartMs
            pcmBuffer.clear()
            bufferStartMs += bufferDurationMs
            bufferDurationMs = 0L
            f to o
        }

        // Native call is outside the mutex to avoid blocking the audio pipeline
        val results = WhisperJni.transcribeChunk(nativeCtx, floats, offsetMs, language)

        val segments = results.map { r ->
            TranscriptSegment(
                id = store.nextId(),
                startMs = r.startMs,
                endMs = r.endMs,
                text = r.text.trim(),
                isFinal = true,
            )
        }.filter { it.text.isNotBlank() }

        store.addSegments(segments)
    }

    override fun onSeek(newPositionMs: Long) {
        innerScope.launch {
            mutex.withLock {
                pcmBuffer.clear()
                bufferStartMs = newPositionMs
                bufferDurationMs = 0L
            }
        }
    }

    override fun stop() {
        innerScope.launch {
            mutex.withLock {
                pcmBuffer.clear()
                bufferDurationMs = 0L
            }
        }
        innerScope.cancel()
        if (nativeCtx != 0L) {
            WhisperJni.freeContext(nativeCtx)
            nativeCtx = 0L
        }
        _state.value = TranscriptEngineState.Idle
    }
}
