package com.test.onlinefurniturestore

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.rememberImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.test.onlinefurniturestore.ui.theme.OnlineFurnitureStoreTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnlineFurnitureStoreTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}



@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        MainNavGraph(navController, Modifier.padding(innerPadding))
    }
}

enum class BottomNavItems(val route: String, val icon: ImageVector, val label: String) {
    Home("home", Icons.Default.Home, "Home"),
    Orders("orders", Icons.Default.List, "Orders"),
    Profile("profile", Icons.Default.Person, "My Profile")
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = BottomNavItems.values()
    val currentRoute = getCurrentRoute(navController)
    BottomNavigation {
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
@Composable
fun getCurrentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}


@Composable
fun MainNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = BottomNavItems.Home.route, modifier = modifier) {
        composable(BottomNavItems.Home.route) { HomeScreen() }
        composable(BottomNavItems.Orders.route) { OrdersScreen() }
        composable(BottomNavItems.Profile.route) { ProfileScreen() }
    }
}


@Composable
fun HomeScreen() {
    val cartCount = remember { mutableStateOf(0) }
    val context = LocalContext.current
    val db = remember { SqliteDb(context) }
    var textSearch by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Product>>(listOf()) }
    var isFetching by remember { mutableStateOf(false) }


    var allProducts by remember { mutableStateOf<List<Product>>(listOf()) }
    var recommendedProducts by remember { mutableStateOf<List<Product>>(listOf()) }

    LaunchedEffect(key1 = Unit) {
        allProducts = fetchAllProducts()
        recommendedProducts = allProducts.shuffled().take(2)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleObserver = remember {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cartCount.value = db.get_count()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    LaunchedEffect(key1 = Unit) {
        cartCount.value = db.get_count()
    }


    val scrollState = rememberScrollState()

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
    ) {


        Box(modifier = Modifier
            .fillMaxWidth()
            .height(250.dp))
        {
            // Banner image
            Image(
                painter = painterResource(id = R.drawable.furniture_store),
                contentDescription = "Promotional Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search TextField
                    OutlinedTextField(
                        value = textSearch,
                        onValueChange = { textSearch = it },
                        placeholder = { Text("Search product") },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (textSearch.isBlank()) {
                                    Toast.makeText(context, "Please enter a search term.", Toast.LENGTH_LONG).show()
                                } else {
                                    isFetching = true
                                    searchProducts(textSearch) { results ->
                                        searchResults = results
                                        isFetching = false

                                        val intent = Intent(context, activity_products::class.java).apply {
                                            putExtra("searchResults", ArrayList(searchResults))
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }


                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            cursorColor = Color.White,
                            trailingIconColor = Color.White,
                            backgroundColor = Color(0x55FFFFFF),
                            placeholderColor = Color.White
                        ),
                        textStyle = TextStyle(color = Color.White)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    // Cart Icon
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = {

                            val intent = Intent(context, activity_cart::class.java)
                            context.startActivity(intent)

                        }) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color.White
                            )
                        }

                        if (cartCount.value > 0) {
                            Badge(content = cartCount.value.toString())
                        }
                    }

                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "THE SALE OF THE YEAR\nBLACK FRIDAY",
                        style = MaterialTheme.typography.h5.copy(color = Color.White),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, activity_products::class.java)
                    context.startActivity(intent)

                },
                modifier = Modifier
                    .wrapContentSize(),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Shop Now",
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp)
                )
            }
        }


        CategoryGrid()


        Text(
            "Recommended for You",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            style = MaterialTheme.typography.h6
        )
      ProductGrid(products = recommendedProducts)
       /* Column {
            recommendedProducts.forEach { product ->
                ProductItem(product)
            }
        }*/


    }

    if (isFetching) {
        wait_dialog(show_dialog = isFetching, onDismissRequest = { /*...*/ })
    }
}

@Composable
fun ProductGrid(products: List<Product>) {
    val context = LocalContext.current

    val rows = (products.size + 1) / 2
    val gridHeight = 220.dp * rows

    Box(modifier = Modifier.height(gridHeight)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            items(products) { product ->
                ProductItem(product) {
                    val intent = Intent(context, activity_product_detail::class.java)
                    intent.putExtra("pid", product.pid)
                    intent.putExtra("pname", product.name)
                    intent.putExtra("pprice", product.price)
                    intent.putExtra("pcategory", product.category)
                    intent.putExtra("pimage", product.imageUrl)
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberImagePainter(product.imageUrl),
                contentDescription = "Product Image",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Text(text = product.name, fontWeight = FontWeight.Bold)
            Text(text = "Price: ${product.price}")
        }
    }
}




@Composable
fun CategoryGrid() {
    val context = LocalContext.current
    val categories = listOf(
        Category("Chairs", R.drawable.chair, "from £59"),
        Category("Beds", R.drawable.bed, "from £59"),
        Category("Coffee Table", R.drawable.coffee_table, "from £59"),
        Category("Desk", R.drawable.desk, "from £59"),
        Category("Sofas", R.drawable.sofa, "from £649"),
        // Add more categories as needed
    )

    // Calculate the height needed for the grid
    val rows = (categories.size + 1) / 2  // Assuming 2 columns
    val gridHeight = 160.dp * rows  // Adjust the height per item as needed

    Box(modifier = Modifier.height(gridHeight)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            items(categories) { category ->
                CategoryItem(category) {
                    val intent = Intent(context, activity_products::class.java)
                    intent.putExtra("category", category.name)
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = category.imageRes),
                contentDescription = category.name,
                modifier = Modifier.size(100.dp) // Set a fixed size for the image
            )
            Text(text = category.name, fontWeight = FontWeight.Bold)
            Text(text = category.priceRange)
        }
    }
}

