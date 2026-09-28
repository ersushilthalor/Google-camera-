package com.example.camera.crash

import android.content.Context
import android.content.Intent
import android.os.Process
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        try {
            val intent = Intent(context, CrashDisplayActivity::class.java).apply {
                putExtra(CrashDisplayActivity.EXTRA_CRASH_INFO, stackTrace)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
            Process.killProcess(Process.myPid())
            System.exit(10)
        } catch (e: Exception) {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        fun install(context: Context) {
            val handler = CrashHandler(context.applicationContext)
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }
    }
}
