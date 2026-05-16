package com.gramayatri.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramayatri.app.databinding.ActivityRegisterBinding
import com.gramayatri.app.repository.FirebaseRepository
import com.gramayatri.app.ui.home.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Create Account"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnRegister.setOnClickListener {
            val name    = binding.etName.text.toString().trim()
            val email   = binding.etEmail.text.toString().trim()
            val pass    = binding.etPassword.text.toString().trim()
            val phone   = binding.etPhone.text.toString().trim()
            val village = binding.etVillage.text.toString().trim()

            when {
                name.isEmpty()    -> { binding.etName.error = "Enter your name"; return@setOnClickListener }
                email.isEmpty()   -> { binding.etEmail.error = "Enter email"; return@setOnClickListener }
                pass.length < 6   -> { binding.etPassword.error = "Min 6 characters"; return@setOnClickListener }
                phone.isEmpty()   -> { binding.etPhone.error = "Enter phone"; return@setOnClickListener }
                village.isEmpty() -> { binding.etVillage.error = "Enter your village"; return@setOnClickListener }
            }

            setLoading(true)
            FirebaseRepository.register(name, email, pass, phone, village) { ok, msg ->
                setLoading(false)
                if (ok) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
