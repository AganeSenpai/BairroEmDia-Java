package br.com.fiap.bairro.web;

import br.com.fiap.bairro.dto.Contratos.*;
import br.com.fiap.bairro.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias")
public class CategoriaController {
    private final CategoriaService categorias;
    private final UsuarioAtual usuario;

    public CategoriaController(CategoriaService categorias, UsuarioAtual usuario) {
        this.categorias = categorias;
        this.usuario = usuario;
    }

    @GetMapping
    @Operation(summary = "Listar categorias com paginação e ordenação")
    public PagedModel<EntityModel<CategoriaResposta>> listar(
        @ParameterObject @PageableDefault(sort = "nome") Pageable pagina,
        PagedResourcesAssembler<CategoriaResposta> assembler) {
        return assembler.toModel(categorias.listar(Paginacao.validar(pagina, Set.of("id", "nome"))), this::modelo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoria pelo identificador")
    public EntityModel<CategoriaResposta> buscar(@PathVariable Long id) {
        return modelo(categorias.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Criar categoria", description = "Exige papel GESTOR.")
    public ResponseEntity<EntityModel<CategoriaResposta>> criar(@Valid @RequestBody CategoriaEntrada entrada) {
        var resposta = categorias.criar(entrada);
        return ResponseEntity.created(linkTo(CategoriaController.class).slash(resposta.id()).toUri()).body(modelo(resposta));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Atualizar categoria", description = "Exige papel GESTOR.")
    public EntityModel<CategoriaResposta> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaEntrada entrada) {
        return modelo(categorias.atualizar(id, entrada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Excluir categoria sem solicitações vinculadas", description = "Exige papel GESTOR.")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categorias.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<CategoriaResposta> modelo(CategoriaResposta categoria) {
        var recurso = linkTo(CategoriaController.class).slash(categoria.id());
        String solicitacoes = linkTo(SolicitacaoController.class).toUriComponentsBuilder()
            .queryParam("categoriaId", categoria.id()).build().toUriString();
        var modelo = EntityModel.of(categoria, recurso.withSelfRel(),
            linkTo(CategoriaController.class).withRel("colecao"), Link.of(solicitacoes, "solicitacoes"));
        if (usuario.gestor()) {
            modelo.add(recurso.withRel("atualizar").withTitle("Atualizar via PUT"));
            modelo.add(recurso.withRel("excluir").withTitle("Excluir via DELETE"));
        }
        return modelo;
    }
}
