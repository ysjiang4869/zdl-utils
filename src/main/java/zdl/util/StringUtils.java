package zdl.util;

import com.alibaba.fastjson.JSONArray;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ZDLegend on 2016/8/16.
 * <p>
 * 字符转相关操作的函数在这里
 */
public final class StringUtils {

    /**
     * 编译后的正则表达式缓存
     */
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    private StringUtils() {
    }

    /**
     * @apiNote sdk结构体中的字符串转换为byte[]
     */
    public static byte[] arrayCopy(byte[] dst, byte[] src) {
        int minLen = dst.length < src.length ? dst.length : src.length;
        System.arraycopy(src, 0, dst, 0, minLen);
        return dst;
    }

    /**
     * @apiNote 给结构体中的字符串赋值
     */
    public static void setSdkBytes(byte[] dst, String content) {
        byte[] srcBytes = content.getBytes(StandardCharsets.UTF_8);
        int size = Math.min(srcBytes.length, dst.length);
        System.arraycopy(srcBytes, 0, dst, 0, size == dst.length ? dst.length - 1 : size);
    }

    /**
     * @apiNote JSONArray转byte[]
     * <p>
     * JSONArray转换为C代码中Char[x][y]
     * 既JSONArray -> byte[x*y] -> Char[x][y]
     */
    public static void byte2Copy(byte[] dst, JSONArray array, int x, int y) {

        if (array.size() < x) {
            x = array.size();
        }

        int length = x * y;
        int f = 0;
        StringBuilder sb = new StringBuilder();
        sb.setLength(length);
        for (int i = 0; i < x; i++) {
            sb.insert(f, array.getString(i));
            f = f + y;
        }
        StringUtils.setSdkBytes(dst, sb.toString());
    }

