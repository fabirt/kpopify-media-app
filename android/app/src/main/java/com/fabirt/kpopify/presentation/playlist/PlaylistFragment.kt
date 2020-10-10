package com.fabirt.kpopify.presentation.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.fabirt.kpopify.databinding.FragmentPlaylistBinding
import com.fabirt.kpopify.domain.model.Song

class PlaylistFragment : Fragment(), PlaylistEventDispatcher {
    private lateinit var adapter: SongAdapter
    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding!!

    companion object {
        private const val TAG = "PlaylistFragment"
        private val dummySongs = List(16) { index ->
            Song(index, "Kill this love", "Blackpink", "", "")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = SongAdapter(this).apply { submitList(dummySongs) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPlaylist.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSongSelected(song: Song) {
        findNavController().navigate(
            PlaylistFragmentDirections.actionPlaylistFragmentToSongPlayerFragment()
        )
    }
}