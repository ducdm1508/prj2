package com.cyber.server.validation;

public class UserNameValidator {

    public static boolean isValidUsername(String username) {
        return username != null && !username.contains(" ");
    }
}
