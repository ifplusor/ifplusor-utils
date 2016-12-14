package psn.ifplusor.core.common;

import java.util.UUID;

public class UUIDGenerator {

    // 要使用生成 URL 的字符
    private static String[] chars =  {
            "a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p",
            "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
    };

    public UUIDGenerator() {
    }

    /**
     * @return String UUID
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * @return String UUID
     */
    public static String getUUIDSmall() {
        StringBuilder shortBuilder = new StringBuilder();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuilder.append(chars[x % 0x3E]);
        }
        return shortBuilder.toString();
    }
    
    /**
     */
    public static String[] getUUIDs(int number) {

        if (number < 1) {
            return null;
        }

        String[] ss = new String[number];
        for (int i = 0; i < number; i++) {
            ss[i] = getUUID();
        }

        return ss;
    }
}
