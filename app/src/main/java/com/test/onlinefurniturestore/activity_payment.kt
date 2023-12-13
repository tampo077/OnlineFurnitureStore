package com.test.onlinefurniturestore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString 
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme
import java.text.SimpleDateFormat
import java.util.*

class activity_payment : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val db = SqliteDb(this)
            val cartProducts = db.get_all_cart_items()

            OnlineFurnitureStoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    PaymentPage(cartProducts)
                }
            }
        }
    }
}




@Composable
fun PaymentPage(cartProducts: List<CartProduct>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var cardNumber by remember { mutableStateOf("") }
        var expiryDate by remember { mutableStateOf("") }
        var cvv by remember { mutableStateOf("") }

        var ctx= LocalContext.current;


        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier
                .size(270.dp)
                .clip(RoundedCornerShape(50))
        )
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = { Text("Card Number") },
            placeholder = { Text("1234 5678 9012 3456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = expiryDate,
            onValueChange = { expiryDate = it },
            label = { Text("Expiry Date") },
            placeholder = { Text("MM/YY") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cvv,
            onValueChange = { cvv = it },
            label = { Text("CVV") },
            placeholder = { Text("123") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            when {
                cardNumber.length != 16 ->
                    Toast.makeText(ctx, "Card number must be 16 digits.", Toast.LENGTH_LONG).show()
                expiryDate.length != 5 || !expiryDate.isValidExpiryDate() ->
                    Toast.makeText(ctx, "Invalid expiry date. Format MM/YY.", Toast.LENGTH_LONG).show()
                cvv.length != 3 ->
                    Toast.makeText(ctx, "CVV must be 3 digits.", Toast.LENGTH_LONG).show()
                else -> placeOrder(ctx, cartProducts)
            }
        }) {
            Text("Place Order")
        }
    }

}

fun String.isValidExpiryDate(): Boolean {
    if (!this.matches(Regex("\\d{2}/\\d{2}"))) return false

    return try {
        val expiry = SimpleDateFormat("MM/yy", Locale.US).parse(this)
        expiry != null && expiry.after(Date())
    } catch (e: Exception) {
        false
    }
}
private fun placeOrder(context: Context, cartProducts: List<CartProduct>) {


    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db_reference = FirebaseDatabase.getInstance().getReference("Orders")

    val order_id = db_reference.push().key ?: return
    val totalAmount = cartProducts.sumOf { it.price * it.quantity }

    val orderInfo = mapOf(
        "order_id" to order_id,
        "userId" to uid,
        "products" to cartProducts,
        "totalAmount" to totalAmount
    )
    db_reference.child(order_id).setValue(orderInfo)
        .addOnSuccessListener {
            val db = SqliteDb(context)
            db.empty_cart()
            Toast.makeText(context,"Order Placed Successfully!",Toast.LENGTH_LONG).show()
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        .addOnFailureListener {
                         Toast.makeText(context,"Order Failed To Placed!",Toast.LENGTH_LONG).show()
        }

}



