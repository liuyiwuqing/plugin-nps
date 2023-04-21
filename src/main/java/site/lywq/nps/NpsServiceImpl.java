package site.lywq.nps;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.time.Instant;
import java.util.Base64;

/**
 * @author WED
 * @date 2023/04/11 16:11
 **/
@Slf4j
@Component
public class NpsServiceImpl implements NpsService {

    /**
     * url类型
     */
    private enum UrlType {
        REGISTER, LOGIN
    }

    private final ReactiveSettingFetcher settingFetcher;
    private final ReactiveExtensionClient client;

    public NpsServiceImpl(ReactiveSettingFetcher settingFetcher, ReactiveExtensionClient client) {
        this.settingFetcher = settingFetcher;
        this.client = client;
    }


    /**
     * 创建nps用户
     *
     * @param npsUser
     * @return reactor.core.publisher.Mono<site.lywq.nps.NpsUser>
     * @author lywq
     * @date 2023/04/12 23:36
     **/
    private Mono<NpsUser> createNpsUser(NpsUser npsUser) {
        Metadata metadata = new Metadata();
        metadata.setName(npsUser.getUserName());
        metadata.setCreationTimestamp(Instant.now());
        npsUser.setMetadata(metadata);
        return client.create(npsUser);
    }


    /**
     * 注册用户
     *
     * @param npsUser
     * @return reactor.core.publisher.Mono<org.springframework.web.reactive.function.server.ServerResponse>
     * @author lywq
     * @date 2023/04/12 23:36
     **/
    @Override
    public Mono<ServerResponse> userRegister(NpsUser npsUser) {
        return buildUrl(UrlType.REGISTER, npsUser)
                .flatMap(url -> Mono.fromCallable(() -> HttpRequest.newBuilder()
                        .uri(URI.create(url)).POST(HttpRequest.BodyPublishers.noBody()).build()))
                .flatMap(request -> {
                    try {
                        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                        NpsResults npsResults = JsonUtils.jsonToObject(response.body(), NpsResults.class);
                        int status = npsResults.getStatus();
                        String msg = npsResults.getMsg();
                        if (1 == status && "register success".equals(msg)) {
                            Mono<NpsUser> npsUserMono = createNpsUser(npsUser);
                            return npsUserMono.flatMap(this::processSuccess);
                        } else {
                            return processFailure(null, msg);
                        }
                    } catch (Exception e) {
                        return processFailure(null, e.getMessage());
                    }
                }).onErrorResume(throwable -> processFailure(null, throwable.getMessage()));
    }


    /**
     * 获取nps用户
     *
     * @return reactor.core.publisher.Mono<org.springframework.web.reactive.function.server.ServerResponse>
     * @author lywq
     * @date 2023/04/12 23:37
     **/
    @Override
    public Mono<ServerResponse> getNpsUser() {
        return getContextUser()
                .flatMap(user -> {
                    Mono<NpsUser> npsUserMono = client.get(NpsUser.class, user.getMetadata().getName());
                    return npsUserMono.map(userInfo -> {
                        NpsUser npsUser = new NpsUser();
                        npsUser.setUserName(user.getMetadata().getName());
                        npsUser.setUserPassword(userInfo.getUserPassword());
                        return npsUser;
                    }).onErrorReturn(new NpsUser().setUserName(user.getMetadata().getName())).defaultIfEmpty(new NpsUser().setUserName(user.getMetadata().getName()));
                })
                .flatMap(npsUser -> {
                    if (ObjectUtils.isEmpty(npsUser)) {
                        return this.processFailure(npsUser, "用户不存在");
                    } else if (Strings.isNullOrEmpty(npsUser.getUserPassword())) {
                        return this.processFailure(npsUser, "暂未注册");
                    } else {
                        return this.processSuccess(npsUser);
                    }
                });
    }


    /**
     * 用户登录
     *
     * @param npsUser
     * @return reactor.core.publisher.Mono<org.springframework.web.reactive.function.server.ServerResponse>
     * @author lywq
     * @date 2023/04/12 23:37
     **/
    @Override
    public Mono<ServerResponse> userLogin(NpsUser npsUser) {
        return buildUrl(UrlType.LOGIN, npsUser)
                .flatMap(this::processSuccess);
    }


    /**
     * 获取Nps配置
     *
     * @return reactor.core.publisher.Mono<site.lywq.nps.NpsConfig>
     * @author lywq
     * @date 2023/04/13 00:58
     **/
    private Mono<NpsConfig> getNpsConfig() {
        return settingFetcher.fetch(NpsConfig.CONFIG_MAP_NAME, NpsConfig.GROUP, NpsConfig.class)
                .switchIfEmpty(Mono.just(new NpsConfig()));
    }


    /**
     * @param urlType
     * @param npsUser
     * @return java.net.URI
     * @author lywq
     * @date 2023/04/11 20:37
     **/
    private Mono<String> buildUrl(UrlType urlType, NpsUser npsUser) {
        return getNpsConfig()
                .map(npsConfig -> {
                    String url = "";
                    switch (urlType) {
                        case REGISTER ->
                                url = npsConfig.getNpsUserRegisterUrl() + "?username=" + npsUser.getUserName() + "&password=" + npsUser.getUserPassword();
                        case LOGIN -> {
                            String params = npsUser.getUserName() + "-" + npsUser.getUserPassword();
                            url = npsConfig.getNpsUserLoginUrl() + "?key=" + Base64.getEncoder().encodeToString(params.getBytes());
                        }
                    }
                    return url;
                });
    }

    /**
     * 处理结果
     *
     * @return reactor.core.publisher.Mono<org.springframework.web.reactive.function.server.ServerResponse>
     * @author lywq
     * @date 2023/04/13 00:58
     **/
    private Mono<ServerResponse> processSuccess(Object data) {
        return process(0, data, "success");
    }

    private Mono<ServerResponse> processFailure(Object data, String msg) {
        if (Strings.isNullOrEmpty(msg)) {
            msg = "failure";
        }
        return process(1, data, msg);
    }

    private Mono<ServerResponse> process(int code, Object data, String msg) {
        NpsResults npsResults = new NpsResults();
        npsResults.setStatus(code).setData(data).setMsg(msg);
        return ServerResponse.ok().bodyValue(npsResults);
    }

    protected Mono<User> getContextUser() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    var name = ctx.getAuthentication().getName();
                    return client.fetch(User.class, name);
                });
    }

    protected Mono<String> getContextUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName);
    }


    public String getCurrentUserName() {
        Authentication currentUser = getCurrentUserAuthentication();
        if (currentUser != null) {
            return currentUser.getName();
        }
        return null;
    }

    public Authentication getCurrentUserAuthentication() {
        Authentication currentUser = null;
        SecurityContext context = SecurityContextHolder.getContext();
        if (null != context) {
            currentUser = context.getAuthentication();
        }
        return currentUser;
    }

}
