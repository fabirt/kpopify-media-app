package com.fabirt.kpopify.presentation.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.fabirt.kpopify.R
import com.fabirt.kpopify.core.util.*
import com.fabirt.kpopify.databinding.FragmentPlaylistBinding
import com.fabirt.kpopify.domain.model.Song
import com.fabirt.kpopify.presentation.viewmodel.MusicPlayerViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.Hold
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : Fragment(), PlaylistEventDispatcher {
    companion object {
        private const val TAG = "PlaylistFragment"
    }

    private lateinit var adapter: SongAdapter
    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding!!

    private val playerViewModel: MusicPlayerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transitionDuration = resources.getInteger(R.integer.nav_transition_duration)
        exitTransition = Hold().apply {
            duration = transitionDuration.toLong()
        }

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
            playerViewModel.refreshMediaBrowserChildren()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvPlaylist.adapter = null
        _binding = null
    }

    override fun onSongSelected(song: Song) {
        playerViewModel.playOrToggleSong(song)
    }

    private fun subscribeToObservers() {
        playerViewModel.songs.observe(viewLifecycleOwner, Observer { result ->
            handleSongsResult(result)
        })

        playerViewModel.currentPlayingSong.observe(viewLifecycleOwner, Observer { mediaItem ->
            val showCurrentSongView = mediaItem?.description?.mediaId != null

            binding.includedCurrentSong.container.isVisible = showCurrentSongView

            binding.rvPlaylist.updatePadding(
                bottom = if (showCurrentSongView) 132.dp else 32.dp
            )

            adapter.setCurrentMediaId(mediaItem?.description?.mediaId)

            mediaItem?.description?.mediaId?.let {
                displayCurrentSong(mediaItem.toSong())
            }
        })

        playerViewModel.playbackState.observe(viewLifecycleOwner, Observer { playbackState ->
            val iconResource =
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play

            binding.includedCurrentSong.btnPlayPause.setImageResource(iconResource)

            if (playbackState?.isError == true) {
                showErrorSnackBar()
            }
        })
    }

    private fun handleSongsResult(result: Resource<List<Song>>) {
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
        val transitionName = getString(R.string.song_window_transition_name)
        val action = PlaylistFragmentDirections.actionPlaylistFragmentToSongPlayerFragment()
        val extras = FragmentNavigatorExtras(view to transitionName)
        findNavController().navigate(action, extras)
    }

    private fun showErrorSnackBar() {
        Snackbar.make(
            requireView(),
            getString(R.string.playback_network_error),
            Snackbar.LENGTH_LONG
        ).show()
    }
}
