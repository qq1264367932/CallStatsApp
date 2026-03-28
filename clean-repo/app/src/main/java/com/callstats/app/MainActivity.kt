package com.callstats.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.callstats.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var callStatsManager: CallStatsManager
    private lateinit var adapter: DailyStatsAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                loadStats()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        callStatsManager = CallStatsManager(this)
        adapter = DailyStatsAdapter()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // 添加查看汇总统计菜单按钮
        binding.btnViewAggregated.setOnClickListener {
            startActivity(Intent(this, AggregatedStatsActivity::class.java))
        }

        // 退出登录
        binding.btnLogout.setOnClickListener {
            getSharedPreferences("CallStats", MODE_PRIVATE).edit().remove("phone").apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val currentPhone = callStatsManager.getCurrentPhone()
        if (currentPhone != null) {
            binding.tvCurrentUser.text = "当前账号: $currentPhone"
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val neededPermissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
        )

        val notGranted = neededPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(notGranted.toTypedArray())
        } else {
            loadStats()
        }
    }

    private fun loadStats() {
        // 保存今日数据到汇总
        val phone = callStatsManager.getCurrentPhone()
        if (!phone.isNullOrEmpty()) {
            callStatsManager.saveTodayData(phone)
        }

        // 今日统计
        val todayStats = callStatsManager.getTodayStats()
        binding.todayCallCount.text = "今日通话次数: ${todayStats.callCount}"
        binding.todayDuration.text = "今日总时长: ${todayStats.getFormattedDuration()}"
        binding.todayOutgoing.text = "今日呼出: ${todayStats.outgoingCount}"

        // 最近7天统计
        val last7Days = callStatsManager.getLastNDays(7)
        adapter.updateData(last7Days.values.toList())

        // 关于微信/企微统计说明
        binding.wechatNote.text = """
            💡 关于微信/企业微信统计说明:
            
            由于Android系统隐私限制，
            微信和企业微信的通话记录不会出现在
            系统通话记录中，第三方App无法直接读取。
            
            如需统计，请您：
            • 使用手机系统自带的「数字健康」
              或「应用使用统计」查看微信使用时长
            • 如果需要自动统计，需要开通
              「辅助功能」服务监听通知，
              本App后续版本可以支持此功能。
        """.trimIndent()
    }

    override fun onResume() {
        super.onResume()
        if (hasPermissions()) {
            loadStats()
        }
    }

    private fun hasPermissions(): Boolean {
        val neededPermissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
        )
        return neededPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
