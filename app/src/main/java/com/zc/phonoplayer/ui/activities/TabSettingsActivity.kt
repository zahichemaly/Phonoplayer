package com.zc.phonoplayer.ui.activities

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.zc.phonoplayer.R
import com.zc.phonoplayer.ui.components.ManageListPreference
import kotlinx.android.synthetic.main.settings_activity.*

class TabSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tab_settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.tab_settings, TabSettingsFragment()).commit()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    class TabSettingsFragment : PreferenceFragmentCompat() {
        private lateinit var manageListPref: ManageListPreference
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.tabs_preferences, rootKey)
            manageListPref = findPreference(getString(R.string.pref_key_tab_settings_tab))!!
            manageListPref.setOnPreferenceChangeListener { _, _ ->
                requireActivity().setResult(Activity.RESULT_OK)
                true
            }
        }
    }
}