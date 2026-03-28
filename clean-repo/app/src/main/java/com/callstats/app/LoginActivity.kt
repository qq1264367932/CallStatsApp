package com.callstats.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.callstats.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("CallStats", MODE_PRIVATE)

        // 检查是否已经登录
        val savedPhone = prefs.getString("phone", null)
        if (!savedPhone.isNullOrEmpty()) {
            // 已登录，直接跳转到主界面
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (phone.isNotEmpty()) {
                // 保存手机号
                prefs.edit().putString("phone", phone).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                binding.etPhone.error = "请输入手机号"
            }
        }
    }
}
