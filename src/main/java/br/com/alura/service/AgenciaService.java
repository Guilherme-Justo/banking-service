package br.com.alura.service;

import br.com.alura.domain.Agencia;
import br.com.alura.enums.SituacaoCadastral;
import br.com.alura.exceptions.AgenciaInativaException;
import br.com.alura.exceptions.AgenciaJaExisteException;
import br.com.alura.exceptions.AgenciaNaoEncontradaException;
import br.com.alura.repository.AgenciaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class AgenciaService {

    @RestClient
    private BankingStatusServiceHttp bankingStatusServiceHttp;

    private final AgenciaRepository agenciaRepository;
    private final MeterRegistry meterRegistry;

    public AgenciaService(AgenciaRepository agenciaRepository, MeterRegistry meterRegistry) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry;
    }

    @WithSession
    public Uni<List<Agencia>> buscarTodos() {
        return agenciaRepository.findAll().list();
    }

    @WithTransaction
    public Uni<Agencia> cadastrar(Agencia agencia) {
        return agenciaRepository.findByCnpj(agencia.getCnpj()).onItem().ifNotNull().failWith(() -> {
            Log.error("Agência com CNPJ: " + agencia.getCnpj() + " já existe.");
            meterRegistry.counter("agencia_ja_existente_counter").increment();
            return new AgenciaJaExisteException();
        }).onItem().ifNull().switchTo(() -> {
            Log.info("Agência com CNPJ: " + agencia.getCnpj() + " cadastrada com sucesso.");
            meterRegistry.counter("agencia_cadastrada_counter").increment();
            return agenciaRepository.persist(agencia);
        });
    }

    @WithSession
    public Uni<Agencia> buscarPorId(Long id) {
        return agenciaRepository.findById(id).onItem().ifNull().failWith(() -> {
            Log.error("Agência com ID: " + id + " não encontrada.");
            meterRegistry.counter("agencia_nao_encontrada_counter").increment();
            return new AgenciaNaoEncontradaException();
        }).onItem().ifNotNull().transformToUni(agencia ->
            bankingStatusServiceHttp.getAgenciaStatusPorId(id).onItem().transformToUni(situacaoCadastral -> {
                if (SituacaoCadastral.INATIVO.equals(situacaoCadastral)) {
                    Log.error("Agência com ID: " + id + " encontrada, mas está inativa.");
                    meterRegistry.counter("agencia_inativa_counter").increment();
                    return Uni.createFrom().failure(new AgenciaInativaException());
                }

                Log.info("Agência com ID: " + id + " encontrada e ativa.");
                meterRegistry.counter("agencia_encontrada_counter").increment();

                return Uni.createFrom().item(agencia);
            })
        );
    }

    @WithSession
    public Uni<Agencia> buscarPorCnpj(String cnpj) {
        String formattedCnpj = cnpj.replaceAll(
                "(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})",
                "$1.$2.$3/$4-$5"
        );

        return agenciaRepository.findByCnpj(formattedCnpj).onItem().ifNull().failWith(() -> {
                    Log.error("Agência com CNPJ: " + formattedCnpj + " não encontrada.");
                    meterRegistry.counter("agencia_nao_encontrada_counter").increment();
                    return new AgenciaNaoEncontradaException();
                })
                .onItem().transformToUni(agencia ->
                        bankingStatusServiceHttp.getAgenciaStatusPorCnpj(formattedCnpj)
                                .onItem().transformToUni(situacaoCadastral -> {
                                    if (SituacaoCadastral.INATIVO.equals(situacaoCadastral)) {
                                        Log.error("Agência com CNPJ: " + formattedCnpj + " encontrada, mas está inativa.");
                                        meterRegistry.counter("agencia_inativa_counter").increment();
                                        return Uni.createFrom().failure(new AgenciaInativaException());
                                    }

                                    Log.info("Agência com CNPJ: " + formattedCnpj + " encontrada e ativa.");
                                    meterRegistry.counter("agencia_encontrada_counter").increment();

                                    return Uni.createFrom().item(agencia);
                                })
                );
    }

    @WithTransaction
    public Uni<Void> deletar(Long id) {
        return buscarPorId(id).onItem().transformToUni(a -> agenciaRepository.delete(a)).invoke(() -> {
            meterRegistry.counter("agencia_deletada_counter").increment();
            Log.info("Agência com ID: " + id + " deletada com sucesso.");
        });
    }

    @WithTransaction
    public Uni<Void> alterar(Agencia agencia) {
        return buscarPorCnpj(agencia.getCnpj()).onItem().transformToUni(a -> agenciaRepository.update("nome = ?1, razaoSocial = ?2, cnpj = ?3 where id = ?4", agencia.getNome(), agencia.getRazaoSocial(), agencia.getCnpj(), agencia.getId())).invoke(() -> {
            Log.info("Agência com CNPJ: " + agencia.getCnpj() + " atualizada com sucesso.");
            meterRegistry.counter("agencia_atualizada_counter").increment();
        }).replaceWithVoid();
    }

    @WithTransaction
    public Uni<Void> alterarSituacaoCadastral(String cnpj, SituacaoCadastral situacaoCadastral) {
        String formattedCnpj = cnpj.replaceAll(
                "(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})",
                "$1.$2.$3/$4-$5"
        );
        return agenciaRepository.findByCnpj(formattedCnpj)
                .onItem().ifNull().failWith(AgenciaNaoEncontradaException::new)
                .onItem().transformToUni(agencia ->
                        bankingStatusServiceHttp.alterarSituacaoCadastral(cnpj, situacaoCadastral)
                ).invoke(() -> {
                    Log.info("Situação cadastral da agência com CNPJ: " + formattedCnpj + " alterada para: " + situacaoCadastral);
                    meterRegistry.counter("agencia_situacao_cadastral_alterada_counter").increment();
                }).replaceWithVoid();
    }
}
