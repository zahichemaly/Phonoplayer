package com.zc.phonoplayer.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zc.phonoplayer.BuildConfig
import com.zc.phonoplayer.R
import com.zc.phonoplayer.util.SharedPreferencesUtil
import kotlinx.android.synthetic.main.settings_activity.*


class SettingsActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_SETTINGS = 1004
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == Activity.RESULT_OK) {
            val frag = supportFragmentManager.findFragmentById(R.id.settings) as SettingsFragment
            frag.setupManageTabs()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var appNamePref: EditTextPreference
        private lateinit var themePref: ListPreference
        private lateinit var aboutPrefs: Preference
        private lateinit var manageTabsPrefs: Preference
        private lateinit var sharedPreferencesUtil: SharedPreferencesUtil

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            appNamePref = findPreference(getString(R.string.pref_key_settings_app_name))!!
            themePref = findPreference(getString(R.string.pref_key_settings_theme))!!
            aboutPrefs = findPreference(getString(R.string.pref_key_settings_about))!!
            manageTabsPrefs = findPreference(getString(R.string.pref_key_settings_tabs))!!
            sharedPreferencesUtil = SharedPreferencesUtil(requireContext(), preferenceManager.sharedPreferences)

            aboutPrefs.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zahichemaly"))
                startActivity(intent)
                true
            }
            themePref.setOnPreferenceChangeListener { _, newValue ->
                when (newValue) {
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                true
            }
            manageTabsPrefs.setOnPreferenceClickListener {
                val intent = Intent(requireActivity(), TabSettingsActivity::class.java)
                requireActivity().startActivityForResult(intent, REQUEST_CODE_SETTINGS)
                true
            }
            setupManageTabs()
        }

        fun setupManageTabs() {
            val tabItemSummary = sharedPreferencesUtil.getTabItems().filter {
                it.isSelected
            }.joinToString { it.text }
            manageTabsPrefs.summary = tabItemSummary
        }
    }

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    fun sendFeedback(text: String) {
        var body: String? = null
        try {
            body = packageManager.getPackageInfo(packageName, 0).versionName
            body += "-----------------------------------\n" +
                    "Please don't remove this information\n" +
                    "Device OS version: ${Build.VERSION.RELEASE}\n" +
                    "App Name: ${BuildConfig.VERSION_NAME}\n" +
                    "App Version: ${BuildConfig.VERSION_CODE}\n" +
                    "Device Brand: ${Build.BRAND}\n" +
                    "Device Model: ${Build.MODEL}\n" +
                    "Device Manufacturer: ${Build.MANUFACTURER}\n" +
                    "-----------------------------------\n"
            body += text
        } catch (e: PackageManager.NameNotFoundException) {
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("chemalyzahi@hotmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app")
        intent.putExtra(Intent.EXTRA_TEXT, body)
        startActivity(Intent.createChooser(intent, getString(R.string.choose_email_client)))
    }
}
