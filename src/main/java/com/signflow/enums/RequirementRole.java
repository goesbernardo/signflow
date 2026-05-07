package com.signflow.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequirementRole {

    // ── Contrato ──────────────────────────────────────────────────────────────

    /**
     * Signatário — papel padrão de assinatura.
     *
     * Pessoa que assina o documento como parte principal da relação contratual.
     * É o papel mais utilizado na grande maioria dos contratos.
     *
     * Exemplos de uso:
     * - Funcionário assinando contrato de trabalho
     * - Cliente assinando contrato de prestação de serviços
     * - Locatário assinando contrato de aluguel
     */
    SIGN("sign"),

    /**
     * Interveniente — parte que participa sem ser contratante nem contratado.
     *
     * Pessoa que adere ao contrato para anuir com seus termos, mas não é parte
     * principal da relação. Sua assinatura é necessária para dar validade a
     * determinadas cláusulas.
     *
     * Exemplos de uso:
     * - Cônjuge que anui na alienação de bem do casal
     * - Empresa controladora que intervém em contrato da subsidiária
     * - Arrendador que anui em sublocação
     */
    INTERVENING("intervening"),

    /**
     * Contratante — quem contrata o serviço ou produto.
     *
     * Parte que solicita e remunera a prestação de serviço ou aquisição de produto.
     * No contrato de prestação de serviços, é o cliente que encomenda o trabalho.
     *
     * Exemplos de uso:
     * - Empresa que contrata desenvolvimento de software
     * - Pessoa física que contrata reforma de imóvel
     * - Órgão público que contrata fornecimento de bens
     */
    CONTRACTEE("contractee"),

    /**
     * Contratado — quem presta o serviço ou fornece o produto.
     *
     * Parte que executa a prestação de serviço ou fornece o produto contratado.
     * Recebe a remuneração pelo trabalho ou bem entregue.
     *
     * Exemplos de uso:
     * - Freelancer contratado para desenvolvimento de sistema
     * - Empresa de construção contratada para reforma
     * - Fornecedor contratado para entrega de insumos
     */
    CONTRACTOR("contractor"),

    /**
     * Receptor — pessoa que confirma o recebimento de bem, serviço ou documento.
     *
     * Assina como confirmação de que recebeu algo. Não é necessariamente parte
     * contratual — pode ser apenas o destinatário de uma entrega.
     *
     * Exemplos de uso:
     * - Recibo de entrega de mercadoria
     * - Confirmação de recebimento de documento oficial
     * - Termo de entrega de equipamento
     */
    RECEIPT("receipt"),

    // ── Garantia ──────────────────────────────────────────────────────────────

    /**
     * Fiador — pessoa que garante o cumprimento da obrigação pelo devedor principal.
     *
     * Assume responsabilidade subsidiária pelo pagamento caso o devedor principal
     * não cumpra. Em geral, só pode ser cobrado após esgotados os bens do devedor.
     *
     * Exemplos de uso:
     * - Fiador em contrato de aluguel residencial
     * - Fiador em contrato de financiamento estudantil
     * - Fiador em contrato de locação comercial
     */
    GUARANTOR("guarantor"),

    /**
     * Devedor solidário — assume a mesma responsabilidade do devedor principal.
     *
     * Diferente do fiador, o devedor solidário pode ser cobrado integralmente
     * sem necessidade de exaurir os bens do devedor principal primeiro.
     * Responde de forma direta e integral pela dívida.
     *
     * Exemplos de uso:
     * - Sócio que assina como devedor solidário em financiamento da empresa
     * - Cônjuge que assume solidariedade em empréstimo
     * - Co-signatário de nota promissória
     */
    JOINT_DEBTOR("joint_debtor"),

    /**
     * Corresponsável — compartilha responsabilidade pela obrigação.
     *
     * Pessoa que divide a responsabilidade sem necessariamente ser devedor
     * solidário formal. A corresponsabilidade pode ser limitada a partes
     * específicas da obrigação.
     *
     * Exemplos de uso:
     * - Corresponsável em contrato de parcelamento
     * - Co-responsável em obra com múltiplos contratantes
     * - Responsável solidário em consórcio
     */
    CO_RESPONSIBLE("co_responsible"),

    /**
     * Confortador — presta suporte adicional à obrigação (comfort letter).
     *
     * Emite carta ou declaração de conforto comprometendo-se a apoiar o devedor
     * principal no cumprimento da obrigação. Não é garantia formal — é compromisso
     * moral ou de melhor esforço, comum em operações financeiras internacionais.
     *
     * Exemplos de uso:
     * - Controladora que emite comfort letter para financiamento da subsidiária
     * - Investidor que presta declaração de suporte a empresa do portfólio
     */
    COMFORTER("comforter"),

    // ── Financeiro ────────────────────────────────────────────────────────────

    /**
     * Endossante — transfere título de crédito para outra pessoa.
     *
     * Quem cede os direitos de um título de crédito (cheque, nota promissória,
     * duplicata, letra de câmbio) por meio do endosso. Ao endossar, pode continuar
     * responsável caso o endossatário não pague.
     *
     * Exemplos de uso:
     * - Empresa que endossa duplicata para factoring
     * - Pessoa que endossa cheque para terceiro
     * - Cedente em operação de desconto de recebíveis
     */
    ENDORSER("endorser"),

    /**
     * Endossatário — recebe o título de crédito endossado.
     *
     * Beneficiário da transferência feita pelo endossante. Passa a ser o
     * novo credor do título e pode cobrá-lo no vencimento.
     *
     * Exemplos de uso:
     * - Factoring que recebe duplicata endossada
     * - Banco que recebe cheque endossado
     * - Cessionário em operação de desconto de recebíveis
     */
    ENDORSEE("endorsee"),

    /**
     * Emitente — quem cria e assina o título de crédito ou documento financeiro.
     *
     * Pessoa ou empresa que emite o instrumento financeiro, assumindo a obrigação
     * principal nele contida.
     *
     * Exemplos de uso:
     * - Empresa que emite nota promissória como promessa de pagamento
     * - Emissor de debêntures para captação de recursos
     * - Sacador em letra de câmbio
     */
    ISSUER("issuer"),

    /**
     * Outorgante — quem concede um direito, garantia ou poder a outra parte.
     *
     * Parte que transfere poderes, direitos reais ou garantias para o outorgado.
     * O outorgante mantém a titularidade, mas concede o uso ou poder ao outro.
     *
     * Exemplos de uso:
     * - Proprietário que outorga procuração para venda de imóvel
     * - Devedor que constitui garantia real (hipoteca, alienação fiduciária) em favor do credor
     * - Cedente de direitos reais de uso
     */
    GRANTOR("grantor"),

    /**
     * Segurado — pessoa coberta pela apólice de seguro.
     *
     * Beneficiário da cobertura contratada. Assina a apólice confirmando
     * as condições do seguro e suas obrigações como segurado.
     *
     * Exemplos de uso:
     * - Segurado em apólice de seguro de vida
     * - Beneficiário em seguro saúde empresarial
     * - Proprietário em seguro de imóvel ou veículo
     */
    INSURED("insured"),

    // ── Comercial ─────────────────────────────────────────────────────────────

    /**
     * Comprador — parte que adquire bem em contrato de compra e venda.
     *
     * Quem paga e recebe a propriedade do bem. Em contratos imobiliários,
     * deve estar presente junto com o vendedor.
     *
     * Exemplos de uso:
     * - Comprador de imóvel em compromisso de compra e venda
     * - Adquirente de veículo em contrato de compra
     * - Empresa compradora em aquisição de equipamentos
     */
    BUYER("buyer"),

    /**
     * Vendedor — parte que aliena bem em contrato de compra e venda.
     *
     * Quem transfere a propriedade do bem ao comprador mediante pagamento.
     *
     * Exemplos de uso:
     * - Vendedor de imóvel em compromisso de compra e venda
     * - Proprietário que vende veículo
     * - Empresa que vende ativo imobilizado
     */
    SELLER("seller"),

    /**
     * Cedente — quem cede um direito, crédito ou posição contratual.
     *
     * Transfere para o cessionário a titularidade de um direito ou a posição
     * em um contrato. Diferente de vendedor — aqui se cedem direitos, não bens.
     *
     * Exemplos de uso:
     * - Credor que cede crédito para empresa de cobrança
     * - Locatário que cede posição no contrato de locação
     * - Empresa que cede contrato de prestação de serviços em aquisição
     */
    TRANSFEROR("transferor"),

    /**
     * Cessionário — quem recebe a cessão de direito, crédito ou posição contratual.
     *
     * Beneficiário da transferência feita pelo cedente. Assume os direitos
     * e, eventualmente, as obrigações cedidas.
     *
     * Exemplos de uso:
     * - Empresa de cobrança que recebe crédito cedido
     * - Novo locatário que assume contrato de locação
     * - Adquirente que assume contratos em fusão ou aquisição
     */
    TRANSFEREE("transferee"),

    /**
     * Franqueador — empresa que concede o direito de uso da marca e modelo de negócio.
     *
     * Titular da marca e do sistema de franquia. Licencia ao franqueado o direito
     * de operar sob sua marca em troca de royalties e cumprimento dos padrões.
     *
     * Exemplos de uso:
     * - Rede de fast-food que concede franquia
     * - Empresa de serviços que expande via franquias
     * - Marca de varejo em contrato de franquia regional
     */
    FRANCHISOR("franchisor"),

    /**
     * Franqueado — pessoa ou empresa que opera sob a marca do franqueador.
     *
     * Adquire o direito de usar o modelo de negócio e a marca do franqueador.
     * Paga taxa inicial e royalties, e deve seguir os padrões da rede.
     *
     * Exemplos de uso:
     * - Empreendedor que abre unidade de franquia
     * - Empresa que adquire master-franquia para região
     */
    FRANCHISEE("franchisee"),

    // ── Jurídico ──────────────────────────────────────────────────────────────

    /**
     * Procurador — pessoa que age em nome de outra mediante procuração.
     *
     * Tem poderes delegados pelo outorgante via instrumento de procuração.
     * Age juridicamente em nome de quem o constituiu, dentro dos limites
     * dos poderes outorgados.
     *
     * Exemplos de uso:
     * - Advogado com procuração para assinar contrato pelo cliente
     * - Representante que assina documentos em nome de terceiro ausente
     * - Procurador com poderes específicos para transação imobiliária
     */
    ATTORNEY("attorney"),

    /**
     * Representante legal — pessoa com poder legal de representar outra.
     *
     * Diferente do procurador, o representante legal tem seu poder derivado
     * da lei — não de procuração. Age em nome do representado por determinação
     * legal, estatutária ou judicial.
     *
     * Exemplos de uso:
     * - Sócio-administrador que representa a empresa por força do contrato social
     * - Pai ou mãe que assina por filho menor de idade
     * - Tutor ou curador que age por incapaz
     */
    LEGAL_REPRESENTATIVE("legal_representative"),

    /**
     * Administrador — gestor com poderes de administração sobre bens ou empresa.
     *
     * Pessoa investida de poderes de gestão, geralmente por ato societário,
     * judicial ou legal. Atua em nível estratégico.
     *
     * Exemplos de uso:
     * - Administrador judicial em processo de recuperação judicial
     * - Administrador de condomínio em contratos do prédio
     * - Gestor de fundo de investimento em contratos do fundo
     */
    ADMINISTRATOR("administrator"),

    /**
     * Gerente — gestor operacional com poderes limitados de representação.
     *
     * Atua no nível gerencial com poderes específicos e limitados.
     * Diferente do administrador — não tem poderes estratégicos plenos.
     *
     * Exemplos de uso:
     * - Gerente de filial que assina contratos locais dentro de sua alçada
     * - Gerente de projeto que assina termos de aceite
     * - Responsável operacional com poderes delegados pelo administrador
     */
    MANAGER("manager");

    @JsonValue
    private final String value;

    @JsonCreator
    public static RequirementRole fromValue(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        for (RequirementRole role : values()) {
            if (role.value.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("RequirementRole inválido: " + value);
    }
}
