package com.kenjkenta0813sketch.tokimeitenabi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.search_input)
        findViewById<View>(R.id.search_button).setOnClickListener {
            openInMaps(searchInput.text.toString())
        }
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openInMaps(searchInput.text.toString())
                true
            } else {
                false
            }
        }

        findViewById<View>(R.id.category_cafe).setOnClickListener {
            openInMaps(getString(R.string.category_cafe))
        }
        findViewById<View>(R.id.category_shopping).setOnClickListener {
            openInMaps(getString(R.string.category_shopping))
        }
        findViewById<View>(R.id.category_park).setOnClickListener {
            openInMaps(getString(R.string.category_park))
        }
        findViewById<View>(R.id.category_museum).setOnClickListener {
            openInMaps(getString(R.string.category_museum))
        }
        findViewById<View>(R.id.suggestion_omotesando).setOnClickListener {
            openInMaps(getString(R.string.suggestion_omotesando_title))
        }
        findViewById<View>(R.id.suggestion_kamakura).setOnClickListener {
            openInMaps(getString(R.string.suggestion_kamakura_title))
        }
        findViewById<View>(R.id.suggestion_kyoto).setOnClickListener {
            openInMaps(getString(R.string.suggestion_kyoto_title))
        }
    }

    private fun openInMaps(query: String) {
        val searchQuery = query.trim()
        if (searchQuery.isEmpty()) {
            Toast.makeText(this, R.string.search_empty_message, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:0,0?q=${Uri.encode(searchQuery)}")
        )
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.maps_unavailable_message, Toast.LENGTH_LONG).show()
        }
    }
}
