package psn.ifplusor.core.utils;

import java.util.UUID;

public class UUIDGenerator {

    // 要使用生成 URL 的字符
    private static final String[] chars =  {
            "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P",
            "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z", "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v",
            "w", "x", "y", "z", "0", "1", "2", "3",
            "4", "5", "6", "7", "8", "9", "+", "/"
    };

    private UUIDGenerator() {}

    /**
     * @return UUID
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * @return small UUID, transform UUID from radix 16 to radix 64
     */
    public static String getUUIDSmall() {
        String uuid = "0" + getUUID();
        StringBuilder shortBuilder = new StringBuilder();
        for (int i = 0; i < 11; i++ ) {
            String str = uuid.substring(i * 3, i * 3 + 3);
            int x = Integer.parseInt(str, 16);
            shortBuilder.append(chars[x / 64]).append(chars[x % 64]);
        }
        return shortBuilder.toString();
    }
    
    /**
     * @return UUID array
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
