package com.test.onlinefurniturestore

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
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme

class activity_login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContent {

            OnlineFurnitureStoreTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    FirebaseApp.initializeApp(this)
                    login()
                }
            }
        }
    }
}

@Composable
fun login() {

    var context= LocalContext.current;
    var textEmail by remember { mutableStateOf("") }
    var textPassword by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier
                .size(270.dp)
                .clip(RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = textEmail,
            onValueChange = {textEmail = it},
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Black,
            focusedIndicatorColor = Color.Black
        )
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = textPassword,
            onValueChange = {textPassword = it},
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Black,
                focusedIndicatorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {

                if(textEmail.equals("admin") && textPassword.equals("admin")){

                    val intent = Intent(context, activity_admin_panel::class.java)
                    context.startActivity(intent)
                }
                else{

                    isFetching=true
                   val auth_firebase = FirebaseAuth.getInstance()
                    auth_firebase.signInWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isFetching = false

                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        }
                        else {
                            isFetching = false

                            Toast.makeText(context,"Login Failed!",Toast.LENGTH_SHORT).show()

                        }
                    }
                }




            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = "Click here to register",
            fontSize = 26.sp,
            modifier = Modifier
                .clickable {
                    val intent = Intent(context, activity_signup::class.java)
                    context.startActivity(intent)

                }
                .align(Alignment.CenterHorizontally),
            textDecoration = TextDecoration.Underline
        )
    }
    wait_dialog(show_dialog = isFetching, onDismissRequest = {  })
}
