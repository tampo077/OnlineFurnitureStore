package com.test.onlinefurniturestore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme

class activity_signup : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnlineFurnitureStoreTheme {


                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {

                    FirebaseApp.initializeApp(this)


                    signup()

                }
            }
        }
    }
}

@Composable
fun signup() {
    var context= LocalContext.current;
    var showDialog by remember { mutableStateOf(false) }
    var textName by remember { mutableStateOf("") }
    var textPhone by remember { mutableStateOf("") }
    var textAddress by remember { mutableStateOf("") }
    var textEmail by remember { mutableStateOf("") }
    var textPassword by remember { mutableStateOf("") }



    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = textName,
            onValueChange = {textName = it},
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent
            )
        ) 
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = textPhone,
            onValueChange = {textPhone = it},
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = textAddress,
            onValueChange = {textAddress = it},
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = textEmail,
            onValueChange = {textEmail = it},
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = textPassword,
            onValueChange = {textPassword=it},
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                showDialog = true

                firebase_signup_user(context,textName,textPhone,textAddress,textEmail,textPassword)


            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Already have an account? Log in",
            fontSize = 16.sp,
            modifier = Modifier
                .clickable {

                    val intent = Intent(context, activity_login::class.java)
                    context.startActivity(intent)
                }.align(Alignment.CenterHorizontally), textDecoration = TextDecoration.Underline)
    }
}


fun firebase_signup_user(context: Context,textName:String,textPhone:String, textAddress:String, textEmail:String, textPassword:String) {
    val auth_firebase = FirebaseAuth.getInstance()

    auth_firebase.createUserWithEmailAndPassword(textEmail, textPassword)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth_firebase.currentUser?.uid ?: ""
                val user = User(userId, textName, textPhone, textAddress, textEmail)

                val database = FirebaseDatabase.getInstance()
                val usersRef = database.getReference("users")
                usersRef.child(userId).setValue(user).addOnSuccessListener {

                    Toast.makeText(context,"Added Success",Toast.LENGTH_SHORT).show()


                }.addOnFailureListener { exception ->

                    Toast.makeText(context,"Added Failed",Toast.LENGTH_SHORT).show()


                }

            } else {

            }
        }
}

data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val email: String = ""
)
