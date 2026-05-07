package br.com.alura.service;

import br.com.alura.domain.Agencia;
import br.com.alura.exceptions.AgenciaJaExisteException;
import br.com.alura.exceptions.AgenciaNaoAtivaOuNaoEncontradaException;
import br.com.alura.repository.AgenciaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
@ApplicationScoped
public class AgenciaService {

    private final AgenciaRepository agenciaRepository;
    private final MeterRegistry meterRegistry;

    public AgenciaService(AgenciaRepository agenciaRepository, MeterRegistry meterRegistry) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry;
    }

    public List<Agencia> buscarTodos() {
        return agenciaRepository.findAll().list();
    }

    public void cadastrar(Agencia agencia) {
        if (agenciaRepository.findByCnpj(agencia.getCnpj()) != null) {
            Log.info("Agência com CNPJ: " + agencia.getCnpj() + " não pode ser cadastrada.");
            meterRegistry.counter("agencia_nao_adicionada_counter").increment();
            throw new AgenciaJaExisteException();
        }
        Log.info("Agência com CNPJ: " + agencia.getCnpj() + " cadastrada com sucesso.");
        meterRegistry.counter("agencia_adicionada_counter").increment();
        agenciaRepository.persist(agencia);
    }

    public Agencia buscarPorId(Long id) {
        if (agenciaRepository.findById(id) == null) {
            throw new AgenciaNaoAtivaOuNaoEncontradaException();
        }
        return agenciaRepository.findById(id);
    }

    public Agencia buscarPorCnpj(String cnpj) {
        String formattedCnpj = cnpj.replaceAll(
                "(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})",
                "$1.$2.$3/$4-$5"
        );
        Agencia agencia = agenciaRepository.findByCnpj(formattedCnpj);
        if (agencia == null) {
            throw new AgenciaNaoAtivaOuNaoEncontradaException();
        }
        return agencia;
    }

    public void deletar(Long id) {
        if (agenciaRepository.findById(id) == null) {
            throw new AgenciaNaoAtivaOuNaoEncontradaException();
        }
        agenciaRepository.delete(buscarPorId(id));
    }

    public void alterar(Agencia agencia) {
        if (agenciaRepository.findById(agencia.getId()) == null) {
            throw new AgenciaNaoAtivaOuNaoEncontradaException();
        }
        agenciaRepository.update(
                "nome = ?1, razaoSocial = ?2, cnpj = ?3 where id = ?4",
                agencia.getNome(), agencia.getRazaoSocial(), agencia.getCnpj(), agencia.getId()
        );
    }
}
