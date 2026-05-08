package br.com.alura.domain;

import br.com.alura.enums.SituacaoCadastral;

public class AgenciaStatus {
    private SituacaoCadastral situacaoCadastral;

    public SituacaoCadastral getSituacaoCadastral() {
        return situacaoCadastral;
    }

    public void setSituacaoCadastral(SituacaoCadastral situacaoCadastral) {
        this.situacaoCadastral = situacaoCadastral;
    }
}
