package com.rogr.swishpayoutclient.core.pkcs11


import java.net.Socket
import java.nio.file.Files
import java.security.*
import java.security.cert.X509Certificate
import java.util.Locale
import javax.net.ssl.*

data class KeyAndCert(val privateKey: PrivateKey, val cert: X509Certificate)
class Pkcs11Handle(val provider: Provider, val keyStore: KeyStore)

/** Create a SunPKCS11 provider using the official JCA hook (JDK 9+). */
private fun createPkcs11ProviderViaConfigure(cfgText: String): Provider {
    val base = Security.getProvider("SunPKCS11")
        ?: error("SunPKCS11 provider not found. Make sure --add-modules=jdk.crypto.cryptoki is set.")

    // SunPKCS11 expects a *file path* for Provider.configure(...)
    val cfgFile = Files.createTempFile("p11-", ".cfg").toFile().apply {
        writeText(cfgText)
        deleteOnExit()
    }
    // Returns a new *configured* provider instance
    return base.configure(cfgFile.absolutePath)
}

/** Load PKCS#11 keystore (YubiKey PIV). */
fun loadPkcs11KeyStore(
    libraryPath: String,
    pin: CharArray,
    slotListIndex: Int = 0
): Pkcs11Handle {
    val cfg = """
        name = YubiKeyPIV
        library = $libraryPath
        slotListIndex = $slotListIndex
    """.trimIndent()

    val provider = createPkcs11ProviderViaConfigure(cfg)
    Security.addProvider(provider)

    /*val ks = KeyStore.getInstance("PKCS11", provider)
    ks.load(null, pin)       // PIN is used here*/

    val ks = KeyStore.Builder.newInstance("PKCS11", provider, KeyStore.PasswordProtection(pin))

    var kestorelist = debugList(ks.keyStore)
    return Pkcs11Handle(provider, ks.keyStore)
}



fun debugList(ks: KeyStore): String {
    val lines = buildList {
        val e = ks.aliases()
        while (e.hasMoreElements()) {
            val a = e.nextElement()
            add("$a | isKeyEntry=${ks.isKeyEntry(a)} | isCertificateEntry=${ks.isCertificateEntry(a)}")
        }
    }.joinToString("\n")
    return lines
}

fun getKeyAndCert(ks: KeyStore, alias: String, pin: CharArray): KeyAndCert {
    // Prefer the Entry API: forces login and returns a PrivateKeyEntry token-ref
    (ks.getEntry(alias, KeyStore.PasswordProtection(pin)) as? KeyStore.PrivateKeyEntry)?.let { pe ->
        val cert = pe.certificate as X509Certificate
        return KeyAndCert(pe.privateKey, cert)
    }
   var aliases = listAliases(ks)
    // Fallbacks + diagnostics
    val key = ks.getKey(alias, pin)
        ?: error("Alias '$alias' has no PrivateKey (isKeyEntry=${ks.isKeyEntry(alias)}). " +
                "Token aliases: ${listAliases(ks)}")
    val cert = ks.getCertificate(alias) as? X509Certificate
        ?: error("Alias '$alias' has no X509Certificate. Aliases: ${listAliases(ks)}")
    return KeyAndCert(key as PrivateKey, cert)
}

fun listAliases(ks: KeyStore): List<String> {
    val out = mutableListOf<String>()
    val e = ks.aliases()
    while (e.hasMoreElements()) out += e.nextElement()
    return out
}

fun x509SerialHex(cert: X509Certificate): String =
    cert.serialNumber.toString(16).uppercase(Locale.ROOT)

private class AliasKeyManager(
    private val delegate: X509ExtendedKeyManager,
    private val preferredAlias: String
) : X509ExtendedKeyManager() {
    override fun chooseClientAlias(keyTypes: Array<String>?, issuers: Array<Principal>?, socket: Socket?) =
        preferredAlias
    override fun chooseEngineClientAlias(keyTypes: Array<String>?, issuers: Array<Principal>?, engine: SSLEngine?) =
        preferredAlias
    override fun getClientAliases(keyType: String?, issuers: Array<Principal>?) =
        delegate.getClientAliases(keyType, issuers)
    override fun getServerAliases(keyType: String?, issuers: Array<Principal>?) =
        delegate.getServerAliases(keyType, issuers)
    override fun chooseServerAlias(keyType: String?, issuers: Array<Principal>?, socket: Socket?) =
        delegate.chooseServerAlias(keyType, issuers, socket)
    override fun getCertificateChain(alias: String?) = delegate.getCertificateChain(alias)
    override fun getPrivateKey(alias: String?) = delegate.getPrivateKey(alias)
}

/** Build TLSv1.2 mTLS context using a fixed card alias (e.g. "9a"). */
fun buildSslContextFromPkcs11(
    ks: KeyStore,
    pin: CharArray,
    preferredAlias: String = "9a",
    trustStore: KeyStore? = null
): SSLContext {
    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(ks, pin)
    val kms = kmf.keyManagers.map {
        if (it is X509ExtendedKeyManager) AliasKeyManager(it, preferredAlias) else it
    }.toTypedArray()

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(trustStore) // null => system trust

    return SSLContext.getInstance("TLSv1.2").apply { init(kms, tmf.trustManagers, null) }
}