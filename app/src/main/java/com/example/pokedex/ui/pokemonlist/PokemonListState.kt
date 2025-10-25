package com.example.pokedex.ui.pokemonlist

import com.example.pokedex.data.models.PokedexListEntry

data class PokemonListState(
    var pokemonList: List<PokedexListEntry> = listOf(),
    var loadError: String = "",
    var isLoading: Boolean = false,
    var endReached: Boolean = false,
    var cachedPokemonList: List<PokedexListEntry> = listOf(),
    var isSearchStarted: Boolean = true,
    var isSearching: Boolean = false,
    var searchQuery: String = ""
)
