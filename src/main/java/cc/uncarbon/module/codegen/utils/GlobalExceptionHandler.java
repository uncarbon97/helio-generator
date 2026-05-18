package cc.uncarbon.module.codegen.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProjectBusinessException.class)
    public R handleBusinessException(ProjectBusinessException ex) {
        log.error(ex.getMessage(), ex);
        return R.error(ex.getCode(), ex.getMsg());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public R handleDuplicateKeyException(DuplicateKeyException ex) {
        log.error(ex.getMessage(), ex);
        return R.error("数据库中已存在该记录");
    }

    @ExceptionHandler(Exception.class)
    public R handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return R.error();
    }
}
