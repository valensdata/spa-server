## Single Page Application Server

Lightweight high-performance single page application server built using [Netty](https://netty.io/). Using this you can easily serve complied single page web applications developed using frameworks like Angular JS, React JS, Vue JS etc.

Server features:
- Builtin support for push-state feature
- Easy to serve requests on HTTPS. You just need your SSL certificates or PKCS12 keystore which contains these certificates
- Support for replacing placeholders like API_URL and other environment specific web configuration parameters in index.html using environment variables or using properties file. This makes it easy to deploy the compiled web application to multiple environments. No need to compile web application multiple times for each environment
- HTTP protocol support for Keep-Alive and Not Modified headers
- Native optimizers for supported Linux and \*nix operating systems for better performance

Usage:
```sh
java -jar spa-server.jar [options]
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
| --placeholder-prefix &lt;placeholder-prefix&gt;  | -pp &lt;placeholder-prefix&gt;  | Environment variable prefix. All environment variables that match the prefix will be used to replace placeholders in index.html  |
| --placeholder-properties &lt;property-file-path&gt;  |   | Path of the properties file which will be used to replace placeholders in index.html  |
| --epoll  |   | Enable native epoll optimization |
| --kqueue  |   | Enable native kqueue optimization |

### Usage examples:
Simply run server on default port:
```sh
java -jar spa-server.jar -d demo-dir-path
```

Run server with port 8080 and epoll optimization:
```sh
java -jar spa-server.jar --epoll -d demo-dir-path -p 8080
```

Run server on HTTPS with SSL certificates:
```sh
java -jar spa-server.jar -d demo-dir-path --ssl -c demo-cert-path -k demo-key-path
```

Run server on HTTPS with PKCS12 keystore:
```sh
java -jar spa-server.jar -d demo-dir-path --ssl --keystore-type PKCS12 --keystore-path demo-keystore-path --keystore-password demo-keystore-password
```

### Using placeholders for web configuration parameters:

Single page applications usually have some web configuration parameters. For example -  
- web application communicates with backend servers and URL of the backend servers is different for each deployment
- web application needs to have parameters like cookie expiry time and this cookie expiry time parameter needs to be different in test(30 minutes) and production(30 days) environments. 

One way to handle such scenarios is to use framework specific features. But this requires rebuild of web application for each different environment.

To avoid rebuilding web application for each environment, web configuration parameters can be defined like this in index.html:
```html
<html>
  <head>
    <script type="text/javascript">
      window.API_URL = "APP_API_URL";
      window.COOKIE_EXPIRE_MINUTES = "APP_COOKIE_EXPIRE_MINUTES";
    </script>
  </head>
  <body>
    ...
  </body>
</html>
```

In the web application code, we can access these parameters using <b>window.API_URL</b>.

### Use environment variables to define web configuration parameters:
In the environment where we need to run web application, we can defined configuration parameters using environment variables. For example-
```sh
export APP_API_URL="http://demohost:9090"
export APP_COOKIE_EXPIRE_MINUTES="30"
```

And run server with placeholder prefix <b>APP</b>
```sh
java -jar spa-server.jar -d demo-dir-path --placeholder-prefix APP
```

### Use property file to define web configuration parameters:
Create <b>application.properties</b> file with values for the configuration parameters. For example-
```properties
APP_API_URL="http://demohost:9090"
APP_COOKIE_EXPIRE_MINUTES="30"
```

And run server with path specifying <b>application.properties</b> file
```sh
java -jar spa-server.jar -d demo-dir-path --placeholder-properties demo-properties-file-path/application.properties
```
