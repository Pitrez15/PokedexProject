package com.example.pokedex.ui.pokemonlist

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.pokedex.data.models.PokedexListEntry
import com.example.pokedex.repository.PokemonRepository
import com.example.pokedex.util.Constants.PAGE_SIZE
import com.example.pokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private var _pokemonListState = MutableStateFlow(PokemonListState())
    val pokemonListState get() = _pokemonListState.asStateFlow()

    private var currentPage = 0

    init {
        loadPokemonPaginated()
    }

    fun searchPokemonList(query: String) {
        val listToSearch = if (_pokemonListState.value.isSearchStarted) {
            _pokemonListState.value.pokemonList
        } else {
            _pokemonListState.value.cachedPokemonList
        }

        viewModelScope.launch(Dispatchers.Default) {
            if (query.isEmpty()) {
                _pokemonListState.update {
                    it.copy(
                        pokemonList = it.cachedPokemonList,
                        isSearching = false,
                        isSearchStarted = true
                    )
                }
                return@launch
            }

            val results = listToSearch.filter {
                it.pokemonName.contains(query.trim(), ignoreCase = true) ||
                        it.number.toString() == query.trim()
            }

            if (_pokemonListState.value.isSearchStarted) {
                _pokemonListState.update {
                    it.copy(
                        cachedPokemonList = it.pokemonList,
                        isSearchStarted = false
                    )
                }
            }

            _pokemonListState.update {
                it.copy(
                    pokemonList = results,
                    isSearching = true
                )
            }
        }
    }

    fun loadPokemonPaginated() {
        viewModelScope.launch {
            _pokemonListState.update { it.copy(isLoading = true) }
            val result = repository.getPokemonList(PAGE_SIZE, currentPage * PAGE_SIZE)
            when (result) {
                is Resource.Success -> {
                    _pokemonListState.update { it.copy(endReached = currentPage * PAGE_SIZE >= result.data!!.count) }
                    val pokedexEntries = result.data?.results?.mapIndexed { index, entry ->
                        val number = if (entry.url.endsWith("/")) {
                            entry.url.dropLast(1).takeLastWhile { it.isDigit() }
                        } else {
                            entry.url.takeLastWhile { it.isDigit() }
                        }
                        val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${number}.png"
                        PokedexListEntry(entry.name.capitalize(Locale.ROOT), url, number.toInt())
                    } ?: emptyList()

                    _pokemonListState.update { curState ->
                        val endReached = currentPage * PAGE_SIZE >= result.data!!.count
                        curState.copy(
                            isLoading = false,
                            loadError = "",
                            pokemonList = curState.pokemonList + pokedexEntries,
                            endReached = endReached,
                            cachedPokemonList = curState.pokemonList + pokedexEntries,
                        )
                    }

                    currentPage++
                }
                is Resource.Error -> {
                    _pokemonListState.update {
                        it.copy(
                            isLoading = false,
                            loadError = result.message!!
                        )
                    }
                }
                is Resource.Loading<*> -> TODO()
            }
        }
    }

    fun calcDominantColor(bitmap: Bitmap, onFinish: (Color) -> Unit) {
        val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }

    fun resetSearch() {
        _pokemonListState.update {
            it.copy(
                pokemonList = it.cachedPokemonList,
                isSearchStarted = true,
                isSearching = false,
                searchQuery = ""
            )
        }
    }
}
