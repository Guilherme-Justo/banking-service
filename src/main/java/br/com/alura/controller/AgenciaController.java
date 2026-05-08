package br.com.alura.controller;

import br.com.alura.domain.Agencia;
import br.com.alura.enums.SituacaoCadastral;
import br.com.alura.service.AgenciaService;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/agencias")
public class AgenciaController {

    private AgenciaService agenciaService;

    public AgenciaController(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @GET
    public Uni<RestResponse<List<Agencia>>> buscarTodos() {
        return this.agenciaService.buscarTodos().onItem().transform(RestResponse::ok);
    }

    @POST
    @NonBlocking
    public Uni<RestResponse<Void>> cadastrar(Agencia agencia, @Context UriInfo uriInfo) {
        return this.agenciaService.cadastrar(agencia).replaceWith(RestResponse.created(uriInfo.getAbsolutePath()));
    }

    @GET()
    @Path("/id/{id}")
    public Uni<RestResponse<Agencia>> buscarPorId(@PathParam("id") Long id) {
        return this.agenciaService.buscarPorId(id).onItem().transform(RestResponse::ok);
    }

    @GET()
    @Path("/cnpj/{cnpj}")
    public Uni<RestResponse<Agencia>> buscarPorCnpj(@PathParam("cnpj") String cnpj) {
        return this.agenciaService.buscarPorCnpj(cnpj).onItem().transform(RestResponse::ok);
    }

    @DELETE()
    @Path("/{id}")
    public Uni<RestResponse<Void>> deletar(@PathParam("id") Long id) {
        return this.agenciaService.deletar(id).replaceWith(RestResponse::noContent);
    }

    @PUT
    public Uni<RestResponse<Void>> alterar(Agencia agencia) {
        return this.agenciaService.alterar(agencia).replaceWith(RestResponse::noContent);
    }

    @PUT
    @Path("/{cnpj}/{situacao}")
    public Uni<RestResponse<Void>> alterarSituacaoCadastral(
            @PathParam("cnpj") String cnpj,
            @PathParam("situacao") SituacaoCadastral situacao
    ) {
        return this.agenciaService.alterarSituacaoCadastral(cnpj, situacao).replaceWith(RestResponse::noContent);
    }
}
