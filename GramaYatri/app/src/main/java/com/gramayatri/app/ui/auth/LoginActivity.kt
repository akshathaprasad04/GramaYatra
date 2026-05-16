package com.gramayatri.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramayatri.app.databinding.ActivityLoginBinding
import com.gramayatri.app.repository.FirebaseRepository
import com.gramayatri.app.ui.home.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString().trim()
            if (email.isEmpty()) { binding.etEmail.error = "Enter email"; return@setOnClickListener }
            if (pass.isEmpty())  { binding.etPassword.error = "Enter password"; return@setOnClickListener }
            setLoading(true)
            FirebaseRepository.login(email, pass) { ok, msg ->
                setLoading(false)
                if (ok) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Demo login hint
        binding.tvDemoHint.setOnClickListener {
            binding.etEmail.setText("demo@gramayatri.app")
            binding.etPassword.setText("demo1234")
        }
    }

    private fun setLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }
}
