package com.example.backgroundremoverlibrary

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.backgroundremoverlibrary.databinding.ActivityRemoveBgBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.theapache64.removebg.RemoveBg
import com.theapache64.removebg.utils.ErrorResponse
import java.io.File

class RemoveBgActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemoveBgBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoveBgBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.button.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!

                RemoveBg.from(File(uri.path!!), object : RemoveBg.RemoveBgCallback {

                    override fun onProcessing() {
                        // will be invoked once finished uploading the image
                        Log.d("TTT", "onProcessing")
                        binding.loader.visibility = View.VISIBLE
                    }

                    override fun onUploadProgress(progress: Float) {
                        // will be invoked on uploading
                        Log.d("TTT", "onUploadProgress")
                        binding.loader.visibility = View.VISIBLE
                    }

                    override fun onError(errors: List<ErrorResponse.Error>) {
                        Log.d("TTT", "onError")
                        // will be invoked if there's any error occurred
                    }

                    override fun onSuccess(bitmap: Bitmap) {
                        runOnUiThread {
                            Log.d("TTT", "onSuccess")
                            binding.img.setImageBitmap(bitmap)
                            binding.loader.visibility = View.GONE
                        }
                    }
                })

            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}