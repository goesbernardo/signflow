package com.signflow.infrastructure.gateway;

import com.signflow.application.port.out.ESignatureGateway;
import com.signflow.enums.ProviderSignature;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registro de gateways de assinatura disponíveis.
 * Permite selecionar dinamicamente a implementação correta baseada no provider.
 */
@Component
public class SignatureGatewayRegistry {
    private final Map<ProviderSignature, ESignatureGateway> byProvider;

    public SignatureGatewayRegistry(List<ESignatureGateway> gateways) {
        this.byProvider = gateways.stream()
                .collect(Collectors.toMap(ESignatureGateway::provider, g -> g));
    }

    public ESignatureGateway get(ProviderSignature p) {
        return Optional.ofNullable(byProvider.get(p))
                .orElseThrow(() -> new IllegalArgumentException("Nenhum gateway encontrado para o provider: " + p));
    }
}
