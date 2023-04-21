package site.lywq.nps;

import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 *
 * @author WED
 * @date 2023/04/11 16:02
 **/
public interface NpsService {

    Mono<ServerResponse> getNpsUser();

    Mono<ServerResponse> userRegister(NpsUser npsUser);

    Mono<ServerResponse> userLogin(NpsUser npsUser);

}
