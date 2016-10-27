package psn.ifplusor.core.common;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    /*
     * ObjectMapper 是 JSON 操作的核心，Jackson 的所有 JSON 操作都是在 ObjectMapper 中实现。
     * ObjectMapper 有多个 JSON 序列化的方法，可以把 JSON 字符串保存 File、OutputStream 等不同的介质中。
     *
     * writeValue(File arg0, Object arg1)           把 arg1 转成 json 序列，并保存到 arg0 文件中。
     * writeValue(OutputStream arg0, Object arg1)   把 arg1 转成 json 序列，并保存到 arg0 输出流中。
     * writeValueAsBytes(Object arg0)               把 arg0 转成 json 序列，并把结果输出成字节数组。
     * writeValueAsString(Object arg0)              把 arg0 转成 json 序列，并把结果输出成字符串。
     */

    public static String generateJsonFromObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static Map generateMapFromJson(String json) {
        try {
            return objectMapper.readValue(json, HashMap.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}