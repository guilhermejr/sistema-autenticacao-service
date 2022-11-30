package net.guilhermejr.sistema.autenticacaoservice.api.request.validation.constrant;

import net.guilhermejr.sistema.autenticacaoservice.api.request.validation.UsuarioUnicoValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UsuarioUnicoValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UsuarioUnico {

    String message() default "Usuário já está cadastrado.";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };

}
