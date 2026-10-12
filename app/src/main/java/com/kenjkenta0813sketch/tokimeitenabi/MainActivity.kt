package com.kenjkenta0813sketch.tokimeitenabi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup

class MainActivity : AppCompatActivity() {
    private lateinit var prefectureSpinner: Spinner
    private lateinit var municipalitySpinner: Spinner
    private lateinit var genreGroup: ChipGroup
    private lateinit var keywordInput: EditText
    private lateinit var searchButton: MaterialButton
    private lateinit var directionsButton: MaterialButton
    private lateinit var saveFavoriteButton: MaterialButton
    private lateinit var favoritesContainer: LinearLayout
    private lateinit var favoritesEmpty: TextView
    private lateinit var favoritesStore: FavoritesStore

    private val catalog by lazy { MunicipalityCatalog.load(assets) }
    private var selectedPrefecture: Prefecture? = null
    private var selectedMunicipality: Municipality? = null
    private var selectedGenre: PlaceGenre? = null

    private val genresByChipId: Map<Int, PlaceGenre> by lazy {
        mapOf(
            R.id.genre_sightseeing to PlaceGenre.SIGHTSEEING,
            R.id.genre_gourmet to PlaceGenre.GOURMET,
            R.id.genre_museum to PlaceGenre.MUSEUM,
            R.id.genre_park to PlaceGenre.PARK,
            R.id.genre_hot_spring to PlaceGenre.HOT_SPRING,
            R.id.genre_shopping to PlaceGenre.SHOPPING,
            R.id.genre_cafe to PlaceGenre.CAFE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefectureSpinner = findViewById(R.id.prefecture_spinner)
        municipalitySpinner = findViewById(R.id.municipality_spinner)
        genreGroup = findViewById(R.id.genre_group)
        keywordInput = findViewById(R.id.keyword_input)
        searchButton = findViewById(R.id.search_button)
        directionsButton = findViewById(R.id.directions_button)
        saveFavoriteButton = findViewById(R.id.save_favorite_button)
        favoritesContainer = findViewById(R.id.favorites_container)
        favoritesEmpty = findViewById(R.id.favorites_empty)
        favoritesStore = FavoritesStore(getSharedPreferences(FavoritesStore.PREFERENCES_NAME, MODE_PRIVATE))

        setupSelectors()
        searchButton.setOnClickListener { searchSelectedPlaces() }
        directionsButton.setOnClickListener { navigateToSelectedPlaces() }
        saveFavoriteButton.setOnClickListener { saveSelectedFavorite() }
        keywordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchSelectedPlaces()
                true
            } else {
                false
            }
        }
        setupRecommendations()
        renderFavorites()
    }

    private fun setupSelectors() {
        prefectureSpinner.adapter = spinnerAdapter(
            listOf(getString(R.string.select_prefecture)) + catalog.prefectures.map(Prefecture::name)
        )
        municipalitySpinner.adapter = spinnerAdapter(listOf(getString(R.string.select_municipality)))
        municipalitySpinner.isEnabled = false
        setGenreSelectionEnabled(false)
        updateActionButtons()

        prefectureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val nextPrefecture = catalog.prefectures.getOrNull(position - 1)
                if (selectedPrefecture?.code == nextPrefecture?.code) return

                selectedPrefecture = nextPrefecture
                selectedMunicipality = null
                selectedGenre = null
                genreGroup.clearCheck()
                val municipalities = nextPrefecture?.let(catalog::municipalitiesFor).orEmpty()
                municipalitySpinner.adapter = spinnerAdapter(
                    listOf(getString(R.string.select_municipality)) + municipalities.map(Municipality::name)
                )
                municipalitySpinner.isEnabled = nextPrefecture != null
                setGenreSelectionEnabled(false)
                updateActionButtons()
            }
        }

        municipalitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val municipalities = selectedPrefecture?.let(catalog::municipalitiesFor).orEmpty()
                selectedMunicipality = municipalities.getOrNull(position - 1)
                selectedGenre = null
                genreGroup.clearCheck()
                setGenreSelectionEnabled(selectedMunicipality != null)
                updateActionButtons()
            }
        }

        genreGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedGenre = checkedIds.firstNotNullOfOrNull(genresByChipId::get)
            updateActionButtons()
        }
    }

    private fun spinnerAdapter(items: List<String>): ArrayAdapter<String> =
        ArrayAdapter(this, android.R.layout.simple_spinner_item, items).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    private fun setGenreSelectionEnabled(enabled: Boolean) {
        genreGroup.isEnabled = enabled
        for (index in 0 until genreGroup.childCount) {
            genreGroup.getChildAt(index).isEnabled = enabled
        }
    }

    private fun updateActionButtons() {
        val canSearch = selectedMunicipality != null && selectedGenre != null
        searchButton.isEnabled = canSearch
        directionsButton.isEnabled = canSearch
        saveFavoriteButton.isEnabled = canSearch
    }

    private fun selectedSearchQuery(): String? {
        val prefecture = selectedPrefecture ?: return null
        val municipality = selectedMunicipality ?: return null
        val genre = selectedGenre ?: return null
        return MapIntentFactory.searchQuery(
            prefecture.name,
            municipality.name,
            genre.searchTerm,
            keywordInput.text.toString()
        )
    }

    private fun searchSelectedPlaces() {
        val query = selectedSearchQuery()
        if (query == null) {
            showToast(R.string.selection_required_message)
            return
        }
        openGoogleMapsSearch(query)
    }

    private fun navigateToSelectedPlaces() {
        val query = selectedSearchQuery()
        if (query == null) {
            showToast(R.string.selection_required_message)
            return
        }
        openDrivingDirections(query)
    }

    private fun openGoogleMapsSearch(query: String) {
        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(MapIntentFactory.searchGeoUri(query)))
            .setPackage(GOOGLE_MAPS_PACKAGE)
        try {
            startActivity(mapsIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MapIntentFactory.googleSearchUrl(query))))
            } catch (_: ActivityNotFoundException) {
                showToast(R.string.maps_unavailable_message)
            }
        }
    }

    private fun openDrivingDirections(destination: String) {
        val navigationIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(MapIntentFactory.drivingNavigationUri(destination))
        ).setPackage(GOOGLE_MAPS_PACKAGE)
        try {
            startActivity(navigationIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MapIntentFactory.googleDrivingUrl(destination))))
            } catch (_: ActivityNotFoundException) {
                showToast(R.string.maps_unavailable_message)
            }
        }
    }

    private fun saveSelectedFavorite() {
        val prefecture = selectedPrefecture ?: return
        val municipality = selectedMunicipality ?: return
        val genre = selectedGenre ?: return
        val query = selectedSearchQuery() ?: return
        val favorite = FavoritePlace(query, prefecture.name, municipality.name, genre.label)
        try {
            if (favoritesStore.add(favorite)) {
                showToast(R.string.favorite_saved_message)
                renderFavorites()
            } else {
                showToast(R.string.favorite_exists_message)
            }
        } catch (_: IllegalArgumentException) {
            showToast(R.string.favorites_load_error)
        } catch (_: IllegalStateException) {
            showToast(R.string.favorites_load_error)
        }
    }

    private fun renderFavorites() {
        val favorites = try {
            favoritesStore.all()
        } catch (_: IllegalArgumentException) {
            showToast(R.string.favorites_load_error)
            emptyList()
        }
        favoritesContainer.removeAllViews()
        favoritesEmpty.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE

        favorites.forEach { favorite ->
            val card = MaterialCardView(this).apply {
                radius = resources.getDimension(R.dimen.favorite_card_corner_radius)
                cardElevation = resources.getDimension(R.dimen.favorite_card_elevation)
                setCardBackgroundColor(getColor(R.color.app_surface))
                strokeColor = getColor(R.color.app_accent)
                strokeWidth = resources.getDimensionPixelSize(R.dimen.favorite_card_stroke_width)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.favorite_card_spacing)
                }
            }
            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.favorite_card_padding),
                    resources.getDimensionPixelSize(R.dimen.favorite_card_padding),
                    resources.getDimensionPixelSize(R.dimen.favorite_card_padding),
                    resources.getDimensionPixelSize(R.dimen.favorite_card_padding)
                )
            }
            content.addView(TextView(this).apply {
                text = favorite.query
                setTextColor(getColor(R.color.app_text))
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            content.addView(TextView(this).apply {
                text = getString(
                    R.string.favorite_location_format,
                    favorite.prefecture,
                    favorite.municipality,
                    favorite.genre
                )
                setTextColor(getColor(R.color.app_text_secondary))
                textSize = 14f
            })

            val actions = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            actions.addView(favoriteActionButton(
                R.string.favorite_open_map,
                getString(R.string.favorite_open_map_accessibility, favorite.query)
            ) { openGoogleMapsSearch(favorite.query) })
            actions.addView(favoriteActionButton(
                R.string.favorite_driving,
                getString(R.string.favorite_driving_accessibility, favorite.query)
            ) { openDrivingDirections(favorite.query) })
            actions.addView(favoriteActionButton(
                R.string.favorite_remove,
                getString(R.string.favorite_remove_accessibility, favorite.query),
                true
            ) {
                try {
                    favoritesStore.remove(favorite)
                    renderFavorites()
                } catch (_: IllegalArgumentException) {
                    showToast(R.string.favorites_load_error)
                } catch (_: IllegalStateException) {
                    showToast(R.string.favorites_load_error)
                }
            })
            content.addView(actions)
            card.addView(content)
            favoritesContainer.addView(card)
        }
    }

    private fun favoriteActionButton(
        textRes: Int,
        accessibilityText: String,
        outlined: Boolean = false,
        onClick: () -> Unit
    ): MaterialButton = MaterialButton(
        this,
        null,
        if (outlined) com.google.android.material.R.attr.materialButtonOutlinedStyle else 0
    ).apply {
        text = getString(textRes)
        contentDescription = accessibilityText
        isAllCaps = false
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = resources.getDimensionPixelSize(R.dimen.favorite_action_spacing)
        }
    }

    private fun setupRecommendations() {
        findViewById<View>(R.id.suggestion_omotesando).setOnClickListener {
            openGoogleMapsSearch(getString(R.string.suggestion_omotesando_title))
        }
        findViewById<View>(R.id.suggestion_kamakura).setOnClickListener {
            openGoogleMapsSearch(getString(R.string.suggestion_kamakura_title))
        }
        findViewById<View>(R.id.suggestion_kyoto).setOnClickListener {
            openGoogleMapsSearch(getString(R.string.suggestion_kyoto_title))
        }
    }

    private fun showToast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }
}
