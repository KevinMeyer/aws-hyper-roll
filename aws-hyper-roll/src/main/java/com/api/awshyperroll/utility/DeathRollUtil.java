package com.api.awshyperroll.utility;

import java.util.Random;

public class DeathRollUtil {
    public static String createRandomCode(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
    
        String code = random.ints(leftLimit, rightLimit + 1)
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();

        return code.toUpperCase();
    }
    
}
