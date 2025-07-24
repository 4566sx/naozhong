package com.biblealarm.app.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.biblealarm.app.data.database.dao.PsalmDao
import com.biblealarm.app.data.model.Psalm
import com.biblealarm.app.manager.BuiltInAudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 音频资源管理仓库 - 支持内置音频和用户自定义音频
 */
@Singleton
class AudioRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val psalmDao: PsalmDao,
    private val builtInAudioManager: BuiltInAudioManager
) {
    
    companion object {
        private const val DEFAULT_AUDIO_FOLDER = "BibleAlarm/Audio"
        private val SUPPORTED_AUDIO_FORMATS = listOf("mp3", "wav", "m4a", "aac", "ogg")
    }
    
    /**
     * 获取所有诗篇
     */
    fun getAllPsalms(): Flow<List<Psalm>> = psalmDao.getAllPsalms()
    
    /**
     * 获取可用诗篇（优先使用用户自定义音频，其次使用内置音频）
     */
    fun getAvailablePsalms(): Flow<List<Psalm>> = psalmDao.getAvailablePsalms()
    
    /**
     * 获取随机诗篇
     */
    suspend fun getRandomPsalm(): Psalm? = psalmDao.getRandomPsalm()
    
    /**
     * 根据编号获取诗篇（智能选择音频源）
     */
    suspend fun getPsalmByNumber(number: Int): Psalm? {
        val psalm = psalmDao.getPsalmByNumber(number)
        return psalm?.let { enhancePsalmWithAudio(it) }
    }
    
    /**
     * 增强诗篇对象，智能选择最佳音频源
     */
    private suspend fun enhancePsalmWithAudio(psalm: Psalm): Psalm {
        // 1. 优先使用用户自定义音频（如果存在且可用）
        if (psalm.audioFilePath.isNotEmpty() && File(psalm.audioFilePath).exists()) {
            return psalm.copy(isAvailable = true)
        }
        
        // 2. 尝试使用内置音频
        val builtInAudioPath = builtInAudioManager.getBuiltInAudioPath(psalm.number)
        if (builtInAudioPath != null) {
            val duration = getAudioDurationFromAssets(builtInAudioPath)
            return psalm.copy(
                audioFilePath = builtInAudioPath,
                audioFileName = "psalm_${psalm.number}.mp3",
                duration = duration,
                isAvailable = true
            )
        }
        
        // 3. 都不可用
        return psalm.copy(isAvailable = false)
    }
    
    /**
     * 初始化诗篇数据库
     */
    suspend fun initializePsalms() {
        withContext(Dispatchers.IO) {
            val existingCount = psalmDao.getTotalPsalmCount()
            if (existingCount == 0) {
                // 插入默认诗篇数据
                val defaultPsalms = Psalm.createDefaultPsalms()
                psalmDao.insertPsalms(defaultPsalms)
            }
            
            // 初始化内置音频
            initializeBuiltInAudio()
        }
    }
    
    /**
     * 初始化内置音频
     */
    private suspend fun initializeBuiltInAudio() {
        val builtInAudioList = builtInAudioManager.getAvailableBuiltInAudio()
        
        for (audioInfo in builtInAudioList) {
            val psalm = psalmDao.getPsalmByNumber(audioInfo.psalmNumber)
            if (psalm != null && psalm.audioFilePath.isEmpty()) {
                // 只有当用户没有自定义音频时，才使用内置音频
                val duration = getAudioDurationFromAssets(audioInfo.assetPath)
                val updatedPsalm = psalm.copy(
                    audioFilePath = audioInfo.assetPath,
                    audioFileName = audioInfo.fileName,
                    duration = duration,
                    isAvailable = true
                )
                psalmDao.updatePsalm(updatedPsalm)
            }
        }
    }
    
    /**
     * 扫描用户自定义音频文件夹
     */
    suspend fun scanUserAudioFolder(folderPath: String? = null): ScanResult {
        return withContext(Dispatchers.IO) {
            val targetFolder = folderPath ?: getDefaultAudioFolder()
            val folder = File(targetFolder)
            
            if (!folder.exists() || !folder.isDirectory) {
                return@withContext ScanResult(
                    success = false,
                    message = "音频文件夹不存在: $targetFolder",
                    foundFiles = 0,
                    updatedPsalms = 0,
                    builtInUsed = 0
                )
            }
            
            val audioFiles = findAudioFiles(folder)
            var updatedCount = 0
            var builtInUsedCount = 0
            
            // 首先处理用户自定义音频
            for (audioFile in audioFiles) {
                val psalmNumber = extractPsalmNumber(audioFile.name)
                if (psalmNumber != null && psalmNumber in 1..150) {
                    val psalm = psalmDao.getPsalmByNumber(psalmNumber)
                    if (psalm != null) {
                        val duration = getAudioDuration(audioFile.absolutePath)
                        val updatedPsalm = psalm.copy(
                            audioFilePath = audioFile.absolutePath,
                            audioFileName = audioFile.name,
                            duration = duration,
                            isAvailable = true
                        )
                        psalmDao.updatePsalm(updatedPsalm)
                        updatedCount++
                    }
                }
            }
            
            // 然后为没有用户音频的诗篇使用内置音频
            val allPsalms = psalmDao.getAllPsalmsSync()
            for (psalm in allPsalms) {
                if (!psalm.isAvailable || psalm.audioFilePath.isEmpty()) {
                    val builtInPath = builtInAudioManager.getBuiltInAudioPath(psalm.number)
                    if (builtInPath != null) {
                        val duration = getAudioDurationFromAssets(builtInPath)
                        val updatedPsalm = psalm.copy(
                            audioFilePath = builtInPath,
                            audioFileName = "psalm_${psalm.number}.mp3",
                            duration = duration,
                            isAvailable = true
                        )
                        psalmDao.updatePsalm(updatedPsalm)
                        builtInUsedCount++
                    }
                }
            }
            
            ScanResult(
                success = true,
                message = "扫描完成，找到 ${audioFiles.size} 个用户音频文件，更新了 $updatedCount 篇诗篇，使用了 $builtInUsedCount 个内置音频",
                foundFiles = audioFiles.size,
                updatedPsalms = updatedCount,
                builtInUsed = builtInUsedCount
            )
        }
    }
    
    /**
     * 查找音频文件
     */
    private fun findAudioFiles(folder: File): List<File> {
        val audioFiles = mutableListOf<File>()
        
        folder.listFiles()?.forEach { file ->
            when {
                file.isDirectory -> {
                    // 递归搜索子文件夹
                    audioFiles.addAll(findAudioFiles(file))
                }
                file.isFile && isAudioFile(file) -> {
                    audioFiles.add(file)
                }
            }
        }
        
        return audioFiles
    }
    
    /**
     * 检查是否为音频文件
     */
    private fun isAudioFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return SUPPORTED_AUDIO_FORMATS.contains(extension)
    }
    
    /**
     * 从文件名提取诗篇编号
     */
    private fun extractPsalmNumber(fileName: String): Int? {
        // 支持多种命名格式：
        // psalm_1.mp3, psalm1.mp3, 诗篇1.mp3, 001.mp3, etc.
        val patterns = listOf(
            Regex("""psalm[_\s]*(\d+)""", RegexOption.IGNORE_CASE),
            Regex("""诗篇[_\s]*(\d+)"""),
            Regex("""^(\d+)(?:\D|$)"""),
            Regex("""(\d+)""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(fileName)
            if (match != null) {
                val number = match.groupValues[1].toIntOrNull()
                if (number != null && number in 1..150) {
                    return number
                }
            }
        }
        
        return null
    }
    
    /**
     * 清理无效的音频记录
     */
    suspend fun cleanupInvalidAudioRecords() = withContext(Dispatchers.IO) {
        val allPsalms = psalmDao.getAllPsalmsSync()
        val invalidPsalms = allPsalms.filter { psalm ->
            psalm.audioFilePath.isNotEmpty() && !File(psalm.audioFilePath).exists()
        }
        
        invalidPsalms.forEach { psalm ->
            psalmDao.updatePsalm(psalm.copy(isAvailable = false, audioFilePath = ""))
        }
    }
    
    /**
     * 获取用户音频文件数量
     */
    suspend fun getUserAudioCount(): Int = withContext(Dispatchers.IO) {
        return@withContext psalmDao.getUserAudioCount()
    }
    
    /**
     * 获取可用音频文件总数
     */
    suspend fun getAvailableAudioCount(): Int = withContext(Dispatchers.IO) {
        return@withContext psalmDao.getAvailableAudioCount()
    }
    }
    
    /**
     * 获取Assets中音频文件时长
     */
    private fun getAudioDurationFromAssets(assetPath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            val afd = context.assets.openFd(assetPath)
            retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            afd.close()
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 获取默认音频文件夹路径
     */
    fun getDefaultAudioFolder(): String {
        val externalStorage = Environment.getExternalStorageDirectory()
        return File(externalStorage, DEFAULT_AUDIO_FOLDER).absolutePath
    }
    
    /**
     * 创建默认音频文件夹
     */
    suspend fun createDefaultAudioFolder(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val folder = File(getDefaultAudioFolder())
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                folder.exists()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * 验证音频文件是否存在
     */
    suspend fun validateAudioFiles(): ValidationResult {
        return withContext(Dispatchers.IO) {
            val allPsalms = psalmDao.getAllPsalmsSync()
            var validCount = 0
            var invalidCount = 0
            var builtInCount = 0
            var userAudioCount = 0
            val invalidPsalms = mutableListOf<Int>()
            
            for (psalm in allPsalms) {
                if (psalm.isAvailable) {
                    val isBuiltIn = psalm.audioFilePath.startsWith("psalms/")
                    val isValid = if (isBuiltIn) {
                        // 验证内置音频
                        builtInAudioManager.isBuiltInAudioAvailable(psalm.number)
                    } else {
                        // 验证用户音频
                        File(psalm.audioFilePath).exists()
                    }
                    
                    if (isValid) {
                        validCount++
                        if (isBuiltIn) builtInCount++ else userAudioCount++
                    } else {
                        invalidCount++
                        invalidPsalms.add(psalm.number)
                        // 更新数据库状态
                        psalmDao.updatePsalmAvailability(psalm.number, false)
                    }
                }
            }
            
            ValidationResult(
                validCount = validCount,
                invalidCount = invalidCount,
                invalidPsalms = invalidPsalms,
                builtInCount = builtInCount,
                userAudioCount = userAudioCount
            )
        }
    }
    
    /**
     * 更新诗篇使用记录
     */
    suspend fun updatePsalmUsage(psalmNumber: Int) {
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        psalmDao.updatePsalmUsage(psalmNumber, currentDate)
    }
    
    /**
     * 获取音频文件Uri
     */
    fun getAudioUri(psalm: Psalm): Uri? {
        return if (psalm.isAvailable) {
            if (psalm.audioFilePath.startsWith("psalms/")) {
                // 内置音频，返回assets URI
                builtInAudioManager.getBuiltInAudioUri(psalm.number)
            } else if (File(psalm.audioFilePath).exists()) {
                // 用户音频文件
                Uri.fromFile(File(psalm.audioFilePath))
            } else {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * 获取音频统计信息
     */
    suspend fun getAudioStatistics(): AudioStatistics {
        return withContext(Dispatchers.IO) {
            val allPsalms = psalmDao.getAllPsalmsSync()
            var totalPsalms = 0
            var availablePsalms = 0
            var builtInAudioCount = 0
            var userAudioCount = 0
            var unavailableCount = 0
            
            for (psalm in allPsalms) {
                totalPsalms++
                if (psalm.isAvailable) {
                    availablePsalms++
                    if (psalm.audioFilePath.startsWith("psalms/")) {
                        builtInAudioCount++
                    } else {
                        userAudioCount++
                    }
                } else {
                    unavailableCount++
                }
            }
            
            AudioStatistics(
                totalPsalms = totalPsalms,
                availablePsalms = availablePsalms,
                builtInAudioCount = builtInAudioCount,
                userAudioCount = userAudioCount,
                unavailableCount = unavailableCount
            )
        }
    }
    
    /**
     * 重置为内置音频（清除用户自定义音频设置）
     */
    suspend fun resetToBuiltInAudio(psalmNumber: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val psalm = psalmDao.getPsalmByNumber(psalmNumber)
            if (psalm != null) {
                val builtInPath = builtInAudioManager.getBuiltInAudioPath(psalmNumber)
                if (builtInPath != null) {
                    val duration = getAudioDurationFromAssets(builtInPath)
                    val updatedPsalm = psalm.copy(
                        audioFilePath = builtInPath,
                        audioFileName = "psalm_${psalmNumber}.mp3",
                        duration = duration,
                        isAvailable = true
                    )
                    psalmDao.updatePsalm(updatedPsalm)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }
}

/**
 * 扫描结果数据类
 */
data class ScanResult(
    val success: Boolean,
    val message: String,
    val foundFiles: Int,
    val updatedPsalms: Int,
    val builtInUsed: Int = 0
)

/**
 * 验证结果数据类
 */
data class ValidationResult(
    val validCount: Int,
    val invalidCount: Int,
    val invalidPsalms: List<Int>,
    val builtInCount: Int = 0,
    val userAudioCount: Int = 0
)

/**
 * 音频统计信息数据类
 */
data class AudioStatistics(
    val totalPsalms: Int,
    val availablePsalms: Int,
    val builtInAudioCount: Int,
    val userAudioCount: Int,
    val unavailableCount: Int
) {
    val availabilityPercentage: Float
        get() = if (totalPsalms > 0) (availablePsalms.toFloat() / totalPsalms) * 100 else 0f
}