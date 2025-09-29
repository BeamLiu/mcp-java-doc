package io.emop;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 实用工具类，提供各种静态方法。
 * Utility class providing various static methods.
 * さまざまな静的メソッドを提供するユーティリティクラス。
 * 다양한 정적 메서드를 제공하는 유틸리티 클래스。
 * 
 * <p>该类包含以下功能模块：
 * This class contains the following functional modules:
 * このクラスには以下の機能モジュールが含まれています：
 * 이 클래스에는 다음과 같은 기능 모듈이 포함되어 있습니다:</p>
 * 
 * <ul>
 *   <li>字符串处理工具 / String processing utilities / 文字列処理ユーティリティ / 문자열 처리 유틸리티</li>
 *   <li>文件操作工具 / File operation utilities / ファイル操作ユーティリティ / 파일 작업 유틸리티</li>
 *   <li>日期时间工具 / Date and time utilities / 日時ユーティリティ / 날짜 시간 유틸리티</li>
 *   <li>集合处理工具 / Collection processing utilities / コレクション処理ユーティリティ / 컬렉션 처리 유틸리티</li>
 *   <li>验证工具 / Validation utilities / 検証ユーティリティ / 검증 유틸리티</li>
 * </ul>
 * 
 * @author Utility Author / ユーティリティ作成者 / 유틸리티 작성자
 * @version 2.0
 * @since 2024-01-01
 */
@UtilityClass
@Slf4j
public class UtilityClassTest {
    
    /**
     * 邮箱验证正则表达式 / Email validation regex / メール検証正規表現 / 이메일 검증 정규식
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    /**
     * 电话号码验证正则表达式 / Phone number validation regex / 電話番号検証正規表現 / 전화번호 검증 정규식
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[1-9]\\d{1,14}$"
    );
    
    /**
     * 默认日期格式 / Default date format / デフォルト日付フォーマット / 기본 날짜 형식
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 支持的日期格式数组 / Supported date formats array / サポートされている日付フォーマット配列 / 지원되는 날짜 형식 배열
     */
    private static final String[] SUPPORTED_DATE_FORMATS = {
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd",
        "dd-MM-yyyy HH:mm:ss",
        "dd-MM-yyyy",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy"
    };
    
    // ==================== 字符串处理工具 / String Processing Utilities ====================
    
    /**
     * 检查字符串是否为有效的邮箱地址。
     * Checks if a string is a valid email address.
     * 文字列が有効なメールアドレスかどうかをチェックします。
     * 문자열이 유효한 이메일 주소인지 확인합니다。
     * 
     * @param email 要验证的邮箱地址 / Email address to validate / 検証するメールアドレス / 검증할 이메일 주소
     * @return 如果是有效邮箱返回 true / Returns true if valid email / 
     *         有効なメールの場合はtrueを返します / 유효한 이메일이면 true를 반환합니다
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        
        boolean isValid = EMAIL_PATTERN.matcher(email.trim()).matches();
        
        log.debug("邮箱验证结果 / Email validation result / メール検証結果 / 이메일 검증 결과: {} -> {}", 
                 email, isValid);
        
        return isValid;
    }
    
    /**
     * 检查字符串是否为有效的电话号码。
     * Checks if a string is a valid phone number.
     * 文字列が有効な電話番号かどうかをチェックします。
     * 문자열이 유효한 전화번호인지 확인합니다。
     * 
     * @param phone 要验证的电话号码 / Phone number to validate / 検証する電話番号 / 검증할 전화번호
     * @return 如果是有效电话号码返回 true / Returns true if valid phone number / 
     *         有効な電話番号の場合はtrueを返します / 유효한 전화번호이면 true를 반환합니다
     */
    public static boolean isValidPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        
        String cleanPhone = StringUtils.deleteWhitespace(phone);
        boolean isValid = PHONE_PATTERN.matcher(cleanPhone).matches();
        
        log.debug("电话验证结果 / Phone validation result / 電話検証結果 / 전화 검증 결과: {} -> {}", 
                 phone, isValid);
        
