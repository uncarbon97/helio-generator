package cc.uncarbon.module.codegen.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 自定义异常
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年10月27日 下午10:11:27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ProjectBusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private String msg;
    private int code = 500;

    public ProjectBusinessException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public ProjectBusinessException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public ProjectBusinessException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public ProjectBusinessException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }
}
