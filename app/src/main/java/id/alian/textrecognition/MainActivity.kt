package id.alian.textrecognition

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import id.alian.textrecognition.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val requestCodeCameraPermission = 1001
    private lateinit var binding: ActivityMainBinding
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            startCameraSource()
        }
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    private fun startCameraSource() {
        textRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational) {
            Log.d(MainActivity::class.simpleName,
                "startCameraSource: detector dependencies not loaded yet")
        } else {
            cameraSource = CameraSource.Builder(this, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build();

            with(binding) {
                surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                    @SuppressLint("MissingPermission")
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        try {
                            cameraSource.start(surfaceView.holder)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    @SuppressLint("MissingPermission")
                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        heigth: Int,
                    ) {
                        try {
                            cameraSource.start(holder)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    @SuppressLint("MissingPermission")
                    override fun surfaceDestroyed(p0: SurfaceHolder) {
                        cameraSource.stop()
                    }
                })

                textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                    override fun release() {
                    }

                    override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                        val items = detections.detectedItems
                        if (items.size() != 0) {
                            runOnUiThread {
                                val stringBuilder = StringBuilder()
                                for (i in 0 until items.size()) {
                                    val item = items.valueAt(i)
                                    stringBuilder.append(item.value)
                                    stringBuilder.append("\n")
                                }
                                textView.text = stringBuilder.toString()
                            }
                        }
                    }
                })
            }
        }
    }
}