data class Category(val name: String, @DrawableRes val imageRes: Int, val priceRange: String)



@Composable
fun OrdersScreen() {
    val coroutineScope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<Order>>(listOf()) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    LaunchedEffect(key1 = userId) {
        userId?.let {
            coroutineScope.launch {
                val dbReference = FirebaseDatabase.getInstance().getReference("Orders")
                val snapshot = dbReference.orderByChild("userId").equalTo(userId).get().await()
                val ordersList = mutableListOf<Order>()
                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.getValue(Order::class.java)?.let { order ->
                        ordersList.add(order)
                    }
                }
                orders = ordersList
            }

        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        LazyColumn {
            itemsIndexed(orders) { index, order ->
                OrderCard(orderNumber = index + 1, order = order)
            }
        }
    }
}

@Composable
fun OrderCard(orderNumber: Int, order: Order) {
    var context= LocalContext.current;
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val intent = Intent(context, activity_order_detail::class.java)
                intent.putExtra("oid", order.order_id)
                context.startActivity(intent)
            },
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order Number: $orderNumber", style = MaterialTheme.typography.h6)
            Text("Total Amount: £${order.totalAmount}", style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap to see order details", style = MaterialTheme.typography.caption)
        }
    }
}


@Composable
fun ProfileScreen() {
    var textName by remember { mutableStateOf(TextFieldValue("")) }
    var textPhone by remember { mutableStateOf(TextFieldValue("")) }
    var textAddress by remember { mutableStateOf(TextFieldValue("")) }
    var textEmail by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(R.drawable.user),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name TextField
        ProfileTextField(
            label = "Name",
            value = textName,
            onValueChange = { textName = it }
        )

        // Phone TextField
        ProfileTextField(
            label = "Phone",
            value = textPhone,
            onValueChange = { textPhone = it }
        )

        // Address TextField
        ProfileTextField(
            label = "Address",
            value = textAddress,
            onValueChange = { textAddress = it }
        )

        // Email TextField
        ProfileTextField(
            label = "Email",
            value = textEmail,
            onValueChange = { textEmail = it }
        )

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(context, activity_login::class.java)
                context.startActivity(intent)

                if (context is Activity) {
                    context.finish()
                }

                      },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Logout")
        }
    }
    LaunchedEffect(userId) {
        userId?.let {
            val userData = fetchUserData(it, context)
            textName = TextFieldValue(userData.name)
            textPhone = TextFieldValue(userData.phone)
            textAddress = TextFieldValue(userData.address)
            textEmail = TextFieldValue(userData.email)
        }
    }

}

private suspend fun fetchUserData(userId: String, context: Context): User {
    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
    var user = User()
    val userDataSnapshot = dbRef.get().await() //
    if (userDataSnapshot.exists()) {
        user = userDataSnapshot.getValue(User::class.java) ?: User()
    }

    return user
}

@Composable
fun ProfileTextField(label: String, value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun Badge(content: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(16.dp)
            .background(Color.Red, shape = CircleShape)

    ) {
        Text(text = content, color = Color.White, fontSize = 10.sp)
    }
}

fun searchProducts(searchTerm: String, onComplete: (List<Product>) -> Unit) {
    val databaseReference = FirebaseDatabase.getInstance().getReference("products")
    val searchResults = mutableListOf<Product>()

    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.children.forEach { categorySnapshot ->
                categorySnapshot.children.forEach {
                    val product = it.getValue(Product::class.java)
                    product?.let { prod ->
                        if (prod.name.contains(searchTerm, ignoreCase = true)) {
                            searchResults.add(prod)
                        }
                    }
                }
            }
            onComplete(searchResults)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Handle errors
        }
    })
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
        System.out.println("Exceptionnn"+e.toString())
        listOf()
    }
}



data class Order(
    val order_id: String = "",
    val userId: String = "",
    val products: List<CartProduct> = emptyList(),
    val totalAmount: Double = 0.0
)

