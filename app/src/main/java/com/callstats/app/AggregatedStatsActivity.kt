package com.callstats.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.callstats.app.databinding.ActivityAggregatedStatsBinding

class AggregatedStatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAggregatedStatsBinding
    private lateinit var callStatsManager: CallStatsManager
    private lateinit var adapter: DailyStatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAggregatedStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "汇总统计（所有账号）"

        callStatsManager = CallStatsManager(this)
        adapter = DailyStatsAdapter()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadAggregatedStats()
    }

    private fun loadAggregatedStats() {
        val aggregatedStats = callStatsManager.getDailyAggregatedByDate()
        adapter.updateData(aggregatedStats.values.toList())

        // 计算总计
        var totalCalls = 0
        var totalDuration = 0L
        var totalOutgoing = 0
        aggregatedStats.values.forEach {
            totalCalls += it.callCount
            totalDuration += it.totalDuration
            totalOutgoing += it.outgoingCount
        }

        val minutes = totalDuration / 60
        val seconds = totalDuration % 60
        binding.tvTotalSummary.text = """
            总计:
            总通话次数: $totalCalls
            总通话时长: ${minutes}分${seconds}秒
            总呼出次数: $totalOutgoing
        """.trimIndent()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
