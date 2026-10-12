package com.kenjkenta0813sketch.tokimeitenabi

import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceSearchDataTest {
    @Test
    fun bundledCatalogContainsAllPrefecturesAndMunicipalities() {
        val contents = File("src/main/assets/municipalities.tsv").readText(Charsets.UTF_8)
        val catalog = MunicipalityCatalog.parse(contents)

        assertEquals(47, catalog.prefectures.size)
        assertEquals(1747, catalog.municipalities.size)
        assertEquals(1747, catalog.municipalities.map(Municipality::code).distinct().size)
        assertTrue(catalog.prefectures.all { catalog.municipalitiesFor(it).isNotEmpty() })

        val tokyo = catalog.prefectures.single { it.name == "東京都" }
        val tokyoMunicipalities = catalog.municipalitiesFor(tokyo)
        assertEquals(23, tokyoMunicipalities.count { it.name.endsWith("区") })
        assertTrue(tokyoMunicipalities.any { it.name == "八丈町" })
    }

    @Test
    fun searchQueryAndMapUrisKeepTheSelectedDestination() {
        val query = MapIntentFactory.searchQuery("東京都", "千代田区", "美術館 博物館", "国立新美術館")

        assertEquals("東京都 千代田区 美術館 博物館 国立新美術館", query)
        assertTrue(MapIntentFactory.searchGeoUri(query).startsWith("geo:0,0?q="))
        assertTrue(MapIntentFactory.googleSearchUrl(query).contains("%E6%9D%B1%E4%BA%AC%E9%83%BD"))
        assertTrue(MapIntentFactory.drivingNavigationUri(query).endsWith("&mode=d"))
        assertTrue(MapIntentFactory.googleDrivingUrl(query).endsWith("&travelmode=driving"))
        assertEquals(
            query,
            URLDecoder.decode(
                MapIntentFactory.drivingNavigationUri(query).removePrefix("google.navigation:q=").substringBefore("&mode="),
                StandardCharsets.UTF_8.name()
            )
        )
    }

    @Test
    fun favoriteCodecRoundTripsJapaneseAndControlCharacters() {
        val favorites = listOf(
            FavoritePlace("東京都 千代田区 カフェ\t朝食\n散歩", "東京都", "千代田区", "カフェ"),
            FavoritePlace("京都府 京都市 観光", "京都府", "京都市", "観光")
        )

        assertEquals(favorites, FavoriteCodec.decode(FavoriteCodec.encode(favorites)))
        assertTrue(FavoriteCodec.decode("").isEmpty())
    }
}
