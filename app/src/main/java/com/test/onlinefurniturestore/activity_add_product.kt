package com.test.onlinefurniturestore

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme
import java.io.Serializable

class activity_add_product : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnlineFurnitureStoreTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    add_product()
                }
            }
        }
    }
}

@Composable
fun add_product() {
    var pname by remember { mutableStateOf("") }
    var pprice by remember { mutableStateOf("") }
    var pcategory by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var context= LocalContext.current;
    var show_dialog by remember { mutableStateOf(false) }
    val launcher_image = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp)
                .clickable { launcher_image.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            val painter = if (imageUri != null) {
                rememberImagePainter(imageUri)
            } else {
                painterResource(R.drawable.ic_baseline_chair_24)
            }

            Image(
                painter = painter,
                contentDescription = "Product Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }


        TextField(
            value = pname,
            onValueChange = { pname = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Black,
                focusedIndicatorColor = Color.Black
            )
        )

        TextField(
            value = pprice,
            onValueChange = { pprice = it },
            label = { Text("Product Price") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Black,
            focusedIndicatorColor = Color.Black
        )
        )

        var expanded by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Product Category: ",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Box {
                Text(text = pcategory, fontSize = 16.sp, color = Color.Black)
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Furniture Categories")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        pcategory = "Chairs"
                        expanded = false
                    }) {
                        Text("Chairs")
                    }
                    DropdownMenuItem(onClick = {
                        pcategory = "Beds"
                        expanded = false
                    }) {
                        Text("Beds")
                    }


                    DropdownMenuItem(onClick = {
                        pcategory = "Coffee Table"
                        expanded = false
                    }) {
                        Text("Coffee Table")
                    }


                    DropdownMenuItem(onClick = {
                        pcategory = "Desk"
                        expanded = false
                    }) {
                        Text("Desk")
                    }


                    DropdownMenuItem(onClick = {
                        pcategory = "Sofas"
                        expanded = false
                    }) {
                        Text("Sofas")
                    }


                }
            }
        }

        Button(
            onClick = {
                imageUri?.let { uri ->
                    show_dialog = true
                    uploadProductWithImage(
                        name = pname,
                        price = pprice,
                        category = pcategory,
                        imageUri = uri,
                        onSuccess = {
                            show_dialog = false
                                    Toast.makeText(context,"Uploaded Success",Toast.LENGTH_LONG).show()
                        },
                        onFailure = { exception ->
                            show_dialog = false
                            Toast.makeText(context,"Uploaded Failed",Toast.LENGTH_LONG).show()
                        }
                    )
                } ?: run {


                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add Product")
        }
    }
    wait_dialog(show_dialog = show_dialog, onDismissRequest = { show_dialog = false })
}


data class Product(
    val pid: String="",
    val name: String="",
    val price: String="",
    val category: String="",
    val imageUrl: String=""
): Serializable

fun uploadProductWithImage(name: String, price: String, category: String, imageUri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference.child("furniture_images/${imageUri.lastPathSegment}")
    storageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
        taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            save_info(name, price, category, imageUrl, onSuccess, onFailure)
        }?.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }.addOnFailureListener { exception ->
        onFailure(exception)
    }
}

fun save_info(name: String, price: String, category: String, imageUrl: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("products").child(category)
    val pushRef = databaseRef.push()
    val pid = pushRef.key ?: return
    val product = Product(pid, name, price, category, imageUrl)
    pushRef.setValue(product).addOnSuccessListener {

        onSuccess()

    }.addOnFailureListener { exception ->

        onFailure(exception)

    }
}
