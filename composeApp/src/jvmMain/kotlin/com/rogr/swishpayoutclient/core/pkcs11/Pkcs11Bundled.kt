package com.rogr.swishpayoutclient.core.pkcs11

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.Paths

object Pkcs11Bundled {
    // ---- Public API (unchanged) ----
    /** Returns an absolute filesystem path to libykcs11 (prefers system/Homebrew, falls back to bundled). */
    fun loadPkcs11Library(): String {
        resolveSystemInstalled()?.let { return it }
        return extractBundled()
    }

    // ---- Resolution order: system -> env -> Homebrew -> bundled ----
    private fun resolveSystemInstalled(): String? {
        // 1) System property override: -Dpkcs11.path=/custom/libykcs11.dylib
        System.getProperty("pkcs11.path")?.trim()?.takeIf { it.isNotEmpty() }?.let { p ->
            if (Files.isRegularFile(Paths.get(p))) return p
        }

        // 2) Env var override: YKCS11_PATH=/custom/libykcs11.dylib
        System.getenv("YKCS11_PATH")?.trim()?.takeIf { it.isNotEmpty() }?.let { p ->
            if (Files.isRegularFile(Paths.get(p))) return p
        }

        // 3) Known Homebrew locations on macOS
        if (isMac()) {
            val brewArm  = Paths.get("/opt/homebrew/opt/yubico-piv-tool/lib/libykcs11.dylib")
            val brewX64  = Paths.get("/usr/local/opt/yubico-piv-tool/lib/libykcs11.dylib")
            when {
                Files.isRegularFile(brewArm) -> return brewArm.toString()
                Files.isRegularFile(brewX64) -> return brewX64.toString()
            }
        }

        // (Windows/Linux system paths could be added here if you want)

        return null
    }

    // ---- Bundled fallback ----
    private fun extractBundled(): String {
        val base = platformDir()
        val tmp: Path = Files.createTempDirectory("pkcs11-embed-")

        val names = when {
            base.endsWith("win-x64") -> listOf("ykcs11.dll")
            else -> listOf("libykcs11.dylib", "libykpiv.dylib", "libcrypto.3.dylib")
        }

        names.forEach { n ->
            val resPath = "$base/$n"
            val ins = readResource(resPath)
                ?: error(
                    "Missing resource: $resPath\n" +
                            "Make sure the file exists at composeApp/src/jvmMain/resources/$resPath"
                )
            ins.use {
                Files.copy(it, tmp.resolve(n), StandardCopyOption.REPLACE_EXISTING)
            }
        }

        // On macOS we adjusted install_name to @loader_path, so keeping them together is enough.
        val mainName = if (base.endsWith("win-x64")) "ykcs11.dll" else "libykcs11.dylib"
        return tmp.resolve(mainName).toAbsolutePath().toString()
    }

    // ---- Helpers ----
    private fun platformDir(): String {
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        return when {
            os.contains("mac") && (arch.contains("arm64") || arch.contains("aarch64")) -> "pkcs11/mac-aarch64"
            os.contains("mac") && (arch.contains("x86_64") || arch.contains("amd64"))  -> "pkcs11/mac-x64"
            os.contains("win") && arch.contains("64")                                  -> "pkcs11/win-x64"
            else -> error("Unsupported platform: os=$os arch=$arch")
        }
    }

    private fun readResource(path: String) =
        Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
            ?: Pkcs11Bundled::class.java.classLoader?.getResourceAsStream(path)

    private fun isMac() = System.getProperty("os.name").lowercase().contains("mac")
}