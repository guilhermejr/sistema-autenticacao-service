package net.guilhermejr.sistema.autenticacaoservice.api.mapper;

import net.guilhermejr.sistema.autenticacaoservice.api.response.PerfilResponse;
import net.guilhermejr.sistema.autenticacaoservice.config.ModelMapperConfig;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Perfil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerfilMapper extends ModelMapperConfig {

    public List<PerfilResponse> mapList(List<Perfil> perfils) {
        return this.mapList(perfils, PerfilResponse.class);
    }

}