        return isValid;
    }
    
    /**
     * 生成随机字符串。
     * Generates a random string.
     * ランダム文字列を生成します。
     * 임의의 문자열을 생성합니다。
     * 
     * @param length 字符串长度 / String length / 文字列の長さ / 문자열 길이
     * @param includeNumbers 是否包含数字 / Whether to include numbers / 数字を含むかどうか / 숫자 포함 여부
     * @param includeSpecialChars 是否包含特殊字符 / Whether to include special characters / 
     *                           特殊文字を含むかどうか / 특수 문자 포함 여부
     * @return 生成的随机字符串 / Generated random string / 生成されたランダム文字列 / 생성된 임의 문자열
     */
    public static String generateRandomString(int length, boolean includeNumbers, boolean includeSpecialChars) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0 / Length must be greater than 0 / " +
                                             "長さは0より大きくなければなりません / 길이는 0보다 커야 합니다");
        }
        
        StringBuilder chars = new StringBuilder("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        
        if (includeNumbers) {
            chars.append("0123456789");
        }
        
        if (includeSpecialChars) {
            chars.append("!@#$%^&*()_+-=[]{}|;:,.<>?");
        }
        
        Random random = new Random();
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        String generated = result.toString();
        log.debug("生成随机字符串 / Generated random string / ランダム文字列を生成 / 임의 문자열 생성: 长度 {} / length {} / 長さ {} / 길이 {}", 
                 length, length, length, length);
        
        return generated;
    }
    
    /**
     * 将字符串转换为驼峰命名法。
     * Converts string to camel case.
     * 文字列をキャメルケースに変換します。
     * 문자열을 카멜 케이스로 변환합니다。
     * 
     * @param input 输入字符串 / Input string / 入力文字列 / 입력 문자열
     * @param delimiter 分隔符 / Delimiter / 区切り文字 / 구분자
     * @return 驼峰命名法字符串 / Camel case string / キャメルケース文字列 / 카멜 케이스 문자열
     */
    public static String toCamelCase(String input, String delimiter) {
        if (StringUtils.isBlank(input)) {
            return StringUtils.EMPTY;
        }
        
        String[] words = StringUtils.split(input.toLowerCase(), delimiter);
        StringBuilder result = new StringBuilder(words[0]);
        
        for (int i = 1; i < words.length; i++) {
            result.append(StringUtils.capitalize(words[i]));
        }
        
        return result.toString();
    }
    
    // ==================== 文件操作工具 / File Operation Utilities ====================
    
    /**
     * 安全地创建目录。
     * Safely creates directories.
     * ディレクトリを安全に作成します。
     * 디렉토리를 안전하게 생성합니다。
     * 
     * @param dirPath 目录路径 / Directory path / ディレクトリパス / 디렉토리 경로
     * @return 如果创建成功返回 true / Returns true if creation successful / 
     *         作成が成功した場合はtrueを返します / 생성이 성공하면 true를 반환합니다
     */
    public static boolean createDirectorySafely(String dirPath) {
        if (StringUtils.isBlank(dirPath)) {
            log.warn("目录路径为空 / Directory path is empty / ディレクトリパスが空です / 디렉토리 경로가 비어있습니다");
            return false;
        }
        
        try {
            File dir = new File(dirPath);
            if (dir.exists()) {
                log.debug("目录已存在 / Directory already exists / ディレクトリは既に存在します / 디렉토리가 이미 존재합니다: {}", dirPath);
                return true;
            }
            
            boolean created = dir.mkdirs();
            if (created) {
                log.info("目录创建成功 / Directory created successfully / ディレクトリが正常に作成されました / 디렉토리가 성공적으로 생성되었습니다: {}", dirPath);
            } else {
                log.error("目录创建失败 / Directory creation failed / ディレクトリ作成に失敗しました / 디렉토리 생성에 실패했습니다: {}", dirPath);
            }
            
            return created;
        } catch (Exception e) {
            log.error("创建目录时发生异常 / Exception occurred while creating directory / " +
                     "ディレクトリ作成中に例外が発生しました / 디렉토리 생성 중 예외가 발생했습니다: {}", dirPath, e);
            return false;
        }
    }
    
    /**
     * 获取文件扩展名。
     * Gets file extension.
     * ファイル拡張子を取得します。
     * 파일 확장자를 가져옵니다。
     * 
     * @param filename 文件名 / Filename / ファイル名 / 파일명
     * @return 文件扩展名 / File extension / ファイル拡張子 / 파일 확장자
     */
    public static String getFileExtension(String filename) {
        if (StringUtils.isBlank(filename)) {
            return StringUtils.EMPTY;
        }
        
        return FilenameUtils.getExtension(filename);
    }
    
    /**
     * 计算文件大小的人类可读格式。
     * Calculates human-readable file size format.
     * 人間が読みやすいファイルサイズ形式を計算します。
     * 사람이 읽기 쉬운 파일 크기 형식을 계산합니다。
     * 
     * @param filePath 文件路径 / File path / ファイルパス / 파일 경로
     * @return 人类可读的文件大小 / Human-readable file size / 
     *         人間が読みやすいファイルサイズ / 사람이 읽기 쉬운 파일 크기
     */
    public static String getHumanReadableFileSize(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return "0 B";
        }
        
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return "文件不存在 / File not found / ファイルが見つかりません / 파일을 찾을 수 없습니다";
        }
        
        return FileUtils.byteCountToDisplaySize(file.length());
    }
    
    // ==================== 日期时间工具 / Date and Time Utilities ====================
    
    /**
     * 解析日期字符串，支持多种格式。
     * Parses date string supporting multiple formats.
     * 複数の形式をサポートする日付文字列を解析します。
     * 여러 형식을 지원하는 날짜 문자열을 구문 분석합니다。
     * 
     * @param dateString 日期字符串 / Date string / 日付文字列 / 날짜 문자열
     * @return 解析后的日期 / Parsed date / 解析された日付 / 구문 분석된 날짜
     * @throws ParseException 如果解析失败 / If parsing fails / 解析に失敗した場合 / 구문 분석에 실패하면
     */
    public static Date parseDate(String dateString) throws ParseException {
        if (StringUtils.isBlank(dateString)) {
            throw new IllegalArgumentException("日期字符串不能为空 / Date string cannot be empty / " +
                                             "日付文字列は空にできません / 날짜 문자열은 비어있을 수 없습니다");
        }
        
        try {
            return DateUtils.parseDate(dateString.trim(), SUPPORTED_DATE_FORMATS);
        } catch (ParseException e) {
            log.error("日期解析失败 / Date parsing failed / 日付解析に失敗しました / 날짜 구문 분석에 실패했습니다: {}", dateString, e);
            throw e;
        }
    }
    
    /**
     * 格式化当前时间。
     * Formats current time.
     * 現在時刻をフォーマットします。
     * 현재 시간을 형식화합니다。
     * 
     * @param pattern 格式模式 / Format pattern / フォーマットパターン / 형식 패턴
     * @return 格式化后的时间字符串 / Formatted time string / 
     *         フォーマットされた時間文字列 / 형식화된 시간 문자열
     */
    public static String formatCurrentTime(String pattern) {
        String formatPattern = StringUtils.isBlank(pattern) ? DEFAULT_DATE_FORMAT : pattern;
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return LocalDateTime.now().format(formatter);
        } catch (Exception e) {
            log.error("时间格式化失败 / Time formatting failed / 時間フォーマットに失敗しました / 시간 형식화에 실패했습니다: {}", pattern, e);
            return LocalDateTime.now().toString();
        }
    }
    
    // ==================== 集合处理工具 / Collection Processing Utilities ====================
    
    /**
     * 安全地获取列表中的元素。
     * Safely gets element from list.
     * リストから要素を安全に取得します。
     * 목록에서 요소를 안전하게 가져옵니다。
     * 
     * @param <T> 元素类型 / Element type / 要素タイプ / 요소 타입
     * @param list 列表 / List / リスト / 목록
     * @param index 索引 / Index / インデックス / 인덱스
     * @return 元素的 Optional 包装 / Optional wrapper of element / 
     *         要素のOptionalラッパー / 요소의 Optional 래퍼
     */
    public static <T> Optional<T> safeGet(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(list.get(index));
    }
    
    /**
     * 过滤并转换列表。
     * Filters and transforms list.
     * リストをフィルタリングして変換します。
     * 목록을 필터링하고 변환합니다。
     * 
     * @param <T> 输入类型 / Input type / 入力タイプ / 입력 타입
     * @param <R> 输出类型 / Output type / 出力タイプ / 출력 타입
     * @param list 输入列表 / Input list / 入力リスト / 입력 목록
     * @param filter 过滤条件 / Filter condition / フィルタ条件 / 필터 조건
     * @param mapper 转换函数 / Mapper function / マッパー関数 / 매퍼 함수
     * @return 转换后的列表 / Transformed list / 変換されたリスト / 변환된 목록
     */
    public static <T, R> List<R> filterAndMap(List<T> list, 
                                              java.util.function.Predicate<T> filter,
                                              java.util.function.Function<T, R> mapper) {
        if (list == null) {
            return Collections.emptyList();
        }
        
        return list.stream()
                .filter(filter != null ? filter : t -> true)
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查集合是否为空或 null。
     * Checks if collection is null or empty.
     * コレクションがnullまたは空かどうかをチェックします。
     * 컬렉션이 null이거나 비어있는지 확인합니다。
     * 
     * @param collection 要检查的集合 / Collection to check / チェックするコレクション / 확인할 컬렉션
     * @return 如果为空返回 true / Returns true if empty / 空の場合はtrueを返します / 비어있으면 true를 반환합니다
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * 检查集合是否不为空。
     * Checks if collection is not empty.
     * コレクションが空でないかどうかをチェックします。
     * 컬렉션이 비어있지 않은지 확인합니다。
     * 
     * @param collection 要检查的集合 / Collection to check / チェックするコレクション / 확인할 컬렉션
     * @return 如果不为空返回 true / Returns true if not empty / 
     *         空でない場合はtrueを返します / 비어있지 않으면 true를 반환합니다
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}