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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.ui.viewModels.EditSongFragmentViewModel
import com.zc.phonoplayer.util.loadUri
import de.hdodenhof.circleimageview.CircleImageView

class EditSongDialogFragment : DialogFragment() {
    private var song: Song? = null
    private lateinit var headerAlbumTv: TextView
    private lateinit var headerArtistTv: TextView
    private lateinit var trackTv: EditText
    private lateinit var albumTv: EditText
    private lateinit var artistTv: EditText
    private lateinit var yearTv: EditText
    private lateinit var genreTv: EditText
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var songArt: CircleImageView
    private val viewModel: EditSongFragmentViewModel by activityViewModels()

    companion object {
        private const val SONG_EDIT = "song_edit"
        private const val IMAGE_PICK_CODE = 1100
        private const val REQUEST_SONG_MODIFY = 1101

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
        headerAlbumTv = view.findViewById(R.id.header_song_name)
        headerArtistTv = view.findViewById(R.id.header_artist_name)
        trackTv = view.findViewById(R.id.track_name)
        albumTv = view.findViewById(R.id.album_name)
        artistTv = view.findViewById(R.id.artist_name)
        yearTv = view.findViewById(R.id.song_year)
        genreTv = view.findViewById(R.id.song_genre)
        cancelButton = view.findViewById(R.id.cancel_button)
        saveButton = view.findViewById(R.id.save_button)

        song?.run {
            headerAlbumTv.text = title
            headerArtistTv.text = artist
            requireContext().loadUri(getAlbumArtUri().toString(), songArt)
            trackTv.setText(title)
            albumTv.setText(album)
            artistTv.setText(artist)
            yearTv.setText(year.toString())
            saveButton.setOnClickListener {
                editSong(this)
            }
        }
        songArt.setOnClickListener { openGallery() }
        cancelButton.setOnClickListener { dialog?.dismiss() }
        viewModel.permissionToModify().observe(this, Observer { intentSender ->
            intentSender?.let {
                startIntentSenderForResult(intentSender, REQUEST_SONG_MODIFY, null, 0, 0, 0, null)
            }
        })
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

    private fun editSong(song: Song) {
        song.apply {
            title = trackTv.text.toString()
            album = albumTv.text.toString()
            artist = artistTv.text.toString()
        }
        viewModel.updateSong(song)
        dialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE -> songArt.setImageURI(data?.data)
                REQUEST_SONG_MODIFY -> viewModel.updatePendingSong()
            }
        }
    }
}
