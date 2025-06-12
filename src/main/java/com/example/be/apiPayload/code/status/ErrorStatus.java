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

    //로그인 관련 에러
    _NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER401", "해당 유저를 찾을 수 없습니다."),
    _EXIST_EMAIL(HttpStatus.BAD_REQUEST, "USER402", "중복된 이메일입니다."),
    _NOT_CORRECT_PASSWORD(HttpStatus.BAD_REQUEST, "USER405", "비밀번호가 틀립니다."),
    _NOT_FOUND_COOKIE(HttpStatus.NOT_FOUND, "USER403", "쿠키가 없습니다."),

    //게시글 관련 에러
    _NOT_FOUND_POST(HttpStatus.NOT_FOUND, "POST401", "해당 게시글을 찾을 수 없습니다."),
    _NOT_USER_POST(HttpStatus.NOT_FOUND, "POST402", "해당 유저의 게시글이 아닙니다."),

    //댓글 관련 에러
    _NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "COMMENT401", "해당 댓글을 찾을 수 없습니다."),
    _NOT_USER_COMMENT(HttpStatus.NOT_FOUND, "COMMENT402", "해당 유저의 댓글이 아닙니다."),

    //스터디룸 관련 에러
    _NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "ROOM401", "해당 방의 이름을 찾을 수 없습니다."),

    //디데이 관련 에러
    _NOT_USER_DDAY(HttpStatus.BAD_REQUEST, "DDAY401", "해당 유저의 디데이가 아닙니다."),
    _NOT_FOUND_DDAY(HttpStatus.NOT_FOUND, "DDAY402", "해당 디데이를 찾을 수 없습니다.");

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
