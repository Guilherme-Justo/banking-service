package br.com.alura.service;

import br.com.alura.domain.Agencia;
import br.com.alura.domain.Endereco;
import br.com.alura.enums.SituacaoCadastral;
import br.com.alura.exceptions.AgenciaJaExisteException;
import br.com.alura.repository.AgenciaRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class AgenciaServiceTest {

    @InjectMock
    AgenciaRepository agenciaRepository;

    @InjectMock
    @RestClient
    BankingStatusServiceHttp bankingStatusServiceHttp;

    @Inject
    AgenciaService agenciaService;

    private Agencia agencia() {
        Endereco endereco = new Endereco(1L, "Rua", "logradouro", "complemento", 123);
        return new Agencia(1L, "Agencia", "Razão Social", "CNPJ", endereco);
    }

    @Test
    @RunOnVertxContext
    @DisplayName("Não deve cadastrar agência quando já existe")
    void naoDeveCadastrarQuandoJaExiste(UniAsserter asserter) {
        Agencia agencia = agencia();

        Mockito.when(agenciaRepository.findByCnpj(Mockito.anyString()))
                .thenReturn(Uni.createFrom().item(agencia));

        asserter.assertFailedWith(
                () -> agenciaService.cadastrar(agencia),
                AgenciaJaExisteException.class
        );

        asserter.execute(() -> {
            Mockito.verify(agenciaRepository, Mockito.never())
                    .persist(Mockito.any(Agencia.class));
        });
    }

    @Test
    @RunOnVertxContext
    @DisplayName("Deve cadastrar agência quando não existe")
    void deveCadastrarQuandoNaoExiste(UniAsserter asserter) {
        Agencia agencia = agencia();

        Mockito.when(agenciaRepository.findByCnpj(Mockito.anyString()))
                .thenReturn(Uni.createFrom().nullItem());

        Mockito.when(agenciaRepository.persist(agencia))
                .thenReturn(Uni.createFrom().item(agencia));

        asserter.assertEquals(
                () -> agenciaService.cadastrar(agencia),
                agencia
        );

        asserter.execute(() -> {
            Mockito.verify(agenciaRepository).persist(agencia);
        });
    }

    @Test
    @RunOnVertxContext
    @DisplayName("Deve retornar agência por CNPJ se estiver ativa")
    void deveRetornarAgenciaPorCnpjQuandoAtiva(UniAsserter asserter) {
        Agencia agencia = agencia();
        agencia.setCnpj("12.345.678/0001-99");

        String cnpjSemFormatacao = "12345678000199";
        String cnpjFormatado = "12.345.678/0001-99";

        Mockito.when(bankingStatusServiceHttp.getAgenciaStatusPorCnpj(cnpjFormatado)).thenReturn(Uni.createFrom().item(SituacaoCadastral.ATIVO));
        Mockito.when(agenciaRepository.findByCnpj(cnpjFormatado)).thenReturn(Uni.createFrom().item(agencia));

        asserter.assertEquals(
                () -> agenciaService.buscarPorCnpj(cnpjSemFormatacao),
                agencia
        );

        asserter.execute(() -> {
            Mockito.verify(agenciaRepository).findByCnpj(cnpjFormatado);
            Mockito.verify(bankingStatusServiceHttp).getAgenciaStatusPorCnpj(cnpjFormatado);
        });
    }
}
