package br.com.alura.service;

import br.com.alura.enums.SituacaoCadastral;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/situacao-agencia")
@RegisterRestClient(configKey = "banking-status")
public interface BankingStatusServiceHttp {

    @GET
    @Path("/cnpj/{cnpj}")
    Uni<SituacaoCadastral> getAgenciaStatusPorCnpj(@PathParam("cnpj") String cnpj);

    @GET
    @Path("/id/{id}")
    Uni<SituacaoCadastral> getAgenciaStatusPorId(@PathParam("id") Long id);

    @PUT
    @Path("/cnpj/{cnpj}/{situacao}")
    Uni<Void> alterarSituacaoCadastral(@PathParam("cnpj") String cnpj, @PathParam("situacao") SituacaoCadastral situacao);
}
