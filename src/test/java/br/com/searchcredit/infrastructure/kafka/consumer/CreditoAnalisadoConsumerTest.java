package br.com.searchcredit.infrastructure.kafka.consumer;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.entity.CreditoAnaliseAutomatica;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.infrastructure.kafka.event.CreditoAnalisadoEvent;
import br.com.searchcredit.infrastructure.repository.jpa.CreditoAnaliseAutomaticaJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditoAnalisadoConsumerTest {

    @Mock
    private CreditoRepository creditoRepository;

    @Mock
    private CreditoAnaliseAutomaticaJpaRepository creditoAnaliseAutomaticaJpaRepository;

    @InjectMocks
    private CreditoAnalisadoConsumer consumer;

    @Test
    void shouldWarnAndIgnoreWhenCreditoDoesNotExist() {
        CreditoAnalisadoEvent event = new CreditoAnalisadoEvent(
                1L,
                "900001",
                "1000001",
                StatusCredito.APROVADO,
                LocalDateTime.now(),
                "OK"
        );

        when(creditoAnaliseAutomaticaJpaRepository.existsByNumeroCredito("900001")).thenReturn(false);
        when(creditoRepository.findByNumeroCredito("900001")).thenReturn(Optional.empty());

        consumer.onMessage(event);

        verify(creditoRepository, times(1)).findByNumeroCredito("900001");
        verify(creditoRepository, never()).save(any());
        verify(creditoAnaliseAutomaticaJpaRepository, never()).save(any());
    }

    @Test
    void shouldIgnoreDuplicateEventWithoutSavingCreditoAgain() {
        CreditoAnalisadoEvent event = new CreditoAnalisadoEvent(
                1L,
                "900001",
                "1000001",
                StatusCredito.APROVADO,
                LocalDateTime.now(),
                "OK"
        );

        when(creditoAnaliseAutomaticaJpaRepository.existsByNumeroCredito("900001")).thenReturn(true);

        consumer.onMessage(event);

        verify(creditoAnaliseAutomaticaJpaRepository, times(1)).existsByNumeroCredito("900001");
        verify(creditoRepository, never()).findByNumeroCredito(any());
        verify(creditoRepository, never()).save(any());
        verify(creditoAnaliseAutomaticaJpaRepository, never()).save(any());
    }

    @Test
    void shouldIgnoreWhenCreditoAlreadyAnalyzedManually() {
        Credito credito = Credito.builder()
                .id(1L)
                .numeroCredito("900001")
                .status(StatusCredito.APROVADO)
                .build();

        CreditoAnalisadoEvent event = new CreditoAnalisadoEvent(
                1L,
                "900001",
                "1000001",
                StatusCredito.REPROVADO,
                LocalDateTime.now(),
                "OK"
        );

        when(creditoAnaliseAutomaticaJpaRepository.existsByNumeroCredito("900001")).thenReturn(false);
        when(creditoRepository.findByNumeroCredito("900001")).thenReturn(Optional.of(credito));

        consumer.onMessage(event);

        verify(creditoRepository, times(1)).findByNumeroCredito("900001");
        verify(creditoRepository, never()).save(any());
    }

    @Test
    void shouldUpdateWhenCreditoIsEmAnalise() {
        Credito credito = Credito.builder()
                .id(1L)
                .numeroCredito("900001")
                .status(StatusCredito.EM_ANALISE)
                .build();

        CreditoAnalisadoEvent event = new CreditoAnalisadoEvent(
                1L,
                "900001",
                "1000001",
                StatusCredito.APROVADO,
                LocalDateTime.now(),
                "OK"
        );

        when(creditoAnaliseAutomaticaJpaRepository.existsByNumeroCredito("900001")).thenReturn(false);
        when(creditoRepository.findByNumeroCredito("900001")).thenReturn(Optional.of(credito));

        consumer.onMessage(event);

        ArgumentCaptor<Credito> captor = ArgumentCaptor.forClass(Credito.class);
        verify(creditoRepository, times(1)).save(captor.capture());
        verify(creditoAnaliseAutomaticaJpaRepository, times(1)).save(any(CreditoAnaliseAutomatica.class));

        Credito saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(StatusCredito.APROVADO);
        assertThat(saved.getAprovadoPor()).isEqualTo("AUTO_WORKER");
        assertThat(saved.getComentarioAnalise()).isEqualTo("Análise automática simulada");
        assertThat(saved.getDataAnalise()).isNotNull();
    }

    @Test
    void shouldIgnoreDuplicateEvenIfCreditoIsNotEmAnalise() {
        CreditoAnalisadoEvent event = new CreditoAnalisadoEvent(
                1L,
                "900001",
                "1000001",
                StatusCredito.REPROVADO,
                LocalDateTime.now(),
                "OK"
        );

        when(creditoAnaliseAutomaticaJpaRepository.existsByNumeroCredito("900001")).thenReturn(true);

        consumer.onMessage(event);

        verify(creditoAnaliseAutomaticaJpaRepository, times(1)).existsByNumeroCredito("900001");
        verify(creditoRepository, never()).findByNumeroCredito(any());
        verify(creditoRepository, never()).save(any());
        verify(creditoAnaliseAutomaticaJpaRepository, never()).save(any());
    }
}
