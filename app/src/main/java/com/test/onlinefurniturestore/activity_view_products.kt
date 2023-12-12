package com.test.onlinefurniturestore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme

class activity_view_products : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnlineFurnitureStoreTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    FirebaseApp.initializeApp(this)
                    view_products_composable()
                }
            }
        }
    }
}

@Composable
fun view_products_composable() {
    var context= LocalContext.current;
    val products = remember { mutableStateListOf<Product>() }
    var isFetching by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit) {
        FirebaseDatabase.getInstance().reference.child("products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    products.clear()
                    for (categorySnapshot in snapshot.children) {
                        for (productSnapshot in categorySnapshot.children) {
                            val product = productSnapshot.getValue(Product::class.java)
                            product?.let { products.add(it) }
                        }
                    }
                    isFetching = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isFetching = false
                }
            })
    }


    if (isFetching) {
        wait_dialog(show_dialog = true, onDismissRequest = { })
    } else {
        LazyColumn {
            items(products) { product ->
                ProductCard(product = product) { productId ->
                    val intent = Intent(context, activity_edit_products::class.java)
                    intent.putExtra("pid", product.pid)
                    intent.putExtra("pname", product.name)
                    intent.putExtra("pprice", product.price)
                    intent.putExtra("pcat", product.category)
                    intent.putExtra("pimg", product.imageUrl)
                    context.startActivity(intent)
                }
            }
        }
    }
}



@Composable
fun ProductCard(product: Product , onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(product.pid) },
        elevation = 4.dp
    ){
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val painter = rememberImagePainter(product.imageUrl)
            Image(
                painter = painter,
                contentDescription = "Product Image",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = product.name, color = Color.Black)
            Text(text = "Price: £${product.price}", color = Color.Gray)
            Text(text = "Category: ${product.category}", color = Color.Gray)
        }
    }
}