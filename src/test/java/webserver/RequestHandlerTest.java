package webserver;

import org.junit.jupiter.api.Test;
import support.StubSocket;
import was.handler.RequestHandler;
import was.utils.FileIoUtils;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class RequestHandlerTest {
    @Test
    void socket_out() {
        // given
        final var socket = new StubSocket();
        final var handler = new RequestHandler(socket);

        // when
        handler.run();

        // then
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 11 ",
                "",
                "Hello world");

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void index() throws IOException, URISyntaxException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /index.html HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        // when
        handler.run();

        // then


        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Content-Length: 6902 \r\n" +
                "\r\n" +
                new String(FileIoUtils.loadFileFromClasspath("templates/index.html"));

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void css() throws IOException, URISyntaxException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /css/styles.css HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: text/css,*/*;q=0.1 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        // when
        handler.run();

        // then


        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/css \r\n" +
                "Content-Length: 7065 \r\n" +
                "\r\n" +
                new String(FileIoUtils.loadFileFromClasspath("static/css/styles.css"));

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void createUser() {
        String body = "userId=cu&password=password&name=%EC%9D%B4%EB%8F%99%EA%B7%9C&email=brainbackdoor%40gmail.com";
        final String httpRequest = String.join("\r\n",
                "POST /user/create HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: */* ",
                "Connection: keep-alive ",
                "Content-Length: " + body.length(),
                "",
                body);

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        // when
        handler.run();

        var expected = "HTTP/1.1 302 FOUND \r\n" +
                "Location: /index.html \r\n" +
                "\r\n";

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void login() {
        createUser();
        String body = "userId=cu&password=password";
        final String httpRequest = String.join("\r\n",
                "POST /user/login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: */* ",
                "Connection: keep-alive ",
                "Content-Length: " + body.length(),
                "",
                body);

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        handler.run();

        var expected = "HTTP/1.1 302 FOUND \r\n" +
                "Location: /index.html \r\n" +
                "Set-Cookie: ";

        String output = socket.output();
        assertThat(output).startsWith(expected);
    }

    @Test
    void tryLoginWithSession() {
        String sessionId = loginForTest();
        String body = "userId=cu&password=password";

        final String httpRequest = String.join("\r\n",
                "POST /user/login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: */* ",
                "Connection: keep-alive ",
                "Content-Length: " + body.length(),
                "Cookie: " + sessionId + "; Path=/ ",
                "",
                body);
        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        handler.run();

        var expected = "HTTP/1.1 302 FOUND \r\n" +
                "Location: /index.html \r\n" +
                "\r\n";

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    void userList() throws IOException, URISyntaxException {
        createUser();
        String sessionId = loginForTest();

        final String httpRequest = String.join("\r\n",
                "GET /user/list HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: */* ",
                "Connection: keep-alive ",
                "Cookie: " + sessionId + "; Path=/ ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        handler.run();

        String result = new String(FileIoUtils.loadFileFromClasspath("templates/user/list.html"));
        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html \r\n" +
                "Content-Length: 4725 \r\n" +
                "\r\n" +
                result;

        assertThat(socket.output()).isEqualTo(expected);
    }

    private String loginForTest() {
        createUser();
        String body = "userId=cu&password=password";
        final String httpRequest = String.join("\r\n",
                "POST /user/login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: */* ",
                "Connection: keep-alive ",
                "Content-Length: " + body.length(),
                "",
                body);

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        handler.run();

        var expected = "HTTP/1.1 302 FOUND \r\n" +
                "Location: /index.html \r\n" +
                "Set-Cookie: ";

        String output = socket.output();
        return output.substring(expected.length()).split(";")[0];
    }

    @Test
    void userListToLogin() throws IOException, URISyntaxException {
        createUser();

        final String httpRequest = String.join("\r\n",
                "GET /user/list HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Accept: */* ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final RequestHandler handler = new RequestHandler(socket);

        handler.run();

        var expected = "HTTP/1.1 302 FOUND \r\n" +
                "Location: /user/login.html \r\n" +
                "\r\n";

        assertThat(socket.output()).isEqualTo(expected);
    }
}