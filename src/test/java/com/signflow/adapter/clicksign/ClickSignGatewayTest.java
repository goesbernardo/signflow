package com.signflow.adapter.clicksign;

import com.signflow.adapter.clicksign.client.ClickSignIntegrationFeignClient;
import com.signflow.adapter.clicksign.dto.ClickSignRequestApiDTO;
import com.signflow.adapter.clicksign.mapper.ClickSignMapper;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.model.Signer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickSignGatewayTest {

    @Mock
    private ClickSignIntegrationFeignClient clickSignClient;

    @Mock
    private ClickSignMapper mapper;

    @InjectMocks
    private ClickSignGateway clickSignGateway;

    @Test
    void shouldPassPhoneNumberWhenAddingSigner() {
        // Given
        String envelopeId = "env-123";
        AddSignerCommand command = AddSignerCommand.builder()
                .name("Bernardo Goes")
                .email("bernardo.goes01@gmail.com")
                .phoneNumber("5511954381495")
                .delivery("email")
                .build();

        when(clickSignClient.createSigner(eq(envelopeId), any())).thenReturn(null);
        when(mapper.toSignerDomain(any())).thenReturn(Signer.builder().name("Bernardo Goes").build());

        // When
        clickSignGateway.addSigners(envelopeId, List.of(command));

        // Then
        ArgumentCaptor<ClickSignRequestApiDTO> captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
        verify(clickSignClient).createSigner(eq(envelopeId), captor.capture());

        ClickSignRequestApiDTO body = captor.getValue();
        // O corpo é um ClickSignRequestApiDTO que contém um ClickSignRequestApiDataDTO, que contém ClickSignCreateSignAttributesDTO
        // Como o ClickSignRequestApiDTO é genérico, precisamos navegar nele
        Object data = body.data();
        assertThat(data).isNotNull();
        
        // Através de reflexão ou conhecendo a estrutura:
        // data é ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO, Void>
        // Mas o captor capturou o tipo bruto.
        
        try {
            var attributesField = data.getClass().getDeclaredMethod("attributes");
            attributesField.setAccessible(true);
            Object attributes = attributesField.invoke(data);
            
            var phoneField = attributes.getClass().getDeclaredMethod("phoneNumber");
            phoneField.setAccessible(true);
            String phoneNumber = (String) phoneField.invoke(attributes);
            
            assertThat(phoneNumber).isEqualTo("5511954381495");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao validar telefone no payload", e);
        }
    }
}
