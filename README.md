# Sistema de Chamados - Trabalho Prático 2

> Disciplina: Programação para Dispositivos Móveis  
> Turma: Engenharia da Computação - 7º Período (2026.1)  
> Aluno: João Victor Borges Carvalho

---

## Sobre o Projeto

Evolução do Sistema de Chamados (TP1), transformado em uma aplicação corporativa completa com menu lateral (Drawer Navigation), armazenamento em nuvem via Back4App, captura de imagens pela câmera do dispositivo e geração de estatísticas.

## Funcionalidades

- **Menu Lateral (Drawer Navigation)** — Navegação via DrawerLayout + Toolbar + NavigationView
- **Cadastro de Chamados** — Registro de título, descrição, local, status (Aberto / Em Andamento / Concluído) e tipo
- **Captura de Imagem** — Fotografia do problema via câmera do dispositivo com preview
- **Armazenamento Local** — Persistência em SQLite (upgrade automático de schema)
- **Armazenamento em Nuvem** — Sincronização automática com Back4App via REST API
- **Listagem com RecyclerView** — Cards com título, local, status (com cores) e miniatura da foto
- **Filtros** — Filtragem por status e período
- **Detalhes do Chamado** — Visualização completa, atualização de status e registro de solução
- **Estatísticas** — Cards com total de chamados, abertos, em andamento e concluídos
- **Sobre o Sistema** — Informações do app, integrantes e descrição

## Tecnologias

- **Linguagem**: Java
- **Plataforma**: Android (minSDK 24, targetSDK 36)
- **UI**: Material Design 3, DrawerLayout, NavigationView, RecyclerView, CardView
- **Persistência**: SQLite (local) + Back4App Parse API (nuvem)
- **Câmera**: Camera Intent + FileProvider

## Estrutura do Projeto

```
app/src/main/java/com/example/trabalho2/
├── MainActivity.java          # DrawerLayout host + Toolbar
├── ChamadosFragment.java      # Fragment com RecyclerView da listagem
├── CadastroDemanda.java       # Cadastro com Spinner de Status + câmera
├── DetalhesChamado.java       # Detalhes + atualização de status/solução
├── EstatisticasActivity.java  # Tela de estatísticas
├── SobreActivity.java         # Tela Sobre o Sistema
├── Chamado.java               # Modelo de dados
├── ChamadoAdapter.java        # Adapter RecyclerView
├── BD.java                    # SQLite Helper (CRUD + estatísticas)
└── Back4AppHelper.java        # Sincronização com Back4App (REST)
```

## Como Executar

1. Clone o repositório
2. Abra no Android Studio
3. Sincronize o Gradle
4. Execute em um dispositivo/emulador com API 24+

## Configuração Back4App

O projeto já está configurado com as credenciais do Back4App. A classe `Chamado` é criada automaticamente no primeiro cadastro.

Campos armazenados na nuvem:
- `titulo` (String)
- `descricao` (String)
- `local` (String)
- `status` (String)
- `dataCadastro` (String)
- `imagemNome` (String)

---

*Trabalho desenvolvido para a disciplina de Programação para Dispositivos Móveis — 2026.1*
