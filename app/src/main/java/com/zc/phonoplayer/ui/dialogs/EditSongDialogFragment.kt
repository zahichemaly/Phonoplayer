package com.zc.phonoplayer.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.util.loadUri
import de.hdodenhof.circleimageview.CircleImageView

class EditSongDialogFragment : DialogFragment() {
    private var song: Song? = null
    private lateinit var songArt: CircleImageView

    companion object {
        private const val SONG_EDIT = "song_edit"
        private const val IMAGE_PICK_CODE = 1000

        fun newInstance(song: Song): EditSongDialogFragment {
            val frag = EditSongDialogFragment()
            val args = Bundle()
            args.putParcelable(SONG_EDIT, song)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        song = arguments?.getParcelable(SONG_EDIT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_dialog_edit_song, container, false)
        songArt = view.findViewById(R.id.album_art)
        val headerAlbumTv: TextView = view.findViewById(R.id.header_song_name)
        val headerArtistTv: TextView = view.findViewById(R.id.header_artist_name)
        val trackTv: EditText = view.findViewById(R.id.track_name)
        val albumTv: EditText = view.findViewById(R.id.album_name)
        val artistTv: EditText = view.findViewById(R.id.artist_name)
        val yearTv: EditText = view.findViewById(R.id.song_year)
        val genreTv: EditText = view.findViewById(R.id.song_genre)
        val cancelButton: Button = view.findViewById(R.id.cancel_button)
        val saveButton: Button = view.findViewById(R.id.save_button)

        song?.let {
            headerAlbumTv.text = it.title
            headerArtistTv.text = it.artist
            requireContext().loadUri(it.getAlbumArtUri().toString(), songArt)
            trackTv.setText(it.title)
            albumTv.setText(it.album)
            artistTv.setText(it.artist)
            yearTv.setText(it.year.toString())
            genreTv.setText("Dummy genre")
        }

        songArt.setOnClickListener { openGallery() }
        cancelButton.setOnClickListener { dialog?.dismiss() }
        saveButton.setOnClickListener { updateSong() }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.setLayout(width, height)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun updateSong() {
        // update album
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            songArt.setImageURI(data?.data)
        }
    }

    interface AlbumCallback {
        fun onSongUpdated(newSong: Song)
    }
}