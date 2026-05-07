package br.com.alura.service;

import br.com.alura.domain.Agencia;
import br.com.alura.domain.Endereco;
import br.com.alura.exceptions.AgenciaJaExisteException;
import br.com.alura.repository.AgenciaRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
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
        Mockito.when(agenciaRepository.findByCnpj(Mockito.anyString())).thenReturn(agencia);
        Assertions.assertThrows(AgenciaJaExisteException.class, () -> agenciaService.cadastrar(agencia));
        Mockito.verify(agenciaRepository, Mockito.never()).persist(agencia);
    }

    @Test
    public void deveCadastrarQuandoNaoExiste() {
        Agencia agencia = agencia();
        Mockito.when(agenciaRepository.findByCnpj(Mockito.anyString())).thenReturn(null);
        agenciaService.cadastrar(agencia);
        Mockito.verify(agenciaRepository).persist(agencia);
    }
}
