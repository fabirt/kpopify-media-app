package com.fabirt.kpopify.presentation.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.fabirt.kpopify.R
import com.fabirt.kpopify.core.exoplayer.isPlaying
import com.fabirt.kpopify.core.exoplayer.toSong
import com.fabirt.kpopify.core.util.Resource
import com.fabirt.kpopify.core.util.bindNetworkImage
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
        binding.includedEmptyView.btnEmpty.setOnClickListener {

        }
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
                    binding.progressBar.isVisible = false
                    binding.topAppBar.isVisible = true
                    binding.rvPlaylist.isVisible = true
                    binding.fabPlay.isVisible = true
                    binding.includedEmptyView.root.isVisible = false
                    adapter.submitList(result.data)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.topAppBar.isVisible = false
                    binding.rvPlaylist.isVisible = false
                    binding.fabPlay.isVisible = false
                    binding.includedEmptyView.root.isVisible = true
                }
                Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.topAppBar.isVisible = false
                    binding.rvPlaylist.isVisible = false
                    binding.fabPlay.isVisible = false
                    binding.includedEmptyView.root.isVisible = false
                }
            }
        })

        playerViewModel.currentPlayingSong.observe(viewLifecycleOwner, Observer { mediaItem ->
            binding.includedCurrentSong.container.isVisible =
                mediaItem?.description?.mediaId != null

            mediaItem?.description?.mediaId?.let {
                displayCurrentSong(mediaItem.toSong())
            }
        })

        playerViewModel.playbackState.observe(viewLifecycleOwner, Observer { playbackState ->
            val iconResource =
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play

            binding.includedCurrentSong.btnPlayPause.setImageResource(iconResource)
        })
    }

    private fun displayCurrentSong(song: Song) {
        val title = getString(R.string.current_song_title, song.title, song.artist)
        binding.includedCurrentSong.tvTitle.text = title
        binding.includedCurrentSong.container.setOnClickListener {
            openMusicPlayerFragment(it)
        }
        bindNetworkImage(binding.includedCurrentSong.ivSong, song.imageUrl)
        binding.includedCurrentSong.btnPlayPause.setOnClickListener {
            playerViewModel.playOrToggleSong(song, true)
        }
    }

    private fun openMusicPlayerFragment(view: View) {
        val action = PlaylistFragmentDirections.actionPlaylistFragmentToSongPlayerFragment()
        val extras = FragmentNavigatorExtras(view to "song_window")
        findNavController().navigate(action, extras)
    }
}
