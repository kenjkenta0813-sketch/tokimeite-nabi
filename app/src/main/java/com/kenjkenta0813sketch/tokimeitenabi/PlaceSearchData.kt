package com.kenjkenta0813sketch.tokimeitenabi

import android.content.res.AssetManager
import android.content.SharedPreferences
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class Prefecture(val code: String, val name: String)

data class Municipality(val code: String, val name: String, val prefectureCode: String)

data class FavoritePlace(
    val query: String,
    val prefecture: String,
    val municipality: String,
    val genre: String
)

enum class PlaceGenre(val label: String, val searchTerm: String) {
    SIGHTSEEING("観光", "観光スポット"),
    GOURMET("グルメ", "グルメ"),
    MUSEUM("美術館・博物館", "美術館 博物館"),
    PARK("公園", "公園"),
    HOT_SPRING("温泉", "温泉"),
    SHOPPING("ショッピング", "ショッピング"),
    CAFE("カフェ", "カフェ")
}

data class MunicipalityCatalog(
    val prefectures: List<Prefecture>,
    val municipalities: List<Municipality>
) {
    fun municipalitiesFor(prefecture: Prefecture): List<Municipality> =
        municipalities.filter { it.prefectureCode == prefecture.code }

    companion object {
        fun parse(contents: String): MunicipalityCatalog {
            val records = contents.lineSequence()
                .filter(String::isNotBlank)
                .mapIndexed { index, line ->
                    val fields = line.split('\t')
                    require(fields.size == 4 && fields.all(String::isNotBlank)) {
                        "Invalid municipality data on line ${index + 1}"
                    }
                    fields
                }
                .toList()

            val municipalities = records.map { Municipality(it[2], it[3], it[0]) }
            val prefectureRows = records.distinctBy { it[0] }
                .map { Prefecture(it[0], it[1]) }
                .toList()

            require(municipalities.isNotEmpty() && prefectureRows.isNotEmpty()) {
                "Municipality dataset is empty"
            }
            require(municipalities.map { it.prefectureCode to it.code }.distinct().size == municipalities.size) {
                "Municipality dataset contains duplicate entries"
            }
            return MunicipalityCatalog(prefectureRows, municipalities)
        }

        fun load(assets: AssetManager): MunicipalityCatalog =
            assets.open("municipalities.tsv").bufferedReader(StandardCharsets.UTF_8).use {
                parse(it.readText())
            }
    }
}

object MapIntentFactory {
    fun searchQuery(
        prefecture: String,
        municipality: String,
        genre: String,
        keyword: String
    ): String = listOf(prefecture, municipality, genre, keyword.trim())
        .filter(String::isNotBlank)
        .joinToString(" ")

    fun searchGeoUri(query: String): String = "geo:0,0?q=${encodeQuery(query)}"

    fun drivingNavigationUri(destination: String): String =
        "google.navigation:q=${encodeQuery(destination)}&mode=d"

    fun googleSearchUrl(query: String): String =
        "https://www.google.com/maps/search/?api=1&query=${encodeQuery(query)}"

    fun googleDrivingUrl(destination: String): String =
        "https://www.google.com/maps/dir/?api=1&destination=${encodeQuery(destination)}&travelmode=driving"

    private fun encodeQuery(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20")
}

object FavoriteCodec {
    fun encode(favorites: List<FavoritePlace>): String =
        favorites.joinToString("\n") { favorite ->
            listOf(favorite.query, favorite.prefecture, favorite.municipality, favorite.genre)
                .joinToString("\t", transform = ::encodeField)
        }

    fun decode(contents: String): List<FavoritePlace> {
        if (contents.isBlank()) return emptyList()
        return contents.lineSequence().filter(String::isNotBlank).mapIndexed { index, line ->
            val fields = line.split('\t')
            require(fields.size == 4) { "Invalid favorite data on line ${index + 1}" }
            FavoritePlace(
                decodeField(fields[0]),
                decodeField(fields[1]),
                decodeField(fields[2]),
                decodeField(fields[3])
            )
        }.toList()
    }

    private fun encodeField(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())

    private fun decodeField(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}

class FavoritesStore(private val preferences: SharedPreferences) {
    fun all(): List<FavoritePlace> = FavoriteCodec.decode(preferences.getString(KEY, "").orEmpty())

    fun add(favorite: FavoritePlace): Boolean {
        val favorites = all()
        if (favorites.any { it.query == favorite.query }) return false
        check(preferences.edit().putString(KEY, FavoriteCodec.encode(favorites + favorite)).commit()) {
            "Unable to save favorite"
        }
        return true
    }

    fun remove(favorite: FavoritePlace) {
        val favorites = all()
        preferences.edit()
            .putString(KEY, FavoriteCodec.encode(favorites.filterNot { it == favorite }))
            .apply()
    }

    companion object {
        const val PREFERENCES_NAME = "saved_places"
        private const val KEY = "favorites_v1"
    }
}
