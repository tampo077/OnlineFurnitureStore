package com.test.onlinefurniturestore

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme
import kotlinx.coroutines.tasks.await

class activity_products : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val category = intent.getStringExtra("category") ?: ""
        val searchResults = intent.getSerializableExtra("searchResults") as? List<Product> ?: listOf()

        setContent {
            OnlineFurnitureStoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ProductsScreen(category, searchResults)
                }
            }
        }
    }
}



    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun ProductsScreen(category: String, searchResults: List<Product>) {
        var ctx= LocalContext.current;
        var products by remember { mutableStateOf(listOf<Product>()) }
        var isFetching by remember { mutableStateOf(true) }

        LaunchedEffect(category) {
            if (category.isNotEmpty()) {
                products = fetchProductsByCategory(category)
                isFetching = false
            }

          else  if (searchResults.size>0) {

                isFetching = false

                products = searchResults
            }
            else {
                products = fetchAllProducts()
                isFetching = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Products in $category") })
            }
        ) {
            if (isFetching) {
                wait_dialog(show_dialog = true, onDismissRequest = { })
            }
             else {
                LazyColumn {
                    items(products) { product ->
                        ProductCard(product) {
                            val intent = Intent(ctx, activity_product_detail::class.java)
                            intent.putExtra("pid", product.pid)
                            intent.putExtra("pname", product.name)
                            intent.putExtra("pprice", product.price)
                            intent.putExtra("pcategory", product.category)
                            intent.putExtra("pimage", product.imageUrl)
                            ctx.startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProductCard(product: Product) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = rememberImagePainter(product.imageUrl),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = product.name)
                Text(text = "Price: £${product.price}")
                Text(text = "Category: ${product.category}")
            }
        }
    }

private suspend fun fetchProductsByCategory(category: String): List<Product> {
    return try {
        val dataSnapshot = Firebase.database.reference
            .child("products")
            .child(category)
            .get()
            .await()

        dataSnapshot.children.mapNotNull { snapshot ->
            snapshot.getValue(Product::class.java)
        }
    } catch (e: Exception) {

        listOf()
    }
}

private suspend fun fetchAllProducts(): List<Product> {
    return try {
        val dataSnapshot = Firebase.database.reference
            .child("products")
            .get()
            .await()

        dataSnapshot.children.flatMap { categorySnapshot ->
            categorySnapshot.children.mapNotNull { snapshot ->
                snapshot.getValue(Product::class.java)
            }
        }
    } catch (e: Exception) {
        listOf()
    }
}