    /**
     * @apiNote 生成随机字符串
     */
    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = new Random().nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * @apiNote 生成MD5摘要值
     */
    public static String md5Util(String ps) throws NoSuchAlgorithmException {
        if (null != ps) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(ps.getBytes());
            byte[] results = md.digest();
            return bytesToHex(results);
        } else {
            return null;
        }
    }

    /**
     * @apiNote二进制转十六进制
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder md5str = new StringBuilder();

        /* 把数组每一字节换成16进制连成md5字符串 */
        int digital;
        for (byte aByte : bytes) {
            digital = aByte;

            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString().toLowerCase();
    }

    /**
     * @apiNote byte[]转String
     */
    public static String bytesToString(byte[] obj) {

        /* trim()去掉末尾的 \u0020 也就是空格 */
        String str = new String(obj, Charset.forName("UTF-8")).trim();

        /* 去掉末尾的0 */
        if (str.indexOf('\u0000') > 0) {
            str = str.substring(0, str.indexOf('\u0000'));
        }

        return str;
    }


    /**
     * @apiNote 去除结构体成员名中的前缀
     */
    public static String removLowerHaed(String word) {

        int index = 0;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isUpperCase(c)) {
                index = i;
                break;
            }
        }

        word = word.replace(word.substring(0, index), "");
        return word;
    }

    /**
     * @apiNote 把URL中的空格（%20）转化成普通的字符串空格
     */
    public static String spaceString(String url) {
        if (url.contains("%20")) {
            url = url.replace("%20", " ");
        }
        return url;
    }

    /**
     * @apiNote 将字符串进行BASE64编码
     */
    public static String getBASE64(String s) {
        if (s == null) return null;
        return Arrays.toString(Base64.getEncoder().encode(s.getBytes()));
    }

    /**
     * @apiNote 将字符串进行BASE64解码
     */
    public static String getFromBASE64(String s) {
        if (s == null) return null;
        try {
            byte[] b = Base64.getDecoder().decode(s);
            return new String(b, Charset.forName("UTF-8")).trim();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }

        return swapStream.toByteArray();
    }


    /**
     * 根据byte数组，生成文件
     */
    public static void getFile(byte[] bfile, String path) throws IOException {
        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(bfile);
        }
    }

    /**
     * 编译一个正则表达式，并且进行缓存,如果换成已存在则使用缓存
     *
     * @param regex 表达式
     * @return 编译后的Pattern
     */
    public static Pattern compileRegex(String regex) {
        return PATTERN_CACHE.computeIfAbsent(regex, p -> {
            Pattern compile = Pattern.compile(p);
            PATTERN_CACHE.put(regex, compile);
            return compile;
        });
    }

    /**
     * 将字符串的第一位转为小写
     *
     * @param str 需要转换的字符串
     * @return 转换后的字符串
     */
    public static String toLowerCaseFirstOne(String str) {
        if (Character.isLowerCase(str.charAt(0)))
            return str;
        else {
            char[] chars = str.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            return new String(chars);
        }
    }

    /**
     * 将字符串的第一位转为大写
     *
     * @param str 需要转换的字符串
     * @return 转换后的字符串
     */
    public static String toUpperCaseFirstOne(String str) {
        if (Character.isUpperCase(str.charAt(0)))
            return str;
        else {
            char[] chars = str.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        }
    }

    /**
     * 下划线命名转为驼峰命名
     *
     * @param str 下划线命名格式
     * @return 驼峰命名格式
     */
    public static final String underScoreCase2CamelCase(String str) {
        if (!str.contains("_")) return str;
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        boolean hitUnderScore = false;
        sb.append(chars[0]);
        for (int i = 1; i < chars.length; i++) {
            char c = chars[i];
            if (c == '_') {
                hitUnderScore = true;
            } else {
                if (hitUnderScore) {
                    sb.append(Character.toUpperCase(c));
                    hitUnderScore = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 驼峰命名法转为下划线命名
     *
     * @param str 驼峰命名格式
     * @return 下划线命名格式
     */
    public static final String camelCase2UnderScoreCase(String str) {
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将异常栈信息转为字符串
     *
     * @param e 字符串
     * @return 异常栈
     */
    public static String throwable2String(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * 字符串连接，将参数列表拼接为一个字符串
     *
     * @param more 追加
     * @return 返回拼接后的字符串
     */
    public static String concat(Object... more) {
        return concatSpiltWith("", more);
    }

    public static String concatSpiltWith(String split, Object... more) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < more.length; i++) {
            if (i != 0) buf.append(split);
            buf.append(more[i]);
        }
        return buf.toString();
    }

    /**
     * 将字符串转移为ASCII码
     *
     * @param str 字符串
     * @return 字符串ASCII码
     */
    public static String toASCII(String str) {
        StringBuilder strBuf = new StringBuilder();
        byte[] bGBK = str.getBytes();
        for (byte b : bGBK) {
            strBuf.append(String.format("%02X", b));
        }
        return strBuf.toString();
    }

    public static String toUnicode(String str) {
        StringBuilder strBuf = new StringBuilder();
        char[] chars = str.toCharArray();
        for (char aChar : chars) {
            strBuf.append("\\u").append(Integer.toHexString(aChar));
        }
        return strBuf.toString();
    }

    public static String toUnicodeString(char[] chars) {
        StringBuilder strBuf = new StringBuilder();
        for (char aChar : chars) {
            strBuf.append("\\u").append(Integer.toHexString(aChar));
        }
        return strBuf.toString();
    }

    static final char CN_CHAR_START = '\u4e00';
    static final char CN_CHAR_END = '\u9fa5';

    /**
     * 是否包含中文字符
     *
     * @param str 要判断的字符串
     * @return 是否包含中文字符
     */
    public static boolean containsChineseChar(String str) {
        char[] chars = str.toCharArray();
        for (char aChar : chars) {
            if (aChar >= CN_CHAR_START && aChar <= CN_CHAR_END) return true;
        }
        return false;
    }

    /**
     * 对象是否为无效值
     *
     * @param obj 要判断的对象
     * @return 是否为有效值（不为null 和 "" 字符串）
     */
    public static boolean isNullOrEmpty(Object obj) {
        return obj == null || "".equals(obj.toString());
    }

    /**
     * 参数是否是有效数字 （整数或者小数）
     *
     * @param obj 参数（对象将被调用string()转为字符串类型）
     * @return 是否是数字
     */
    public static boolean isNumber(Object obj) {
        if (obj instanceof Number) return true;
        return isInt(obj) || isDouble(obj);
    }

    public static String matcherFirst(String patternStr, String text) {
        Pattern pattern = compileRegex(patternStr);
        Matcher matcher = pattern.matcher(text);
        String group = null;
        if (matcher.find()) {
            group = matcher.group();
        }
        return group;
    }

    /**
     * 参数是否是有效整数
     *
     * @param obj 参数（对象将被调用string()转为字符串类型）
     * @return 是否是整数
     */
    public static boolean isInt(Object obj) {
        if (isNullOrEmpty(obj))
            return false;
        if (obj instanceof Integer)
            return true;
        return obj.toString().matches("[-+]?\\d+");
    }

    /**
     * 字符串参数是否是double
     *
     * @param obj 参数（对象将被调用string()转为字符串类型）
     * @return 是否是double
     */
    public static boolean isDouble(Object obj) {
        if (isNullOrEmpty(obj))
            return false;
        if (obj instanceof Double || obj instanceof Float)
            return true;
        return compileRegex("[-+]?\\d+\\.\\d+").matcher(obj.toString()).matches();
    }

    /**
     * 判断一个对象是否为boolean类型,包括字符串中的true和false
     *
     * @param obj 要判断的对象
     * @return 是否是一个boolean类型
     */
    public static boolean isBoolean(Object obj) {
        if (obj instanceof Boolean) return true;
        String strVal = String.valueOf(obj);
        return "true".equalsIgnoreCase(strVal) || "false".equalsIgnoreCase(strVal);
    }

    /**
     * 对象是否为true
     *
     * @param obj
     * @return
     */
    public static boolean isTrue(Object obj) {
        return "true".equals(String.valueOf(obj));
    }

    /**
     * 判断一个数组里是否包含指定对象
     *
     * @param arr 对象数组
     * @param obj 要判断的对象
     * @return 是否包含
     */
    public static boolean contains(Object[] arr, Object... obj) {
        if (arr == null || obj == null || arr.length == 0) return false;
        return Arrays.asList(arr).containsAll(Arrays.asList(obj));
    }

    /**
     * 将对象转为int值,如果对象无法进行转换,则使用默认值
     *
     * @param object       要转换的对象
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static int toInt(Object object, int defaultValue) {
        if (object instanceof Number)
            return ((Number) object).intValue();
        if (isInt(object)) {
            return Integer.parseInt(object.toString());
        }
        if (isDouble(object)) {
            return (int) Double.parseDouble(object.toString());
        }
        return defaultValue;
    }

    /**
     * 将对象转为int值,如果对象不能转为,将返回0
     *
     * @param object 要转换的对象
     * @return 转换后的值
     */
    public static int toInt(Object object) {
        return toInt(object, 0);
    }

    /**
     * 将对象转为long类型,如果对象无法转换,将返回默认值
     *
     * @param object       要转换的对象
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static long toLong(Object object, long defaultValue) {
        if (object instanceof Number)
            return ((Number) object).longValue();
        if (isInt(object)) {
            return Long.parseLong(object.toString());
        }
        if (isDouble(object)) {
            return (long) Double.parseDouble(object.toString());
        }
        return defaultValue;
    }

    /**
     * 将对象转为 long值,如果无法转换,则转为0
     *
     * @param object 要转换的对象
     * @return 转换后的值
     */
    public static long toLong(Object object) {
        return toLong(object, 0);
    }

    /**
     * 将对象转为Double,如果对象无法转换,将使用默认值
     *
     * @param object       要转换的对象
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static double toDouble(Object object, double defaultValue) {
        if (object instanceof Number)
            return ((Number) object).doubleValue();
        if (isNumber(object)) {
            return Double.parseDouble(object.toString());
        }
        if (null == object) return defaultValue;
        return 0;
    }

    /**
     * 将对象转为Double,如果对象无法转换,将使用默认值0
     *
     * @param object 要转换的对象
     * @return 转换后的值
     */
    public static double toDouble(Object object) {
        return toDouble(object, 0);
    }

    /**
     * 分隔字符串,根据正则表达式分隔字符串,只分隔首个,剩下的的不进行分隔,如: 1,2,3,4 将分隔为 ['1','2,3,4']
     *
     * @param str   要分隔的字符串
     * @param regex 分隔表达式
     * @return 分隔后的数组
     */
    public static String[] splitFirst(String str, String regex) {
        return str.split(regex, 2);
    }

    /**
     * 将对象转为字符串,如果对象为null,则返回null,而不是"null"
     *
     * @param object 要转换的对象
     * @return 转换后的对象
     */
    public static String toString(Object object) {
        return toString(object, null);
    }

    /**
     * 将对象转为字符串,如果对象为null,则使用默认值
     *
     * @param object       要转换的对象
     * @param defaultValue 默认值
     * @return 转换后的字符串
     */
    public static String toString(Object object, String defaultValue) {
        if (object == null) return defaultValue;
        return String.valueOf(object);
    }

    /**
     * 将对象转为String后进行分割，如果为对象为空或者空字符,则返回null
     *
     * @param object 要分隔的对象
     * @param regex  分隔规则
     * @return 分隔后的对象
     */
    public static final String[] toStringAndSplit(Object object, String regex) {
        if (isNullOrEmpty(object)) return null;
        return String.valueOf(object).split(regex);
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 提取字符串中所有数字
     *
     * @param content 字符串
     * @return 字符串中的所有数字数组
     */
    public static List<Integer> getDigit(String content) {
        String[] cs = Pattern.compile("[^0-9]+").split(content);
        return Stream.of(cs).distinct()
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .filter(s -> s.length() < 5)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    public static boolean isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = 0;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
                chLength++;
            }
        }
        float result = count / chLength;
        if (result > 0.4) {
            return true;
        } else {
            return false;
        }
    }
}
