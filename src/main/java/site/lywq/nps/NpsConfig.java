package site.lywq.nps;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author WED
 * @date 2023/04/11 18:11
 **/
@Data
@Accessors(chain = true)
public class NpsConfig {

    public static final String CONFIG_MAP_NAME = "plugin-nps-config";
    public static final String GROUP = "basic";

    private boolean enable = Boolean.FALSE;

    private String npsUserRegisterUrl;

    private String npsUserLoginUrl;
}
