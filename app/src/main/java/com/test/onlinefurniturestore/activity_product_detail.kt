package com.test.onlinefurniturestore

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme

class activity_product_detail : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var db=SqliteDb(this)


        // Retrieve data from intent
        val productId = intent.getStringExtra("pid")
        val productName = intent.getStringExtra("pname")
        val productPrice = intent.getStringExtra("pprice")
        val productCategory = intent.getStringExtra("pcategory")
        val productImageUrl = intent.getStringExtra("pimage")

        setContent {
            OnlineFurnitureStoreTheme {
                ProductDetailScreen(db,
                    productId = productId.orEmpty(),
                    productName = productName.orEmpty(),
                    productPrice = productPrice.orEmpty(),
                    productCategory = productCategory.orEmpty(),
                    productImageUrl = productImageUrl.orEmpty()
                )
            }
        }
    }
}

@Composable
fun ProductDetailScreen(db:SqliteDb,
    productId: String,
    productName: String,
    productPrice: String,
    productCategory: String,
    productImageUrl: String
) {
    var context= LocalContext.current;
    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberImagePainter(productImageUrl),
            contentDescription = productName,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Name: $productName", modifier = Modifier.padding(16.dp))
        Text(text = "Price: £$productPrice", modifier = Modifier.padding(16.dp))
        Text(text = "Category: $productCategory", modifier = Modifier.padding(16.dp))
        Button(
            onClick = {


                    val isNewProduct = db.add_to_cart(productId, productName.orEmpty(), productPrice.toDouble(), productImageUrl.orEmpty())
                    if (isNewProduct) {
                        Toast.makeText(context, "Product added to cart", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Product quantity updated in cart", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Add to Cart")
        }

    }
}
