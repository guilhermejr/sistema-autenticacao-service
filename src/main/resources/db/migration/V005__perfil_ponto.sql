-- ON CONFLICT: no banco local o perfil foi criado à mão, com este mesmo id, antes desta migration.
INSERT INTO public.perfis VALUES ('45428e6b-2530-41a1-9d0d-33f8c073991e', 'ROLE_PONTO') ON CONFLICT DO NOTHING;
INSERT INTO public.usuarios_perfis (usuario_id, perfis_id) VALUES('70d740b7-e632-4c95-b0a7-d824e7aacb4b', '45428e6b-2530-41a1-9d0d-33f8c073991e') ON CONFLICT DO NOTHING;
