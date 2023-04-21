package site.lywq.nps;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 请求结果
 *
 * @author WED
 * @date 2023/04/21 11:31
 **/
@Data
@Accessors(chain = true)
public class NpsResults {

    private int status;

    private Object data;

    private String msg;
}
