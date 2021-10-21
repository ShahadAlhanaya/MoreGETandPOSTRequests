package com.example.moregetandpostrequests

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var name1EditText: EditText
    lateinit var location1EditText: EditText
    lateinit var name2EditText: EditText
    lateinit var location2EditText: EditText
    lateinit var name3EditText: EditText
    lateinit var location3EditText: EditText
    lateinit var addButton: Button
    lateinit var getUsersButton: Button
    lateinit var getLocationLinearLayout: LinearLayout
    lateinit var recyclerView: RecyclerView

    var usersList = arrayListOf<User>()
    var entryList = arrayListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        name1EditText = findViewById(R.id.edt_name1)
        location1EditText = findViewById(R.id.edt_location1)
        name2EditText = findViewById(R.id.edt_name2)
        location2EditText = findViewById(R.id.edt_location2)
        name3EditText = findViewById(R.id.edt_name3)
        location3EditText = findViewById(R.id.edt_location3)
        addButton = findViewById(R.id.btn_addEntry)
        getUsersButton = findViewById(R.id.btn_getLocation)
        getLocationLinearLayout = findViewById(R.id.ll_getLocation)

        //initialize recyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RVAdapter(usersList)


        addButton.setOnClickListener {
            entryList.clear()
            if (name1EditText.text.trim().isNotEmpty() && location1EditText.text.trim()
                    .isNotEmpty()
            ) {
                entryList.add(
                    User(
                        name1EditText.text.toString(),
                        location1EditText.text.toString()
                    )
                )
                if (name2EditText.text.trim().isNotEmpty() && location2EditText.text.trim()
                        .isNotEmpty()
                ) {
                    entryList.add(
                        User(
                            name2EditText.text.toString(),
                            location2EditText.text.toString()
                        )
                    )
                }
                if (name3EditText.text.trim().isNotEmpty() && location3EditText.text.trim()
                        .isNotEmpty()
                ) {
                    entryList.add(
                        User(
                            name3EditText.text.toString(),
                            location3EditText.text.toString()
                        )
                    )
                }

                addName(entryList)

            } else {
                Toast.makeText(this, "please complete your entry", Toast.LENGTH_SHORT).show()
            }
        }

        getUsersButton.setOnClickListener {
            usersList.clear()
            getUsers()
        }

    }


    private fun addName(entryList: ArrayList<User>) {
        CoroutineScope(Dispatchers.IO).launch {
            for(entry in entryList){
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("name", entry.name)
                    jsonObject.put("location", entry.location)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonObject.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://dojo-recipes.herokuapp.com/test/")
                    .post(requestBody)
                    .build()

                var response: Response? = null
                try {
                    response = client.newCall(request).execute()
                    if (response.code == 201) {
                        withContext(Dispatchers.Main) {
                            clearFields()
                            Toast.makeText(this@MainActivity, "added successfully", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "something went wrong", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        }

    }

    private fun clearFields(){
        name1EditText.text.clear()
        location1EditText.text.clear()
        name2EditText.text.clear()
        location2EditText.text.clear()
        name3EditText.text.clear()
        location3EditText.text.clear()
    }

    private fun getUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient()
                val request = Request.Builder()
                    .url("https://dojo-recipes.herokuapp.com/test/")
                    .build()
                val response = okHttpClient.newCall(request).execute()

                if (response != null) {
                    if (response.code == 200) {
                        val jsonArray = JSONArray(response.body!!.string())
                        Log.d("HELP", jsonArray.toString())
                        for (index in 0 until jsonArray.length()) {
                            val nameObj = jsonArray.getJSONObject(index)
                            val userName = nameObj.getString("name")
                            val userLocation = nameObj.getString("location")
                            withContext(Main) {
                                usersList.add(User(userName,userLocation))
                            }
                        }
                        withContext(Main) {
                            recyclerView.adapter!!.notifyDataSetChanged()
                        }

                    }
                }
            } catch (e: Exception) {
                Log.d("TextViewActivity", e.message.toString())
            }
        }
    }
}

