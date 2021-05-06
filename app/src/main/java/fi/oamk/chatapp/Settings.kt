package fi.oamk.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Settings : AppCompatActivity(){

private lateinit var tvEmail: TextView

@SuppressLint("RestrictedApi")
override fun onCreate(savedInstanceState: Bundle?){
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)
    tvEmail = findViewById(R.id.email)

    supportActionBar?.apply{
        title="Settings"
        setDisplayHomeAsUpEnabled(true)
        setDisplayShowHomeEnabled(true)
    }

    val currentUser = intent.getParcelableExtra<FirebaseUser>("currentUser")
    tvEmail.setText(currentUser?.email)
}

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun signOut(view: View) {
        FirebaseAuth.getInstance().signOut()
        tvEmail.setText("")

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("currentUser", "")
        }

        startActivity(intent)
    }

}