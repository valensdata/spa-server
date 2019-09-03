# Single Page Application Server

Lightweight high-performance single page application server built using [Netty](https://netty.io/). Using this you can easily serve complied single page web applications developed using frameworks like Angular JS, React JS, Vue JS etc.

Server features:
- Builtin support for push-state feature
- Easy to serve requests on HTTPS. You just need your SSL certificates or PKCS12 keystore which contains these certificates
- Support for replacing placeholders like API_URL and other environment specific web configuration parameters in index.html using environment variables or using properties file. This makes it easy to deploy the compiled web application to multiple environments. No need to compile web application multiple times for each environment
- HTTP protocol support for Keep-Alive and Not Modified headers
- Native optimizers for supported Linux and \*nix operating systems for better performance

Usage:
```sh
java -jar spa-server-x.x.jar [options]
```

Following options are supported while running the server:

| Option | Short option | Description  |
|---|---|---|
| --directory &lt;path&gt;  | -d &lt;path&gt; | Single page application directory (default: current user working directory)  |
| --port &lt;port&gt;  | -p &lt;port&gt; | Server port (default: 5000) |
| --ssl  |   | Enable https  |
| --keystore-type &lt;type&gt;  |   | This is optional parameter and should be provided when SSL certificates are inside keystore<br>Possible values: PKCS12 |
| --keystore-path &lt;path&gt; |   | Path of the keystore file  |
| --keystore-password &lt;password&gt;  |   | Keystore password   |
| --cert &lt;path&gt; | -c &lt;path&gt; | Path to ssl certificate file   |
| --key &lt;cert-key&gt; | -k &lt;cert-key&gt; | Path to ssl key file  |
| --placeholder-prefix  | -pp  | Environment variable prefix which will be used to replace placeholders in index.html  |
| --epoll  |   | Enable native epoll optimization |
| --kqueue  |   | Enable native kqueue optimization |
