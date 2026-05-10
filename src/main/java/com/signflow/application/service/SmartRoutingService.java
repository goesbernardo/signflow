package com.signflow.application.service;

import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.CreateFullEnvelopeCommand;
import com.signflow.enums.ProviderSignature;

/**
 * Serviço responsável por decidir qual provedor de assinatura utilizar
 * quando nenhum é especificado na requisição.
 */
public interface SmartRoutingService {
    ProviderSignature route(String userId, CreateEnvelopeCommand cmd);
    ProviderSignature route(String userId, CreateFullEnvelopeCommand cmd);
}
