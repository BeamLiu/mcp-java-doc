package io.emop;

import lombok.Getter;

/**
 * 自定义业务异常类。
 * Custom business exception class.
 * カスタムビジネス例外クラス。
 * 사용자 정의 비즈니스 예외 클래스。
 * 
 * <p>该异常类用于处理业务逻辑中的特定错误情况，提供详细的错误信息和错误代码。
 * This exception class is used to handle specific error situations in business logic, 
 * providing detailed error information and error codes.
 * この例外クラスは、ビジネスロジックの特定のエラー状況を処理するために使用され、
 * 詳細なエラー情報とエラーコードを提供します。
 * 이 예외 클래스는 비즈니스 로직의 특정 오류 상황을 처리하는 데 사용되며,
 * 자세한 오류 정보와 오류 코드를 제공합니다。</p>
 * 
 * <h3>错误代码说明 / Error Code Description / エラーコード説明 / 오류 코드 설명:</h3>
 * <ul>
 *   <li><strong>1000-1999:</strong> 用户相关错误 / User-related errors / ユーザー関連エラー / 사용자 관련 오류</li>
 *   <li><strong>2000-2999:</strong> 数据验证错误 / Data validation errors / データ検証エラー / 데이터 검증 오류</li>
 *   <li><strong>3000-3999:</strong> 业务逻辑错误 / Business logic errors / ビジネスロジックエラー / 비즈니스 로직 오류</li>
 *   <li><strong>4000-4999:</strong> 系统错误 / System errors / システムエラー / 시스템 오류</li>
 *   <li><strong>5000-5999:</strong> 外部服务错误 / External service errors / 外部サービスエラー / 외부 서비스 오류</li>
 * </ul>
 * 
 * @author Exception Author / 例外作成者 / 예외 작성자
 * @version 1.0
 * @since 2024-01-01
 * @see RuntimeException
 * @see TestInterface
 */
@Getter
public class CustomException extends RuntimeException {
    
    /**
     * 序列化版本 UID / Serialization version UID / シリアライゼーションバージョンUID / 직렬화 버전 UID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码 / Error code / エラーコード / 오류 코드
     */
    private final int errorCode;
    
    /**
     * 错误类型 / Error type / エラータイプ / 오류 타입
     */
    private final ErrorType errorType;
    
    /**
     * 错误详细信息 / Error details / エラー詳細 / 오류 세부사항
     */
    private final String details;
    
    /**
     * 错误类型枚举。
     * Error type enumeration.
     * エラータイプ列挙型。
     * 오류 타입 열거형。
     */
    public enum ErrorType {
        /**
         * 用户错误 / User error / ユーザーエラー / 사용자 오류
         */
        USER_ERROR("用户错误 / User Error / ユーザーエラー / 사용자 오류"),
        
        /**
         * 验证错误 / Validation error / 検証エラー / 검증 오류
         */
        VALIDATION_ERROR("验证错误 / Validation Error / 検証エラー / 검증 오류"),
        
        /**
         * 业务错误 / Business error / ビジネスエラー / 비즈니스 오류
         */
        BUSINESS_ERROR("业务错误 / Business Error / ビジネスエラー / 비즈니스 오류"),
        
        /**
         * 系统错误 / System error / システムエラー / 시스템 오류
         */
        SYSTEM_ERROR("系统错误 / System Error / システムエラー / 시스템 오류"),
        
        /**
         * 外部服务错误 / External service error / 外部サービスエラー / 외부 서비스 오류
         */
        EXTERNAL_ERROR("外部服务错误 / External Service Error / 外部サービスエラー / 외부 서비스 오류");
        
        private final String description;
        
        ErrorType(String description) {
            this.description = description;
        }
        
        /**
         * 获取错误类型描述。
         * Gets error type description.
         * エラータイプの説明を取得します。
         * 오류 타입 설명을 가져옵니다。
         * 
         * @return 错误类型描述 / Error type description / エラータイプ説明 / 오류 타입 설명
         */
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 构造函数，使用错误消息。
     * Constructor with error message.
     * エラーメッセージを使用するコンストラクタ。
     * 오류 메시지를 사용하는 생성자。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     */
    public CustomException(String message) {
        super(message);
        this.errorCode = 9999;
        this.errorType = ErrorType.SYSTEM_ERROR;
        this.details = null;
    }
    
