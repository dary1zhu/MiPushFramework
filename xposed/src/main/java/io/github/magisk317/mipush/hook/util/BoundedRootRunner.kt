package io.github.magisk317.mipush.hook.util

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

internal data class XposedShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String = "",
    val timedOut: Boolean = false,
) {
    val isSuccess: Boolean
        get() = !timedOut && exitCode == 0
}

internal object BoundedRootRunner {
    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "mipush-xposed-root-runner").apply { isDaemon = true }
    }

    fun run(command: String, timeoutMs: Long = 5_000L): XposedShellResult {
        var process: Process? = null
        var outputFuture: Future<String>? = null
        
        return try {
            val started = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start()
            process = started
            
            // 提交异步读取任务
            val output = executor.submit(Callable { 
                started.inputStream.bufferedReader().use { it.readText() } 
            })
            outputFuture = output
            
            val completed = started.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
            if (!completed) {
                started.destroyForcibly()
                output.cancel(true) // 👈 强制取消异步线程，绝不留作僵尸死锁
                return XposedShellResult(exitCode = -1, stdout = "", stderr = "timeout", timedOut = true)
            }
            
            // 尝试在限时内捞取回显数据
            val stdoutResult = runCatching { 
                output.get(1, TimeUnit.SECONDS) 
            }.getOrElse { throwable ->
                // 如果读取超时或异状，强行中断任务，防止 FD 线程泄漏
                output.cancel(true)
                ""
            }
            
            XposedShellResult(
                exitCode = started.exitValue(),
                stdout = stdoutResult,
            )
        } catch (e: InterruptedException) {
            outputFuture?.cancel(true)
            Thread.currentThread().interrupt()
            XposedShellResult(exitCode = -1, stdout = "", stderr = e.message ?: e.javaClass.simpleName)
        } catch (e: Exception) {
            outputFuture?.cancel(true)
            XposedShellResult(exitCode = -1, stdout = "", stderr = e.message ?: e.javaClass.simpleName)
        } finally {
            // ====================================================================
            // 🔥 绝对防御：显式打断并释放子进程的底层管道输入流，绝不让 FD 挂起泄露
            // ====================================================================
            runCatching { process?.inputStream?.close() }
            runCatching { process?.outputStream?.close() }
            runCatching { process?.errorStream?.close() }
            runCatching { process?.destroy() }
            // ====================================================================
        }
    }
}
