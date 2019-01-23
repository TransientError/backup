package backupKotlin

import arrow.effects.IO
import mu.KLogger
import java.nio.charset.Charset

fun closeProcessStreams(process: Process) {
    process.inputStream.close()
    process.outputStream.close()
    process.errorStream.close()
}

fun runAsynchronously(log: KLogger): (IO<Unit>, IO<Unit>) -> IO<Unit> {
    return {first, second ->
        first.runAsync { it.fold(
                {exception -> log.error(exception) {  }; second},
                {second}
        ) }
    }
}

fun logIfNotEmpty(logger: ((String) -> Unit), byteArray: ByteArray) {
    if (byteArray.isNotEmpty()) {
        logger(byteArray.toString(Charset.defaultCharset()).trim())
    }
}
