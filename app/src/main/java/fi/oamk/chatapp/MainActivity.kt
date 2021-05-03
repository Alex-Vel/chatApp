package fi.oamk.chatapp

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.name
    private lateinit var messages: ArrayList<String>
    private lateinit var database: DatabaseReference
    private lateinit var edMessage: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var rcMessagesList: RecyclerView
    private var currentUser: FirebaseUser? = null


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
               if(snapshot.value != null){
                   val messagesFromDatabase = (snapshot.value as HashMap<String,ArrayList<String>>).get("messages")
                   messages.clear()
                   messagesFromDatabase?.forEach{
                       if (it != null) messages.add(it)
                   }
                   rcMessagesList.adapter?.notifyDataSetChanged()
               }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Chat", error.toString())
            }


        }

        database.addValueEventListener(messageListener)
        rcMessagesList.layoutManager = LinearLayoutManager(this)
        rcMessagesList.adapter = MyAdapter(messages)
    }

    override fun onStart() {
        super.onStart()
        loginDialog()
    }

    fun loginDialog() {
        val builder = AlertDialog.Builder(this)

        with(builder) {
            setTitle("Login")
            val linearLayout: LinearLayout = LinearLayout(this@MainActivity)
            linearLayout.orientation = LinearLayout.VERTICAL

            val inputEmail: EditText = EditText(this@MainActivity)
            inputEmail.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            inputEmail.hint = "Enter email"
            linearLayout.addView(inputEmail)

            val inputPw: EditText = EditText(this@MainActivity)
            inputPw.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_PASSWORD
            inputPw.hint = "enter password"
            linearLayout.addView(inputPw)
            builder.setView(linearLayout)

            builder.setPositiveButton("OK") { dialog, which ->
                login(inputEmail.text.toString(), inputPw.text.toString())
            }.show()
        }
    }

    fun login(email: String, password: String){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful) {
                    Log.d(TAG,"sign in success")
                    currentUser = auth.currentUser
                }
                else {
                    Log.w(TAG, "sign in failure", task.exception)
                    Toast.makeText(baseContext,"auth failure",
                    Toast.LENGTH_SHORT).show()
                }
            }
    }





    fun addMessage() {
        val newMessage = edMessage.text.toString()
        messages.add(newMessage)
        database.child("messages").setValue(messages)
        edMessage.setText("")
        closeKeyBoard()
    }

    private fun closeKeyBoard(){
        val view = this.currentFocus
        if(view != null){
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,0)
        }
    }
}