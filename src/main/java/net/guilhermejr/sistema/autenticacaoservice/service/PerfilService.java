package net.guilhermejr.sistema.autenticacaoservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.mapper.PerfilMapper;
import net.guilhermejr.sistema.autenticacaoservice.api.response.PerfilResponse;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Perfil;
import net.guilhermejr.sistema.autenticacaoservice.domain.repository.PerfilRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final PerfilMapper perfilMapper;

    // --- Retornar -----------------------------------------------------------
    public List<PerfilResponse> retornar() {

        List<Perfil> perfis = perfilRepository.findAll();
        return perfilMapper.mapList(perfis);

    }

}
