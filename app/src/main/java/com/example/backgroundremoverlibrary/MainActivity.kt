package com.example.backgroundremoverlibrary

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Bitmap.wrapHardwareBuffer
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.opengl.Matrix
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.get
import androidx.lifecycle.lifecycleScope
import com.example.backgroundremoverlibrary.databinding.ActivityMainBinding
import com.example.bgremover.BackgroundRemover
import com.example.bgremover.OnBackgroundChangeListener
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*


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
                .start()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                val bitmap = uriToBitmap(uri)

                if (bitmap != null) {

                    val tempBitmap = createBitmap(bitmap).copy(Bitmap.Config.ARGB_8888, true)

                    BackgroundRemover.bitmapForProcessing(
                        tempBitmap,
                        object : OnBackgroundChangeListener {
                            override fun onSuccess(bitmap: Bitmap) {
                                binding.img.setImageBitmap(bitmap)
                                bitmap.setHasAlpha(true)

                                uploadTableAndCheckImage(bitmap)
                            }

                            override fun onFailed(exception: Exception) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Error Occur",
                                    Toast.LENGTH_SHORT
                                )
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
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

    val providesRetrofitInstance = Retrofit.Builder()
        .baseUrl("http://192.168.0.97:4000")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val imageAPI = providesRetrofitInstance.create(Image::class.java)


    fun uploadTableAndCheckImage(img: Bitmap) {
//    fun uploadTableAndCheckImage(matrix: Array<Array<Int>>) {

        lifecycleScope.launchWhenResumed {

            val fileCheck = File(baseContext.cacheDir, "img.png")

            fileCheck.createNewFile()

            val checkByteArrayOutputStream = ByteArrayOutputStream()

            img.compress(
                Bitmap.CompressFormat.PNG,
                100,
                checkByteArrayOutputStream
            )
            val checkByteArray = checkByteArrayOutputStream.toByteArray()
            var checkFileOutputStream: FileOutputStream? = null

            try {
                checkFileOutputStream = FileOutputStream(fileCheck)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            try {
                checkFileOutputStream?.write(checkByteArray)
                checkFileOutputStream?.flush()
                checkFileOutputStream?.close()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            val checkBody: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file",
                fileCheck.name,
                fileCheck.asRequestBody("image/png".toMediaTypeOrNull())
            )

            val userId =
                "fd7fa9d1-1f93-48f1-bcc5-5e47d0284eb6".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = imageAPI.uploadEmployeeProfileImage(
                userId,
                checkBody
            )

            val result = response.body()
            Log.d("TTT", response.code().toString())
            Log.d("TTT", response.message().toString())
            if (response.isSuccessful) {
                Log.d("TTT", result.toString())

                delay(2000)
                Toast.makeText(this@MainActivity, "New file", Toast.LENGTH_SHORT).show()
                Log.d("TTT", "$${fileCheck.path}")
                binding.img.setImageBitmap(BitmapFactory.decodeFile(fileCheck.path))
            } else {
                Log.d("TTT", "uploadTableAndCheckImage: else")
            }
        }
    }
}


