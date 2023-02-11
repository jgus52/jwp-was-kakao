package controller;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import was.annotation.Controller;
import was.annotation.Mapping;
import was.annotation.RequestMethod;
import was.domain.request.Request;
import was.domain.response.Response;
import was.domain.response.ResponseHeader;
import was.domain.response.StatusCode;
import was.domain.response.Version;

import java.util.Optional;

@Controller
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Mapping(method = RequestMethod.GET, path = "/")
    public static Optional<Response> socket_out(Request request) {
        return Optional.of(Response.builder()
                .version(Version.HTTP_1_1)
                .statusCode(StatusCode.OK)
                .responseHeader(ResponseHeader.builder()
                        .contentType("text/html;charset=utf-8")
                        .contentLength("Hello world".getBytes().length)
                        .build())
                .body("Hello world".getBytes()).build());
    }
}
