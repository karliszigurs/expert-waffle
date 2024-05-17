package testutils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {

    public static String asJsonString(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
