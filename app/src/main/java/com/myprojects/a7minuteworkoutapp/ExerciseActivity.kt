package com.myprojects.a7minuteworkoutapp

import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Visibility
import com.myprojects.a7minuteworkoutapp.databinding.ActivityExcerciseBinding
import java.util.Locale
import javax.net.ssl.SSLEngineResult.Status

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var binding: ActivityExcerciseBinding? = null

    private var restTimer: CountDownTimer? = null
    private var restProgress = 0

    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts: TextToSpeech? = null

    private var isTtsInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcerciseBinding.inflate(layoutInflater)

        setContentView(binding?.root)
        setSupportActionBar(binding?.toolBarExercise)

        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        exerciseList = Constants.defaultExerciseList()
        tts = TextToSpeech(this, this)

        binding?.toolBarExercise?.setNavigationOnClickListener {
            onBackPressed()
        }

        speakOut("Upcoming exercise " + exerciseList!![currentExercisePosition + 1].getName())
        setupRestView()
    }

    private fun setupRestView() {
        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        if (isTtsInitialized) {
            speakOut("Upcoming exercise " + exerciseList?.get(currentExercisePosition + 1)?.getName().toString())
        }

        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.tvExercise?.visibility = View.INVISIBLE
        binding?.flExerciseProgressBar?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.flProgressBar?.visibility = View.VISIBLE
        binding?.upcomingExercise?.visibility = View.VISIBLE
        binding?.nextExerciseTitle?.visibility = View.VISIBLE
        binding?.nextExerciseTitle?.text = exerciseList?.get(currentExercisePosition + 1)?.getName()
        setRestProgressBar()
    }


    private fun setRestProgressBar() {
        binding?.progressBar?.progress = restProgress

        restTimer = object: CountDownTimer(10000, 1000) {
            override fun onTick(p0: Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10 - restProgress).toString()
            }

            override fun onFinish() {
                currentExercisePosition++
                binding?.upcomingExercise?.visibility = View.INVISIBLE
                binding?.nextExerciseTitle?.visibility = View.INVISIBLE
                binding?.flProgressBar?.visibility = View.INVISIBLE
                binding?.tvTitle?.visibility = View.INVISIBLE
                setupExerciseView()
            }
        }.start()
    }

    private fun setupExerciseView() {
        if(exerciseTimer != null)
        {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        binding?.tvExercise?.visibility = View.VISIBLE
        binding?.tvExercise?.text = exerciseList?.get(currentExercisePosition)?.getName()
        binding?.flExerciseProgressBar?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        exerciseList?.get(currentExercisePosition)?.getImage()
            ?.let { binding?.ivImage?.setImageResource(it) }
        setExerciseProgressBar()
    }

    private fun setExerciseProgressBar() {
        binding?.exerciseProgressBar?.progress = exerciseProgress
        exerciseTimer = object: CountDownTimer(30000, 1000) {
            override fun onTick(p0: Long) {
                exerciseProgress++
                binding?.exerciseProgressBar?.progress = 30 - exerciseProgress
                binding?.tvExerciseTimer?.text = (30 - exerciseProgress).toString()
            }

            override fun onFinish() {
                if(currentExercisePosition < exerciseList?.size!! - 1) {
                    setupRestView()
                }else {
                    Toast.makeText(
                        this@ExerciseActivity,
                        "Congratulations, You have completed teh 7 minute workout.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        }.start()
    }

    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
         super.onDestroy()
        if(restTimer != null)
        {
            restTimer?.cancel()
            restProgress = 0
        }
        if(tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        binding = null
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.ENGLISH)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("E", "Error")
            } else {
                isTtsInitialized = true
                speakOut("Upcoming exercise " + exerciseList!![currentExercisePosition + 1].getName())
            }
        }
    }

}