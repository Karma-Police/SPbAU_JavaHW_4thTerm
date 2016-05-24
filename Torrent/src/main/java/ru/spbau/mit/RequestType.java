package ru.spbau.mit;

/**
 * Created by michael on 15.05.16.
 */

public enum RequestType {
    UNKNOWN(0),
    GET_FILES(1),
    UPLOAD(2),
    GET_FILE_SEEDERS(3),
    UPDATE(4),
    STAT(5),
    GET_FILE_PART(6);


    private final int id;
    RequestType(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static RequestType getServerRequest(int request) {
        if (request > 0 && request < STAT.id) {
            return values()[request];
        } else {
            return UNKNOWN;
        }
    }

    public static RequestType getP2PRequest(int request) {
        request += 4;
        if (request != 5 && request != 6) {
            return UNKNOWN;
        } else {
            return values()[request];
        }
    }
}
