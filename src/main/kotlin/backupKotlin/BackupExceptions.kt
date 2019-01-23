package backupKotlin

class BackupException(private val retryable: Boolean = false) : Exception()
