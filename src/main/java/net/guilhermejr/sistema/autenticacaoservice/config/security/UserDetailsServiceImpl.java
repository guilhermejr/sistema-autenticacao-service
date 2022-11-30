package net.guilhermejr.sistema.autenticacaoservice.config.security;

import lombok.RequiredArgsConstructor;
import net.guilhermejr.sistema.autenticacaoservice.api.mapper.UsuarioMapper;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Usuario;
import net.guilhermejr.sistema.autenticacaoservice.domain.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuário (email) não encontrado: " + email));
        return usuarioMapper.mapUserDetailsImpl(usuario);
    }

    public UserDetails loadUserById(UUID id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuário não encontrado: " +  id));
        return usuarioMapper.mapUserDetailsImpl(usuario);
    }

}
