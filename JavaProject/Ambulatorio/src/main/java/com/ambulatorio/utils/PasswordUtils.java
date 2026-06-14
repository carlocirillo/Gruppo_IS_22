package com.ambulatorio.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtils {

    public static String generaPasswordHash(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static boolean verifica(String password, String hash) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
        return result.verified;
    }
}
