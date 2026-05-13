package br.com.alura.dto;

import br.com.alura.enums.SituacaoCadastral;

public record NovaSituacaoDTO(
        SituacaoCadastral situacaoCadastral,
        String cnpj
) {
}
