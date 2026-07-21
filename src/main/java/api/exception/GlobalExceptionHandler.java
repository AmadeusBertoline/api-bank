package api.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex){

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", HttpStatus.NOT_FOUND.value());
        corpo.put("erro", "Recurso não encontrado");
        corpo.put("mensagem", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);

    } 

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleNotValid(MethodArgumentNotValidException ex){

        Map<String, String> errosPorCampo = new HashMap<>();

        ex.getBindingResult()
        .getAllErrors()
        .forEach(erro -> {
            String campo = ((FieldError) erro).getField();
            String mensagem = erro.getDefaultMessage();

            errosPorCampo.put(campo, mensagem);
        });

        Map<String, Object> erros = new HashMap<>();

        erros.put("timestamp", LocalDateTime.now());
        erros.put("status", HttpStatus.BAD_REQUEST.value());
        erros.put("erro", "Dados inválidos");
        erros.put("campos", errosPorCampo);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
        
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleJoker(Exception ex){

        Map<String, Object> corpo = new HashMap<>();

        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        corpo.put("erro", "Erro interno do servidor");
        corpo.put("mensagem", "Ocorreu um erro inesperado. Tente novamente mais tarde.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);

    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleRegraNegocio(RegraNegocioException ex){

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", HttpStatus.BAD_REQUEST.value());
        corpo.put("erro", "Uma regra de negócio foi ferida.");
        corpo.put("mensagem", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);

    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex){

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timeStamp", LocalDateTime.now());
        corpo.put("status", HttpStatus.FORBIDDEN.value());
        corpo.put("erro", "Acesso negado. Você não tem permissão para acessar esta rota.");
        corpo.put("mensagem", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corpo);

    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AuthenticationException ex){

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timeStamp", LocalDateTime.now());
        corpo.put("status", HttpStatus.UNAUTHORIZED.value());
        corpo.put("erro", "Não autorizado. É necessário estar autenticado para usar esta rota.");
        corpo.put("mensagem", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(corpo);

    }

}
