package com.example.backgroundremoverlibrary

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Config
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.get
import com.example.backgroundremoverlibrary.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.slowmac.autobackgroundremover.BackgroundRemover
import com.slowmac.autobackgroundremover.OnBackgroundChangeListener
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import kotlin.math.floor


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val displayWidth = Resources.getSystem().displayMetrics.widthPixels


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val bitmap = getBitmapFromRes(resources, R.drawable.woman, displayWidth)

        BackgroundRemover.bitmapForProcessing(bitmap, object : OnBackgroundChangeListener {
            override fun onSuccess(bitmap: Bitmap) {
                binding.img.setImageBitmap(bitmap)
            }

            override fun onFailed(exception: Exception) {
                Toast.makeText(this@MainActivity, "Error Occur", Toast.LENGTH_SHORT).show()
            }
        })

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
                val bitmap = uriToBitmap(uri)

                // Use Uri object instead of File to avoid storage permissions

                if (bitmap != null) {

//                    for (y in 0 until bitmap.height) {
//                        for (x in 0 until bitmap.width) {
//                            Log.d("TTT" ,"x y : ${bitmap.get()}")
//                        }
//                    }
                    val tempBitmap = Bitmap.createBitmap(bitmap).copy(Bitmap.Config.ARGB_8888, true)

                    BackgroundRemover.bitmapForProcessing(
                        tempBitmap,
                        object : OnBackgroundChangeListener {
                            override fun onSuccess(bitmap: Bitmap) {
                                binding.img.setImageBitmap(bitmap)
                            }

                            override fun onFailed(exception: Exception) {
                                Toast.makeText(this@MainActivity, "Error Occur", Toast.LENGTH_SHORT)
                                    .show()
                                Log.d("ttt", exception.message.toString())
                                Log.d("ttt", exception.toString())
                            }
                        })
                }
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }
}