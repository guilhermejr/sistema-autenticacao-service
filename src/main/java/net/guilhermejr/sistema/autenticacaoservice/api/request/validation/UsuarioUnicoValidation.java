package net.guilhermejr.sistema.autenticacaoservice.api.request.validation;

import net.guilhermejr.sistema.autenticacaoservice.api.request.validation.constrant.UsuarioUnico;
import net.guilhermejr.sistema.autenticacaoservice.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class UsuarioUnicoValidation  implements ConstraintValidator<UsuarioUnico, String> {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void initialize(UsuarioUnico constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return !this.usuarioRepository.existsByEmail(email);
    }

}
