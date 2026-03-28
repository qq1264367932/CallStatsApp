package com.callstats.app

import android.content.Context
import android.provider.CallLog
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class CallStatsManager(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences("call_stats_data", Context.MODE_PRIVATE)

    // 统计每日通话数据
    fun getDailyCallStats(startDate: Long, endDate: Long): Map<String, DailyCallStat> {
        val stats = mutableMapOf<String, DailyCallStat>()

        val projection = arrayOf(
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )

        // 查询通话记录
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            "${CallLog.Calls.DATE} >= ? AND ${CallLog.Calls.DATE} <= ?",
            arrayOf(startDate.toString(), endDate.toString()),
            CallLog.Calls.DATE + " DESC"
        )

        cursor?.use {
            val dateCol = it.getColumnIndex(CallLog.Calls.DATE)
            val durationCol = it.getColumnIndex(CallLog.Calls.DURATION)
            val typeCol = it.getColumnIndex(CallLog.Calls.TYPE)

            while (it.moveToNext()) {
                val dateMillis = it.getLong(dateCol)
                val duration = it.getLong(durationCol)
                val type = it.getInt(typeCol)

                val date = dateFormat.format(Date(dateMillis))

                val existing = stats[date] ?: DailyCallStat(date, 0, 0, 0, 0)
                val isOutgoing = type == CallLog.Calls.OUTGOING_TYPE

                stats[date] = existing.copy(
                    callCount = existing.callCount + 1,
                    totalDuration = existing.totalDuration + duration,
                    outgoingCount = if (isOutgoing) existing.outgoingCount + 1 else existing.outgoingCount,
                    incomingCount = if (!isOutgoing) existing.incomingCount + 1 else existing.incomingCount
                )
            }
        }

        return stats.toSortedMap(reverseOrder())
    }

    // 获取今天的统计
    fun getTodayStats(): DailyCallStat {
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val todayEnd = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val stats = getDailyCallStats(todayStart, todayEnd)
        val todayStr = dateFormat.format(Date())
        return stats[todayStr] ?: DailyCallStat(todayStr, 0, 0, 0, 0)
    }

    // 获取最近N天的统计
    fun getLastNDays(days: Int): Map<String, DailyCallStat> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, -(days - 1))
        val startDate = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        return getDailyCallStats(startDate, endDate)
    }

    // 保存当前用户今日数据（用于汇总）
    fun saveTodayData(phone: String) {
        val today = dateFormat.format(Date())
        val todayStats = getTodayStats()

        // 读取已保存的汇总数据
        val savedMap = getAggregatedStats()

        // 更新当前用户今日数据
        val key = "$phone-$today"
        savedMap[key] = todayStats
        prefs.edit().putString("aggregated_stats", gson.toJson(savedMap)).apply()
    }

    // 获取所有汇总数据（按日期分组，每个用户一条）
    fun getAggregatedStats(): MutableMap<String, DailyCallStat> {
        val json = prefs.getString("aggregated_stats", "{}")
        val type = object : TypeToken<MutableMap<String, DailyCallStat>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    // 按日期汇总所有用户数据
    fun getDailyAggregatedByDate(): Map<String, DailyCallStat> {
        val allStats = getAggregatedStats()
        val result = mutableMapOf<String, DailyCallStat>()

        allStats.forEach { (key, stat) ->
            // key format: phone-yyyy-MM-dd
            val date = key.split("-").last()
            val existing = result[date] ?: DailyCallStat(date, 0, 0, 0, 0)
            result[date] = existing.copy(
                callCount = existing.callCount + stat.callCount,
                totalDuration = existing.totalDuration + stat.totalDuration,
                outgoingCount = existing.outgoingCount + stat.outgoingCount,
                incomingCount = existing.incomingCount + stat.incomingCount
            )
        }

        return result.toSortedMap(reverseOrder())
    }

    // 获取当前登录用户手机号
    fun getCurrentPhone(): String? {
        val prefs = context.getSharedPreferences("CallStats", Context.MODE_PRIVATE)
        return prefs.getString("phone", null)
    }

    // 重要说明：
    // 由于Android系统隐私限制，第三方应用无法直接读取
    // 微信和企业微信的内部通话记录数据
    // 这些数据是微信私有，不对外暴露
    // 
    // 如果需要统计，只能通过以下方式：
    // 1. 开启辅助服务，监听通知（需要用户手动授权）
    // 2. 使用通话记录匹配，但是微信/企微通话不会出现在系统通话记录中
}
