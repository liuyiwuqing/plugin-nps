package site.lywq.nps;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author lywq
 * @date 2023/04/10 22:37
 **/
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "nps.lywq.site", version = "v1alpha1",
        kind = "NpsUser", plural = "npsUsers", singular = "npsUser")
public class NpsUser extends AbstractExtension {

    /**
     * 用户名
     */
    @Schema(required = true)
    private String userName;

    /**
     * 用户密码
     */
    @Schema(required = true)
    private String userPassword;

}
