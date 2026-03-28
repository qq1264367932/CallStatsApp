package com.callstats.app

data class DailyCallStat(
    val date: String,      // 日期格式: yyyy-MM-dd
    val callCount: Int,    // 通话次数
    val totalDuration: Long, // 总通话时长(秒)
    val outgoingCount: Int,  // 呼出次数
    val incomingCount: Int   // 呼入次数
) {
    fun getFormattedDuration(): String {
        val minutes = totalDuration / 60
        val seconds = totalDuration % 60
        return if (minutes > 0) {
            "${minutes}分${seconds}秒"
        } else {
            "${seconds}秒"
        }
    }
}

data class AppCallStat(
    val date: String,
    val weChatOutgoingCount: Int,    // 微信外呼次数
    val weChatOutgoingDuration: Long, // 微信外呼时长
    val workWeChatOutgoingCount: Int, // 企业微信外呼次数
    val workWeChatOutgoingDuration: Long // 企业微信外呼时长
)
