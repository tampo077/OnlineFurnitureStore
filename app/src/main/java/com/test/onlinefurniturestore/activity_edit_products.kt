package com.test.onlinefurniturestore

import android.content.Context
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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

class activity_edit_products : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pid = intent.getStringExtra("pid")
        val pname = intent.getStringExtra("pname")
        val pprice = intent.getStringExtra("pprice")
        val pcat = intent.getStringExtra("pcat")
        val pimg = intent.getStringExtra("pimg")

        setContent {
            OnlineFurnitureStoreTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    if (pid != null && pname !=null && pprice !=null && pcat!=null && pimg!=null) {
                        edit_product(pid, pname, pprice, pcat, pimg)
                    }
                }
            }
        }
    }
}

@Composable
fun edit_product(pid: String, p_name: String, p_price: String, p_category: String, p_url: String) {
    var pname by remember { mutableStateOf(p_name ?: "") }
    var pprice by remember { mutableStateOf(p_price ?: "") }
    var pcategory by remember { mutableStateOf(p_category ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var context= LocalContext.current;
    var initialImageUrl by remember { mutableStateOf(p_url) }
    var isImageUpdated by remember { mutableStateOf(false) }
    var show_dialog by remember { mutableStateOf(false) }
    val launcher_image = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        isImageUpdated = true
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
        )



        {
            val painter = if (isImageUpdated && imageUri != null) {
                rememberImagePainter(imageUri)
            } else {
                rememberImagePainter(initialImageUrl)
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
                        pcategory = "Category 1"
                        expanded = false
                    }) {
                        Text("Category 1")
                    }
                    DropdownMenuItem(onClick = {
                        pcategory = "Category 2"
                        expanded = false
                    }) {
                        Text("Category 2")
                    }
                }
            }
        }

        Button(
            onClick = {

                      if(isImageUpdated){
                          imageUri?.let { uri ->
                              show_dialog = true
                              uploadProductWithImage(
                                  name = pname,
                                  price = pprice,
                                  category = pcategory,
                                  imageUri = uri,
                                  onSuccess = {
                                      show_dialog = false
                                      Toast.makeText(context,"Update Success",Toast.LENGTH_LONG).show()
                                  },
                                  onFailure = { exception ->
                                      show_dialog = false
                                      Toast.makeText(context,"Update Failed",Toast.LENGTH_LONG).show()
                                  }
                              )
                          } ?: run {


                          }
                      }
                else{

                    update_product(pid,p_name,p_price,p_category,initialImageUrl,context)
                      }

            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Update Product")
        }
    }
    wait_dialog(show_dialog = show_dialog, onDismissRequest = { show_dialog = false })
}


fun update_product(productId: String, name: String, price: String, category: String, imageUri: String, context: Context) {

        val product = Product(productId, name, price, category, imageUri.toString())
        FirebaseDatabase.getInstance().reference.child("products").child(productId)
            .setValue(product)
            .addOnSuccessListener {
                Toast.makeText(context, "Product updated successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update product", Toast.LENGTH_LONG).show()
            }

}

