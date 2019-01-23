package backupKotlin

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.Test

class ArchiveGeneratorTest {

    @Test
    fun defaultPackageManagerDoesNotNeedListGenerator() {
        sweepInput(PackageManager("cargo"))
    }

    @Test
    fun nonDefaultPackageManagerDoesNeedListGenerator() {
        assertThatIllegalArgumentException().isThrownBy {sweepInput(PackageManager("unknown"))}
    }

    @Test
    fun archivePathShouldWork() {
        assertThat(getArchivePath(PackageManager("name")).toString()).isEqualTo("./archives/name-package.txt")
    }
}