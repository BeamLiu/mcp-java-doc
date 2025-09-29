package io.emop;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 测试接口，用于验证接口文档的 JSON 生成。
 * Test interface for verifying JSON generation of interface documentation.
 * インターフェースドキュメントのJSON生成を検証するためのテストインターフェース。
 * 인터페이스 문서의 JSON 생성을 검증하기 위한 테스트 인터페이스。
 * 
 * <p>该接口演示了以下特性：
 * This interface demonstrates the following features:
 * このインターフェースは以下の機能を実演します：
 * 이 인터페이스는 다음 기능을 보여줍니다:</p>
 * 
 * <ul>
 *   <li>泛型方法 / Generic methods / ジェネリックメソッド / 제네릭 메서드</li>
 *   <li>默认方法 / Default methods / デフォルトメソッド / 기본 메서드</li>
 *   <li>静态方法 / Static methods / 静的メソッド / 정적 메서드</li>
 *   <li>异步处理 / Asynchronous processing / 非同期処理 / 비동기 처리</li>
 * </ul>
 * 
 * @param <T> 数据类型参数 / Data type parameter / データ型パラメータ / 데이터 타입 매개변수
 * @author Test Author / テスト作成者 / 테스트 작성자
 * @version 2.0
 * @since 2024-01-01
 */
public interface TestInterface<T> {
    
    /**
     * 常量字段示例。
     * Constant field example.
     * 定数フィールドの例。
     * 상수 필드 예제。
     */
    String CONSTANT_VALUE = "测试常量 / Test Constant / テスト定数 / 테스트 상수";
    
    /**
     * 最大重试次数常量。
     * Maximum retry count constant.
     * 最大リトライ回数定数。
     * 최대 재시도 횟수 상수。
     */
    int MAX_RETRY_COUNT = 3;
    
    /**
     * 默认超时时间（毫秒）。
     * Default timeout in milliseconds.
     * デフォルトタイムアウト（ミリ秒）。
     * 기본 타임아웃 (밀리초)。
     */
    long DEFAULT_TIMEOUT_MS = 5000L;
    
    /**
     * 处理数据的方法。
     * Method for processing data.
     * データを処理するメソッド。
     * 데이터를 처리하는 메서드。
     * 
     * @param data 要处理的数据 / Data to process / 処理するデータ / 처리할 데이터
     * @return 处理后的结果 / Processed result / 処理結果 / 처리된 결과
     * @throws Exception 如果处理过程中发生错误 / If an error occurs during processing / 
     *                   処理中にエラーが発生した場合 / 처리 중 오류가 발생하면
     */
    T processData(T data) throws Exception;
    
    /**
     * 批量处理数据的方法。
     * Method for batch processing data.
     * データをバッチ処理するメソッド。
     * 데이터를 일괄 처리하는 메서드。
     * 
     * @param dataList 要处理的数据列表 / List of data to process / 処理するデータリスト / 처리할 데이터 목록
     * @return 处理后的结果列表 / List of processed results / 処理結果リスト / 처리된 결과 목록
     * @throws Exception 如果批量处理失败 / If batch processing fails / 
     *                   バッチ処理が失敗した場合 / 일괄 처리가 실패하면
     */
    List<T> processBatch(List<T> dataList) throws Exception;
    
    /**
     * 异步处理数据的方法。
     * Method for asynchronous data processing.
     * 非同期データ処理のメソッド。
     * 비동기 데이터 처리 메서드。
     * 
     * @param data 要异步处理的数据 / Data to process asynchronously / 非同期処理するデータ / 비동기로 처리할 데이터
     * @return 包含处理结果的 CompletableFuture / CompletableFuture containing the result / 
     *         処理結果を含むCompletableFuture / 처리 결과를 포함하는 CompletableFuture
     */
    CompletableFuture<T> processAsync(T data);
    
    /**
     * 验证输入的方法。
     * Method for validating input.
     * 入力を検証するメソッド。
     * 입력을 검증하는 메서드。
     * 
     * @param input 要验证的输入 / Input to validate / 検証する入力 / 검증할 입력
     * @return 如果输入有效则返回 true / Returns true if input is valid / 
     *         入力が有効な場合はtrueを返します / 입력이 유효하면 true를 반환합니다
     */
    boolean validateInput(T input);
    
    /**
     * 查找数据的方法。
     * Method for finding data.
     * データを検索するメソッド。
     * 데이터를 찾는 메서드。
     * 
     * @param criteria 搜索条件 / Search criteria / 検索条件 / 검색 조건
     * @return 包含找到数据的 Optional / Optional containing found data / 
     *         見つかったデータを含むOptional / 찾은 데이터를 포함하는 Optional
     */
    Optional<T> findData(Map<String, Object> criteria);
    
    /**
     * 默认方法示例（Java 8+）。
     * Default method example (Java 8+).
     * デフォルトメソッドの例（Java 8+）。
     * 기본 메서드 예제 (Java 8+)。
     * 
     * @param message 要记录的消息 / Message to log / ログに記録するメッセージ / 로그에 기록할 메시지
     */
    default void logMessage(String message) {
        System.out.println("日志 / Log / ログ / 로그: " + message);
    }
    
    /**
     * 默认方法：获取处理统计信息。
     * Default method: Get processing statistics.
     * デフォルトメソッド：処理統計情報を取得。
     * 기본 메서드: 처리 통계 정보 가져오기。
     * 
     * @return 统计信息映射 / Statistics map / 統計情報マップ / 통계 정보 맵
     */
    default Map<String, Object> getProcessingStats() {
        return Map.of(
            "maxRetryCount", MAX_RETRY_COUNT,
            "defaultTimeout", DEFAULT_TIMEOUT_MS,
            "constantValue", CONSTANT_VALUE
        );
    }
    
    /**
     * 静态方法：创建默认配置。
     * Static method: Create default configuration.
     * 静的メソッド：デフォルト設定を作成。
     * 정적 메서드: 기본 구성 생성。
     * 
     * @return 默认配置映射 / Default configuration map / デフォルト設定マップ / 기본 구성 맵
     */
    static Map<String, Object> createDefaultConfig() {
        return Map.of(
            "timeout", DEFAULT_TIMEOUT_MS,
            "retryCount", MAX_RETRY_COUNT,
            "enableLogging", true,
            "description", "默认配置 / Default Config / デフォルト設定 / 기본 구성"
        );
    }
    
    /**
     * 静态方法：验证配置是否有效。
     * Static method: Validate if configuration is valid.
     * 静的メソッド：設定が有効かどうかを検証。
     * 정적 메서드: 구성이 유효한지 검증。
     * 
     * @param config 要验证的配置 / Configuration to validate / 検証する設定 / 검증할 구성
     * @return 如果配置有效返回 true / Returns true if config is valid / 
     *         設定が有効な場合はtrueを返します / 구성이 유효하면 true를 반환합니다
     */
    static boolean isValidConfig(Map<String, Object> config) {
        return config != null && 
               config.containsKey("timeout") && 
               config.containsKey("retryCount");
    }
}