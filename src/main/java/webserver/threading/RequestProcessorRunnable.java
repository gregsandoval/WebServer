package webserver.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.RequestParser;
import webserver.http.message.RequestMessage;
import webserver.http.message.ResponseMessage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Function;

public class RequestProcessorRunnable implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(RequestProcessorRunnable.class);
  private final Function<RequestMessage, ResponseMessage> processor;
  private final Socket socket;

  public RequestProcessorRunnable(Socket socket, Function<RequestMessage, ResponseMessage> processor) {
    this.processor = processor;
    this.socket = socket;
  }

  @Override
  public void run() {
    try (socket) {
      final var request = RequestParser.parse(socket.getInputStream());
      final var response = processor.apply(request);
      final var os = new BufferedOutputStream(socket.getOutputStream());
      for (var bytes : response.getBytes())
        os.write(bytes);
    } catch (IOException e) {
      logger.error("Could not process request, reason: " + e.getMessage());
    }
  }
}