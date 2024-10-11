package com.hl.downloader.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.hl.downloader.DownloadListener
import com.hl.downloader.DownloadStatus
import com.hl.downloader.DownloadTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {


    private val TAG = "seagull"

    private companion object {
        const val REQUEST_PERMISSIONS_CODE = 0x0001
    }

    private var mainScope: CoroutineScope? = null
    private var downloadTask:DownloadTask ?= null

    private val downloadStatus by lazy {
        MutableLiveData<DownloadStatus>().apply {
            value = DownloadStatus.READY_TO_DOWNLOAD
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainScope = MainScope()
    }

    override fun onStart() {
        super.onStart()
        download.setOnClickListener {
            val needPermissions = listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            ).filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (needPermissions.isEmpty()) {
                startDownloadTest()
            } else {
                ActivityCompat.requestPermissions(this, needPermissions.toTypedArray(), REQUEST_PERMISSIONS_CODE)
            }
        }

        download.setOnLongClickListener {
            downloadTask?.cancelDownload()
            true
        }

        downloadStatus.observe(this) {
            when (downloadStatus.value) {
                DownloadStatus.DOWNLOADING -> {
                    pauseResumeDown.visibility = View.VISIBLE
                    pauseResumeDown.text = "暂停下载"
                    pauseResumeDown.setOnClickListener {
                        println("开始暂停下载")
                        downloadTask?.pauseDownLoad()
                    }
                }
                DownloadStatus.DOWNLOAD_PAUSE -> {
                    pauseResumeDown.visibility = View.VISIBLE
                    pauseResumeDown.text = "继续下载"
                    pauseResumeDown.setOnClickListener {
                        downloadTask?.resumeDownLoad()
                    }
                }
                else -> {
                    pauseResumeDown.visibility = View.GONE
                }
            }
        }
    }

    private fun startDownloadTest() {

        val downloadUrl = "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_8.9.38.10545_537154734_64.apk"
        val externalFilesDir = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val saveFilePath = "$externalFilesDir/QQ.apk"

         downloadTask = DownloadTask(this,
            downloadUrl,
            saveFilePath = saveFilePath,
            downloadListener = SoftReference(getDownloadListener())
        )
        downloadTask?.startDownload()
    }

    private fun getDownloadListener(): DownloadListener {
        return object : DownloadListener() {
            override fun deal(
                downloadStatus: DownloadStatus,
                error: Throwable?,
                progress: String?,
                downloadFilePath: String?
            ) {
                mainScope?.launch {
                    super.deal(downloadStatus, error, progress, downloadFilePath)
                }
            }
            override fun downloadIng(progress: String) {
                this@MainActivity.displayInfo.text = "下载中$progress%"

                downloadStatus.value = DownloadStatus.DOWNLOADING
            }

            override fun downloadError(error: Throwable?) {
                this@MainActivity.displayInfo.text = "下载出错:${error?.message}"

                downloadStatus.value = DownloadStatus.DOWNLOAD_ERROR
            }

            override fun downloadComplete(downLoadFilePath: String) {
                this@MainActivity.displayInfo.text = "下载完成--->$downLoadFilePath"

                downloadStatus.value = DownloadStatus.DOWNLOAD_COMPLETE
            }

            override fun downloadPause() {
                this@MainActivity.displayInfo.text = "下载暂停"

                downloadStatus.value = DownloadStatus.DOWNLOAD_PAUSE
            }

            override fun downloadCancel() {
                this@MainActivity.displayInfo.text = "下载取消"

                downloadStatus.value = DownloadStatus.DOWNLOAD_CANCEL
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            val noGrantResult = grantResults.filter { it != PackageManager.PERMISSION_GRANTED }
            if (noGrantResult.isEmpty()) {
                startDownloadTest()
            } else {
                val noGrantPermissionIndex = mutableListOf<Int>()
                grantResults.forEachIndexed { index, result ->
                    if (result in noGrantResult) noGrantPermissionIndex.add(index)
                }
                val noGrantPermissions = permissions.filterIndexed { index, _ ->
                    index in noGrantPermissionIndex
                }
                Toast.makeText(this, "$noGrantPermissions 这些权限未授予", Toast.LENGTH_SHORT).show()
            }
        }
    }
}