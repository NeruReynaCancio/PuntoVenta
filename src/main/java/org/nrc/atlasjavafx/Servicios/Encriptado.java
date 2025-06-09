package org.nrc.atlasjavafx.Servicios;
import org.mindrot.jbcrypt.BCrypt;

public class Encriptado{
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}