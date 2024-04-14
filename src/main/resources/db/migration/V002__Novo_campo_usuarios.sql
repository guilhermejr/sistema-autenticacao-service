ALTER TABLE public.usuarios ADD COLUMN tentativa_login int2;
ALTER TABLE auditoria.usuarios_auditoria ADD COLUMN tentativa_login int2;