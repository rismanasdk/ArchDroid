package id.archdroid.domain.model

data class TerminalSessionModel(
    val id: String,
    val title: String,
    val cwd: String,
    val shell: String,
    val isActive: Boolean,
    val updatedAt: Long,
    val bootstrapCommand: String = ""
)

enum class PackageActionType(val pacmanArgs: List<String>) {
    Update(listOf("-Syu")),
    Install(listOf("-S")),
    Remove(listOf("-R")),
    Search(listOf("-Ss"))
}
