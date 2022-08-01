package com.yashkasera.learnwithus.ui.grammar

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
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
import com.google.android.material.snackbar.Snackbar
import com.yashkasera.learnwithus.R
import com.yashkasera.learnwithus.databinding.FragmentGrammarBinding
import java.util.*
import kotlin.math.abs

/**
 * @author yashkasera
 * Created 19/04/22 at 8:29 PM
 */
class GrammarFragment : Fragment() {
    private val binding by lazy {
        FragmentGrammarBinding.inflate(layoutInflater)
    }
    private val viewModel by lazy {
        ViewModelProvider(this).get(GrammarViewModel::class.java)
    }

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
        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.record.setOnClickListener {
            if (viewModel.isListening.value == true)
                speechRecognizer.stopListening()
            else {
                speechRecognizer.startListening(speechRecognizerIntent)
                binding.progressBar.isVisible = false
            }
        }
        viewModel.isListening.observe(viewLifecycleOwner) {
            binding.record.setImageResource(if (it) R.drawable.ic_baseline_mic_off_24 else R.drawable.ic_baseline_mic_24)
        }
        viewModel.response.observe(viewLifecycleOwner) {
            binding.answerLayout.isVisible = it != null
            it?.let {
                binding.progressBar.isVisible = true
                binding.status.text = if (it.isCorrect) "Correct" else "Incorrect"
                binding.status.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (it.isCorrect) R.color.green else R.color.red
                    )
                )
                binding.answer.text = it.message
            }
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireActivity()).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}

                override fun onBeginningOfSpeech() {
                    viewModel.isListening.value = true
                    viewModel.response.value = null
                    binding.textView.text = null
                    binding.textView.hint = "Listening..."
                    binding.progressBar.isVisible = false
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
                    viewModel.isListening.value = false
                }

                override fun onError(error: Int) {
                    Snackbar.make(binding.root, "An error occurred", Snackbar.LENGTH_SHORT).show()
                }

                override fun onResults(results: Bundle?) {
                    viewModel.isListening.value = false
                    Log.i("TAG", "onResults: ${results?.keySet()}")
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                        Log.i("TAG", "onResults: $it")
                        binding.textView.text = it[0]
                        binding.progressBar.visibility = View.VISIBLE
                        viewModel.checkGrammar(it[0])
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
}