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
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.util.loadUri
import de.hdodenhof.circleimageview.CircleImageView

class EditAlbumDialogFragment : DialogFragment() {
    private var album: Album? = null
    private lateinit var albumArt: CircleImageView

    companion object {
        private const val ALBUM_EDIT = "album_edit"
        private const val IMAGE_PICK_CODE = 1000

        fun newInstance(album: Album): DialogFragment {
            val frag = EditAlbumDialogFragment()
            val args = Bundle()
            args.putParcelable(ALBUM_EDIT, album)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        album = arguments?.getParcelable(ALBUM_EDIT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_dialog_edit_album, container, false)
        albumArt = view.findViewById(R.id.album_art)
        val headerAlbumTv: TextView = view.findViewById(R.id.header_album_name)
        val headerArtistTv: TextView = view.findViewById(R.id.header_artist_name)
        val headerNbOfSongsTv: TextView = view.findViewById(R.id.heaer_no_of_tracks)
        val albumEt: EditText = view.findViewById(R.id.album_name)
        val artistEt: EditText = view.findViewById(R.id.artist_name)
        val albumArtistEt: EditText = view.findViewById(R.id.album_artist_name)
        val albumYearEt: EditText = view.findViewById(R.id.album_year)
        val albumGenreEt: EditText = view.findViewById(R.id.album_genre)
        val cancelButton: Button = view.findViewById(R.id.cancel_button)
        val saveButton: Button = view.findViewById(R.id.save_button)

        album?.let {
            headerAlbumTv.text = it.title
            headerArtistTv.text = it.artist
            headerNbOfSongsTv.text = it.getNbOfTracks()
            requireContext().loadUri(it.getAlbumArtUri().toString(), albumArt)

            albumEt.setText(it.title)
            artistEt.setText(it.artist)
            albumArtistEt.setText(it.artist)
            albumYearEt.setText("")
            albumGenreEt.setText("")
        }

        albumArt.setOnClickListener { openGallery() }
        cancelButton.setOnClickListener { dialog?.dismiss() }
        saveButton.setOnClickListener { updateAlbum() }
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

    private fun updateAlbum() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            albumArt.setImageURI(data?.data)
        }
    }
}
