package com.biblealarm.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 诗篇数据模型
 */
@Entity(tableName = "psalms")
@Parcelize
data class Psalm(
    @PrimaryKey
    val number: Int,                    // 诗篇编号 (1-150)
    val title: String,                  // 诗篇标题
    val audioFileName: String,          // 音频文件名
    val audioFilePath: String,          // 音频文件完整路径
    val duration: Long = 0L,            // 音频时长(毫秒)
    val isAvailable: Boolean = true,    // 音频文件是否可用
    val lastUsedDate: String? = null,   // 最后使用日期
    val usageCount: Int = 0             // 使用次数
) : Parcelable {
    
    /**
     * 获取显示标题
     */
    fun getDisplayTitle(): String {
        return "诗篇 $number"
    }
    
    /**
     * 获取完整标题
     */
    fun getFullTitle(): String {
        return if (title.isNotEmpty()) {
            "诗篇 $number - $title"
        } else {
            "诗篇 $number"
        }
    }
    
    companion object {
        /**
         * 创建默认诗篇列表 (1-150篇)
         */
        fun createDefaultPsalms(): List<Psalm> {
            return (1..150).map { number ->
                Psalm(
                    number = number,
                    title = "诗篇第${number}篇",
                    audioFileName = "psalm_${number}.mp3",
                    audioFilePath = "",
                    isAvailable = false
                )
            }
        }
        
        /**
         * 诗篇标题映射 (部分常见诗篇的标题)
         */
        val psalmTitles = mapOf(
            1 to "义人与恶人的对比",
            23 to "耶和华是我的牧者",
            91 to "在至高者隐密处",
            121 to "我要向山举目",
            139 to "神的全知全在"
        )
    }
}