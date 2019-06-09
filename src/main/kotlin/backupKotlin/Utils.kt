package backupKotlin

import com.google.common.base.Throwables
import mu.KLogger
import java.nio.charset.Charset

fun closeProcessAndStreams(process: Process) {
    process.inputStream.close()
    process.outputStream.close()
    process.errorStream.close()
    process.destroy()
}

fun logIfNotEmpty(logger: ((String) -> Unit), byteArray: ByteArray) {
    if (byteArray.isNotEmpty()) {
        logger(byteArray.toString(Charset.defaultCharset()).trim())
    }
}

fun runIndependently(runnable: () -> Unit, log: KLogger) : Unit {
    try {
        runnable.invoke()
    } catch (e: Exception) {
        log.error { Throwables.getStackTraceAsString(e) }
    }
}
