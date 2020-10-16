package com.fabirt.kpopify.presentation.musicplayer

import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fabirt.kpopify.R
import com.fabirt.kpopify.core.util.isPlaying
import com.fabirt.kpopify.core.util.toSong
import com.fabirt.kpopify.core.util.bindNetworkImage
import com.fabirt.kpopify.core.util.isStopped
import com.fabirt.kpopify.databinding.FragmentMusicPlayerBinding
import com.fabirt.kpopify.presentation.viewmodel.MusicPlayerViewModel
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MusicPlayerFragment : Fragment() {

    companion object {
        private const val TAG = "MusicPlayerFragment"
    }

    private val playerViewModel: MusicPlayerViewModel by activityViewModels()

    private var _binding: FragmentMusicPlayerBinding? = null
    private val binding: FragmentMusicPlayerBinding
        get() = _binding!!

    private var shouldUpdateSeekBar = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureTransitions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMusicPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickActions()
        setupSeekBar()
        subscribeToObservers()

        viewLifecycleOwner.lifecycleScope.launch {
            playerViewModel.updateCurrentPlaybackPosition()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setOnClickActions() {
        binding.includedPlayerControls.fabPlayPause.setOnClickListener {
            playerViewModel.playOrPauseCurrentSong()
        }

        binding.includedPlayerControls.ivPrevious.setOnClickListener {
            playerViewModel.skipToPreviousSong()
        }

        binding.includedPlayerControls.ivNext.setOnClickListener {
            playerViewModel.skipToNextSong()
        }

        binding.includedPlayerControls.ivReplay.setOnClickListener {
            playerViewModel.toggleRepeatMode()
        }
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurrentDurationToView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    shouldUpdateSeekBar = true
                    playerViewModel.seekTo(it.progress.toLong())
                }
            }
        })
    }

    private fun subscribeToObservers() {
        playerViewModel.currentPlayingSong.observe(viewLifecycleOwner, Observer { mediaItem ->
            mediaItem?.description?.mediaId?.let {
                val song = mediaItem.toSong()
                binding.tvTitle.text = song.title
                binding.tvArtist.text = song.artist
                bindNetworkImage(binding.ivSong, song.imageUrl)
            }
        })

        playerViewModel.playbackState.observe(viewLifecycleOwner, Observer { playbackState ->
            Log.i(TAG, playbackState.toString())
            handlePlaybackState(playbackState)
        })

        playerViewModel.currentSongDuration.observe(viewLifecycleOwner, Observer { duration ->
            binding.seekBar.max = duration.toInt()
            binding.tvDuration.text = formatLong(duration)
        })

        playerViewModel.currentPlaybackPosition.observe(viewLifecycleOwner, Observer { pos ->
            pos?.let {
                if (shouldUpdateSeekBar) {
                    binding.seekBar.progress = it.toInt()
                    binding.tvCurrentPos.text = formatLong(it)
                }
            }
        })
    }

    private fun handlePlaybackState(playbackState: PlaybackStateCompat?) {
        val iconResource =
            if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play

        binding.includedPlayerControls.fabPlayPause.setImageResource(iconResource)

        val replayModeColor = if (playerViewModel.isInRepeatMode)
            requireContext().getColor(R.color.colorOnSurface)
        else Color.WHITE

        binding.includedPlayerControls.ivReplay.setColorFilter(replayModeColor)

        if (playbackState?.isStopped == true) {
            findNavController().navigateUp()
        }
    }

    private fun setCurrentDurationToView(value: Long) {
        binding.tvCurrentPos.text = formatLong(value)
    }

    private fun formatLong(value: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return dateFormat.format(value)
    }

    private fun configureTransitions() {
        val color = requireContext().getColor(R.color.colorPrimary)
        val transitionDuration = resources.getInteger(R.integer.nav_transition_duration)
        val transition = MaterialContainerTransform().apply {
            duration = transitionDuration.toLong()
            containerColor = color
            drawingViewId = R.id.nav_host_fragment
        }
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
    }
}
