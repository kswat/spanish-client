package com.example;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class HolaController {

    private final Logger log = LoggerFactory.getLogger(HolaController.class);
    private final EurekaClient discoveryClient;
    private final WebClient.Builder loadBalancedWebClientBuilder;
//    private final ReactorLoadBalancerExchangeFilterFunction lbFunction;
    private final String englishAlias;

    public HolaController(EurekaClient discoveryClient, WebClient.Builder loadBalancedWebClientBuilder,
                          @Value("${services.client}") final String englishAlias) {
        this.discoveryClient = discoveryClient;
        this.loadBalancedWebClientBuilder = loadBalancedWebClientBuilder;
        this.englishAlias = englishAlias;
    }

//    public HolaController(EurekaClient discoveryClient, WebClient.Builder loadBalancedWebClientBuilder,
//                          ReactorLoadBalancerExchangeFilterFunction lbFunction,
//                          @Value("${services.client}") final String englishAlias) {
//        this.discoveryClient = discoveryClient;
//        this.loadBalancedWebClientBuilder = loadBalancedWebClientBuilder;
//        this.lbFunction = lbFunction;
//        this.englishAlias = englishAlias;
//    }

    @GetMapping("/home")
    public String home() {

        final Applications applications = discoveryClient.getApplications();
        for (Application registeredApplication : applications.getRegisteredApplications()) {
            System.out.println(registeredApplication.getName());

            List<InstanceInfo> instances = registeredApplication.getInstances();
            for (InstanceInfo instance : instances) {
                System.out.println(instance.getInstanceId());
                System.out.println(instance.getHostName());
                System.out.println(instance.getHomePageUrl());
                System.out.println(instance.getStatus());
                System.out.println(instance.getVIPAddress());
                System.out.println("_______________________________________________________");
            }
        }
        return "Spanish service!";
    }
    @GetMapping("/hola")
    public Mono<String> hola() {
        log.info(">>>>>>>>>>>>>in hola..."+ englishAlias);
        final InstanceInfo instance = discoveryClient.getNextServerFromEureka(englishAlias, false);

        return loadBalancedWebClientBuilder.build().get().uri(instance.getHomePageUrl()+"/home")
                .retrieve().bodyToMono(String.class)
                .map(m -> String.format("%s - My English peer says : %s!",home(), m));
    }
}
