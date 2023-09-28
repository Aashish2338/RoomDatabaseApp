package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.roomclasses.VehicleData
import com.example.myapplication.roomclasses.VehicleDatabase
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class FormDataSubmitActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mContext: Context
    private lateinit var rcNumber_Et: EditText
    private lateinit var vehicleNumber_Et: EditText
    private lateinit var vehicleType_Et: EditText
    private lateinit var vehicleImage_Img: ImageView
    private lateinit var captureImage_btn: AppCompatButton
    private lateinit var submit_btn: AppCompatButton
    private var rcNumber: String = ""
    private var vehicleNumber: String = ""
    private var vehicleType: String = ""
    private var vehicleDocsImage: String = ""
    private var serialPhotoPath: String? = null
    private var vehicleDocs: File? = null
    private var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_data_submit)
        mContext = this

        getUiIdInit()

        captureImage_btn.setOnClickListener(this)
        submit_btn.setOnClickListener(this)

    }

    private fun getUiIdInit() {
        try {
            rcNumber_Et = findViewById<EditText>(R.id.rcNumber_Et)
            vehicleNumber_Et = findViewById<EditText>(R.id.vehicleNumber_Et)
            vehicleType_Et = findViewById<EditText>(R.id.vehicleType_Et)
            vehicleImage_Img = findViewById<ImageView>(R.id.vehicleImage_Img)
            captureImage_btn = findViewById<AppCompatButton>(R.id.captureImage_btn)
            submit_btn = findViewById<AppCompatButton>(R.id.submit_btn)

            vehicleType_Et.isFocusable = true
            vehicleImage_Img.visibility = View.GONE

            captureImage_btn.visibility == View.GONE
            submit_btn.visibility == View.VISIBLE

        } catch (exp: Exception) {
            exp.stackTrace
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.captureImage_btn -> {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            var photoFile: File? = null
                            try {
                                photoFile = createImageFile()
                                if (photoFile != null) {
                                    photoURI = FileProvider.getUriForFile(
                                        mContext,
                                        "com.example.myapplication.FileProvider",
                                        photoFile
                                    )
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    launchCameraActivity.launch(takePictureIntent)
                                }
                            } catch (ex: IOException) {
                                Log.d("Tag", ex.toString())
                            }
                        } else {
                            val cameraAadharIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            launchCameraActivity.launch(cameraAadharIntent)
                        }
                    } catch (exp: Exception) {
                        exp.stackTrace
                    }
                }

                R.id.submit_btn -> {
                    try {
                        if (isFormDataValidate()) {
                            getDataForSubmit(rcNumber, vehicleNumber, vehicleType, vehicleDocsImage)
                        }
                    } catch (exp: Exception) {
                        exp.stackTrace
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        serialPhotoPath = image.absolutePath
        return image
    }

    var launchCameraActivity = registerForActivityResult<Intent, ActivityResult>(
        StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                var image: Bitmap? = null
                vehicleImage_Img.setImageURI(photoURI)
                if (data != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vehicleDocs = File(serialPhotoPath)
                        vehicleDocsImage = vehicleDocs.toString()
                        println("Image path file A $vehicleDocsImage")
                    } else {
                        image = data.extras!!["data"] as Bitmap?
                        vehicleImage_Img.setImageBitmap(image)
                        val bytes = ByteArrayOutputStream()
                        image!!.compress(
                            Bitmap.CompressFormat.JPEG,
                            90,
                            bytes
                        )
                        val imeiSerialImage = File(
                            Environment.getExternalStorageDirectory(),
                            "vehicleImages.jpg"
                        )
                        vehicleDocs = File(imeiSerialImage.absolutePath)
                        vehicleDocsImage = vehicleDocs.toString()
                        println("Image path file B $vehicleDocsImage")
                        val fo: FileOutputStream
                        try {
                            fo = FileOutputStream(vehicleDocs)
                            fo.write(bytes.toByteArray())
                            fo.close()
                            vehicleDocsImage = vehicleDocs.toString()
                            println("Image path file C $vehicleDocsImage")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    if (vehicleImage_Img.visibility == View.GONE) {
                        vehicleImage_Img.visibility = View.VISIBLE
                    }
                    submit_btn.visibility = View.VISIBLE
                    captureImage_btn.visibility = View.GONE
                } else {
                    println("Image Path :- not found!")
                    submit_btn.visibility = View.GONE
                    captureImage_btn.visibility = View.VISIBLE
                }
            }
        } catch (exp: java.lang.Exception) {
            exp.stackTrace
        }
    }

    private fun getDataForSubmit(
        rcNumber: String,
        vehicleNumber: String,
        vehicleType: String,
        vehicleDocsImage: String
    ) {
        try {
            val newNote = VehicleData(rcNumber, vehicleNumber, vehicleType, vehicleDocsImage)
            val vehicleDatabase by lazy { VehicleDatabase.getDatabase(mContext).repoDao() }
            lifecycleScope.launch { vehicleDatabase.addNote(newNote) }
            Toast.makeText(mContext, "Data added successfully", Toast.LENGTH_SHORT).show()
            setClearAllFields()
        } catch (expn: Exception) {
            expn.stackTrace
            println("Data saving duration ${expn.cause}")
        }
    }

    private fun setClearAllFields() {
        try {
            rcNumber_Et.text.clear()
            vehicleNumber_Et.text.clear()
            vehicleType_Et.text.clear()
            vehicleImage_Img.visibility = View.GONE
            vehicleType_Et.isFocusable = false

            captureImage_btn.visibility == View.GONE
            submit_btn.visibility == View.VISIBLE
        } catch (exp: Exception) {
            exp.stackTrace
        }
    }

    private fun isFormDataValidate(): Boolean {
        try {
            rcNumber = rcNumber_Et.text.toString().trim { it <= ' ' }
            vehicleNumber = vehicleNumber_Et.text.toString().trim { it <= ' ' }
            vehicleType = vehicleType_Et.text.toString().trim { it <= ' ' }
            if (rcNumber.isEmpty()) {
                rcNumber_Et.error = "Enter RC No."
                rcNumber_Et.requestFocus()
                return false
            } else if (vehicleNumber.isEmpty()) {
                vehicleNumber_Et.error = "Enter Vehicle No."
                vehicleNumber_Et.requestFocus()
                return false
            } else if (vehicleType.isEmpty()) {
                vehicleType_Et.error = "Enter Vehicle Type"
                vehicleType_Et.requestFocus()
                return false
            }
        } catch (exp: Exception) {
            exp.stackTrace
        }
        return true
    }
}