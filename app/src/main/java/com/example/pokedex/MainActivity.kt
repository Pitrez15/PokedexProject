package com.example.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokedex.ui.pokemondetail.PokemonDetailScreen
import com.example.pokedex.ui.pokemondetail.PokemonDetailViewModel
import com.example.pokedex.ui.pokemondetail.PokemonDetailsUiEvent
import com.example.pokedex.ui.pokemonlist.PokemonListScreen
import com.example.pokedex.ui.pokemonlist.PokemonListUiEvent
import com.example.pokedex.ui.pokemonlist.PokemonListViewModel
import com.example.pokedex.ui.theme.JetpackComposePokedexTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pokemonListViewModel by viewModels<PokemonListViewModel>()
    private val pokemonDetailViewModel by viewModels<PokemonDetailViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JetpackComposePokedexTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "pokemon_list_screen") {
                    composable("pokemon_list_screen") {
                        PokemonListScreen(state = pokemonListViewModel.pokemonListState.collectAsState().value) {
                            pokemonListUiEvent(it, navController)
                        }
                    }
                    composable(
                        "pokemon_detail_screen/{dominantColor}/{pokemonName}",
                        listOf(
                            navArgument("dominantColor") {
                                type = NavType.IntType
                            },
                            navArgument("pokemonName") {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        val dominantColor = remember {
                            val color = it.arguments?.getInt("dominantColor")
                            color?.let { Color(it) } ?: Color.White
                        }
                        val pokemonName = remember {
                            it.arguments?.getString("pokemonName")
                        }
                        PokemonDetailScreen(
                            dominantColor = dominantColor,
                            pokemonName = pokemonName?.lowercase(Locale.ROOT) ?: "",
                        ) { pokemonDetailsUiEvent(event = it, navController) }
                    }
                }
            }
        }
    }

    private fun pokemonListUiEvent(event: PokemonListUiEvent, navController: NavController) {
        when (event) {
            is PokemonListUiEvent.TriggerLoadPaginated -> {
                pokemonListViewModel.loadPokemonPaginated()
            }
            is PokemonListUiEvent.OnPokemonSearched -> {
                pokemonListViewModel.searchPokemonList(event.query)
            }
            is PokemonListUiEvent.CalcDominantColor -> {
                pokemonListViewModel.calcDominantColor(event.bitmap, event.onFinish)
            }
            is PokemonListUiEvent.OnPokemonClicked -> {
                navController.navigate("pokemon_detail_screen/${event.dominantColor.toArgb()}/${event.pokedexListEntry.pokemonName}")
                pokemonListViewModel.resetSearch()
            }
        }
    }

    private fun pokemonDetailsUiEvent(event: PokemonDetailsUiEvent, navController: NavController) {
        when (event) {
            is PokemonDetailsUiEvent.OnBackPressed -> {
                navController.popBackStack()
            }
            is PokemonDetailsUiEvent.GetPokemonDetails -> {
                lifecycleScope.launch {
                    pokemonDetailViewModel.getPokemonInfo(event.pokemonName, event.onFinish)
                }
            }
        }
    }
}
