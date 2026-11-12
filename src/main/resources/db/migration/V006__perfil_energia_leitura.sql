-- Consulta a aba de energia sem poder gravar acompanhamentos. Não vai para o Guilherme,
-- que já tem ROLE_ENERGIA; é atribuído pela administração a quem só deve ver.
INSERT INTO public.perfis VALUES ('b3e1c6a2-5d47-4f0e-9a8c-2e6f1d7b4c93', 'ROLE_ENERGIA_LEITURA') ON CONFLICT DO NOTHING;
