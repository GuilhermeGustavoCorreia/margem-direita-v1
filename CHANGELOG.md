# CHANGELOG — MargemDireitaModel1

Convenção: cada alteração recebe um código `ALT-NNN-descricao`, vira um commit com o mesmo nome no `main` e é registrada aqui.

---

## ALT-000 — baseline
- **Data:** 2026-10-10
- **O que:** versão inicial do modelo versionada no GitHub (`margem-direita-v1`), antes de qualquer alteração.

## ALT-001 — remover-solicitacoes
- **Data:** 2026-10-10
- **Status:** pendente (aguardando exclusão no AnyLogic)
- **O que:** remover os tipos de agente `solicitacao_alivio` e `solicitacao_faixas` (pasta `agentes-auxiliares/gestor-de-alivios`).
- **Análise de dependências:** nenhuma referência aos dois agentes, nem por nome nem por Id, nos outros 180 arquivos do modelo (Main, gestores, terminais, classes e `.alpx`). Remoção segura.
- **Arquivos removidos:**
  - `_alp/Agents/solicitacao_alivio/` (AOC, Variables)
  - `_alp/Agents/solicitacao_faixas/` (AOC, Variables, Levels)
- **Commit:** `ALT-001 remover agentes solicitacao_alivio e solicitacao_faixas`
