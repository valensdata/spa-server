package com.valens.spaserver;

public class ServerParams {

    private String hostname;

    private Integer port;

    private String basePath;

    private String certificatePath;

    private String privateKeyPath;

    private String placeholderPrefix;

    private ServerParams(String hostname, Integer port, String basePath, String certificatePath, String privateKeyPath, String placeholderPrefix) {
        this.hostname = hostname;
        this.port = port;
        this.basePath = basePath;
        this.certificatePath = certificatePath;
        this.privateKeyPath = privateKeyPath;
        this.placeholderPrefix = placeholderPrefix;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public String getPlaceholderPrefix() {
        return placeholderPrefix;
    }

    public static class Builder {

        private String hostname;

        private Integer port;

        private String basePath;

        private String certificatePath;

        private String privateKeyPath;

        private String placeholderPrefix;

        public static Builder newInstance() {
            return new Builder();
        }

        private Builder() {}

        public Builder setHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder setPort(Integer port) {
            this.port = port;
            return this;
        }

        public Builder setBasePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder setCertificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
            return this;
        }

        public Builder setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
            return this;
        }

        public Builder setPlaceholderPrefix(String placeholderPrefix) {
            this.placeholderPrefix = placeholderPrefix;
            return this;
        }

        public ServerParams build() {
            boolean isSSLEnabled = false;
            if (certificatePath != null) {
                if (privateKeyPath == null) {
                    throw new RuntimeException("private key for the certificate is not provided");
                }
                isSSLEnabled = true;
            }
            if (port == null) {
                if (isSSLEnabled) {
                    port = 8443;
                } else {
                    port = 8080;
                }
            }

            if (basePath == null) {
                basePath = System.getProperty("user.dir");
            }

            return new ServerParams(hostname, port, basePath, certificatePath, privateKeyPath, placeholderPrefix);
        }
    }

}
