package com.valens.spaserver;

import com.valens.spaserver.constants.KeyStoreType;
import com.valens.spaserver.constants.ServerParams;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;

class SslContextUtil {

    static SslContext newInstance() throws Exception {
        HashMap<String, String> serverParamsMap = HttpStaticFileServer.serverParamsMap;
        switch (serverParamsMap.get(ServerParams.KEYSTORE_TYPE)) {
            case KeyStoreType.CERT_CHAIN:
                return certChainSslContext(serverParamsMap);
            case KeyStoreType.PKCS12:
                return pkcs12SslContext(serverParamsMap);
            default:
                throw new UnsupportedOperationException("keystore type not supported");
        }
    }

    private static SslContext certChainSslContext(HashMap<String, String> serverParamsMap) throws Exception {
        return SslContextBuilder.forServer(new File(serverParamsMap.get(ServerParams.CERTIFICATE_PATH)), new File(serverParamsMap.get(ServerParams.PRIVATE_KEY_PATH))).build();
    }

    private static SslContext pkcs12SslContext(HashMap<String, String> serverParamsMap) throws Exception {
        char[] ksPassword = serverParamsMap.get(ServerParams.KEYSTORE_PASSWORD).toCharArray();

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(serverParamsMap.get(ServerParams.KEYSTORE_PATH)), ksPassword);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, ksPassword);
        return SslContextBuilder.forServer(kmf).clientAuth(ClientAuth.OPTIONAL).build();
    }

}
