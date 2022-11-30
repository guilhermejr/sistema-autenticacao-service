package net.guilhermejr.sistema.autenticacaoservice.api.mapper;

import net.guilhermejr.sistema.autenticacaoservice.api.request.UsuarioRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.UsuarioResponse;
import net.guilhermejr.sistema.autenticacaoservice.config.ModelMapperConfig;
import net.guilhermejr.sistema.autenticacaoservice.config.security.UserDetailsImpl;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Perfil;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Usuario;
import org.modelmapper.Converter;
import org.springframework.data.domain.Page;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UsuarioMapper extends ModelMapperConfig {

    public UsuarioMapper() {

        Converter<List<String>, List<Perfil>> perfisConverter = ctx -> ctx.getSource().stream()
                .map(p -> Perfil.builder().descricao(p).build()).collect(Collectors.toList());

        Converter<List<Perfil>, List<GrantedAuthority>> perfisImplConverter = ctx -> ctx.getSource().stream()
                .map(p -> new SimpleGrantedAuthority(p.getDescricao())).collect(Collectors.toList());

        this.modelMapper.createTypeMap(UsuarioRequest.class, Usuario.class)
                .addMappings(mapper -> mapper.using(perfisConverter).map(UsuarioRequest::getPerfis, Usuario::setPerfis));

        this.modelMapper.createTypeMap(Usuario.class, UserDetailsImpl.class)
                .addMappings(mapper -> mapper.using(perfisImplConverter).map(Usuario::getPerfis, UserDetailsImpl::setPerfis));
    }

    public Usuario mapUserDetailsImpl(UserDetailsImpl userDetails) {
        return this.mapObject(userDetails, Usuario.class);
    }

    public UserDetailsImpl mapUserDetailsImpl(Usuario usuario) {
        return this.mapObject(usuario, UserDetailsImpl.class);
    }

    public Usuario mapObject(UsuarioRequest usuarioRequest) {
        return this.mapObject(usuarioRequest, Usuario.class);
    }

    public UsuarioResponse mapObject(Usuario usuario) {
        return this.mapObject(usuario, UsuarioResponse.class);
    }

    public Page<UsuarioResponse> mapPage(Page<Usuario> usuario) {
        return this.mapPage(usuario, UsuarioResponse.class);
    }

}
