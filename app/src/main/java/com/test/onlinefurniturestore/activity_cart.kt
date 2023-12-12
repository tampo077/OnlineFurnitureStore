package com.test.onlinefurniturestore

import android.content.Intent
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.rememberImagePainter
import java.io.Serializable

class activity_cart : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnlineFurnitureStoreTheme {
                CartScreen()
            }
        }
    }
}


@Composable
fun CartScreen() {
    val db = SqliteDb(LocalContext.current)
    val productsInCart = remember { mutableStateOf(listOf<CartProduct>()) }
    val totalAmount = remember { mutableStateOf(0.0) }
    var ctx= LocalContext.current;
    LaunchedEffect(key1 = Unit) {
        productsInCart.value = db.get_all_cart_items()
        totalAmount.value = db.get_cart_total()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(productsInCart.value) { product ->
                CartProductItem(product = product, onDelete = {


                    db.delete_cart_item(product.productId)
                    productsInCart.value = db.get_all_cart_items()
                    totalAmount.value = db.get_cart_total()


                }


                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total: £${totalAmount.value}", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val intent = Intent(ctx, activity_payment::class.java)
                    intent.putExtra("totalAmount",totalAmount.value.toString())
                    ctx.startActivity(intent)

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Checkout")
            }
        }
    }
}

@Composable
fun CartProductItem(product: CartProduct, onDelete: (CartProduct) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(product.imageUrl),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )
        Column(modifier = Modifier
            .padding(8.dp)
            .weight(1f)
        ) {
            Text(product.name, fontWeight = FontWeight.Bold)
            Text("Price: ${product.price}£")
            Text("Quantity: ${product.quantity}")
        }
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { onDelete(product) }) {
            Image(
                painter = painterResource(id = R.drawable.trash_bin),
                contentDescription = "Delete"
            )
        }
    }
}
