package br.com.alura.producers;

import br.com.alura.dto.NovaSituacaoDTO;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;

@ApplicationScoped
public class NovaSituacaoProducer {

    @Channel("nova-situacao-channel")
    MutinyEmitter<NovaSituacaoDTO> emitter;

    public Uni<Void> sendMessage(NovaSituacaoDTO novaSituacaoDTO) {
        return emitter.send(novaSituacaoDTO);
    }
}
