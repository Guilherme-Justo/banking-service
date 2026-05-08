package br.com.alura.service;

import br.com.alura.domain.Agencia;
import br.com.alura.domain.Endereco;
import br.com.alura.exceptions.AgenciaJaExisteException;
import br.com.alura.repository.AgenciaRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class AgenciaServiceTest {

    @InjectMock
    private AgenciaRepository agenciaRepository;

    @Inject
    AgenciaService agenciaService;

    private Agencia agencia() {
        Endereco endereco = new Endereco(1L, "Rua", "logradouro", "complemento", 123);
        return new Agencia(1L, "Agencia", "Razão Social", "CNPJ", endereco);
    }

    @Test
    public void naoDeveCadastrarQuandoJaExiste() {
        Agencia agencia = agencia();

        Mockito.when(agenciaRepository.findByCnpj(Mockito.anyString()))
                .thenReturn(Uni.createFrom().item(agencia));

        Assertions.assertThrows(
                AgenciaJaExisteException.class,
                () -> agenciaService.cadastrar(agencia).await().indefinitely()
        );

        Mockito.verify(agenciaRepository, Mockito.never()).persist(Mockito.any(Agencia.class));
    }

    @Test
    public void deveCadastrarQuandoNaoExiste() {
        Agencia agencia = agencia();

        Mockito.when(agenciaRepository.findByCnpj(Mockito.anyString()))
                .thenReturn(Uni.createFrom().nullItem());

        Mockito.when(agenciaRepository.persist(agencia))
                .thenReturn(Uni.createFrom().item(agencia));

        agenciaService.cadastrar(agencia).await().indefinitely();

        Mockito.verify(agenciaRepository).persist(agencia);
    }
}
