package io.emop;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 这是一个测试类，用于验证 Java 文档 JSON 插件的功能。
 * This is a test class for verifying the functionality of the Java documentation JSON plugin.
 * これは、Java ドキュメント JSON プラグインの機能を検証するためのテストクラスです。
 * 이것은 Java 문서 JSON 플러그인의 기능을 검증하기 위한 테스트 클래스입니다。
 * 
 * <p>该类演示了以下功能：
 * This class demonstrates the following features:
 * このクラスは以下の機能を実演します：
 * 이 클래스는 다음 기능을 보여줍니다:</p>
 * 
 * <ul>
 *   <li>Lombok 注解的使用 / Lombok annotation usage / Lombok アノテーションの使用 / Lombok 어노테이션 사용</li>
 *   <li>Apache Commons 工具类 / Apache Commons utilities / Apache Commons ユーティリティ / Apache Commons 유틸리티</li>
 *   <li>多语言文档注释 / Multilingual documentation / 多言語ドキュメント / 다국어 문서</li>
 * </ul>
 * 
 * @author Test Author / テスト作成者 / 테스트 작성자
 * @version 2.0
 * @since 2024-01-01
 */
public class TestClass {
    
    /**
     * 用户名称字段。
     * User name field.
     * ユーザー名フィールド。
     * 사용자 이름 필드。
     */
    private String name;
    
    /**
     * 用户年龄字段。
     * User age field.
     * ユーザー年齢フィールド。
     * 사용자 나이 필드。
     */
    private int age;
    
    /**
     * 用户邮箱地址。
     * User email address.
     * ユーザーのメールアドレス。
     * 사용자 이메일 주소。
     */
    private String email;
    
    /**
     * 用户标签列表。
     * User tags list.
     * ユーザータグリスト。
     * 사용자 태그 목록。
     */
    private List<String> tags;
    
    /**
     * 用户属性映射。
     * User properties mapping.
     * ユーザープロパティマッピング。
     * 사용자 속성 매핑。
     */
    private Map<String, Object> properties;
    
    /**
     * 验证用户名是否有效。
     * Validates if the username is valid.
     * ユーザー名が有効かどうかを検証します。
     * 사용자 이름이 유효한지 검증합니다。
     * 
     * @return 如果用户名有效返回 true / Returns true if username is valid / 
     *         ユーザー名が有効な場合は true を返します / 사용자 이름이 유효하면 true를 반환합니다
     */
    public boolean isValidName() {
        return StringUtils.isNotBlank(this.name) && this.name.length() >= 2;
    }
    
    /**
     * 获取格式化的用户信息。
     * Gets formatted user information.
     * フォーマットされたユーザー情報を取得します。
     * 형식화된 사용자 정보를 가져옵니다。
     * 
     * @return 格式化的字符串 / Formatted string / フォーマットされた文字列 / 형식화된 문자열
     */
    public String getFormattedInfo() {
        if (StringUtils.isBlank(name)) {
            return "未知用户 / Unknown User / 不明なユーザー / 알 수 없는 사용자";
        }
        
        return String.format("用户: %s, 年龄: %d / User: %s, Age: %d / ユーザー: %s, 年齢: %d / 사용자: %s, 나이: %d", 
                           name, age, name, age, name, age, name, age);
    }
    
    /**
     * 计算两个数的和（静态工具方法）。
     * Calculates the sum of two numbers (static utility method).
     * 2つの数の合計を計算します（静的ユーティリティメソッド）。
     * 두 숫자의 합을 계산합니다 (정적 유틸리티 메서드)。
     * 
     * @param a 第一个数 / First number / 最初の数 / 첫 번째 숫자
     * @param b 第二个数 / Second number / 2番目の数 / 두 번째 숫자
     * @return 两个数的和 / Sum of the two numbers / 2つの数の合計 / 두 숫자의 합
     */
    public static int add(int a, int b) {
        return a + b;
    }
    
    /**
     * 检查字符串是否为回文。
     * Checks if a string is a palindrome.
     * 文字列が回文かどうかをチェックします。
     * 문자열이 회문인지 확인합니다。
     * 
     * @param text 要检查的文本 / Text to check / チェックするテキスト / 확인할 텍스트
     * @return 如果是回文返回 true / Returns true if palindrome / 
     *         回文の場合は true を返します / 회문이면 true를 반환합니다
     */
    public static boolean isPalindrome(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        
        String cleaned = StringUtils.deleteWhitespace(text.toLowerCase());
        return StringUtils.equals(cleaned, StringUtils.reverse(cleaned));
    }
}