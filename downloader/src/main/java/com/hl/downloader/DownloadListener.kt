package com.hl.downloader

import android.util.Log

/**
 * @Author  张磊  on  2020/11/04 at 20:47
 * Email: 913305160@qq.com
 */
open class DownloadListener {

    private val TAG = Constants.BASE_TAG + this.javaClass.simpleName

    /**
     * 下载错误
     * @param  error 下载异常
     */
    open fun downloadError(error: Throwable?) {}

    /**
     * 下载完成
     * @param downLoadFilePath 下载完成的文件路径
     */
    open fun downloadComplete(downLoadFilePath: String) {}

    /**
     * 下载中
     * @param  progress 进度为保留两位小数去除尾数 0 的字符串：如：1.5, 10 , 99.99
     */
    open fun downloadIng(progress: String) {}

    /**
     * 下载暂停
     * 注意：当前下载已完成或已暂停，请求暂停不会受到此通知
     */
    open fun downloadPause() {}

    /**
     * 下载取消
     * 注意：当前下载已完成或已取消，请求取消不会受到此通知
     */
    open fun downloadCancel() {}

    open fun deal( downloadStatus: DownloadStatus,
              error: Throwable? = null,
              progress: String? = null,
              downloadFilePath: String? = null){
        Log.d(TAG, "下载状态 == $downloadStatus, 下载进度 == $progress")
        when (downloadStatus) {
            DownloadStatus.DOWNLOAD_ERROR -> downloadError(error)
            DownloadStatus.DOWNLOADING -> downloadIng(progress ?: "")
            DownloadStatus.DOWNLOAD_COMPLETE -> downloadComplete(downloadFilePath ?: "")
            DownloadStatus.DOWNLOAD_PAUSE -> downloadPause()
            DownloadStatus.DOWNLOAD_CANCEL -> {
                downloadCancel()
            }
            else -> {
            }
        }
    }
}