package com.yashkasera.learnwithus.ui.narration

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.yashkasera.learnwithus.R
import com.yashkasera.learnwithus.databinding.FragmentNarrationBinding
import com.yashkasera.learnwithus.util.hexStringToByteArray
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.abs


/**
 * @author yashkasera
 * Created 19/04/22 at 8:28 PM
 */
class NarrationFragment : Fragment() {
    private val binding by lazy {
        FragmentNarrationBinding.inflate(layoutInflater)
    }
    private val viewModel by lazy {
        ViewModelProvider(this)[NarrationViewModel::class.java]
    }

    private var mediaPlayer: MediaPlayer = MediaPlayer()

    private val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private lateinit var speechRecognizer: SpeechRecognizer


    private val animator1: ObjectAnimator by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.pulse1,
            PropertyValuesHolder.ofFloat("scaleX", 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f)
        )
    }

    private val animator2: ObjectAnimator by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.pulse2,
            PropertyValuesHolder.ofFloat("scaleX", 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.back.setOnClickListener{
            findNavController().navigateUp()
        }
        binding.record.setOnClickListener {
            if (viewModel.isListening.get())
                speechRecognizer.stopListening()
            else {
                speechRecognizer.startListening(speechRecognizerIntent)
                binding.progressBar.isVisible = false
            }
        }
        viewModel.sounds.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe
            if (binding.playAutomatically.isChecked)
                it[it.keys.first()]?.let { sound -> playSound(sound) }
            else {
                addSoundChips(it)
            }
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireActivity()).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}

                override fun onBeginningOfSpeech() {
                    viewModel.isListening.set(true)
                    binding.textView.text = null
                    binding.textView.hint = "Listening..."
                    binding.progressBar.isVisible = false
                    binding.chipGroup.removeAllViews()
                }

                override fun onRmsChanged(rmsdB: Float) {
                    val scale = abs(rmsdB) / 5
                    animator1.setValues(
                        PropertyValuesHolder.ofFloat("scaleX", scale),
                        PropertyValuesHolder.ofFloat("scaleY", scale)
                    )
                    animator1.interpolator = FastOutSlowInInterpolator()
                    animator1.start()
                    animator2.setValues(
                        PropertyValuesHolder.ofFloat("scaleX", scale * 1.4f),
                        PropertyValuesHolder.ofFloat("scaleY", scale * 1.4f)
                    )
                    animator2.interpolator = FastOutSlowInInterpolator()
                    animator2.start()
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                }

                override fun onEndOfSpeech() {
                    viewModel.isListening.set(false)
                }

                override fun onError(error: Int) {
                    Snackbar.make(binding.root, "An error occurred", Snackbar.LENGTH_SHORT).show()
                }

                override fun onResults(results: Bundle?) {
                    viewModel.isListening.set(false)
                    Log.i("TAG", "onResults: ${results?.keySet()}")
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                        Log.i("TAG", "onResults: $it")
                        binding.textView.text = it[0]
                        binding.progressBar.visibility = View.VISIBLE
                        binding.progressBar.isIndeterminate = true
                        if (binding.playAutomatically.isChecked)
                            viewModel.getSound(it[0])
                        else
                            viewModel.getSounds(it[0])
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                        binding.textView.text = it[0]
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {

                }
            })
        }

    }

    private fun playSound(sound: String) {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
            }
            val mp3SoundByteArray: ByteArray = hexStringToByteArray(sound)
            val tempMp3: File =
                File.createTempFile("learn_with_us", "mp3", requireActivity().cacheDir)
            tempMp3.deleteOnExit()
            val fos = FileOutputStream(tempMp3)
            fos.write(mp3SoundByteArray)
            fos.close()
            val fis = FileInputStream(tempMp3)
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepare()
            mediaPlayer.setOnPreparedListener { obj: MediaPlayer -> obj.start() }
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBar.isIndeterminate = false
            binding.progressBar.progress = 100
            mediaPlayer.setOnCompletionListener { mp: MediaPlayer ->
                mp.reset()
                binding.progressBar.visibility = View.GONE
                if (binding.playAutomatically.isChecked)
                    speechRecognizer.startListening(speechRecognizerIntent);
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        }
    }

    private fun addSoundChips(sounds: Map<String, String>) {
        for (key in sounds.keys) {
            Log.d("NarrationFragment.kt", "YASH => addSoundChips: $key")
            val chip = Chip(context)
            chip.text = key
            chip.setTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.pink
                    )
                )
            )
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background_secondary
                )
            )
            chip.chipStrokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink
                )
            )
            chip.setTextStartPaddingResource(R.dimen.margin_medium)
            chip.setTextEndPaddingResource(R.dimen.margin_medium)
            chip.setChipStrokeWidthResource(R.dimen.strokeWidth)
            chip.isClickable = true
            chip.setOnClickListener {
                playSound(
                    sounds[key]!!
                )
            }
            binding.chipGroup.addView(chip)
        }
    }

}