package com.test.onlinefurniturestore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme

class activity_admin_panel : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnlineFurnitureStoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    admin_panel_composable()
                }
            }
        }
    }
}

@Composable
fun admin_panel_composable() {
    var context= LocalContext.current;
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Admin Panel",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = {

                val intent = Intent(context, activity_add_product::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Add Product")
        }
        Button(
            onClick = {
                val intent = Intent(context, activity_view_products::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("View Products")
        }
    }
}
