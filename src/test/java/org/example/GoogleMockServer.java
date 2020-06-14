package org.example;

import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Component
@Scope(value= ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GoogleMockServer {

    private final Logger logger = LoggerFactory.getLogger(GoogleMockServer.class);

    private final ClientAndServer mockServer;

    @Autowired
    public GoogleMockServer(@Value("${search.client.url}") String url) {
        URI uri = URI.create(url);
        mockServer = ClientAndServer.startClientAndServer(uri.getPort());
        mockServer.when(
                request().withPath(uri.getPath()))
                .respond(new ServerCallback());
    }

    public void stop() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    public class ServerCallback implements ExpectationResponseCallback {

        @Override
        public HttpResponse handle(HttpRequest httpRequest) {
            String queryWord = httpRequest.getFirstQueryStringParameter("q");
            if ("java".equals(queryWord)) {
                return response().withStatusCode(200).withBody(read(this.getClass().getClassLoader().getResourceAsStream("responses/javaResponse.json")));
            } else if ("oracle".equals(queryWord)) {
                return response().withStatusCode(200).withBody(read(this.getClass().getClassLoader().getResourceAsStream("responses/oracleResponse.json")));
            } else {
                return HttpResponse.notFoundResponse();
            }
        }
    }

    //move to IOUtils
    private String read(InputStream is) {
        try (InputStream inputStream = is;
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return "";
    }
}
