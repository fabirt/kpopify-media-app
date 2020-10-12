package com.fabirt.kpopify.presentation.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.fabirt.kpopify.core.util.Resource
import com.fabirt.kpopify.databinding.FragmentPlaylistBinding
import com.fabirt.kpopify.domain.model.Song
import com.fabirt.kpopify.presentation.viewmodels.MusicPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : Fragment(), PlaylistEventDispatcher {

    private lateinit var adapter: SongAdapter
    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding!!

    private val playerViewModel: MusicPlayerViewModel by activityViewModels()

    companion object {
        private const val TAG = "PlaylistFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = SongAdapter(this)
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
        subscribeToObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSongSelected(song: Song) {
        playerViewModel.playOrToggleSong(song)
    }

    private fun subscribeToObservers() {
        playerViewModel.songs.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    adapter.submitList(result.data)
                }
                is Resource.Error -> Unit
                Resource.Loading -> Unit
            }
        })
    }
}