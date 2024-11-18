package com.capstoneproject.aji

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstoneproject.aji.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val udata = "Underlined Text"
        val content = SpannableString(udata)
        content.setSpan(UnderlineSpan(), 0, udata.length, 0)
        binding.tvForgetPassword.setText(content)
    }
}