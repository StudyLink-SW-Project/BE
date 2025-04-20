package com.example.be.apiPayload.code.status;



import com.example.be.apiPayload.code.BaseErrorCode;
import com.example.be.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // COMMON - 가장 일반적 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다"),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    //유저 관련 응답
    _NOT_EXIST_USER(HttpStatus.NOT_FOUND, "USER501", "존재하지 않는 유저 아이디입니다"),
    _SAME_EMAIL(HttpStatus.NOT_FOUND, "USER501", "이미 존재하는 이메일 입니다."),
    _NOT_EXIST_EMAIL(HttpStatus.NOT_FOUND, "USER502", "존재하지 않는 이메일 입니다."),
    _NOT_MATCH_PASSWORD(HttpStatus.NOT_FOUND, "USER503", "비밀번호가 틀렸습니다."),
    
    //JWT 관련 응답
    _INVALID_JWT(HttpStatus.UNAUTHORIZED, "JWT401", "유효하지 않은 JWT 토큰입니다."),
    _EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "JWT402", "액세스 토큰이 만료되었습니다."),
    _UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "JWT403", "지원되지 않는 JWT 토큰입니다."),
    _EMPTY_JWT(HttpStatus.UNAUTHORIZED, "JWT404", "JWT 토큰이 비어 있습니다."),
    _EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "JWT405", "리프레쉬토큰이 만료되었거나, 없습니다. 로그인을 진행해주세요."),
    _REFRESHED_ACCESS_TOKEN(HttpStatus.valueOf(419), "JWT406", "AccessToken이 재발급되었습니다. 요청을 다시 보내주세요."),

    //소셜로그인 관련 응답
    _INVALID_SOCIAL_LOGIN(HttpStatus.UNAUTHORIZED, "SOCIAL401", "지원하지 않는 소셜로그인 입니다."),

    //토큰 관련 응답
    _NOT_EXIST_TOKEN_TYPE(HttpStatus.NOT_FOUND, "TOKEN501", "존재하지 않는 토큰 타입입니다"),

    //채팅 관련 응답
    _NOT_EXIST_CHAT(HttpStatus.NOT_FOUND, "CHAT501", "존재하지 않는 채팅방 아이디입니다"),
    _NOT_MATCH_CHAT(HttpStatus.NOT_FOUND, "CHAT502", "사용할 수 없는 채팅방입니다."),
    _INVALID_AI_TYPE(HttpStatus.NOT_FOUND, "CHAT503", "유효하지 않은 AI 타입입니다."),

    //AI 관련 응답
    _NOT_EXIST_TOKEN(HttpStatus.NOT_FOUND, "AI501", "토큰을 보유하고 있지 않습니다"),
    _BARD_CONNECT_FAIL(HttpStatus.BAD_REQUEST, "AI502", "Bard 응답 요청 중 에러가 발생했습니다"),
    _BARD_RESPONSE_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "AI503", "Bard 응답값이 비어있습니다"),
    _GPT_CONNECT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "AI504", "GPT 응답 요청 중 에러가 발생했습니다"),
    _GPT_RESPONSE_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "AI504", "GPT 응답값이 비어있습니다"),
    _CLAUDE_CONNECT_FAIL(HttpStatus.BAD_REQUEST, "AI505", "CLAUDE 응답 요청 중 에러가 발생했습니다"),
    _CLAUDE_RESPONSE_NULL(HttpStatus.BAD_REQUEST, "AI505", "CLAUDE 응답값이 비어있습니다"),
    _DEEPSEEK_CONNECT_FAIL(HttpStatus.BAD_REQUEST, "AI505", "DeepSeek 응답 요청 중 에러가 발생했습니다"),
    _DEEPSEEK_RESPONSE_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "AI506", "DeepSeek 응답값이 비어있습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .code(code)
                .message(message)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
