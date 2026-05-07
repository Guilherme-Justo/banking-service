package br.com.alura.controller;

import br.com.alura.domain.Agencia;
import br.com.alura.service.AgenciaService;
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
    public RestResponse<List<Agencia>> buscarTodos() {
        return RestResponse.ok(this.agenciaService.buscarTodos());
    }

    @POST
    public RestResponse<Void> cadastrar(Agencia agencia, @Context UriInfo uriInfo) {
        this.agenciaService.cadastrar(agencia);
        return RestResponse.created(uriInfo.getAbsolutePath());
    }

    @GET()
    @Path("/id/{id}")
    public RestResponse<Agencia> buscarPorId(Long id) {
        Agencia agencia = this.agenciaService.buscarPorId(id);
        return RestResponse.ok(agencia);
    }

    @GET()
    @Path("/cnpj/{cnpj}")
    public RestResponse<Agencia> buscarPorCnpj(String cnpj) {
        Agencia agencia = this.agenciaService.buscarPorCnpj(cnpj);
        return RestResponse.ok(agencia);
    }

    @DELETE()
    @Path("/{id}")
    public RestResponse<Void> deletar(Long id) {
        this.agenciaService.deletar(id);
        return RestResponse.noContent();
    }

    @PUT
    public RestResponse<Void> alterar(Agencia agencia) {
        this.agenciaService.alterar(agencia);
        return RestResponse.noContent();
    }
}
