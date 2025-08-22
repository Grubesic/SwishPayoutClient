package com.rogr.swishpayoutclient.core



import java.io.FileInputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

data class SigningMaterial(val privateKey: PrivateKey, val serialHex: String, val publicKey: PublicKey)

fun loadSigningMaterial(path: String, password: CharArray): SigningMaterial {
    val ks = KeyStore.getInstance("PKCS12")
    FileInputStream(path).use { ks.load(it, password) }

    val alias = ks.aliases().toList().first()
    val key = ks.getKey(alias, password) as PrivateKey
    val cert = ks.getCertificate(alias) as X509Certificate
    return SigningMaterial(
        privateKey = key,
        serialHex = cert.serialNumber.toString(16).uppercase(),
        publicKey = cert.publicKey
    )
}

fun buildSslContext(tlsP12Path: String, password: CharArray): SSLContext {
    val ks = KeyStore.getInstance("PKCS12")
    FileInputStream(tlsP12Path).use { ks.load(it, password) }

    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(ks, password)

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(null as KeyStore?)

    return SSLContext.getInstance("TLS").apply {
        init(kmf.keyManagers, tmf.trustManagers, null)
    }
}