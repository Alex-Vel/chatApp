package fi.oamk.chatapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fi.oamk.chatapp.R.drawable.border_box_rounded
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.name
    private lateinit var messages: ArrayList<Message>
    private lateinit var database: DatabaseReference
    private lateinit var edMessage: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var rcMessagesList: RecyclerView
    private var currentUser: FirebaseUser? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edMessage = findViewById(R.id.messageText)
        rcMessagesList = findViewById(R.id.messageList)
        database = Firebase.database.reference
        auth = Firebase.auth
        messages = arrayListOf()

        edMessage.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                addMessage()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }


        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    val messagesFromDatabase =
                        (snapshot.value as HashMap<String, ArrayList<Message>>).get("messages")
                    messages.clear()

                    if (messagesFromDatabase != null) {
                        for (i in 0..messagesFromDatabase.size - 1) {
                            val message: Message =
                                Message.from(messagesFromDatabase.get(i) as HashMap<String, String>)
                            messages.add(message)
                        }
                    }
                }
                rcMessagesList.adapter?.notifyDataSetChanged()
                rcMessagesList.smoothScrollToPosition(rcMessagesList.adapter!!.itemCount)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Chat", error.toString())
            }


        }




        edMessage.isVisible = currentUser != null


        database.addValueEventListener(messageListener)
        rcMessagesList.layoutManager = LinearLayoutManager(this)
        rcMessagesList.adapter = MyAdapter(messages)

    }


    override fun onStart() {
        super.onStart()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if(currentUser == null)
            loginDialog()
        }
    }

    fun showSettings() {
        val intent = Intent(this, Settings::class.java).apply {
            putExtra("currentUser", currentUser)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.settings -> {
            this.showSettings()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun loginDialog() {
        val builder = AlertDialog.Builder(this)

        with(builder) {
            setTitle("Login")
            val linearLayout: LinearLayout = LinearLayout(this@MainActivity)
            linearLayout.orientation = LinearLayout.VERTICAL

            linearLayout.setPadding(20, 20, 20, 20)

            val inputEmail: EditText = EditText(this@MainActivity)
            inputEmail.inputType =
                InputType.TYPE_CLASS_TEXT// or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            inputEmail.hint = "Enter email"
            inputEmail.setPadding(0, 0, 0, 25)
            linearLayout.addView(inputEmail)

            val inputPw: EditText = EditText(this@MainActivity)
            inputPw.inputType =
                InputType.TYPE_CLASS_TEXT //or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_PASSWORD
            inputPw.hint = "Enter password"
            //inputPw.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.white))
            linearLayout.addView(inputPw)
            builder.setView(linearLayout)
            linearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.white
                )
            )
            builder.setPositiveButton("OK") { dialog, which ->
                login(inputEmail.text.toString(), inputPw.text.toString())
            }.show()
        }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "sign in success")
                    currentUser = auth.currentUser
                    edMessage.isVisible = true
                } else {
                    Log.w(TAG, "sign in failure", task.exception)
                    Toast.makeText(
                        baseContext, "auth failure",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addMessage() {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val newMessage: Message = Message(
            edMessage.text.toString(),
            currentUser?.email.toString(),
            formatter.format(LocalDateTime.now())
        )

        messages.add(newMessage)
        database.child("messages").setValue(messages)
        edMessage.setText("")
        closeKeyBoard()
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}