package backupKotlin.StorageServices

import com.google.common.io.Files
import java.nio.charset.Charset
import java.nio.file.Path

class FileReader(private val charset: Charset) {
    fun readFiles(archivePath: Path): String = Files.asCharSource(archivePath.toFile(), charset).read()
}