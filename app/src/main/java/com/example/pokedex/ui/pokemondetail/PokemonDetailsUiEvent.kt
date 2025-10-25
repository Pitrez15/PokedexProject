package com.example.pokedex.ui.pokemondetail

import com.example.pokedex.data.remote.responses.Pokemon
import com.example.pokedex.util.Resource

sealed interface PokemonDetailsUiEvent {
    object OnBackPressed: PokemonDetailsUiEvent
    data class GetPokemonDetails(val pokemonName: String, val onFinish: (Resource<Pokemon>) -> Unit): PokemonDetailsUiEvent
}
