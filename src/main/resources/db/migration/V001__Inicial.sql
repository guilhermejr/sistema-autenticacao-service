CREATE SCHEMA auditoria AUTHORIZATION postgres;

CREATE TABLE auditoria.revinfo (
    rev int4 NOT NULL,
    revtstmp int8 NULL,
    CONSTRAINT revinfo_pkey PRIMARY KEY (rev)
);

CREATE TABLE auditoria.perfis_auditoria (
    id uuid NOT NULL,
    descricao varchar(255) NULL,
    revisao int4 NOT NULL,
    tipo int2 NULL,
    CONSTRAINT perfis_auditoria_pkey PRIMARY KEY (id, revisao),
    CONSTRAINT fkncc95vwpjoncpnkxa91ag2dwo FOREIGN KEY (revisao) REFERENCES auditoria.revinfo(rev)
);

CREATE TABLE auditoria.usuarios_auditoria (
    id uuid NOT NULL,
    nome varchar(255) NULL,
    email varchar(255) NULL,
    senha varchar(255) NULL,
    ativo bool NULL,
    criado timestamp NULL,
    atualizado timestamp NULL,
    ultimo_acesso timestamp NULL,
    usuario_id uuid NULL,
    revisao int4 NOT NULL,
    tipo int2 NULL,
    CONSTRAINT usuarios_auditoria_pkey PRIMARY KEY (id, revisao),
    CONSTRAINT fk8v7300kg61vssfvcfkj937luk FOREIGN KEY (revisao) REFERENCES auditoria.revinfo(rev)
);

CREATE TABLE auditoria.usuarios_perfis_auditoria (
    usuario_id uuid NOT NULL,
    perfis_id uuid NOT NULL,
    revisao int4 NOT NULL,
    tipo int2 NULL,
    CONSTRAINT usuarios_perfis_auditoria_pkey PRIMARY KEY (revisao, usuario_id, perfis_id),
    CONSTRAINT fkp733g68n4ful3o7hyu8uy6w26 FOREIGN KEY (revisao) REFERENCES auditoria.revinfo(rev)
);

CREATE SEQUENCE public.hibernate_sequence
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE public.perfis (
    id uuid NOT NULL,
    descricao varchar(255) NOT NULL,
    CONSTRAINT perfis_pkey PRIMARY KEY (id),
    CONSTRAINT uk_46fwiur1v4jn08eg093a3bckv UNIQUE (descricao)
);

CREATE TABLE public.usuarios (
    id uuid NOT NULL,
    nome varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    senha varchar(255) NOT NULL,
    ativo bool NOT NULL,
    criado timestamp NULL,
    atualizado timestamp NULL,
    ultimo_acesso timestamp NULL,
    usuario_id uuid NULL,
    CONSTRAINT uk_kfsp0s1tflm1cwlj8idhqsad0 UNIQUE (email),
    CONSTRAINT usuarios_pkey PRIMARY KEY (id),
    CONSTRAINT fkduud97bl1pdfhruhbcfy3x2ti FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id)
);

CREATE TABLE public.usuarios_perfis (
    usuario_id uuid NOT NULL,
    perfis_id uuid NOT NULL,
    CONSTRAINT usuarios_perfis_pkey PRIMARY KEY (usuario_id, perfis_id),
    CONSTRAINT fkcvq90lk95py1n889uimu18atx FOREIGN KEY (perfis_id) REFERENCES public.perfis(id),
    CONSTRAINT fkotpfkn8c9nmhqqy4pb8hkgp18 FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id)
);

INSERT INTO public.perfis VALUES ('bf46be73-815e-410b-b787-cb48c35f8b1c', 'ROLE_ADMIN');
INSERT INTO public.perfis VALUES ('73de00bc-6ddf-47f5-b645-5be28295ff8e', 'ROLE_ENERGIA');
INSERT INTO public.perfis VALUES ('31e4d565-7690-478a-b322-c8936a1f6486', 'ROLE_SUPERMERCADO');
INSERT INTO public.perfis VALUES ('92a9dc62-a185-42f0-9b88-16b3c5203d8b', 'ROLE_REMEDIOS');
INSERT INTO public.perfis VALUES ('3040541e-912c-449c-99ec-bcdfac857234', 'ROLE_GASTOS');
INSERT INTO public.perfis VALUES ('279cffe2-30b5-41bd-933b-30160f6dc193', 'ROLE_LIVROS');

INSERT INTO public.usuarios (id, nome, email, senha, ativo, criado, atualizado) VALUES ('70d740b7-e632-4c95-b0a7-d824e7aacb4b', 'Guilherme Jr.',  'falecom@guilhermejr.net', '$2a$10$xEQR2fW67/9nQBM3/9YXLuiObBgNqqjgmlFKowI3V3wCmdtIXZDru', true, '2022-06-19 18:18:57.742', '2022-06-19 18:18:57.741');

INSERT INTO public.usuarios_perfis (usuario_id, perfis_id) VALUES('70d740b7-e632-4c95-b0a7-d824e7aacb4b', 'bf46be73-815e-410b-b787-cb48c35f8b1c');
INSERT INTO public.usuarios_perfis (usuario_id, perfis_id) VALUES('70d740b7-e632-4c95-b0a7-d824e7aacb4b', '73de00bc-6ddf-47f5-b645-5be28295ff8e');
INSERT INTO public.usuarios_perfis (usuario_id, perfis_id) VALUES('70d740b7-e632-4c95-b0a7-d824e7aacb4b', '31e4d565-7690-478a-b322-c8936a1f6486');
INSERT INTO public.usuarios_perfis (usuario_id, perfis_id) VALUES('70d740b7-e632-4c95-b0a7-d824e7aacb4b', '92a9dc62-a185-42f0-9b88-16b3c5203d8b');
INSERT INTO public.usuarios_perfis (usuario_id, perfis_id) VALUES('70d740b7-e632-4c95-b0a7-d824e7aacb4b', '3040541e-912c-449c-99ec-bcdfac857234');
INSERT INTO public.usuarios_perfis (usuario_id, perfis_id) VALUES('70d740b7-e632-4c95-b0a7-d824e7aacb4b', '279cffe2-30b5-41bd-933b-30160f6dc193');
