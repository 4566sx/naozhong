package com.biblealarm.app.manager

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.biblealarm.app.data.model.Psalm
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 内置音频资源管理器
 * 负责管理应用内置的诗篇音频文件
 */
@Singleton
class BuiltInAudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BuiltInAudioManager"
        private const val BUILT_IN_AUDIO_FOLDER = "psalms"
        private const val INTERNAL_AUDIO_DIR = "built_in_psalms"
        private const val AUDIO_FILE_PREFIX = "psalm_"
        private const val AUDIO_FILE_EXTENSION = ".mp3"
    }

    private val internalAudioDir: File by lazy {
        File(context.filesDir, INTERNAL_AUDIO_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * 初始化内置音频资源
     * 将assets中的音频文件复制到内部存储
     */
    suspend fun initializeBuiltInAudio(): Boolean = withContext(Dispatchers.IO) {
        try {
            val assetManager = context.assets
            val audioFiles = getBuiltInAudioFiles(assetManager)
            
            Log.d(TAG, "发现 ${audioFiles.size} 个内置音频文件")
            
            audioFiles.forEach { fileName ->
                copyAssetToInternalStorage(assetManager, fileName)
            }
            
            Log.d(TAG, "内置音频资源初始化完成")
            true
        } catch (e: Exception) {
            Log.e(TAG, "初始化内置音频资源失败", e)
            false
        }
    }

    /**
     * 获取assets中的音频文件列表
     */
    private fun getBuiltInAudioFiles(assetManager: AssetManager): List<String> {
        return try {
            assetManager.list(BUILT_IN_AUDIO_FOLDER)?.toList() ?: emptyList()
        } catch (e: IOException) {
            Log.w(TAG, "无法读取assets中的音频文件夹", e)
            emptyList()
        }
    }

    /**
     * 将asset文件复制到内部存储
     */
    private suspend fun copyAssetToInternalStorage(
        assetManager: AssetManager,
        fileName: String
    ) = withContext(Dispatchers.IO) {
        try {
            val targetFile = File(internalAudioDir, fileName)
            
            // 如果文件已存在且大小相同，跳过复制
            if (targetFile.exists()) {
                val assetSize = assetManager.open("$BUILT_IN_AUDIO_FOLDER/$fileName").use { it.available() }
                if (targetFile.length() == assetSize.toLong()) {
                    Log.d(TAG, "跳过已存在的文件: $fileName")
                    return@withContext
                }
            }
            
            assetManager.open("$BUILT_IN_AUDIO_FOLDER/$fileName").use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            Log.d(TAG, "复制音频文件: $fileName")
        } catch (e: Exception) {
            Log.e(TAG, "复制音频文件失败: $fileName", e)
        }
    }

    /**
     * 获取内置音频文件路径
     */
    fun getBuiltInAudioPath(psalmNumber: Int): String? {
        val fileName = "${AUDIO_FILE_PREFIX}${psalmNumber}${AUDIO_FILE_EXTENSION}"
        val file = File(internalAudioDir, fileName)
        
        return if (file.exists()) {
            file.absolutePath
        } else {
            Log.w(TAG, "内置音频文件不存在: $fileName")
            null
        }
    }

    /**
     * 检查诗篇是否有内置音频
     */
    fun hasBuiltInAudio(psalmNumber: Int): Boolean {
        return getBuiltInAudioPath(psalmNumber) != null
    }

    /**
     * 获取所有可用的内置音频诗篇编号
     */
    fun getAvailableBuiltInPsalms(): List<Int> {
        return internalAudioDir.listFiles()
            ?.mapNotNull { file ->
                extractPsalmNumberFromFileName(file.name)
            }
            ?.sorted()
            ?: emptyList()
    }

    /**
     * 从文件名中提取诗篇编号
     */
    private fun extractPsalmNumberFromFileName(fileName: String): Int? {
        return try {
            val regex = "${AUDIO_FILE_PREFIX}(\\d+)${AUDIO_FILE_EXTENSION}".toRegex()
            val matchResult = regex.find(fileName)
            matchResult?.groupValues?.get(1)?.toInt()
        } catch (e: Exception) {
            Log.w(TAG, "无法从文件名提取诗篇编号: $fileName", e)
            null
        }
    }

    /**
     * 获取内置音频文件大小（MB）
     */
    fun getBuiltInAudioSize(): Double {
        val totalSize = internalAudioDir.listFiles()
            ?.sumOf { it.length() }
            ?: 0L
        
        return totalSize / (1024.0 * 1024.0)
    }

    /**
     * 清理内置音频缓存
     */
    suspend fun clearBuiltInAudioCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            internalAudioDir.deleteRecursively()
            internalAudioDir.mkdirs()
            Log.d(TAG, "内置音频缓存已清理")
            true
        } catch (e: Exception) {
            Log.e(TAG, "清理内置音频缓存失败", e)
            false
        }
    }

    /**
     * 获取可用的内置音频列表
     */
    fun getAvailableBuiltInAudio(): List<BuiltInAudioInfo> {
        return try {
            val manifestJson = context.assets.open("psalms/audio_manifest.json").bufferedReader().use { it.readText() }
            val gson = com.google.gson.Gson()
            val manifest = gson.fromJson(manifestJson, AudioManifest::class.java)
            
            manifest.available_audio.map { audioItem ->
                BuiltInAudioInfo(
                    psalmNumber = audioItem.psalm_number,
                    fileName = audioItem.file_name,
                    assetPath = "psalms/${audioItem.file_name}",
                    title = audioItem.title,
                    durationMs = audioItem.duration_ms,
                    fileSizeKb = audioItem.file_size_kb
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取内置音频清单失败", e)
            // 返回完整的1-150篇音频列表
            getFullBuiltInAudio()
        }
    }
    
    /**
     * 获取完整内置音频列表（1-150篇）
     */
    private fun getFullBuiltInAudio(): List<BuiltInAudioInfo> {
        return (1..150).map { number ->
            BuiltInAudioInfo(
                psalmNumber = number,
                fileName = "$number.mp3",
                assetPath = "psalms/$number.mp3",
                title = Psalm.psalmTitles[number] ?: "诗篇第${number}篇",
                durationMs = 0,
                fileSizeKb = 0
            )
        }
    }

    /**
     * 预加载指定诗篇的音频（用于提升播放性能）
     */
    suspend fun preloadAudio(psalmNumbers: List<Int>) = withContext(Dispatchers.IO) {
        psalmNumbers.forEach { psalmNumber ->
            val audioPath = getBuiltInAudioPath(psalmNumber)
            if (audioPath != null) {
                // 这里可以实现音频预加载逻辑
                Log.d(TAG, "预加载音频: 诗篇 $psalmNumber")
            }
        }
    }

    /**
     * 获取音频文件信息
     */
    fun getAudioFileInfo(psalmNumber: Int): AudioFileInfo? {
        val audioPath = getBuiltInAudioPath(psalmNumber) ?: return null
        val file = File(audioPath)
        
        return if (file.exists()) {
            AudioFileInfo(
                psalmNumber = psalmNumber,
                filePath = audioPath,
                fileSize = file.length(),
                lastModified = file.lastModified(),
                isBuiltIn = true
            )
        } else {
            null
        }
    }
}

/**
 * 音频文件信息数据类
 */
data class AudioFileInfo(
    val psalmNumber: Int,
    val filePath: String,
    val fileSize: Long,
    val lastModified: Long,
    val isBuiltIn: Boolean
)

/**
 * 内置音频信息数据类
 */
data class BuiltInAudioInfo(
    val psalmNumber: Int,
    val fileName: String,
    val assetPath: String,
    val title: String,
    val durationMs: Long = 0,
    val fileSizeKb: Int = 0
)

/**
 * 音频清单数据类
 */
data class AudioManifest(
    val version: String,
    val description: String,
    val total_count: Int,
    val available_audio: List<AudioItem>
)

/**
 * 音频项数据类
 */
data class AudioItem(
    val psalm_number: Int,
    val file_name: String,
    val title: String,
    val duration_ms: Long,
    val file_size_kb: Int
)

/**
 * 获取可用的内置音频数量
 */
suspend fun getAvailableBuiltInAudioCount(): Int = withContext(Dispatchers.IO) {
    return@withContext getAvailableBuiltInAudio().size
}
