package com.example.pokedex.ui.pokemonlist

import androidx.compose.ui.graphics.Color
import coil3.Bitmap
import com.example.pokedex.data.models.PokedexListEntry

sealed interface PokemonListUiEvent {
    object TriggerLoadPaginated: PokemonListUiEvent
    data class OnPokemonSearched(val query: String): PokemonListUiEvent
    data class CalcDominantColor(val bitmap: Bitmap, val onFinish: (Color) -> Unit): PokemonListUiEvent
    data class OnPokemonClicked(val dominantColor: Color, val pokedexListEntry: PokedexListEntry): PokemonListUiEvent
}
