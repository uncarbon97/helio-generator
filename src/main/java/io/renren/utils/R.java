package io.renren.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R {

    private int code;
    private String msg;
    private Map<String, Object> data;

    public static R error() {
        return error(500, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(500, msg);
    }

    public static R error(int code, String msg) {
        return new R(code, msg, null);
    }

    public static R ok(String msg) {
        return new R(0, msg, null);
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.setCode(0);
        r.setData(new HashMap<>(map));
        return r;
    }

    public static R ok() {
        return new R(0, null, null);
    }

    /**
     * 兼容链式 put 调用，如 R.ok().put("page", pageUtil)
     */
    public R put(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
        return this;
    }
}
