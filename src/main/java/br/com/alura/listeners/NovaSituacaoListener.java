package br.com.alura.listeners;

import br.com.alura.dto.NovaSituacaoDTO;
import br.com.alura.producers.NovaSituacaoProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

@ApplicationScoped
public class NovaSituacaoListener {

    private NovaSituacaoProducer novaSituacaoProducer;

    public NovaSituacaoListener(NovaSituacaoProducer novaSituacaoProducer) {
        this.novaSituacaoProducer = novaSituacaoProducer;
    }

    public void eventHandler(@Observes(during = TransactionPhase.AFTER_SUCCESS) NovaSituacaoDTO novaSituacaoDTO) {
        novaSituacaoProducer.sendMessage(novaSituacaoDTO)
                .subscribe().with(
                        ignored -> {},
                        Throwable::printStackTrace
                );
    }
}
