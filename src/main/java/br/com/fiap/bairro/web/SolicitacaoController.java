package br.com.fiap.bairro.web;

import br.com.fiap.bairro.domain.StatusSolicitacao;
import br.com.fiap.bairro.dto.Contratos.*;
import br.com.fiap.bairro.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.*;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/solicitacoes")
@Tag(name = "Solicitações")
public class SolicitacaoController {
    private final SolicitacaoService solicitacoes;
    private final UsuarioAtual usuario;

    public SolicitacaoController(SolicitacaoService solicitacoes, UsuarioAtual usuario) {
        this.solicitacoes = solicitacoes;
        this.usuario = usuario;
    }

    @GetMapping
    @Operation(summary = "Listar solicitações com filtros combináveis", description = "Cidadãos consultam seus registros. Gestores consultam todos. Ordenação por id, titulo, status, prioridade, criadaEm ou atualizadaEm.")
    public PagedModel<EntityModel<SolicitacaoResposta>> listar(@RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) StatusSolicitacao status, @RequestParam(required = false) String termo,
        @ParameterObject @PageableDefault(sort = "criadaEm", direction = Sort.Direction.DESC) Pageable pagina,
        PagedResourcesAssembler<SolicitacaoResposta> assembler) {
        var validada = Paginacao.validar(pagina, Set.of("id", "titulo", "status", "prioridade", "criadaEm", "atualizadaEm"));
        return assembler.toModel(solicitacoes.listar(categoriaId, status, termo, validada), this::modelo);
    }

    @GetMapping("/resumo")
    @Operation(summary = "Contar solicitações por status", description = "O resumo respeita o acesso do usuário autenticado.")
    public Resumo resumo() {
        return solicitacoes.resumo();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar solicitação")
    public EntityModel<SolicitacaoResposta> buscar(@PathVariable Long id) {
        return modelo(solicitacoes.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Registrar solicitação aberta com prioridade média")
    public ResponseEntity<EntityModel<SolicitacaoResposta>> criar(@Valid @RequestBody SolicitacaoEntrada entrada) {
        var resposta = solicitacoes.criar(entrada);
        return ResponseEntity.created(linkTo(SolicitacaoController.class).slash(resposta.id()).toUri()).body(modelo(resposta));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar dados de uma solicitação aberta")
    public EntityModel<SolicitacaoResposta> atualizar(@PathVariable Long id, @Valid @RequestBody SolicitacaoEntrada entrada) {
        return modelo(solicitacoes.atualizar(id, entrada));
    }

    @PatchMapping("/{id}/atendimento")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Atualizar status e prioridade", description = "Exige GESTOR. Fluxo ABERTA → EM_ATENDIMENTO → RESOLVIDA. ABERTA e EM_ATENDIMENTO também podem ser canceladas.")
    public EntityModel<SolicitacaoResposta> atender(@PathVariable Long id, @Valid @RequestBody AtendimentoEntrada entrada) {
        return modelo(solicitacoes.atender(id, entrada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir solicitação aberta")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        solicitacoes.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<SolicitacaoResposta> modelo(SolicitacaoResposta solicitacao) {
        var recurso = linkTo(SolicitacaoController.class).slash(solicitacao.id());
        var modelo = EntityModel.of(solicitacao, recurso.withSelfRel(),
            linkTo(SolicitacaoController.class).withRel("colecao"),
            linkTo(CategoriaController.class).slash(solicitacao.categoria().id()).withRel("categoria"));
        if (solicitacao.status() == StatusSolicitacao.ABERTA) {
            modelo.add(recurso.withRel("atualizar").withType("PUT"));
            modelo.add(recurso.withRel("excluir").withType("DELETE"));
        }
        if (usuario.gestor()) {
            modelo.add(recurso.slash("atendimento").withRel("atendimento").withType("PATCH"));
        }
        return modelo;
    }
}
