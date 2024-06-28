package com.myprojects.a7minuteworkoutapp

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.transition.Visibility
import com.myprojects.a7minuteworkoutapp.databinding.ActivityExcerciseBinding
import com.myprojects.a7minuteworkoutapp.databinding.DialogCustomBackConfirmationBinding
import java.util.Locale
import javax.net.ssl.SSLEngineResult.Status

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var binding: ActivityExcerciseBinding? = null

    private var restTimer: CountDownTimer? = null
    private var restProgress = 0
    private var restTimeDuration: Long = 10

    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0
    private var exerciseTimeDuration: Long = 30

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts: TextToSpeech? = null

    private var isTtsInitialized = false

    private var player: MediaPlayer? = null

    private var exerciseStatusAdapter: ExerciseStatusAdapter? = null

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
            customDialogForBackButton()
        }

        setupRestView()

        setUpExerciseStatusRecyclerView()
    }

    override fun onBackPressed() {
        customDialogForBackButton()
        super.onBackPressed()
    }

    private fun customDialogForBackButton() {
        val customDialog = Dialog(this)
        val bindingDialog = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(bindingDialog.root)
        customDialog.setCanceledOnTouchOutside(false)
        bindingDialog.btnYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        bindingDialog.btnNo.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun setUpExerciseStatusRecyclerView() {
        binding?.rvExerciseStatus?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        exerciseStatusAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = exerciseStatusAdapter
    }

    private fun setupRestView() {
        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

//Just check
//        val uri = Uri.parse("android.resource://com.myprojects.a7minuteworkoutapp/" + R.raw.press_start)
//        player = MediaPlayer.create(applicationContext, uri)
//        player?.start()

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

        restTimer = object: CountDownTimer(restTimeDuration * 1000, 1000) {
            override fun onTick(p0: Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10 - restProgress).toString()
            }

            override fun onFinish() {
                currentExercisePosition++
                exerciseList?.get(currentExercisePosition)?.setIsSelected(true)
                exerciseStatusAdapter!!.notifyDataSetChanged()
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
        exerciseTimer = object: CountDownTimer(exerciseTimeDuration * 1000, 1000) {
            override fun onTick(p0: Long) {
                exerciseProgress++
                binding?.exerciseProgressBar?.progress = 30 - exerciseProgress
                binding?.tvExerciseTimer?.text = (30 - exerciseProgress).toString()
            }

            override fun onFinish() {
                if(currentExercisePosition < exerciseList?.size!! - 1) {
                    exerciseList?.get(currentExercisePosition)?.setIsSelected(false)
                    exerciseList?.get(currentExercisePosition)?.setIsCompleted(true)
                    exerciseStatusAdapter!!.notifyDataSetChanged()
                    setupRestView()
                }else {
                    nextScreen()
                }

            }
        }.start()
    }

    private fun nextScreen() {
        val intent = Intent(this, FinishActivity::class.java)
        startActivity(intent)
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
        if(player != null) {
            player?.stop()
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