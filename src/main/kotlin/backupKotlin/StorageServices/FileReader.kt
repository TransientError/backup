package backupKotlin.StorageServices

import com.google.common.io.Files
import java.nio.charset.Charset
import java.nio.file.Path

class FileReader(private val charset: Charset) {
    fun readFiles(archivePath: Path): String = Files.asCharSource(archivePath.toFile(), charset).read()

    fun readAsByteArray (archivePath: Path) : ByteArray = Files.asByteSource(archivePath.toFile()).read()

    fun writeFiles(archivePath: Path, toWrite: ByteArray) {
        Files.asByteSink(archivePath.toFile()).write(toWrite)
    }
}