package br.com.alura.repository;

import br.com.alura.domain.Agencia;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgenciaRepository implements PanacheRepository<Agencia> {

    public Agencia findByCnpj(String cnpj) {
        return find("cnpj", cnpj).firstResult();
    }
}
