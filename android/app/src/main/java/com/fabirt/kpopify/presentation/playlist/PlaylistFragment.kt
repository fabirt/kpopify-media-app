package com.fabirt.kpopify.presentation.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fabirt.kpopify.data.network.RemoteMusicDatabase
import com.fabirt.kpopify.databinding.FragmentPlaylistBinding
import com.fabirt.kpopify.domain.model.Song
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistFragment : Fragment(), PlaylistEventDispatcher {
    private lateinit var adapter: SongAdapter
    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding!!

    @Inject
    lateinit var remoteMusicDatabase: RemoteMusicDatabase

    companion object {
        private const val TAG = "PlaylistFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = SongAdapter(this)

        lifecycleScope.launch {
            val songs = remoteMusicDatabase.getAllSongs()
            adapter.submitList(songs)
        }
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