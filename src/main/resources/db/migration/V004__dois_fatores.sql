ALTER TABLE public.usuarios ADD COLUMN dois_fatores_ativo bool NOT NULL DEFAULT false;
ALTER TABLE public.usuarios ADD COLUMN dois_fatores_segredo varchar(255) NULL;
ALTER TABLE public.usuarios ADD COLUMN dois_fatores_ultimo_passo bigint NULL;
