package com.test.onlinefurniturestore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun wait_dialog(show_dialog: Boolean, onDismissRequest: () -> Unit) {
    if (show_dialog) {
        Dialog(onDismissRequest = onDismissRequest) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 150.dp, height = 170.dp)
                    .background(color = Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(15.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Loading"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Please wait . . .", color = Color.Black)
                }
            }
        }
    }
}
