package com.example.upasthithai

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import androidx.core.content.ContentProviderCompat.requireContext as requireContext1

class chatbotActivity : AppCompatActivity() {

    private var datalist  = mutableListOf<recyclerdataclass>()
    private lateinit var chatAdapter: ArrayAdapter<String>
    private lateinit var chatListView: ListView
    private lateinit var message : String
    private lateinit var recyleradapter : chatRecyclerAdapter
    private lateinit var recyler : RecyclerView
    private lateinit var sendtext : EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chatbotview)

        recyler = findViewById<RecyclerView>(R.id.chatListView)
        sendtext = findViewById<EditText>(R.id.inputField)
        val sendbutton = findViewById<ImageButton>(R.id.sendButton)


        message = sendtext.text.toString()
        val generativeModel = GenerativeModel(
            // Set the model name to the latest Gemini model.
            modelName = "gemini-1.5-pro-latest",
            // Set your Gemini API key in the API_KEY variable in your
            // local.properties file
            apiKey = getString(R.string.gemini),
            // Set a system instruction to set the behavior of the model.
        )

        sendbutton.setOnClickListener()
        {
            sendmessage(recyler,message,generativeModel)
//            lifecycleScope.launch {
//                val response = generativeModel.generateContent(prompt = message)
//                datalist.add(recyclerdataclass(message,response.text.toString()))
//                sendtext.text.clear()
//                recyleradapter.notifyItemInserted(datalist.size - 1) // Notify the adapter of the new item
//            }

        }
        recyler.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        recyleradapter = chatRecyclerAdapter(datalist)
        recyler.adapter = recyleradapter

        // Generate content with the prompt


        }



    private fun sendmessage(recycler : RecyclerView,string : String,model : GenerativeModel)
    {
        lifecycleScope.launch {
//            val chat = model.startChat()
//            val response = chat.sendMessage(string)
            val response = model.generateContent(prompt = string)
            Log.i("Gemini response",response.text.toString())
            datalist.add(recyclerdataclass(string,response.text.toString()))
            sendtext.text.clear()
            recyleradapter.notifyItemInserted(datalist.size - 1) // Notify the adapter of the new item
            recycler.scrollToPosition(datalist.size - 1)

        }
    }

    }



