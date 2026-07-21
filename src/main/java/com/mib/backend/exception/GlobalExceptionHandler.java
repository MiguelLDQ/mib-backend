package com.mib.backend.exception;

import com.mib.backend.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.mib.backend.exception.AiServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleAiUnavailable(
            com.mib.backend.exception.AiServiceUnavailableException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req);
    }

    @ExceptionHandler(com.mib.backend.exception.AiRateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleAiRateLimit(
            com.mib.backend.exception.AiRateLimitExceededException ex, HttpServletRequest req) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    @ExceptionHandler(MessageBlockedByModerationException.class)
    public ResponseEntity<ApiErrorResponse> handleModerationBlocked(MessageBlockedByModerationException ex,
                                                                      HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler({ResourceNotFoundException.class, ChatRoomNotFoundException.class, AnonymousMessageNotFoundException.class,
            TaskNotFoundException.class, ShopItemNotFoundException.class, BreathingTechniqueNotFoundException.class,
            NotificationNotFoundException.class, MessageNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(MissionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMissionNotFound(MissionNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({BadRequestException.class, CannotFriendSelfException.class,
            NotFriendsException.class, InvalidFileException.class, MissionAlreadyCompletedException.class,
            CannotLikeOwnMessageException.class, InsufficientXpException.class, ItemAlreadyOwnedException.class,
            ItemNotOwnedException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler({EmailAlreadyInUseException.class, UsernameAlreadyInUseException.class,
            FriendshipAlreadyExistsException.class, DuplicateReportException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(FriendRequestNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFriendRequestNotFound(FriendRequestNotFoundException ex,
                                                                          HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenProfileAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenProfile(ForbiddenProfileAccessException ex,
                                                                     HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(ChatRoomAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleChatRoomAccessDenied(ChatRoomAccessDeniedException ex,
                                                                         HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class,
            BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciais invalidas", req);
    }

    @ExceptionHandler({AccountSuspendedException.class, LockedException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                               HttpServletRequest req) {
        List<ApiErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiErrorResponse.FieldErrorDetail(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ApiErrorResponse body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Validation Error",
                "Um ou mais campos sao invalidos", req.getRequestURI(), fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo excede o tamanho maximo permitido (2MB)", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde.", req);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
