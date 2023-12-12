package com.test.onlinefurniturestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme

class activity_order_detail : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = intent.getStringExtra("oid") ?: return

        setContent {
            OrderDetailsScreen(orderId)
        }
    }
}

@Composable
fun OrderDetailsScreen(orderId: String) {
    val products = remember { mutableStateOf<List<OrderProducts>>(listOf()) }

    LaunchedEffect(orderId) {
        fetchOrderDetails(orderId) { fetchedProducts ->
            products.value = fetchedProducts
        }
    }

    LazyColumn {
        items(products.value) { product ->
            ProductRow(product)
        }
    }
}

@Composable
fun ProductRow(product: OrderProducts) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(
                    data = product.imageUrl,
                    builder = {
                        crossfade(true)
                    }
                ),
                contentDescription = "Product Image",
                modifier = Modifier
                    .size(100.dp) // Adjust the size as needed
                    .clip(RoundedCornerShape(8.dp)), // Optional: to clip the image with rounded corners
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(product.name, style = MaterialTheme.typography.subtitle1)
                Text("Price: £${product.price}", style = MaterialTheme.typography.body2)
                Text("Quantity: ${product.quantity}", style = MaterialTheme.typography.body2)
            }
        }
    }
}

fun fetchOrderDetails(orderId: String, onResult: (List<OrderProducts>) -> Unit) {
    val productsRef = FirebaseDatabase.getInstance().getReference("Orders").child(orderId).child("products")
    productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val productsList = mutableListOf<OrderProducts>()
            for (productSnapshot in snapshot.children) {
                val product = productSnapshot.getValue(OrderProducts::class.java)
                product?.let { productsList.add(it) }
            }
            onResult(productsList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle the error
        }
    })
}





data class OrderProducts(
    val imageUrl: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val productId: String = "",
    val quantity: Int = 0
)