    /**
     * 构造函数，使用错误消息和错误代码。
     * Constructor with error message and error code.
     * エラーメッセージとエラーコードを使用するコンストラクタ。
     * 오류 메시지와 오류 코드를 사용하는 생성자。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     */
    public CustomException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = determineErrorType(errorCode);
        this.details = null;
    }
    
    /**
     * 构造函数，使用错误消息、错误代码和错误类型。
     * Constructor with error message, error code, and error type.
     * エラーメッセージ、エラーコード、エラータイプを使用するコンストラクタ。
     * 오류 메시지, 오류 코드, 오류 타입을 사용하는 생성자。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @param errorType 错误类型 / Error type / エラータイプ / 오류 타입
     */
    public CustomException(String message, int errorCode, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType != null ? errorType : determineErrorType(errorCode);
        this.details = null;
    }
    
    /**
     * 构造函数，使用错误消息、错误代码、错误类型和详细信息。
     * Constructor with error message, error code, error type, and details.
     * エラーメッセージ、エラーコード、エラータイプ、詳細を使用するコンストラクタ。
     * 오류 메시지, 오류 코드, 오류 타입, 세부사항을 사용하는 생성자。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @param errorType 错误类型 / Error type / エラータイプ / 오류 타입
     * @param details 错误详细信息 / Error details / エラー詳細 / 오류 세부사항
     */
    public CustomException(String message, int errorCode, ErrorType errorType, String details) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType != null ? errorType : determineErrorType(errorCode);
        this.details = details;
    }
    
    /**
     * 构造函数，使用错误消息和原因。
     * Constructor with error message and cause.
     * エラーメッセージと原因を使用するコンストラクタ。
     * 오류 메시지와 원인을 사용하는 생성자。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param cause 异常原因 / Exception cause / 例外の原因 / 예외 원인
     */
    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 9999;
        this.errorType = ErrorType.SYSTEM_ERROR;
        this.details = null;
    }
    
    /**
     * 构造函数，使用错误消息、错误代码和原因。
     * Constructor with error message, error code, and cause.
     * エラーメッセージ、エラーコード、原因を使用するコンストラクタ。
     * 오류 메시지, 오류 코드, 원인을 사용하는 생성자。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @param cause 异常原因 / Exception cause / 例外の原因 / 예외 원인
     */
    public CustomException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = determineErrorType(errorCode);
        this.details = null;
    }
    
    /**
     * 完整构造函数，包含所有参数。
     * Full constructor with all parameters.
     * すべてのパラメータを含む完全なコンストラクタ。
     * 모든 매개변수를 포함하는 전체 생성자。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @param errorType 错误类型 / Error type / エラータイプ / 오류 타입
     * @param details 错误详细信息 / Error details / エラー詳細 / 오류 세부사항
     * @param cause 异常原因 / Exception cause / 例外の原因 / 예외 원인
     */
    public CustomException(String message, int errorCode, ErrorType errorType, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType != null ? errorType : determineErrorType(errorCode);
        this.details = details;
    }
    
    /**
     * 根据错误代码确定错误类型。
     * Determines error type based on error code.
     * エラーコードに基づいてエラータイプを決定します。
     * 오류 코드를 기반으로 오류 타입을 결정합니다。
     * 
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @return 错误类型 / Error type / エラータイプ / 오류 타입
     */
    private static ErrorType determineErrorType(int errorCode) {
        if (errorCode >= 1000 && errorCode < 2000) {
            return ErrorType.USER_ERROR;
        } else if (errorCode >= 2000 && errorCode < 3000) {
            return ErrorType.VALIDATION_ERROR;
        } else if (errorCode >= 3000 && errorCode < 4000) {
            return ErrorType.BUSINESS_ERROR;
        } else if (errorCode >= 4000 && errorCode < 5000) {
            return ErrorType.SYSTEM_ERROR;
        } else if (errorCode >= 5000 && errorCode < 6000) {
            return ErrorType.EXTERNAL_ERROR;
        } else {
            return ErrorType.SYSTEM_ERROR;
        }
    }
    
    /**
     * 获取格式化的错误信息。
     * Gets formatted error information.
     * フォーマットされたエラー情報を取得します。
     * 형식화된 오류 정보를 가져옵니다。
     * 
     * @return 格式化的错误信息 / Formatted error information / 
     *         フォーマットされたエラー情報 / 형식화된 오류 정보
     */
    public String getFormattedErrorInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("错误代码 / Error Code / エラーコード / 오류 코드: ").append(errorCode).append("\n");
        sb.append("错误类型 / Error Type / エラータイプ / 오류 타입: ").append(errorType.getDescription()).append("\n");
        sb.append("错误消息 / Error Message / エラーメッセージ / 오류 메시지: ").append(getMessage()).append("\n");
        
        if (details != null && !details.trim().isEmpty()) {
            sb.append("错误详情 / Error Details / エラー詳細 / 오류 세부사항: ").append(details).append("\n");
        }
        
        if (getCause() != null) {
            sb.append("异常原因 / Exception Cause / 例外の原因 / 예외 원인: ").append(getCause().getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * 检查是否为用户错误。
     * Checks if it's a user error.
     * ユーザーエラーかどうかをチェックします。
     * 사용자 오류인지 확인합니다。
     * 
     * @return 如果是用户错误返回 true / Returns true if user error / 
     *         ユーザーエラーの場合はtrueを返します / 사용자 오류이면 true를 반환합니다
     */
    public boolean isUserError() {
        return errorType == ErrorType.USER_ERROR;
    }
    
    /**
     * 检查是否为系统错误。
     * Checks if it's a system error.
     * システムエラーかどうかをチェックします。
     * 시스템 오류인지 확인합니다。
     * 
     * @return 如果是系统错误返回 true / Returns true if system error / 
     *         システムエラーの場合はtrueを返します / 시스템 오류이면 true를 반환합니다
     */
    public boolean isSystemError() {
        return errorType == ErrorType.SYSTEM_ERROR;
    }
    
    /**
     * 检查是否为业务错误。
     * Checks if it's a business error.
     * ビジネスエラーかどうかをチェックします。
     * 비즈니스 오류인지 확인합니다。
     * 
     * @return 如果是业务错误返回 true / Returns true if business error / 
     *         ビジネスエラーの場合はtrueを返します / 비즈니스 오류이면 true를 반환합니다
     */
    public boolean isBusinessError() {
        return errorType == ErrorType.BUSINESS_ERROR;
    }
    
    /**
     * 创建用户错误异常。
     * Creates user error exception.
     * ユーザーエラー例外を作成します。
     * 사용자 오류 예외를 생성합니다。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @return 用户错误异常 / User error exception / ユーザーエラー例外 / 사용자 오류 예외
     */
    public static CustomException userError(String message, int errorCode) {
        return new CustomException(message, errorCode, ErrorType.USER_ERROR);
    }
    
    /**
     * 创建验证错误异常。
     * Creates validation error exception.
     * 検証エラー例外を作成します。
     * 검증 오류 예외를 생성합니다。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @return 验证错误异常 / Validation error exception / 検証エラー例外 / 검증 오류 예외
     */
    public static CustomException validationError(String message, int errorCode) {
        return new CustomException(message, errorCode, ErrorType.VALIDATION_ERROR);
    }
    
    /**
     * 创建业务错误异常。
     * Creates business error exception.
     * ビジネスエラー例外を作成します。
     * 비즈니스 오류 예외를 생성합니다。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @return 业务错误异常 / Business error exception / ビジネスエラー例外 / 비즈니스 오류 예외
     */
    public static CustomException businessError(String message, int errorCode) {
        return new CustomException(message, errorCode, ErrorType.BUSINESS_ERROR);
    }
    
    /**
     * 创建系统错误异常。
     * Creates system error exception.
     * システムエラー例外を作成します。
     * 시스템 오류 예외를 생성합니다。
     * 
     * @param message 错误消息 / Error message / エラーメッセージ / 오류 메시지
     * @param errorCode 错误代码 / Error code / エラーコード / 오류 코드
     * @param cause 异常原因 / Exception cause / 例外の原因 / 예외 원인
     * @return 系统错误异常 / System error exception / システムエラー例外 / 시스템 오류 예외
     */
    public static CustomException systemError(String message, int errorCode, Throwable cause) {
        return new CustomException(message, errorCode, ErrorType.SYSTEM_ERROR, null, cause);
    }
    
    @Override
    public String toString() {
        return String.format("CustomException{errorCode=%d, errorType=%s, message='%s', details='%s'}", 
                           errorCode, errorType, getMessage(), details);
    }
}