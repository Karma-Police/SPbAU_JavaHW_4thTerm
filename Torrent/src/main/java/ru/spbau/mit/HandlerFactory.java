package ru.spbau.mit;

import java.net.Socket;

/**
 * Created by michael on 16.05.16.
 */

public interface HandlerFactory {
    Runnable createHandler(Socket socket);
}
