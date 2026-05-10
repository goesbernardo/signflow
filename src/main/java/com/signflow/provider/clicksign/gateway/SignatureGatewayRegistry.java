package com.signflow.adapter.clicksign.gateway;

import com.signflow.gateway.ESignatureGateway;
import com.signflow.enums.ProviderSignature;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SignatureGatewayRegistry {
    private final Map<ProviderSignature, ESignatureGateway> byProvider;

    public SignatureGatewayRegistry(List<ESignatureGateway> gateways) {
        this.byProvider = gateways.stream().collect(Collectors.toMap(ESignatureGateway::provider, g -> g));
    }

    public ESignatureGateway get(ProviderSignature p) {
        return Optional.ofNullable(byProvider.get(p)).orElseThrow(() -> new IllegalArgumentException("No gateway found for provider: " + p));
    }
}
