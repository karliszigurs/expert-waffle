package testutils;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Utils {

    public static String asJsonString(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
