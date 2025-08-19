import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelapp.data.UserPreferences
import com.example.travelapp.data.model.User
import com.example.travelapp.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(viewModel: AppViewModel, onRegisterSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrazione", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            Log.d("RegisterScreen", "Click Registrati: name=$name, email=$email, password=$password")

            val newUser = User(id = 0, name = name, email = email, password = password)

            try {
                viewModel.registerUser(newUser) { addedUser ->
                    Log.d("RegisterScreen", "Utente registrato: $addedUser")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Log.d("RegisterScreen", "Salvataggio ID su DataStore: ${addedUser.id}")
                            UserPreferences.saveUserId(context, addedUser.id)
                            withContext(Dispatchers.Main) {
                                Log.d("RegisterScreen", "Chiamo onRegisterSuccess()")
                                onRegisterSuccess()
                            }
                        } catch (e: Exception) {
                            Log.e("RegisterScreen", "Errore salvataggio DataStore", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterScreen", "Errore registerUser", e)
            }

        }, modifier = Modifier.fillMaxWidth()) {
            Text("Registrati")
        }
    }
}
