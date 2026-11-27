INSERT INTO clientes (
    uuid, nome, cpf, email, telefone,
    endereco_logradouro, endereco_numero, endereco_complemento, endereco_bairro,
    endereco_cidade, endereco_uf, endereco_cep,
    is_active, created_at, created_by
)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Cliente Exemplo',
    '52998224725',
    'cliente.exemplo@mekano.com.br',
    '11999999999',
    'Rua Exemplo',
    '100',
    NULL,
    'Centro',
    'Sao Paulo',
    'SP',
    '01001000',
    TRUE,
    CURRENT_TIMESTAMP,
    'SEED'
)
ON CONFLICT DO NOTHING;

INSERT INTO veiculos (
    uuid, cliente_uuid, placa, marca, modelo, ano,
    is_active, created_at, created_by
)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'ABC1D23',
    'Volkswagen',
    'Gol',
    2020,
    TRUE,
    CURRENT_TIMESTAMP,
    'SEED'
)
ON CONFLICT DO NOTHING;

INSERT INTO servicos (
    uuid, nome, descricao, valor_unitario,
    is_active, created_at, created_by
)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    'Troca de Óleo',
    'Troca de óleo e filtro',
    150.00,
    TRUE,
    CURRENT_TIMESTAMP,
    'SEED'
)
ON CONFLICT DO NOTHING;

INSERT INTO servicos (
    uuid, nome, descricao, valor_unitario,
    is_active, created_at, created_by
)
VALUES (
    '44444444-4444-4444-4444-444444444444',
    'Alinhamento e Balanceamento',
    'Alinhamento e balanceamento das rodas',
    200.00,
    TRUE,
    CURRENT_TIMESTAMP,
    'SEED'
)
ON CONFLICT DO NOTHING;

INSERT INTO pecas (
    uuid, codigo, descricao, valor_unitario,
    is_active, created_at, created_by
)
VALUES (
    '55555555-5555-5555-5555-555555555555',
    'PEA-001',
    'Filtro de Óleo',
    45.90,
    TRUE,
    CURRENT_TIMESTAMP,
    'SEED'
)
ON CONFLICT DO NOTHING;

INSERT INTO pecas (
    uuid, codigo, descricao, valor_unitario,
    is_active, created_at, created_by
)
VALUES (
    '66666666-6666-6666-6666-666666666666',
    'PEA-002',
    'Pastilha de Freio',
    120.00,
    TRUE,
    CURRENT_TIMESTAMP,
    'SEED'
)
ON CONFLICT DO NOTHING;
