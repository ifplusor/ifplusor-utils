package psn.ifplusor.core.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    /*
     * ObjectMapper 是 JSON 操作的核心，Jackson 的所有 JSON 操作都是在 ObjectMapper 中实现。
     * ObjectMapper 有多个 JSON 序列化的方法，可以把 JSON 字符串保存 File、OutputStream 等不同的介质中。
     *
     * writeValue(File arg0, Object arg1)           把 arg1 转成 json 序列，并保存到 arg0 文件中。
     * writeValue(OutputStream arg0, Object arg1)   把 arg1 转成 json 序列，并保存到 arg0 输出流中。
     * writeValueAsBytes(Object arg0)               把 arg0 转成 json 序列，并把结果输出成字节数组。
     * writeValueAsString(Object arg0)              把 arg0 转成 json 序列，并把结果输出成字符串。
     */

    public static String generateJSONFromObject(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}